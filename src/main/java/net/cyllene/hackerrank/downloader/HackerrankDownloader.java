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
		DownloaderSettings.cmd = parseArguments(args);

		if (DownloaderSettings.cmd.hasOption("help")) {
			printHelp();
		}
		if (DownloaderSettings.cmd.hasOption('v')) {
			DownloaderSettings.beVerbose = true;
		}

		DownloaderCore dc = DownloaderCore.INSTANCE;

		List<HRChallenge> challenges = new LinkedList<>();

		// Download everything first
		try {
			Integer limit = DownloaderSettings.ITEMS_TO_DOWNLOAD;
			if (DownloaderSettings.cmd.hasOption("n")) {
				try {
					limit = ((Number) DownloaderSettings.cmd.getParsedOptionValue("n")).intValue();
				} catch (ParseException e) {
					System.out.println("Error: " + e.getMessage() + System.lineSeparator() + "Using default: " + limit);
				}
			}

			Integer offset = DownloaderSettings.ITEMS_TO_SKIP;
			if (DownloaderSettings.cmd.hasOption("o")) {
				try {
					offset = ((Number) DownloaderSettings.cmd.getParsedOptionValue("o")).intValue();
				} catch (ParseException e) {
					System.out.println("Error: " + e.getMessage() + " Using default: " + offset);
				}
			}

			Map<String, List<Integer>> structure = dc.getStructure(offset, limit);
			for (Map.Entry<String, List<Integer>> entry : structure.entrySet()) {
				String challengeSlug = entry.getKey();
				HRChallenge currentChallenge = dc.getChallengeDetails(challengeSlug);

				for (Integer submissionId : entry.getValue()) {
					HRSubmission submission = dc.getSubmissionDetails(submissionId);

					// TODO: probably should move filtering logic elsewhere(getStructure, maybe)
					if (submission.getStatus().equalsIgnoreCase("Accepted")) {
						currentChallenge.getSubmissions().add(submission);
					}
				}

				challenges.add(currentChallenge);
			}
		} catch (IOException e) {
			System.err.println("Fatal Error: Unable to parse or download data.");
			e.printStackTrace();
		}

		// Now dump all data to disk
		try {
			for (HRChallenge currentChallenge : challenges) {
				if (currentChallenge.getSubmissions().isEmpty())
					continue;

				final String sChallengePath = "./hr_downloaded_solutions/" + currentChallenge.getSlug();
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
	static Options createCliOptions() {
		final Options options = new Options();
		options.addOption(Option.builder("h").longOpt("help")
				.required(false)
				.desc("display this help and exit")
				.build());
		options.addOption(Option.builder("d").longOpt("directory")
				.hasArg(true)
				.argName("PATH")
				.desc("path to output directory. Default: current working directory")
				.build());
		options.addOption(Option.builder("n").longOpt("number-of-items")
				.required(false)
				.hasArg(true)
				.argName("NUMBER")
				.type(Number.class)
				.desc("number of items to download. Default is " + DownloaderSettings.ITEMS_TO_DOWNLOAD)
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
		final String confPathStr = System.getProperty("user.home") + File.separator + ".hackerrank-downloader-key";
		final Path confPath = Paths.get(confPathStr);
		String result = null;
		try {
			result = Files.readAllLines(confPath, StandardCharsets.US_ASCII).get(0);
		} catch (IOException e) {
			System.err.println("Fatal Error: Unable to open configuration file " + confPathStr
					+ "\nFile might be missing, empty or inaccessible by user."
					+ "\nIt must contain a single ASCII line, a value of \"_hackerrank_session\" Cookie variable,"
					+ "\nwhich length is about 430 characters.");
			System.exit(1);
		}

		return result;
	}

	private static void printHelp() {
		HelpFormatter formatter = new HelpFormatter();
		String progname = "program.jar";
		try {
			progname = new File(HackerrankDownloader.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getName();
		} catch (URISyntaxException e) {
			// do nothing
		}
		formatter.printHelp(progname, DownloaderSettings.cliOptions);
	}
}