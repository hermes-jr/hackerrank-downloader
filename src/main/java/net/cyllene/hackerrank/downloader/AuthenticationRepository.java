/*
 * Copyright 2016-2020 Mikhail Antonov
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

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Setter;
import net.cyllene.hackerrank.downloader.dto.AuthRequest;
import net.cyllene.hackerrank.downloader.exceptions.ExitWithErrorException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;

import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Singleton-ish repository.
 * <p>
 * Obtains a session cookie for httpClient.
 * <p>
 * Supposed to be injected into the main program.
 * <p>
 * It appears that calling /rest/auth/login is not enough, the csrf token must be extracted from page markup first
 */
enum AuthenticationRepository {
    INSTANCE;
    @Setter
    private HttpClient httpClient;
    @Setter
    private Settings settings;

    /**
     * Sign in and update cookies storage
     */
    void sendAuthRequest() {
        ResponseHandler<String> handler = new BasicResponseHandler();

        try {
            HttpGet httpGet = new HttpGet(Settings.BASE_URL + "/dashboard");
            String dashboardPageSource = httpClient.execute(httpGet, handler);

            String csrfToken = getCsrfTokenFromMarkup(dashboardPageSource);
            if (settings.isVerbose()) {
                System.out.println("Got CSRF token: " + csrfToken);
            }

            HttpPost httpPost = new HttpPost(Settings.BASE_URL + "/rest/auth/login");
            httpPost.setHeader("X-CSRF-Token", csrfToken);

            AuthRequest authRequest = new AuthRequest(settings.getUsername(), settings.getPassword());
            StringEntity authRequestEntity = new StringEntity(
                    new ObjectMapper().writeValueAsString(authRequest),
                    ContentType.APPLICATION_JSON);
            httpPost.setEntity(authRequestEntity);

            HttpResponse authResponse = httpClient.execute(httpPost);

            erasePassword(settings.getPassword());
            erasePassword(authRequest.getPassword());
            httpPost.setEntity(null);
            authRequestEntity = null;

            // FIXME: for debugging purposes
            System.out.println(Arrays.deepToString(authResponse.getAllHeaders()).replaceAll(",", "\n"));

            if (settings.isVerbose()) {
                System.out.println("Must be logged in");
            }
        } catch (IOException e) {
            if (settings.isVerbose()) {
                e.printStackTrace();
            }
            throw new ExitWithErrorException("Unable to login");
        }

    }

    private String getCsrfTokenFromMarkup(String dashboardPageSource) {
        Pattern tokenRegex = Pattern.compile(".*content=\"(?<token>[^\"]+)\"[^>]*id=\"csrf-token\".*",
                Pattern.DOTALL | Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
        Matcher matcher = tokenRegex.matcher(dashboardPageSource);
        if (matcher.matches()) {
            return matcher.group("token");
        }
        return null;
    }

    private void erasePassword(char[] password) {
        Arrays.fill(password, '\0');
    }

}