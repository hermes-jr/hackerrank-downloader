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

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;

/**
 * Sort of a bean factory.
 * <p>
 * Creates a configured {@link HttpClient} to be injected into {@link ChallengesRepository}
 */
class HttpClientConfiguration {

    static HttpClient configureHttpClient(String secretKey) {
        BasicCookieStore cookieStore = new BasicCookieStore();
        BasicClientCookie cookieSession = new BasicClientCookie(Settings.COOKIE_SESSION_NAME, secretKey);
        cookieSession.setDomain(Settings.DOMAIN);
        cookieSession.setPath("/");
        cookieStore.addCookie(cookieSession);

        RequestConfig customRequestConfig = RequestConfig.custom()
                .setContentCompressionEnabled(true)
                .setCookieSpec(CookieSpecs.STANDARD)
                .build();

        return HttpClientBuilder.create()
                .setDefaultRequestConfig(customRequestConfig)
                .setDefaultCookieStore(cookieStore)
                .build();
    }

}
