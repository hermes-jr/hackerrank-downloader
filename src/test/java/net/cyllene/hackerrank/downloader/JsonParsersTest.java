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

import net.cyllene.hackerrank.downloader.dto.Challenge;
import net.cyllene.hackerrank.downloader.dto.ChallengeDescription;
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
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class JsonParsersTest {

    @Mock
    private HttpClient mockHttpClient;

    private DownloaderCore dc;

    @BeforeEach
    void init() {
        MockitoAnnotations.initMocks(this);

        dc = DownloaderCore.INSTANCE;
        dc.setSettings(new Settings()); // Defaults
        dc.setHttpClient(mockHttpClient);
    }

    @Test
    public void getStructure() throws Exception {
        String responseBody = getFakeData("/submissions_list_sample.json");

        HttpResponse response = prepareFakeSuccessResponse(responseBody);

        when(mockHttpClient.execute(any(HttpUriRequest.class)))
                .thenReturn(response);

        Map<String, List<Integer>> result = dc.getStructure(0, 10);
        // There are 3 valid challenges in the sample json file
        assertThat(result.values().stream().flatMap(List::stream).collect(Collectors.toSet())).hasSize(3);

        // Grouped into 2 challenges
        assertThat(result).hasSize(2);

        // One of them is "birthday-cake-candles"
        assertThat(result).containsKey("birthday-cake-candles");
    }

    @Test
    public void getChallengeDetails() throws Exception {
        String responseBody = getFakeData("/submission_details_sample.json");

        HttpResponse response = prepareFakeSuccessResponse(responseBody);

        when(mockHttpClient.execute(any(HttpUriRequest.class)))
                .thenReturn(response);

        Challenge challenge = dc.getChallengeDetails(new Random().nextInt());

        System.out.println(challenge);
    }

    @Test
    public void getChallengeDescriptions() throws Exception {
        String jsonStr = getFakeData("/challenge_details_sample.json");

        List<ChallengeDescription> z = dc.getChallengeDescriptions(jsonStr);

        // There are 4 valid descriptions in the sample json file
        System.out.println(z);

        assertThat(z.size()).isEqualTo(4);
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
