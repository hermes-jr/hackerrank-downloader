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

import lombok.RequiredArgsConstructor;
import net.cyllene.hackerrank.downloader.dto.ChallengeDetails;
import net.cyllene.hackerrank.downloader.dto.SubmissionDetails;
import net.cyllene.hackerrank.downloader.exceptions.ExitWithErrorException;
import net.cyllene.hackerrank.downloader.exceptions.ExitWithHelpException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpClient;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static net.cyllene.hackerrank.downloader.HttpClientConfiguration.configureHttpClient;

@RequiredArgsConstructor
public class HackerrankDownloader implements Runnable {
    private final Settings settings;
    private final ChallengesRepository challengesRepository;
    private final AuthenticationRepository authenticationRepository;
    private final LocalStorageRepository localStorageRepository;

    public static void main(String[] args) {
        try {
            // Parse and validate arguments, configure settings
            Settings settings = CommandLineDispatcher.INSTANCE.parseArguments(args);

            LocalStorageRepository localStorageRepository = LocalStorageRepository.INSTANCE;
            localStorageRepository.setSettings(settings);

            HttpClient httpClient = configureHttpClient(localStorageRepository.getSessionIdFromDotFile());
            // Initialize data repository, inject dependencies
            ChallengesRepository cr = ChallengesRepository.INSTANCE;
            cr.setSettings(settings);
            cr.setHttpClient(httpClient);

            AuthenticationRepository ar = AuthenticationRepository.INSTANCE;
            ar.setSettings(settings);
            ar.setHttpClient(httpClient);
            // Initialize main class
            HackerrankDownloader downloader = new HackerrankDownloader(settings, cr, ar, localStorageRepository);

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
        localStorageRepository.ensureOutputDirectoryIsAvailable();
        if (!StringUtils.isBlank(settings.getUsername())) {
            // fixme: if a key is present in dotfile, this does nothing. should be solved before this moment
            authenticationRepository.sendAuthRequest();
        }
        /* else {
            // todo: the cookie must be set by this moment. Here is too late. refactor, DiskIORepository maybe?
            getSessionIdFromDotFile();
        }*/

        Map<String, List<Long>> groupedSubmissionIds;
        try {
            groupedSubmissionIds = challengesRepository.getSubmissionsList(settings.getOffset(), settings.getLimit());
        } catch (IOException e) {
            throw new ExitWithErrorException("Fatal Error: could not get submissions list.");
        }

        for (Map.Entry<String, List<Long>> entry : groupedSubmissionIds.entrySet()) {
            String challengeSlug = entry.getKey();
            downloadAndSaveChallenge(challengeSlug, entry.getValue());
        }
    }

    private void downloadAndSaveChallenge(String challengeSlug, Iterable<Long> submissionIds) {
        ChallengeDetails currentChallenge = challengesRepository.getChallengeDetails(challengeSlug);

        localStorageRepository.dumpChallengeToFiles(currentChallenge);

        for (Long submissionId : submissionIds) {
            SubmissionDetails submissionSummary = challengesRepository.getSubmissionDetails(submissionId);
            localStorageRepository.dumpSubmissionToFile(challengeSlug, submissionSummary);
        }
    }

}