package net.cyllene.hackerrank.downloader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;

import java.io.IOException;
import java.util.*;

/**
 * URLs:
 * 'https://www.hackerrank.com/rest/contests/master/submissions/grouped?offset=0&limit=1'
 * 'https://www.hackerrank.com/rest/contests/master/submissions/grouped?limit=99999'
 * 'https://www.hackerrank.com/rest/contests/master/submissions/17813507'
 * 'https://www.hackerrank.com/rest/contests/master/challenges/101'
 * 'https://www.hackerrank.com/rest/contests/master/challenges/some-slug'
 */

/**
 * Singleton {@link DownloaderCore} implemented as enum
 */
enum DownloaderCore {
	INSTANCE;
	private static HttpClient httpClient;

	/**
	 * "Fake" constructor
	 */
	static {
		BasicCookieStore cookieStore = new BasicCookieStore();
		BasicClientCookie cookie = new BasicClientCookie(Defaults.SECRET_COOKIE_ID, HackerrankDownloader.SECRET_KEY);
		cookie.setDomain(Defaults.DOMAIN);
		cookie.setPath("/");
		cookieStore.addCookie(cookie);

		RequestConfig customRequestConfig = RequestConfig.custom()
				.setContentCompressionEnabled(true)
				.setCookieSpec(CookieSpecs.STANDARD)
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
	public Map getStructure(int offset, int limit) throws IOException {
		Map<String, List<Integer>> result = new TreeMap<>();

		String body = getJsonStringFrom("/rest/contests/master/submissions/grouped?offset=" + offset + "&limit=" + limit);
		ObjectMapper mapper = new ObjectMapper();

		JsonNode jnRoot = mapper.readValue(body.getBytes(), JsonNode.class);

		for (JsonNode jnChallenge : jnRoot.get("models")) {
			List<Integer> currentChallengeSubmissions = new LinkedList<>();
			String ci = jnChallenge.get("challenge").get("slug").asText();
			for (JsonNode jnSubmission : jnChallenge.get("submissions")) {
				currentChallengeSubmissions.add(jnSubmission.get("id").asInt());
				//ci = jnSubmission.get("slug").asText();
			}
			result.put(ci, currentChallengeSubmissions);
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
	public List getChallengeDescriptions(String jsonString) throws IOException {
		List<HRChallengeDescription> result = new ArrayList<>();

		ObjectMapper mapper = new ObjectMapper();

		JsonNode jnRoot = mapper.readValue(jsonString.getBytes(), JsonNode.class);
		JsonNode jnChallenge = jnRoot.get("model");

		HRChallengeDescription desc = new HRChallengeDescription.Builder()
				.language("English")
				.body(jnChallenge.get("body").asText().replaceAll("\n", System.lineSeparator()))
				.bodyHTML(jnChallenge.get("body_html").asText().replaceAll("\n", System.lineSeparator()))
				.build();
		result.add(desc);

		if (jnChallenge.hasNonNull("available_translations")) {
			for (JsonNode jnTranslation : jnChallenge.get("available_translations")) {
				desc = new HRChallengeDescription.Builder()
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
	 * Returns an assembled {@link HRSubmission} object
	 *
	 * @param id Challenge id, which is passed to server in URL
	 * @return {@link HRChallenge} object created from JSON returned by server
	 */
	public HRChallenge getChallengeDetails(int id) throws IOException {
		return getChallengeDetails("" + id);
	}

	public HRChallenge getChallengeDetails(String slug) throws IOException {
		String body = getJsonStringFrom("/rest/contests/master/challenges/" + slug);

		ObjectMapper mapper = new ObjectMapper();
		JsonNode jnRoot = mapper.readValue(body.getBytes(), JsonNode.class);

		JsonNode jnChallenge = jnRoot.get("model");

		HRChallenge cleanCh = new HRChallenge();

		cleanCh.setSlug(jnChallenge.get("slug").asText());
		cleanCh.setName(jnChallenge.get("name").asText());
		cleanCh.setDescriptions(getChallengeDescriptions(body));
		cleanCh.setSubmissions(new ArrayList<HRSubmission>());

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
				.sourceCode(jnSubmission.get("code").asText().replaceAll("\n", System.lineSeparator()))
				.build();
	}

	/**
	 * Returns body from GET request to specified URL using Cookie authentication
	 *
	 * @param url The url argument must specify an absolute URL
	 * @return JSON string returned by server from supplied address
	 */
	private static String getJsonStringFrom(String url) {
		ResponseHandler<String> handler = new BasicResponseHandler();

		String body = null;
		try {
			body = handler.handleResponse(authenticateAndGetURL(Defaults.HOST + url));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return body;
	}

	private static HttpResponse authenticateAndGetURL(String url) throws IOException {
		System.out.println("Getting: " + url);
		return httpClient.execute(new HttpGet(url));
	}

}
