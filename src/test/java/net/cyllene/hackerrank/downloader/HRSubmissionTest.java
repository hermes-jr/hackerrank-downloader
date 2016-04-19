package net.cyllene.hackerrank.downloader;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by hermes on 19.04.2016.
 */
public class HRSubmissionTest {
	@Test
	public void isHRSubmissionBuilderBuildingFull() {
		HRSubmission submission = new HRSubmission.Builder(1, 2L, 3)
				.score(4.0)
				.sourceCode("abc")
				.hackerId(5)
				.kind("bcd")
				.language("cde")
				.status("def")
				.build();

		System.out.println("Inspecting " + submission);

		assertThat(submission.getId(), equalTo(1));
		assertThat(submission.getCtime(), equalTo(2L));
		assertThat(submission.getStatusCode(), equalTo(3));
		assertThat(submission.getScore(), equalTo(4.0));
		assertThat(submission.getSourceCode(), equalTo("abc"));
		assertThat(submission.getHackerId(), equalTo(5));
		assertThat(submission.getKind(), equalTo("bcd"));
		assertThat(submission.getLanguage(), equalTo("cde"));
		assertThat(submission.getStatus(), equalTo("def"));
	}

	@Test
	public void isHRSubmissionBuilderBuildingPartial() {
		HRSubmission submission = new HRSubmission.Builder(10, 100L, 1)
				.sourceCode("abc cba")
				.score(20.0)
				.build();

		System.out.println("Inspecting " + submission);

		assertThat(submission.getId(), equalTo(10));
		assertThat(submission.getCtime(), equalTo(100L));
		assertThat(submission.getStatusCode(), equalTo(1));
		assertThat(submission.getScore(), equalTo(20.0));
		assertThat(submission.getSourceCode(), equalTo("abc cba"));
		assertThat(submission.getHackerId(), equalTo(0));
		assertThat(submission.getKind(), nullValue());
		assertThat(submission.getLanguage(), nullValue());
		assertThat(submission.getStatus(), nullValue());
	}
}
