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

import org.apache.commons.cli.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class HackerrankDownloader {
    static final String SECRET_KEY = getSecretFromConfig();

    static {
        DownloaderSettings.cliOptions = createCliOptions();
    }

    public static void main(String[] args) {
        // Parse arguments and set up the defaults
        DownloaderSettings.cmd = parseArguments(args);

        if (DownloaderSettings.cmd.hasOption("help")) {
            printHelp();
            System.exit(0);
        }

        if (DownloaderSettings.cmd.hasOption("verbose")) {
            DownloaderSettings.beVerbose = true;
        }

		/*
		  Output directory logic:
		  1) if directory exists, ask for -f option to overwrite, quit with message
		  2) if -f flag is set, check if user has access to a parent directory
		  3) if no access, quit with error
		  4) if everything is OK, remember the path
		 */
        String sDesiredPath = DownloaderSettings.outputDir;
        if (DownloaderSettings.cmd.hasOption("directory")) {
            sDesiredPath = DownloaderSettings.cmd.getOptionValue("d", DownloaderSettings.outputDir);
        }
        if (DownloaderSettings.beVerbose) {
            System.out.println("Checking output dir: " + sDesiredPath);
        }
        Path desiredPath = Paths.get(sDesiredPath);
        if (Files.exists(desiredPath) && Files.isDirectory(desiredPath)) {
            if (!DownloaderSettings.cmd.hasOption("f")) {
                System.out.println("I wouldn't like to overwrite existing directory: " + sDesiredPath
                        + ", set the --force flag if you are sure. May lead to data loss, be careful.");
                System.exit(0);
            } else {
                System.out.println("WARNING!"
                        + System.lineSeparator()
                        + "--force flag is set. Overwriting directory: "
                        + sDesiredPath + System.lineSeparator()
                        + "WARNING!");
            }
        }
        if ((Files.exists(desiredPath) && !Files.isWritable(desiredPath)) || !Files.isWritable(desiredPath.getParent())) {
            System.err.println("Fatal error: " + sDesiredPath + " cannot be created or modified. Check permissions.");
            // TODO: use Exceptions instead of system.exit
            System.exit(1);
        }
        DownloaderSettings.outputDir = sDesiredPath;

        int limit = DownloaderSettings.ITEMS_TO_DOWNLOAD;
        if (DownloaderSettings.cmd.hasOption("limit")) {
            try {
                limit = ((Number) DownloaderSettings.cmd.getParsedOptionValue("l")).intValue();
            } catch (ParseException e) {
                System.out.println("Incorrect limit: " + e.getMessage() + System.lineSeparator() + "Using default value: " + limit);
            }
        }

        int offset = DownloaderSettings.ITEMS_TO_SKIP;
        if (DownloaderSettings.cmd.hasOption("offset")) {
            try {
                offset = ((Number) DownloaderSettings.cmd.getParsedOptionValue("o")).intValue();
            } catch (ParseException e) {
                System.out.println("Incorrect offset: " + e.getMessage() + " Using default value: " + offset);
            }
        }

        DownloaderCore dc = DownloaderCore.INSTANCE;

        List<HRChallenge> challenges = new LinkedList<>();

        // Download everything first
        Map<String, List<Integer>> structure = null;
        try {
            structure = dc.getStructure(offset, limit);
        } catch (IOException e) {
            System.err.println("Fatal Error: could not get data structure.");
            e.printStackTrace();
            System.exit(1);
        }

        for (Map.Entry<String, List<Integer>> entry : structure.entrySet()) {
            String challengeSlug = entry.getKey();
            HRChallenge currentChallenge;
            try {
                currentChallenge = dc.getChallengeDetails(challengeSlug);
            } catch (IOException e) {
                System.err.println("Error: could not get challenge info for: " + challengeSlug);
                if (DownloaderSettings.beVerbose) {
                    e.printStackTrace();
                }
                continue;
            }

            for (Integer submissionId : entry.getValue()) {
                HRSubmission submission;
                try {
                    submission = dc.getSubmissionDetails(submissionId);
                } catch (IOException e) {
                    System.err.println("Error: could not get submission info for: " + submissionId);
                    if (DownloaderSettings.beVerbose) {
                        e.printStackTrace();
                    }
                    continue;
                }

                // TODO: probably should move filtering logic elsewhere(getStructure, maybe)
                if (submission.getStatus().equalsIgnoreCase("Accepted")) {
                    currentChallenge.getSubmissions().add(submission);
                }
            }

            challenges.add(currentChallenge);
        }

        // Now dump all data to disk
        try {
            for (HRChallenge currentChallenge : challenges) {
                if (currentChallenge.getSubmissions().isEmpty())
                    continue;

                final String sChallengePath = DownloaderSettings.outputDir + "/" + currentChallenge.getSlug();
                final String sSolutionPath = sChallengePath + "/accepted_solutions";
                final String sDescriptionPath = sChallengePath + "/problem_description";

                Files.createDirectories(Paths.get(sDescriptionPath));
                Files.createDirectories(Paths.get(sSolutionPath));

                // FIXME: this should be done the other way
                String plainBody = currentChallenge.getDescriptions().get(0).getBody();
                String sFname;
                if (!plainBody.equals("null")) {
                    sFname = sDescriptionPath + "/english.txt";
                    if (DownloaderSettings.beVerbose) {
                        System.out.println("Writing to: " + sFname);
                    }

                    Files.write(Paths.get(sFname), plainBody.getBytes(StandardCharsets.UTF_8.name()));
                }

                String htmlBody = currentChallenge.getDescriptions().get(0).getBodyHTML();
                String temporaryHtmlTemplate = "<html></body>" + htmlBody + "</body></html>";

                sFname = sDescriptionPath + "/english.html";
                if (DownloaderSettings.beVerbose) {
                    System.out.println("Writing to: " + sFname);
                }
                Files.write(Paths.get(sFname), temporaryHtmlTemplate.getBytes(StandardCharsets.UTF_8.name()));

                for (HRSubmission submission : currentChallenge.getSubmissions()) {
                    sFname = String.format("%s/%d.%s", sSolutionPath, submission.getId(), submission.getLanguage());
                    if (DownloaderSettings.beVerbose) {
                        System.out.println("Writing to: " + sFname);
                    }

                    Files.write(Paths.get(sFname), submission.getSourceCode().getBytes(StandardCharsets.UTF_8.name()));
                }

            }
        } catch (IOException e) {
            System.err.println("Fatal Error: couldn't dump data to disk.");
            System.exit(1);
        }
    }

    /**
     * @return {@link Options} object containing all valid options for this program
     */
    private static Options createCliOptions() {
        final Options options = new Options();
        options.addOption(Option.builder("h").longOpt("help")
                .desc("display this help and exit")
                .build());
        options.addOption(Option.builder("d").longOpt("directory")
                .hasArg(true)
                .argName("PATH")
                .desc("path to output directory. Default: current working directory")
                .build());
        options.addOption(Option.builder("f").longOpt("force-overwrite")
                .desc("Force overwrite if output directory exists. May lead to data loss.")
                .build());
        options.addOption(Option.builder("l").longOpt("limit")
                .hasArg(true)
                .argName("NUMBER")
                .type(Number.class)
                .desc("number of solved challenges to download. Default is " + DownloaderSettings.ITEMS_TO_DOWNLOAD)
                .build());
        options.addOption(Option.builder("o").longOpt("offset")
                .required(false)
                .hasArg(true)
                .argName("NUMBER")
                .type(Number.class)
                .desc("number of items to skip. Default is " + DownloaderSettings.ITEMS_TO_SKIP)
                .build());
        options.addOption(Option.builder("v").longOpt("verbose")
                .required(false)
                .desc("run in verbose mode")
                .build());
        return options;
    }

    static CommandLine parseArguments(String[] args) {
        final CommandLineParser parser = new DefaultParser();
        try {
            return parser.parse(DownloaderSettings.cliOptions, args);
        } catch (ParseException e) {
            System.err.println("Fatal Error: " + e.getMessage());
            System.exit(1);
        }
        return null;
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
        final String confPathStr = System.getProperty("user.home") + File.separator + DownloaderSettings.KEY_FILENAME;
        final Path confPath = Paths.get(confPathStr);
        String result = null;
        try {
            result = Files.readAllLines(confPath, StandardCharsets.US_ASCII).get(0);
        } catch (IOException e) {
            System.err.println("Fatal Error: Unable to open configuration file " + confPathStr
                    + System.lineSeparator() + "File might be missing, empty or inaccessible by user."
                    + System.lineSeparator() + "It must contain a single ASCII line, a value of \""
                    + DownloaderSettings.SECRET_COOKIE_ID + "\" cookie variable,"
                    + System.lineSeparator() + "which length is about 430 symbols.");
            System.exit(1);
        }

        return result;
    }

    private static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        String sUsage = "java -jar ";
        try {
            sUsage += new File(HackerrankDownloader.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getName();
        } catch (URISyntaxException e) {
            sUsage += "hackerrank-downloader.jar";
        }

        String header = "";
        String footer = System.lineSeparator() +
                "If you are experiencing problems with JSON parser, "
                + "try to run program with \"-Dfile.encoding=UTF-8\" option"
                + System.lineSeparator() + System.lineSeparator()
                + "Application expects a file " + DownloaderSettings.KEY_FILENAME
                + " to be created in your home directory. "
                + "It must contain a single ASCII line, a value of \""
                + DownloaderSettings.SECRET_COOKIE_ID + "\" cookie variable, "
                + "which length is about 430 symbols.";
        formatter.printHelp(sUsage, header, DownloaderSettings.cliOptions, footer, true);
    }
}