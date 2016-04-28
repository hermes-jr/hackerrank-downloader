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
