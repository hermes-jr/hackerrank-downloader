package net.cyllene.hackerrank.downloader;

class Defaults {
	// Suppress default constructor for noninstantiability
	private Defaults() {
		throw new AssertionError();
	}

	static final String DOMAIN = "www.hackerrank.com";
	static final String HOST = "https://" + DOMAIN;
	static final String SECRET_COOKIE_ID = "_hackerrank_session";
	static final int ITEMS_TO_DOWNLOAD = 65535;
	static final int ITEMS_TO_SKIP = 0;
}
