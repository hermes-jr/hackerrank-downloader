package net.cyllene.hackerrank.downloader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ArgumentParserTest {
	@Test
	public void argumentsAreParsed() {
		CommandLine cmd = HackerrankDownloader.parseArguments(
				new String[]{"--help", "--offset", "10", "-v", "--directory=something"});
		assertThat(cmd.hasOption("help"), is(true)); // help
		assertThat(cmd.hasOption('o'), is(true)); // offset
		assertThat(cmd.hasOption('v'), is(true)); // verbose
		Integer offset = DownloaderSettings.ITEMS_TO_SKIP;
		try {
			offset = ((Number) cmd.getParsedOptionValue("offset")).intValue();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		assertThat(offset, equalTo(10));
		assertThat(cmd.hasOption("directory"), is(true));
		assertThat(cmd.getOptionValue("directory"), equalTo("something"));
	}
}
