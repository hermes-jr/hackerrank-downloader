package net.cyllene.hackerrank.downloader;

public class HRSubmission {
	private final int id;
	private final String status;
	private final long ctime;
	private final int statusCode;
	private final String sourceCode;
	private final int hackerId;
	private final String kind;
	private final double score;
	private final String language;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		HRSubmission that = (HRSubmission) o;

		if (id != that.id) return false;
		if (ctime != that.ctime) return false;
		if (statusCode != that.statusCode) return false;
		if (hackerId != that.hackerId) return false;
		if (Double.compare(that.score, score) != 0) return false;
		if (status != null ? !status.equals(that.status) : that.status != null) return false;
		if (sourceCode != null ? !sourceCode.equals(that.sourceCode) : that.sourceCode != null) return false;
		if (kind != null ? !kind.equals(that.kind) : that.kind != null) return false;
		return language != null ? language.equals(that.language) : that.language == null;

	}

	@Override
	public int hashCode() {
		int result = id;
		result = 31 * result + (int) (ctime ^ (ctime >>> 32));
		result = 31 * result + statusCode;
		return result;
	}

	@Override
	public String toString() {
		return "HRSubmission{" +
				"id=" + id +
				", status='" + status + '\'' +
				", ctime=" + ctime +
				", statusCode=" + statusCode +
				", sourceCode='" + sourceCode + '\'' +
				", hackerId=" + hackerId +
				", kind='" + kind + '\'' +
				", score=" + score +
				", language='" + language + '\'' +
				'}';
	}

	public int getId() {
		return id;
	}

	public String getStatus() {
		return status;
	}

	public long getCtime() {
		return ctime;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public String getSourceCode() {
		return sourceCode;
	}

	public int getHackerId() {
		return hackerId;
	}

	public String getKind() {
		return kind;
	}

	public double getScore() {
		return score;
	}

	public String getLanguage() {
		return language;
	}

	public static class Builder {
		private int id;
		private String status;
		private long ctime;
		private int statusCode;
		private String sourceCode;
		private int hackerId;
		private String kind;
		private double score;
		private String language;

		public Builder(int id, long ctime, int statusCode) {
			this.id = id;
			this.ctime = ctime;
			this.statusCode = statusCode;
		}

		public Builder status(String val) {
			status = val;
			return this;
		}

		public Builder sourceCode(String val) {
			sourceCode = val;
			return this;
		}

		public Builder kind(String val) {
			kind = val;
			return this;
		}

		public Builder hackerId(int val) {
			hackerId = val;
			return this;
		}

		public Builder score(double val) {
			score = val;
			return this;
		}

		public Builder language(String val) {
			language = val;
			return this;
		}

		public HRSubmission build() {
			return new HRSubmission(this);
		}
	}

	private HRSubmission(Builder builder) {
		id = builder.id;
		status = builder.status;
		ctime = builder.ctime;
		statusCode = builder.statusCode;
		sourceCode = builder.sourceCode;
		hackerId = builder.hackerId;
		kind = builder.kind;
		score = builder.score;
		language = builder.language;
	}
}
