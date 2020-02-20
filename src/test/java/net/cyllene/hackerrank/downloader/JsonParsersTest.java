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

import net.cyllene.hackerrank.downloader.dto.ChallengeDetails;
import net.cyllene.hackerrank.downloader.dto.SubmissionDetails;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class JsonParsersTest {

    @Mock
    private HttpClient mockHttpClient;

    private ChallengesRepository dc;

    @BeforeEach
    void init() {
        MockitoAnnotations.initMocks(this);

        dc = ChallengesRepository.INSTANCE;
        dc.setSettings(new Settings()); // Defaults
        dc.setHttpClient(mockHttpClient);
    }

    @Test
    void submissionsListShouldBeParsed() throws Exception {
        String responseBody = getFakeData("/submissions_list_sample.json");

        HttpResponse response = prepareFakeSuccessResponse(responseBody);

        when(mockHttpClient.execute(any(HttpUriRequest.class)))
                .thenReturn(response);

        Settings downloadEverything = new Settings();
        downloadEverything.setAcceptedOnly(false);
        dc.setSettings(downloadEverything);

        Map<String, List<Long>> result = dc.getSubmissionsList(0, 10);
        // There are 3 valid challenges in the sample json file
        assertThat(result.values().stream().flatMap(List::stream).collect(Collectors.toSet())).hasSize(3);

        // Grouped into 2 challenges
        assertThat(result).hasSize(2);

        // One of them is "birthday-cake-candles"
        assertThat(result).containsKey("birthday-cake-candles");
    }

    @Test
    void challengeDetailsShouldBeParsed() throws Exception {
        String responseBody = getFakeData("/challenge_details_sample.json");

        HttpResponse response = prepareFakeSuccessResponse(responseBody);

        when(mockHttpClient.execute(any(HttpUriRequest.class)))
                .thenReturn(response);

        ChallengeDetails challenge = dc.getChallengeDetails("stub-slug");

        assertThat(challenge.getPreview()).isEqualTo("Find the maximum and minimum values obtained by summing four of five integers.");

        assertThat(challenge.getBodyHtml()).contains("challenge_problem_statement");
    }

    @Test
    void submissionCodeShouldBeAvailable() throws Exception {
        String responseBody = getFakeData("/submission_details_sample.json");

        HttpResponse response = prepareFakeSuccessResponse(responseBody);

        when(mockHttpClient.execute(any(HttpUriRequest.class)))
                .thenReturn(response);

        SubmissionDetails submissionDetails = dc.getSubmissionDetails(92273619);

        assertThat(submissionDetails.getId()).isEqualTo(92273619);

        assertThat(submissionDetails.getCreatedAt().toLocalDate()).isEqualTo(LocalDate.of(2018, 12, 5));
        assertThat(submissionDetails.getLanguage()).isEqualTo("go");
        assertThat(submissionDetails.getCode())
                .contains("source code")
                .contains("multiline");
    }

    private HttpResponse prepareFakeSuccessResponse(String expectedResponseBody) {
        int okCode = 200;
        HttpResponse response = new BasicHttpResponse(new BasicStatusLine(
                new ProtocolVersion("HTTP", 1, 1), okCode, ""));
        response.setStatusCode(okCode);
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
