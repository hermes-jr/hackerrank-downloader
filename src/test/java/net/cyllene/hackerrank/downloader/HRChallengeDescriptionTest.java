package net.cyllene.hackerrank.downloader;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class HRChallengeDescriptionTest {
	@Test
	public void isHRChallengeDescriptionBuilderBuildingFull() {
		HRChallengeDescription submission = new HRChallengeDescription.Builder()
				.language("Chinese")
				.body("a b c d\nc d e f")
				.bodyHTML("<h1>markup</h1>")
				.build();

		System.out.println("Inspecting " + submission);

		assertThat(submission.getLanguage(), equalTo("Chinese"));
		assertThat(submission.getBody(), equalTo("a b c d\nc d e f"));
		assertThat(submission.getBodyHTML(), equalTo("<h1>markup</h1>"));
	}
}
