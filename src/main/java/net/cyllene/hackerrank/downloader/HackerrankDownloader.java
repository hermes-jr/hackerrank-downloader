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

public class HackerrankDownloader {
	static final String SECRET_KEY = getSecretFromConfig();

	public static void main(String[] args) {
		try {
			setupCLI(args);
		} catch (ParseException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}

		DownloaderCore dc = DownloaderCore.INSTANCE;

		List<HRChallenge> challenges = new LinkedList<>();

		// Download everything first
		/*
		try {
			Map<String, List<Integer>> structure = dc.getStructure(Defaults.ITEMS_TO_SKIP, Defaults.ITEMS_TO_DOWNLOAD);
			for (Entry<String, List<Integer>> entry : structure.entrySet()) {
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
				if (!plainBody.equals("null")) {
					Files.write(Paths.get(sDescriptionPath + "/english.txt"), plainBody.getBytes(Charsets.UTF_8));
				}

				String htmlBody = currentChallenge.getDescriptions().get(0).getBodyHTML();
				String temporaryHtmlTemplate = "<html></body>" + htmlBody + "</body></html>";
				Files.write(Paths.get(sDescriptionPath + "/english.html"), temporaryHtmlTemplate.getBytes(Charsets.UTF_8));

				for (HRSubmission submission : currentChallenge.getSubmissions()) {
					Files.write(Paths.get(
							String.format("%s/%d.%s", sSolutionPath, submission.getId(), submission.getLanguage())),
							submission.getSourceCode().getBytes(Charsets.UTF_8));
				}

			}
		} catch (IOException e) {
			System.err.println("Fatal Error: couldn't dump data to disk.");
			System.exit(1);
		}
		*/
	}

	private static void setupCLI(String[] args) throws ParseException {
		final Options options = new Options();
		options.addOption(Option.builder().longOpt("help")
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
				.desc("number of items to download. Default is " + Defaults.ITEMS_TO_DOWNLOAD)
				.build());
		options.addOption(Option.builder("o").longOpt("offset")
				.required(false)
				.desc("number of items to skip. Default is " + Defaults.ITEMS_TO_SKIP)
				.build());
		options.addOption("v", false, "run in verbose mode");

		final CommandLineParser parser = new DefaultParser();
		final CommandLine cmd = parser.parse(options, args);

		if (cmd.hasOption("help")) {
			printHelp(options);
		}
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

	private static void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		String progname = "program.jar";
		try {
			progname = new File(HackerrankDownloader.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getName();
		} catch (URISyntaxException e) {
			// do nothing
		}
		formatter.printHelp(progname, options);
	}
}