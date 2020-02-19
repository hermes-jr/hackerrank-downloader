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

import lombok.Builder;
import lombok.Data;

/**
 * An immutable object containing single description of a challenge
 * in one specific language
 */
@Data
@Builder
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
}
