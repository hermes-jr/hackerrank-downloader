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

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
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
		String responseBody = getFakeData("/test_sample_submission.json");

		HttpResponse response = prepareFakeResponse(200, responseBody);

		when(mockHttpClient.execute(any(HttpUriRequest.class)))
				.thenReturn(response);

		DownloaderCore dc = DownloaderCore.INSTANCE;
		dc.setHttpClient(mockHttpClient);
		HRSubmission candidate = dc.getSubmissionDetails(new Random().nextInt());

		HRSubmission reference = new HRSubmission.Builder(100, 999919L, 1)
				.language("java")
				.hackerId(222)
				.sourceCode("import something\nexport something".replaceAll("\n", System.lineSeparator()))
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
		String responseBody = getFakeData("/test_sample_list_of_submissions.json");

		HttpResponse response = prepareFakeResponse(200, responseBody);

		when(mockHttpClient.execute(any(HttpUriRequest.class)))
				.thenReturn(response);

		DownloaderCore dc = DownloaderCore.INSTANCE;
		dc.setHttpClient(mockHttpClient);

		Map<String, List<Integer>> result = dc.getStructure(0, 10);
		// There are 10 valid challenges in the sample json file
		assertThat(result.size(), equalTo(10));
		assertTrue(result.containsKey("maximise-sum"));
	}

	@Test
	public void getChallengeDetails() throws Exception {
		String responseBody = getFakeData("/test_sample_challenge_details.json");

		HttpResponse response = prepareFakeResponse(200, responseBody);

		when(mockHttpClient.execute(any(HttpUriRequest.class)))
				.thenReturn(response);

		DownloaderCore dc = DownloaderCore.INSTANCE;
		dc.setHttpClient(mockHttpClient);

		HRChallenge challenge = dc.getChallengeDetails(new Random().nextInt());

		System.out.println(challenge);
	}

	@Test
	public void getChallengeDescriptions() throws Exception {
		String jsonStr = getFakeData("/test_sample_challenge_details.json");

		DownloaderCore dc = DownloaderCore.INSTANCE;
		List z = dc.getChallengeDescriptions(jsonStr);

		// There are 4 valid descriptions in the sample json file
		System.out.println(z);

		assertThat(z.size(), equalTo(4));
	}

	private HttpResponse prepareFakeResponse(int expectedResponseStatus,
											 String expectedResponseBody) {
		HttpResponse response = new BasicHttpResponse(new BasicStatusLine(
				new ProtocolVersion("HTTP", 1, 1), expectedResponseStatus, ""));
		response.setStatusCode(expectedResponseStatus);
		try {
			response.setEntity(new StringEntity(expectedResponseBody));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}

	private String getFakeData(String path) {
		Scanner fakeData = new Scanner(this.getClass().getResourceAsStream(path), StandardCharsets.UTF_8.name());
		return fakeData.useDelimiter("\\Z").next();
	}
}
