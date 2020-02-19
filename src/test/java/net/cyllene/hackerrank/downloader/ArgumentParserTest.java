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
import org.apache.commons.cli.ParseException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ArgumentParserTest {
    @Test
    public void argumentsAreParsed() {
        CommandLine cmd = HackerrankDownloader.parseArguments(
                new String[]{"--help", "--offset", "10", "-v", "--directory=something"});
        assertThat(cmd.hasOption("help")).isTrue(); // help
        assertThat(cmd.hasOption('o')).isTrue(); // offset
        assertThat(cmd.hasOption('v')).isTrue(); // verbose
        int offset = DownloaderSettings.ITEMS_TO_SKIP;
        try {
            offset = ((Number) cmd.getParsedOptionValue("offset")).intValue();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        assertThat(offset).isEqualTo(10);
        assertThat(cmd.hasOption("directory")).isTrue();
        assertThat(cmd.getOptionValue("directory")).isEqualTo("something");
    }
}
