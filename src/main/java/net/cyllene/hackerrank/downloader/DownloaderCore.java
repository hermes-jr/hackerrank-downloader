package net.cyllene.hackerrank.downloader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * URL examples:
 * 'https://www.hackerrank.com/rest/contests/master/submissions/grouped?offset=0&limit=1'
 * 'https://www.hackerrank.com/rest/contests/master/submissions/grouped?limit=99999'
 * 'https://www.hackerrank.com/rest/contests/master/submissions/17813507'
 * 'https://www.hackerrank.com/rest/contests/master/challenges/102'
 * 'https://www.hackerrank.com/rest/contests/master/challenges/some-slug'
 */

/**
 * Singleton {@link DownloaderCore} implemented as enum
 */
public enum DownloaderCore {
	INSTANCE;

	private static HttpClient httpClient;
	private static final String DOMAIN = "www.hackerrank.com";
	private static final String HOST = "https://" + DOMAIN;
	private static final String SECRET_COOKIE_ID = "_hackerrank_session";

	/**
	 * "Fake" constructor
	 */
	static {
		BasicCookieStore cookieStore = new BasicCookieStore();
		BasicClientCookie cookie = new BasicClientCookie(SECRET_COOKIE_ID, HackerrankDownloader.SECRET_KEY);
		cookie.setDomain(DOMAIN);
		cookie.setPath("/");
		cookieStore.addCookie(cookie);

		RequestConfig customRequestConfig = RequestConfig.custom()
				.setContentCompressionEnabled(true)
				.build();

		httpClient = HttpClientBuilder.create()
				.setDefaultRequestConfig(customRequestConfig)
				.setDefaultCookieStore(cookieStore)
				.build();
	}

	public void setHttpClient(HttpClient client) {
		httpClient = client;
	}

	/**
	 * @return TreeMap with IDs of challenges and submissions
	 * @throws IOException
	 */
	public Map getStructure() throws IOException {
		Map<Integer, List<Integer>> result = new TreeMap<>();

		String body = getJsonStringFrom("/rest/contests/master/submissions/grouped?limit=3");
		ObjectMapper mapper = new ObjectMapper();

		JsonNode jnRoot = mapper.readValue(body.getBytes(), JsonNode.class);

		for (JsonNode jnChallenge : jnRoot.get("models")) {
			List<Integer> currentChallengeSubmissions = new LinkedList<>();
			int ci = 0;
			for (JsonNode jnSubmission : jnChallenge.get("submissions")) {
				currentChallengeSubmissions.add(jnSubmission.get("id").asInt());
				ci = jnSubmission.get("challenge_id").asInt();
			}
			result.put(ci, currentChallengeSubmissions);
		}

		System.out.println(result);
		return result;
	}

	/**
	 * Returns an assembled {@link HRSubmission} object
	 *
	 * @param id Challenge id, which is passed to server in URL
	 * @return {@link HRChallenge} object created from JSON returned by server
	 */
	public HRChallenge getChallengeDetails(int id) throws IOException {
		String body = getJsonStringFrom("/rest/contests/master/challenges/" + id);

		ObjectMapper mapper = new ObjectMapper();
		JsonNode jnRoot = mapper.readValue(body.getBytes(), JsonNode.class);

		JsonNode jnSubmission = jnRoot.get("model");

		HRChallenge cleanCh = new HRChallenge();
		return cleanCh;
	}

	/**
	 * Returns an assembled {@link HRSubmission} object
	 *
	 * @param id Submission id, which is passed to server in URL
	 * @return {@link HRSubmission} object created from JSON returned by server
	 */
	public HRSubmission getSubmissionDetails(int id) throws IOException {
		String body = getJsonStringFrom("/rest/contests/master/submissions/" + id);

		ObjectMapper mapper = new ObjectMapper();
		JsonNode jnRoot = mapper.readValue(body.getBytes(), JsonNode.class);

		JsonNode jnSubmission = jnRoot.get("model");

		return new HRSubmission.Builder(jnSubmission.get("id").asInt(), jnSubmission.get("created_at").asLong(), jnSubmission.get("status_code").asInt())
				.hackerId(jnSubmission.get("hacker_id").asInt())
				.status(jnSubmission.get("status").asText())
				.kind(jnSubmission.get("kind").asText())
				.language(jnSubmission.get("language").asText())
				.score(jnSubmission.get("score").asDouble())
				.sourceCode(jnSubmission.get("code").asText())
						.build();
	}

	/**
	 * Returns an assembled HRSubmission object
	 *
	 * @param url The url argument must specify an absolute URL
	 * @return JSON string returned by server from supplied address
	 */
	private static String getJsonStringFrom(String url) {
		ResponseHandler<String> handler = new BasicResponseHandler();

		String body = null;
		try {
			body = handler.handleResponse(authenticateAndGetURL(HOST + url));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return body;
	}

	private static HttpResponse authenticateAndGetURL(String url) throws IOException {
		return httpClient.execute(new HttpGet(url));
	}

}
