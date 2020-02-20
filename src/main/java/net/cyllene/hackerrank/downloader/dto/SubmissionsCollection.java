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
package net.cyllene.hackerrank.downloader.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * /rest/contests/master/submissions/
 * A wrapper class
 */
@Data
@NoArgsConstructor
public class SubmissionsCollection {
    private List<SubmissionSummary> models;
    private int total;
}
