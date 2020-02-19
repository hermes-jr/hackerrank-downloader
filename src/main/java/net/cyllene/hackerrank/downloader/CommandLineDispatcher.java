package net.cyllene.hackerrank.downloader;

import net.cyllene.hackerrank.downloader.exceptions.ExitWithErrorException;
import net.cyllene.hackerrank.downloader.exceptions.ExitWithHelpException;
import org.apache.commons.cli.*;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;

enum CommandLineDispatcher {
    INSTANCE;

    Options cliOptions = createCliOptions();

    /**
     * @return {@link Options} object containing all valid options for this program
     */
    private Options createCliOptions() {
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
                .desc("number of solved challenges to download. Default is " + Settings.DEFAULT_ITEMS_TO_DOWNLOAD)
                .build());
        options.addOption(Option.builder("o").longOpt("offset")
                .required(false)
                .hasArg(true)
                .argName("NUMBER")
                .type(Number.class)
                .desc("number of items to skip. Default is " + Settings.DEFAULT_ITEMS_TO_SKIP)
                .build());
        options.addOption(Option.builder("v").longOpt("verbose")
                .required(false)
                .desc("run in verbose mode")
                .build());
        return options;
    }

    Settings parseArguments(String[] args) {
        final CommandLineParser parser = new DefaultParser();

        CommandLine cmd;
        try {
            cmd = parser.parse(cliOptions, args);
        } catch (ParseException e) {
            throw new ExitWithErrorException(e);
        }

        if (cmd == null || cmd.hasOption("help")) {
            throw new ExitWithHelpException();
        }

        return buildSettings(cmd);
    }

    private Settings buildSettings(CommandLine cmd) {
        Settings settings = new Settings();

        settings.setVerbose(cmd.hasOption("verbose"));
        settings.setForcedFilesOverwrite(cmd.hasOption("f"));

        if (cmd.hasOption("directory")) {
            String providedD = cmd.getOptionValue("d");
            try {
                settings.setOutputDir(Paths.get(providedD));
            } catch (InvalidPathException e) {
                throw new ExitWithErrorException("Value provided with --directory option is invalid: " + providedD);
            }
        }

        if (cmd.hasOption("limit")) {
            try {
                int limit = ((Number) cmd.getParsedOptionValue("l")).intValue();
                settings.setItemsToDownload(limit);
            } catch (ParseException e) {
                throw new ExitWithErrorException("Incorrect limit: " + e.getMessage());
            }
        }

        if (cmd.hasOption("offset")) {
            try {
                int offset = ((Number) cmd.getParsedOptionValue("o")).intValue();
                settings.setItemsToSkip(offset);
            } catch (ParseException e) {
                throw new ExitWithErrorException("Incorrect offset: " + e.getMessage());
            }
        }

        return settings;
    }

    void printHelp() {
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
                + "Application expects a file " + Settings.KEY_FILENAME
                + " to be created in your home directory. "
                + "It must contain a single ASCII line, a value of \""
                + Settings.SECRET_COOKIE_NAME + "\" cookie variable, "
                + "which length is about 430 symbols.";
        formatter.printHelp(sUsage, header, cliOptions, footer, true);
    }

}
