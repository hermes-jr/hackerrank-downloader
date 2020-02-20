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
    static final String SECRET_KEY = getSecretFromConfig();
    private final Settings settings;

    public static void main(String[] args) {
        // Parse and validate arguments, configure settings
        Settings settings;
        try {
            settings = CommandLineDispatcher.INSTANCE.parseArguments(args);
            HackerrankDownloader prog = new HackerrankDownloader(settings);
            prog.run();
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
        final Path desiredPath = ensureOutputDirectoryIsAvailable();

        ChallengesRepository dc = ChallengesRepository.INSTANCE;
        dc.setSettings(settings);
        dc.setHttpClient(httpClient(SECRET_KEY));

        // Download everything first
        Map<String, List<Long>> structure;
        try {
            structure = dc.getSubmissionsList(settings.getOffset(), settings.getLimit());
        } catch (IOException e) {
            throw new ExitWithErrorException("Fatal Error: could not get data structure.");
        }

        for (Map.Entry<String, List<Long>> entry : structure.entrySet()) {
            String challengeSlug = entry.getKey();
            ChallengeDetails currentChallenge;
            try {
                currentChallenge = dc.getChallengeDetails(challengeSlug);
            } catch (IOException e) {
                if (settings.isVerbose()) {
                    e.printStackTrace();
                }
                System.out.println("Error: could not get challenge info for: " + challengeSlug);
                continue;
            }

            for (Long submissionId : entry.getValue()) {
                SubmissionDetails submissionSummary;
                try {
                    submissionSummary = dc.getSubmissionDetails(submissionId);
                } catch (IOException e) {
                    if (settings.isVerbose()) {
                        e.printStackTrace();
                    }
                    System.err.println("Error: could not get submission info for: " + submissionId);
                    continue;
                }

/*
                if (settings.isVerbose()) {
                    System.out.printf("Challenge %s\nslug %s\nsubmission id %s\ncode %s\n\n",
                            currentChallenge.getId(), currentChallenge.getSlug(), submissionId, submissionSummary.getCode());
                }
*/
                // TODO: save file
            }

            // currentChallenge add
        }

        // Now dump all data to disk
/*
        try {
            for (Challenge currentChallenge : challenges) {
                if (currentChallenge.getSubmissionSummaries().isEmpty())
                    continue;

                final Path sChallengePath = desiredPath.resolve(currentChallenge.getSlug());
                final Path sSolutionPath = sChallengePath.resolve("accepted_solutions");
                final Path sDescriptionPath = sChallengePath.resolve("problem_description");

                Files.createDirectories(sDescriptionPath);
                Files.createDirectories(sSolutionPath);

                // FIXME: this should be done the other way
                String plainBody = currentChallenge.getDescriptions().get(0).getBody();
                Path problemFilePath;
                if (!plainBody.equals("null")) {
                    problemFilePath = sDescriptionPath.resolve("english.txt");
                    if (settings.isVerbose()) {
                        System.out.println("Writing to: " + problemFilePath);
                    }

                    Files.write(problemFilePath, plainBody.getBytes(StandardCharsets.UTF_8.name()));
                }

                String htmlBody = currentChallenge.getDescriptions().get(0).getBodyHTML();
                String temporaryHtmlTemplate = "<html></body>" + htmlBody + "</body></html>";

                problemFilePath = sDescriptionPath.resolve("english.html");
                if (settings.isVerbose()) {
                    System.out.println("Writing to: " + problemFilePath);
                }
                Files.write(problemFilePath, temporaryHtmlTemplate.getBytes(StandardCharsets.UTF_8.name()));

                for (SubmissionSummary submissionSummary : currentChallenge.getSubmissionSummaries()) {
                    String solutionFilename = String.format("%d.%s", submissionSummary.getId(), submissionSummary.getLanguage());
                    Path solutionFilePath = sSolutionPath.resolve(solutionFilename);
                    if (settings.isVerbose()) {
                        System.out.println("Writing to: " + solutionFilePath);
                    }

//                    Files.write(solutionFilePath, submissionSummary.getSourceCode().getBytes(StandardCharsets.UTF_8.name()));
                }

            }
        } catch (IOException e) {
            throw new ExitWithErrorException("Fatal Error: couldn't dump data to disk.");
        }
*/
    }

    /**
     * Output directory logic:
     * <ol>
     * <li> if directory exists, ask for -f option to overwrite, quit with message
     * <li> having -f flag set, check if user has access to a parent directory, exit with error if denied
     * <li> if everything is OK, use that path for output
     * </ol>
     */
    private Path ensureOutputDirectoryIsAvailable() {
        Path desiredDirectory = settings.getOutputDir();
        if (settings.isVerbose()) {
            System.out.println("Checking output dir: " + desiredDirectory);
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
        if ((Files.exists(desiredDirectory) && !Files.isWritable(desiredDirectory)) || !Files.isWritable(desiredDirectory.getParent())) {
            throw new ExitWithErrorException("Fatal error: " + desiredDirectory + " cannot be created or modified. Check permissions.");
        }
        return desiredDirectory;
    }

    /**
     * Gets a secret key from configuration file in user.home.
     * The secret key is a _hackerrank_session variable stored in cookies by server.
     * To simplify things, no login logic is present in this program, it means
     * you should login somewhere else and then provide this value in the config.
     *
     * @return String representing a _hackerrank_session id, about 430 characters long.
     */
    private static String getSecretFromConfig() {
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