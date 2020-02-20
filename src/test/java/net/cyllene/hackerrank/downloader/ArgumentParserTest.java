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

import net.cyllene.hackerrank.downloader.exceptions.ExitWithErrorException;
import net.cyllene.hackerrank.downloader.exceptions.ExitWithHelpException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class ArgumentParserTest {
/*    @Test
    public void argumentsAreParsed() {
        Settings settings = CommandLineDispatcher.INSTANCE.parseArguments(
                new String[]{"--help", "--offset", "10", "-v", "--directory=something"});
        assertThat(settings.hasOption("help")).isTrue(); // help
        assertThat(settings.hasOption('o')).isTrue(); // offset
        assertThat(settings.hasOption('v')).isTrue(); // verbose
        int offset = Settings.ITEMS_TO_SKIP;
        try {
            offset = ((Number) settings.getParsedOptionValue("offset")).intValue();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        assertThat(offset).isEqualTo(10);
        assertThat(settings.hasOption("directory")).isTrue();
        assertThat(settings.getOptionValue("directory")).isEqualTo("something");
    }*/

    @Test
    public void helpOptionShouldCauseProgramToQuitWithMessage() {
        assertThatExceptionOfType(ExitWithHelpException.class).isThrownBy(() ->
                CommandLineDispatcher.INSTANCE.parseArguments(
                        new String[]{"-v", "--help"})
        );
    }

    @Test
    public void limitOptionShouldCauseErrorIfWrongDataSupplied() {
        assertThatExceptionOfType(ExitWithErrorException.class).isThrownBy(
                () -> CommandLineDispatcher.INSTANCE.parseArguments(
                        new String[]{"--limit", "Not_a_number"})
        )
                .withMessageStartingWith("Incorrect limit");
    }

    @Test
    public void offsetOptionShouldCauseErrorIfWrongDataSupplied() {
        assertThatExceptionOfType(ExitWithErrorException.class).isThrownBy(
                () -> CommandLineDispatcher.INSTANCE.parseArguments(
                        new String[]{"--offset", "Not_a_number"})
        )
                .withMessageStartingWith("Incorrect offset");
    }

    @Test
    public void limitOptionShouldBeParsed() {

        Settings settings = CommandLineDispatcher.INSTANCE.parseArguments(
                new String[]{"--limit", "981"});

        assertThat(settings.getLimit()).isEqualTo(981);
    }

    @Test
    public void offsetOptionShouldBeParsed() {
        Settings settings = CommandLineDispatcher.INSTANCE.parseArguments(
                new String[]{"--offset", "463"});

        assertThat(settings.getOffset()).isEqualTo(463);
    }

    @Test
    public void directoryOptionShouldBeConsidered() {
        Settings settings = CommandLineDispatcher.INSTANCE.parseArguments(
                new String[]{"--directory", "custom-out"});

        assertThat(settings.getOutputDir().toString()).endsWith("custom-out");

        settings = CommandLineDispatcher.INSTANCE.parseArguments(
                new String[]{"-d", "another-custom-out"});

        assertThat(settings.getOutputDir().toString()).endsWith("another-custom-out");
    }
}
