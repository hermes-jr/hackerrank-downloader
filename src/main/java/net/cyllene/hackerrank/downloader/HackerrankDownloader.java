/*
 * Copyright 2016 Mikhail Antonov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.cyllene.hackerrank.downloader;

import lombok.RequiredArgsConstructor;
import net.cyllene.hackerrank.downloader.dto.ChallengeDetails;
import net.cyllene.hackerrank.downloader.dto.SubmissionDetails;
import net.cyllene.hackerrank.downloader.exceptions.ExitWithErrorException;
import net.cyllene.hackerrank.downloader.exceptions.ExitWithHelpException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static net.cyllene.hackerrank.downloader.HttpClientConfiguration.httpClient;

@RequiredArgsConstructor
public class HackerrankDownloader implements Runnable {
    static final String SECRET_KEY = getSessionFromDotFile();
    private final Settings settings;
    private final ChallengesRepository dc;

    public static void main(String[] args) {
        try {
            // Parse and validate arguments, configure settings
            Settings settings = CommandLineDispatcher.INSTANCE.parseArguments(args);

            // Initialize data repository, inject dependencies
            ChallengesRepository dc = ChallengesRepository.INSTANCE;
            dc.setSettings(settings);
            dc.setHttpClient(httpClient(SECRET_KEY));

            // Initialize main class
            HackerrankDownloader downloader = new HackerrankDownloader(settings, dc);

            downloader.run();
        } catch (ExitWithHelpException e) {
            CommandLineDispatcher.INSTANCE.printHelp();
            System.exit(0);
        } catch (ExitWithErrorException e) {
            System.err.println(e.getLocalizedMessage());
            System.exit(1);
        }
    }

    @Override
    public void run() {
        ensureOutputDirectoryIsAvailable();

        Map<String, List<Long>> groupedSubmissionIds;
        try {
            groupedSubmissionIds = dc.getSubmissionsList(settings.getOffset(), settings.getLimit());
        } catch (IOException e) {
            throw new ExitWithErrorException("Fatal Error: could not get submissions list.");
        }

        for (Map.Entry<String, List<Long>> entry : groupedSubmissionIds.entrySet()) {
            String challengeSlug = entry.getKey();
            downloadAndSaveChallenge(challengeSlug, entry.getValue());
        }
    }

    private void downloadAndSaveChallenge(String challengeSlug, Iterable<Long> submissionIds) {
        ChallengeDetails currentChallenge;
        try {
            currentChallenge = dc.getChallengeDetails(challengeSlug);
        } catch (IOException e) {
            if (settings.isVerbose()) {
                e.printStackTrace();
            }
            System.out.println("Error: could not get challenge info for: " + challengeSlug);
            return;
        }

        dumpChallengeToFiles(currentChallenge);

        for (Long submissionId : submissionIds) {
            SubmissionDetails submissionSummary;
            try {
                submissionSummary = dc.getSubmissionDetails(submissionId);

                dumpSubmissionToFile(challengeSlug, submissionSummary);
            } catch (IOException e) {
                if (settings.isVerbose()) {
                    e.printStackTrace();
                }
                System.err.println("Error: could not get submission info for: " + submissionId);
                return;
            }
        }
    }

    private void dumpSubmissionToFile(String challengeSlug, SubmissionDetails submissionSummary) {
        final Path sChallengePath = settings.getOutputDir().resolve(challengeSlug);
        final Path sSolutionPath = sChallengePath.resolve("accepted_solutions");

        try {
            Files.createDirectories(sSolutionPath);
        } catch (IOException e) {
            throw new ExitWithErrorException("Unable to create solutions directory for challenge: " + challengeSlug);
        }

        String solutionFilename = String.format("%d.%s", submissionSummary.getId(), submissionSummary.getLanguage());
        Path solutionFilePath = sSolutionPath.resolve(solutionFilename);

        if (settings.isVerbose()) {
            System.out.println("Writing: " + solutionFilePath);
        }

        try {
            Files.write(solutionFilePath, submissionSummary.getCode().getBytes(StandardCharsets.UTF_8.name()));
        } catch (IOException e) {
            throw new ExitWithErrorException(e);
        }
    }

    private void dumpChallengeToFiles(ChallengeDetails currentChallenge) {
        final Path sChallengePath = settings.getOutputDir().resolve(currentChallenge.getSlug());
        final Path sDescriptionPath = sChallengePath.resolve("problem_description");

        try {
            Files.createDirectories(sDescriptionPath);
        } catch (IOException e) {
            throw new ExitWithErrorException("Unable to create problems directory for challenge: " + currentChallenge.getSlug());
        }

        String htmlBody = currentChallenge.getBodyHtml();
        String temporaryHtmlTemplate = "<html></body>" + htmlBody + "</body></html>";

        Path problemFilePath = sDescriptionPath.resolve("english.html");
        if (settings.isVerbose()) {
            System.out.println("Writing: " + problemFilePath);
        }

        try {
            Files.write(problemFilePath, temporaryHtmlTemplate.getBytes(StandardCharsets.UTF_8.name()));
        } catch (IOException e) {
            throw new ExitWithErrorException(e);
        }
    }

    /**
     * Output directory logic:
     * <ol>
     * <li> if directory exists, ask for -f option to overwrite, quit with message
     * <li> having -f flag set, check if user has access to a parent directory, exit with error if denied
     * <li> if everything is OK, use that path for output
     * </ol>
     */
    private void ensureOutputDirectoryIsAvailable() {
        Path desiredDirectory = settings.getOutputDir();
        if (settings.isVerbose()) {
            System.out.println("Checking if output dir " + desiredDirectory + " is available");
        }
        if (Files.exists(desiredDirectory) && Files.isDirectory(desiredDirectory)) {
            if (settings.isForcedFilesOverwrite()) {
                System.out.println("WARNING!"
                        + System.lineSeparator()
                        + "--force flag is set. Overwriting directory: "
                        + desiredDirectory + System.lineSeparator()
                        + "WARNING!");
            } else {
                throw new ExitWithErrorException("Directory exists: " + desiredDirectory
                        + ", set the --force flag if you are sure. May lead to data loss, be careful.");
            }
        }
        // FIXME: when a name without relative path is provided by user we get NPE (.getParent)
        if ((Files.exists(desiredDirectory) && !Files.isWritable(desiredDirectory)) || !Files.isWritable(desiredDirectory.getParent())) {
            throw new ExitWithErrorException("Fatal error: " + desiredDirectory + " cannot be created or modified. Check permissions.");
        }
    }

    /**
     * Gets a secret key from configuration file in user.home.
     * The secret key is a _hrank_session variable stored in cookies by server.
     * To simplify things, no login logic is present in this program, it means
     * you should login somewhere else and then provide this value in the config.
     *
     * @return String representing a _hrank_session id, about 430 characters long.
     */
    private static String getSessionFromDotFile() {
        final String confPathStr = System.getProperty("user.home") + File.separator + Settings.KEY_FILENAME;
        final Path confPath = Paths.get(confPathStr);
        String result = null;
        try {
            result = Files.readAllLines(confPath, StandardCharsets.US_ASCII).get(0);
        } catch (IOException e) {
            System.err.println("Fatal Error: Unable to open configuration file " + confPathStr
                    + System.lineSeparator() + "File might be missing, empty or inaccessible by user."
                    + System.lineSeparator() + "It must contain a single ASCII line, a value of \""
                    + Settings.COOKIE_NAME + "\" cookie variable,"
                    + System.lineSeparator() + "which length is about 430 symbols.");
            System.exit(1);
        }

        return result;
    }

}