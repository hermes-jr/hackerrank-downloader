/*
  Copyright 2016 Mikhail Antonov
  <p>
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
  <p>
  http://www.apache.org/licenses/LICENSE-2.0
  <p>
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */
package net.cyllene.hackerrank.downloader;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Setter;
import net.cyllene.hackerrank.downloader.dto.ChallengeDetails;
import net.cyllene.hackerrank.downloader.dto.SubmissionDetails;
import net.cyllene.hackerrank.downloader.dto.SubmissionSummary;
import net.cyllene.hackerrank.downloader.dto.SubmissionsCollection;
import net.cyllene.hackerrank.downloader.exceptions.ExitWithErrorException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Singleton-ish repository.
 * <p>
 * Provides data in form of DTOs for further processing.
 * <p>
 * Supposed to be injected into the main program.
 */
enum ChallengesRepository {
    INSTANCE;
    @Setter
    private HttpClient httpClient;
    @Setter
    private Settings settings;
    private ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    /**
     * @return map with IDs of challenges grouped by submissions
     * @throws IOException mapper read failed
     */
    Map<String, List<Long>> getSubmissionsList(int offset, int limit) throws IOException {
        Map<String, List<Long>> result = new HashMap<>();

        String body = getJsonStringFrom("/rest/contests/master/submissions/?offset=" + offset + "&limit=" + limit);

        SubmissionsCollection submissions = mapper.readValue(body.getBytes(), SubmissionsCollection.class);

        for (SubmissionSummary submissionSummary : submissions.getModels()) {
            if (settings.isAcceptedOnly() && Settings.STATUS_CODE_ACCEPTED != submissionSummary.getStatusCode()) {
                continue;
            }
            String slug = submissionSummary.getChallenge().getSlug();
            List<Long> currentChallengeSubmissions = result.getOrDefault(slug, new LinkedList<>());
            currentChallengeSubmissions.add(submissionSummary.getId());
            result.put(slug, currentChallengeSubmissions);
        }

        if (settings.isVerbose()) {
            System.out.println("Data structure is: " + result);
        }
        return result;
    }

    /**
     * Request and unmarshal challenge details from server by slug
     *
     * @param slug Challenge id (slug), which is passed to server in URL
     * @return {@link ChallengeDetails} object created from JSON returned by server
     */
    public ChallengeDetails getChallengeDetails(String slug) throws IOException {
        String body = getJsonStringFrom("/rest/contests/master/challenges/" + slug);

        JsonNode node = mapper.readValue(body.getBytes(), JsonNode.class).get("model"); // Strip of wrapper class
        return mapper.convertValue(node, ChallengeDetails.class);
    }

    /**
     * Returns an assembled {@link SubmissionSummary} object
     *
     * @param id Submission id, which is passed to server in URL
     * @return {@link SubmissionSummary} object created from JSON returned by server
     */
    public SubmissionDetails getSubmissionDetails(long id) throws IOException {
        String body = getJsonStringFrom("/rest/contests/master/submissions/" + id);

        JsonNode node = mapper.readValue(body.getBytes(), JsonNode.class).get("model");  // Strip of wrapper class
        SubmissionDetails submissionDetails = mapper.convertValue(node, SubmissionDetails.class);
        submissionDetails.setCode(submissionDetails.getCode().replaceAll("\n", System.lineSeparator())); // ?
        return submissionDetails;
    }

    /**
     * Returns body from GET request to specified URL using Cookie authentication
     *
     * @param url The url argument must specify an absolute URL
     * @return JSON string returned by server from supplied address
     */
    private String getJsonStringFrom(String url) {
        ResponseHandler<String> handler = new BasicResponseHandler();

        String body;
        try {
            body = handler.handleResponse(authenticateAndGetURL(Settings.BASE_URL + url));
        } catch (IOException e) {
            if (settings.isVerbose()) {
                e.printStackTrace();
            }
            throw new ExitWithErrorException("Could not get JSON data from server");
        }
        return body;
    }

    private HttpResponse authenticateAndGetURL(String url) throws IOException {
        if (settings.isVerbose()) {
            System.out.println("Getting: " + url);
        }
        return httpClient.execute(new HttpGet(url));
    }

}
