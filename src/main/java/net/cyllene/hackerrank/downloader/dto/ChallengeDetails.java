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

package net.cyllene.hackerrank.downloader.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * /rest/contests/master/challenges/{id}
 */
@Data
@NoArgsConstructor
public class ChallengeDetails {
    private long id;
    /**
     * Part of url
     */
    private String slug;
    /**
     * contains a UTF-8 string with HTML markup
     */
    @JsonProperty("body_html")
    private String bodyHtml;
    /**
     * Short description
     */
    private String preview;
}
