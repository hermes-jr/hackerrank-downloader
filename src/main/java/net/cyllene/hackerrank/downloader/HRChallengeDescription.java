package net.cyllene.hackerrank.downloader;

/**
 * An immutable object containing single description of a challenge
 * in one specific language
 */
public class HRChallengeDescription {
	private final String language;
	/**
	 * usually is null
	 */
	private final String body;
	/**
	 * contains a UTF-8 string with HTML markup
	 */
	private final String bodyHTML;

	public String getBodyHTML() {
		return bodyHTML;
	}

	public String getBody() {
		return body;
	}

	public String getLanguage() {
		return language;
	}

	@Override
	public String toString() {
		return "HRChallengeDescription{" +
				"language='" + language + '\'' +
				", body='" + body + '\'' +
				", bodyHTML='" + bodyHTML + '\'' +
				'}';
	}

	public static class Builder {
		private String language;
		private String body;
		private String bodyHTML;

		public Builder language(String val) {
			language = val;
			return this;
		}

		public Builder body(String val) {
			body = val;
			return this;
		}

		public Builder bodyHTML(String val) {
			bodyHTML = val;
			return this;
		}

		public HRChallengeDescription build() {
			return new HRChallengeDescription(this);
		}
	}

	private HRChallengeDescription(Builder builder) {
		this.language = builder.language;
		this.body = builder.body;
		this.bodyHTML = builder.bodyHTML;
	}

}
