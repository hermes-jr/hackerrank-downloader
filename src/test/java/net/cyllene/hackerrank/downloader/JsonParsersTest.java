package net.cyllene.hackerrank.downloader;

import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.UnsupportedEncodingException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class JsonParsersTest {

	@Mock
	private HttpClient mockHttpClient;

	/**
	 * Test authenticateAndGetURL(String url) with a mock server response
	 */
	@Test
	public void testChallengeDescriptionParser() throws Exception {
		MockitoAnnotations.initMocks(this);

		String responseBody = " {\"model\":{\"id\":100,\"hacker_id\":222,\"language\":\"java\",\"code\":\"import something\\nexport something\", \"status\":\"Accepted\",\"created_at\":999919,\"status_code\":1,\"kind\":\"code\",\"score\":20.0}}";
		HttpResponse response = prepareResponse(200, responseBody);

		//mockHttpClient = mock(HttpClient.class);
		when(mockHttpClient.execute(any(HttpUriRequest.class)))
				.thenReturn(response);

		DownloaderCore dc = DownloaderCore.INSTANCE;
		dc.setHttpClient(mockHttpClient);
		HRSubmission candidate = dc.getSubmissionDetails(17813507);

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
