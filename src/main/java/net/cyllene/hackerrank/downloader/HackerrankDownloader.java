package net.cyllene.hackerrank.downloader;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HackerrankDownloader {
	public static final String SECRET_KEY = getSecretFromConfig();

	public static void main(String[] args) {
		DownloaderCore dc = DownloaderCore.INSTANCE;
		HRSubmission submission = dc.getSubmissionDetails(17813507);
		System.out.println(submission);
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