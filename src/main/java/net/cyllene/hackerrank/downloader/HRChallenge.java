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

import java.util.List;

public class HRChallenge {
	private long ctime;
	private String name;
	private String slug;
	private List<HRChallengeDescription> descriptions;
	private List<HRSubmission> submissions;

	public long getCtime() {
		return ctime;
	}

	public void setCtime(long ctime) {
		this.ctime = ctime;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSlug() {
		return slug;
	}

	public void setSlug(String slug) {
		this.slug = slug;
	}

	public List<HRChallengeDescription> getDescriptions() {
		return descriptions;
	}

	public void setDescriptions(List<HRChallengeDescription> descriptions) {
		this.descriptions = descriptions;
	}

	public List<HRSubmission> getSubmissions() {
		return submissions;
	}

	public void setSubmissions(List<HRSubmission> submissions) {
		this.submissions = submissions;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		HRChallenge that = (HRChallenge) o;

		if (getCtime() != that.getCtime()) return false;
		if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null) return false;
		if (getSlug() != null ? !getSlug().equals(that.getSlug()) : that.getSlug() != null) return false;
		if (!getDescriptions().equals(that.getDescriptions())) return false;
		return getSubmissions() != null ? getSubmissions().equals(that.getSubmissions()) : that.getSubmissions() == null;

	}

	@Override
	public int hashCode() {
		int result = (int) (getCtime() ^ (getCtime() >>> 32));
		result = 31 * result + (getName() != null ? getName().hashCode() : 0);
		result = 31 * result + (getSlug() != null ? getSlug().hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "HRChallenge{" +
				"ctime=" + ctime +
				", name='" + name + '\'' +
				", slug='" + slug + '\'' +
				", descriptions=" + descriptions +
				", submissions=" + submissions +
				'}';
	}
}
