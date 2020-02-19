package net.cyllene.hackerrank.downloader;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;

class HttpClientConfiguration {
    static HttpClient httpClient(String secretKey) {
        BasicCookieStore cookieStore = new BasicCookieStore();
        BasicClientCookie cookie = new BasicClientCookie(Settings.SECRET_COOKIE_NAME, secretKey);
        cookie.setDomain(Settings.DOMAIN);
        cookie.setPath("/");
        cookieStore.addCookie(cookie);

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
