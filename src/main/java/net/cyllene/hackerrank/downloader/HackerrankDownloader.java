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
			Map<Integer, List<Integer>> structure = dc.getStructure();
			for (Entry<Integer, List<Integer>> entry : structure.entrySet()) {
				int challengeId = entry.getKey();
				HRChallenge currentChallenge = dc.getChallengeDetails(challengeId);
				for (Integer submissionId : entry.getValue()) {
					HRSubmission submission = dc.getSubmissionDetails(submissionId);
					// FIXME: should find a better place to filter
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

		System.out.println(challenges);

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