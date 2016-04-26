package net.cyllene.hackerrank.downloader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

class DownloaderSettings {
	// Suppress default constructor for noninstantiability
	private DownloaderSettings() {
		throw new AssertionError();
	}

	static final String DOMAIN = "www.hackerrank.com";
	static final String HOST = "https://" + DOMAIN;
	static final String SECRET_COOKIE_ID = "_hackerrank_session";
	static final String KEYFILE_NAME = ".hackerrank-downloader-key";
	static final int ITEMS_TO_DOWNLOAD = 65535;
	static final int ITEMS_TO_SKIP = 0;

	static Options cliOptions = null;
	static CommandLine cmd = null;
	static String outputDir = "./hr_solved_challenges";

	static boolean beVerbose = false;
}
