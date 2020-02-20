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

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class SubmissionSummary {
    private int id;
    private long createdAt;
    private int statusCode;
    private String status;
    private int hackerId;
    private String kind;
    private BigDecimal score;
    private String language;
    private ChallengeSummary challenge;
}
