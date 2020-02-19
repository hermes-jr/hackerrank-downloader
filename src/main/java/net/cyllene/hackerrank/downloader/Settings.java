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

import lombok.Data;

import java.nio.file.Path;
import java.nio.file.Paths;

@Data
class Settings {
    public static final String DOMAIN = "www.hackerrank.com";
    public static final String HOST = "https://" + DOMAIN;
    public static final String SECRET_COOKIE_NAME = "_hrank_session";
    public static final String KEY_FILENAME = ".hackerrank-downloader-key";
    public static final int DEFAULT_ITEMS_TO_DOWNLOAD = 65535;
    public static final int DEFAULT_ITEMS_TO_SKIP = 0;

    private Path outputDir = Paths.get("./hackerrank_challenges");
    private int itemsToDownload = DEFAULT_ITEMS_TO_DOWNLOAD;
    private int itemsToSkip = DEFAULT_ITEMS_TO_SKIP;
    private boolean verbose = false;
    private boolean forcedFilesOverwrite = false;
}
