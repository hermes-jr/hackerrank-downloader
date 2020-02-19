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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import net.cyllene.hackerrank.downloader.dto.Challenge;
import net.cyllene.hackerrank.downloader.dto.ChallengeDescription;
import net.cyllene.hackerrank.downloader.dto.Submission;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;

import java.io.IOException;
import java.util.*;

@Getter
enum DownloaderCore {
    INSTANCE; // Singleton
    @Setter
    private HttpClient httpClient;
    @Setter
    private Settings settings = new Settings(); // fixme: default settings, for tests

    /**
     * @return TreeMap with IDs of challenges and submissions
     * @throws IOException mapper read failed
     */
    public Map<String, List<Integer>> getStructure(int offset, int limit) throws IOException {
        Map<String, List<Integer>> result = new TreeMap<>();

        String body = getJsonStringFrom("/rest/contests/master/submissions/?offset=" + offset + "&limit=" + limit);
        ObjectMapper mapper = new ObjectMapper();

        JsonNode jnRoot = mapper.readValue(body.getBytes(), JsonNode.class);

        for (JsonNode jnChallenge : jnRoot.get("models")) {
            List<Integer> currentChallengeSubmissions = new LinkedList<>();
            String ci = jnChallenge.get("challenge").get("slug").asText();
            // get challenge submissions at:
            // https://www.hackerrank.com/rest/contests/master/challenges/birthday-cake-candles/submissions/?offset=0&limit=10
            for (JsonNode jnSubmission : jnChallenge.get("submissions")) {
                currentChallengeSubmissions.add(jnSubmission.get("id").asInt());
                //ci = jnSubmission.get("slug").asText();
            }
            result.put(ci, currentChallengeSubmissions);
        }

        if (settings.isVerbose()) {
            System.out.println("Data structure is: " + result);
        }
        return result;
    }

    /**
     * Returns a list of translations and a description
     *
     * @param jsonString raw challenge details data to parse
     * @return List<HRChallengeDescription> object created from JSON returned by server,
     * item 0 always exists (English version)
     */
    public List<ChallengeDescription> getChallengeDescriptions(String jsonString) throws IOException {
        List<ChallengeDescription> result = new ArrayList<>();

        ObjectMapper mapper = new ObjectMapper();

        JsonNode jnRoot = mapper.readValue(jsonString.getBytes(), JsonNode.class);
        JsonNode jnChallenge = jnRoot.get("model");

        ChallengeDescription desc = ChallengeDescription.builder()
                .language("English")
                .body(jnChallenge.get("body").asText().replaceAll("\n", System.lineSeparator()))
                .bodyHTML(jnChallenge.get("body_html").asText().replaceAll("\n", System.lineSeparator()))
                .build();
        result.add(desc);

        if (jnChallenge.hasNonNull("available_translations")) {
            for (JsonNode jnTranslation : jnChallenge.get("available_translations")) {
                desc = ChallengeDescription.builder()
                        .language(jnTranslation.get("language").asText())
                        .body(jnTranslation.get("body").asText().replaceAll("\n", System.lineSeparator()))
                        .bodyHTML(jnTranslation.get("body_html").asText().replaceAll("\n", System.lineSeparator()))
                        .build();
                result.add(desc);
            }
        }

        return result;
    }

    /**
     * Returns an assembled {@link Submission} object
     *
     * @param id Challenge id, which is passed to server in URL
     * @return {@link Challenge} object created from JSON returned by server
     */
    public Challenge getChallengeDetails(int id) throws IOException {
        return getChallengeDetails("" + id);
    }

    /**
     * Returns an assembled {@link Submission} object
     *
     * @param slug Challenge id (slug), which is passed to server in URL
     * @return {@link Challenge} object created from JSON returned by server
     */
    public Challenge getChallengeDetails(String slug) throws IOException {
        String body = getJsonStringFrom("/rest/contests/master/challenges/" + slug);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jnRoot = mapper.readValue(body.getBytes(), JsonNode.class);

        JsonNode jnChallenge = jnRoot.get("model");

        Challenge cleanCh = new Challenge();

        cleanCh.setSlug(jnChallenge.get("slug").asText());
        cleanCh.setName(jnChallenge.get("name").asText());
        cleanCh.setDescriptions(getChallengeDescriptions(body));
        cleanCh.setSubmissions(new ArrayList<>());

        return cleanCh;
    }

    /**
     * Returns an assembled {@link Submission} object
     *
     * @param id Submission id, which is passed to server in URL
     * @return {@link Submission} object created from JSON returned by server
     */
    public Submission getSubmissionDetails(int id) throws IOException {
        String body = getJsonStringFrom("/rest/contests/master/submissions/" + id);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jnRoot = mapper.readValue(body.getBytes(), JsonNode.class);

        JsonNode jnSubmission = jnRoot.get("model");

        return Submission.builder()
                .id(jnSubmission.get("id").asInt())
                .ctime(jnSubmission.get("created_at").asLong())
                .statusCode(jnSubmission.get("status_code").asInt())
                .hackerId(jnSubmission.get("hacker_id").asInt())
                .status(jnSubmission.get("status").asText())
                .kind(jnSubmission.get("kind").asText())
                .language(jnSubmission.get("language").asText())
                .score(jnSubmission.get("score").asDouble())
                .sourceCode(jnSubmission.get("code").asText().replaceAll("\n", System.lineSeparator()))
                .build();
    }

    /**
     * Returns body from GET request to specified URL using Cookie authentication
     *
     * @param url The url argument must specify an absolute URL
     * @return JSON string returned by server from supplied address
     */
    private String getJsonStringFrom(String url) {
        ResponseHandler<String> handler = new BasicResponseHandler();

        String body = null;
        try {
            body = handler.handleResponse(authenticateAndGetURL(Settings.HOST + url));
        } catch (IOException e) {
            e.printStackTrace();
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
