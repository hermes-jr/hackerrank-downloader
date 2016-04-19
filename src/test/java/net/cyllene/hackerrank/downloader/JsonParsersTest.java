package net.cyllene.hackerrank.downloader;

import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.UnsupportedEncodingException;
import java.util.Random;
import java.util.Scanner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class JsonParsersTest {

	@Mock
	private HttpClient mockHttpClient;

	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Test authenticateAndGetURL(String url) with a mock server response
	 */
	@Test
	public void testChallengeDescriptionParser() throws Exception {
		//MockitoAnnotations.initMocks(this);

		Scanner fakeData = new Scanner(this.getClass().getResourceAsStream("/test_sample_submission.json"));
		String responseBody = fakeData.useDelimiter("\\Z").next();

		HttpResponse response = prepareResponse(200, responseBody);

		when(mockHttpClient.execute(any(HttpUriRequest.class)))
				.thenReturn(response);

		DownloaderCore dc = DownloaderCore.INSTANCE;
		dc.setHttpClient(mockHttpClient);
		HRSubmission candidate = dc.getSubmissionDetails(new Random().nextInt());

		HRSubmission reference = new HRSubmission.Builder(100, 999919L, 1)
				.language("java")
				.hackerId(222)
				.sourceCode("import something\nexport something")
				.status("Accepted")
				.kind("code")
				.score(20.0)
				.build();

		System.out.println("Can: " + candidate);
		System.out.println("Ref: " + reference);

		assertThat(candidate, equalTo(reference));
	}

	/**
	 * Test authenticateAndGetURL(String url) with a mock server response
	 */
	@Test
	public void getStructure() throws Exception {
		//MockitoAnnotations.initMocks(this);

		Scanner fakeData = new Scanner(this.getClass().getResourceAsStream("/test_sample_list_of_submissions.json"));
		String responseBody = fakeData.useDelimiter("\\Z").next();

		HttpResponse response = prepareResponse(200, responseBody);

		when(mockHttpClient.execute(any(HttpUriRequest.class)))
				.thenReturn(response);

		DownloaderCore dc = DownloaderCore.INSTANCE;
		dc.setHttpClient(mockHttpClient);
		dc.getStructure();

	}

	private HttpResponse prepareResponse(int expectedResponseStatus,
										 String expectedResponseBody) {
		HttpResponse response = new BasicHttpResponse(new BasicStatusLine(
				new ProtocolVersion("HTTP", 1, 1), expectedResponseStatus, ""));
		response.setStatusCode(expectedResponseStatus);
		try {
			response.setEntity(new StringEntity(expectedResponseBody));
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException(e);
		}
		return response;
	}
}
