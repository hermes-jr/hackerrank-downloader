package net.cyllene.hackerrank.downloader;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class HackerrankDownloader {
	public static final String SECRET_KEY = getSecretFromConfig();

	public static void main(String[] args) {
		DownloaderCore dc = DownloaderCore.INSTANCE;

		List<HRChallenge> challenges = new LinkedList<>();

		try {
			Map<String, List<Integer>> structure = dc.getStructure();
			for (Entry<String, List<Integer>> entry : structure.entrySet()) {
				String challengeSlug = entry.getKey();
				HRChallenge currentChallenge = dc.getChallengeDetails(challengeSlug);

				// FIXME: I'll put it here for now
				final String sChallengePath = "./hr_downloaded_solutions/" + currentChallenge.getSlug();
				final String sSolutionPath = sChallengePath + "/accepted_solutions";
				final String sDescriptionPath = sChallengePath + "/problem_description";

				Files.createDirectories(Paths.get(sDescriptionPath));
				Files.createDirectories(Paths.get(sSolutionPath));
				Files.write(Paths.get(sDescriptionPath + "/English.txt"), currentChallenge.getDescriptions().get(0).getBody().getBytes());
				//Files.write(Paths.get(sDescriptionPath + "/English.html"), currentChallenge.getDescriptions().get(0).getBodyHTML().getBytes());

				for (Integer submissionId : entry.getValue()) {
					HRSubmission submission = dc.getSubmissionDetails(submissionId);
					//currentChallenge.getSubmissions().add(submission); // Add every solution to result (failed attempts too)

					// FIXME: I'll put it here for now
					if (submission.getStatus().equalsIgnoreCase("Accepted")) {
						currentChallenge.getSubmissions().add(submission);

						Files.write(Paths.get(
								String.format("%s/%d.%s", sSolutionPath, submission.getId(), submission.getLanguage())),
								submission.getSourceCode().getBytes());
					}
				}

				challenges.add(currentChallenge);
			}
		} catch (IOException e) {
			System.err.println("Fatal Error: Unable to parse or download data.");
			e.printStackTrace();
		}
	}

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
			//e.printStackTrace();
			System.exit(1);
		}

		return result;
	}
}