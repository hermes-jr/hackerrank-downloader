/*
 * Copyright 2016-2020 Mikhail Antonov
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

import lombok.Setter;
import net.cyllene.hackerrank.downloader.dto.ChallengeDetails;
import net.cyllene.hackerrank.downloader.dto.SubmissionDetails;
import net.cyllene.hackerrank.downloader.exceptions.ExitWithErrorException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Provides access to data on disk.
 * <p>
 * Supposed to be injected into the main program.
 */
enum LocalStorageRepository {
    INSTANCE;
    @Setter
    private Settings settings;

    /**
     * Creates necessary directories, stores each challenge description in a file
     *
     * @param currentChallenge Data that describes a single challenge: id, problem text
     */
    void dumpChallengeToFiles(ChallengeDetails currentChallenge) {
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
     * Creates necessary directories, stores each submission in a file named {submission_id}.{programming_language}
     *
     * @param challengeSlug     Challenge short code suitable for directory naming
     * @param submissionDetails Data that describes a single submission: id, type, source, etc.
     */
    void dumpSubmissionToFile(String challengeSlug, SubmissionDetails submissionDetails) {
        final Path sChallengePath = settings.getOutputDir().resolve(challengeSlug);
        final Path sSolutionPath = sChallengePath.resolve("accepted_solutions");

        try {
            Files.createDirectories(sSolutionPath);
        } catch (IOException e) {
            throw new ExitWithErrorException("Unable to create solutions directory for challenge: " + challengeSlug);
        }

        String solutionFilename = String.format("%d.%s", submissionDetails.getId(), submissionDetails.getLanguage());
        Path solutionFilePath = sSolutionPath.resolve(solutionFilename);

        if (settings.isVerbose()) {
            System.out.println("Writing: " + solutionFilePath);
        }

        try {
            Files.write(solutionFilePath, submissionDetails.getCode().getBytes(StandardCharsets.UTF_8.name()));
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
    void ensureOutputDirectoryIsAvailable() {
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

        if ((Files.exists(desiredDirectory) && !Files.isWritable(desiredDirectory)) || !Files.isWritable(desiredDirectory.getParent())) {
            throw new ExitWithErrorException("Fatal error: " + desiredDirectory + " cannot be created or modified. Check permissions.");
        }
    }

    /**
     * Gets a secret session ID from configuration file in user.home.
     * It is a value of {@link Settings#COOKIE_SESSION_NAME} cookie stored by server.
     * May be used as an option when you want your credentials to be kept in secret even
     * from such simple programs as this one ;-) OR if you sign in using OAuth.
     *
     * @return String representing a _hrank_session id, about 430 characters long.
     */
    String getSessionIdFromDotFile() {
        final String confPathStr = System.getProperty("user.home") + File.separator + Settings.KEY_FILENAME;
        final Path confPath = Paths.get(confPathStr);
        try {
            return Files.readAllLines(confPath, StandardCharsets.US_ASCII).get(0);
        } catch (IOException | IndexOutOfBoundsException e) {
            String message = "Fatal Error: Unable to open configuration file " + confPathStr
                    + System.lineSeparator() + "File might be missing, empty or inaccessible by user."
                    + System.lineSeparator() + "It must contain a single ASCII line, a value of \""
                    + Settings.COOKIE_SESSION_NAME + "\" cookie variable,"
                    + System.lineSeparator() + "which length is about 430 symbols.";
            throw new ExitWithErrorException(message);
        }
    }

}
