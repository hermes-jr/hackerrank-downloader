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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HRChallengeDescriptionTest {
    @Test
    public void isHRChallengeDescriptionBuilderBuildingFull() {
        HRChallengeDescription submission = HRChallengeDescription.builder()
                .language("Chinese")
                .body("a b c d\nc d e f")
                .bodyHTML("<h1>markup</h1>")
                .build();

        System.out.println("Inspecting " + submission);

        assertThat(submission.getLanguage()).isEqualTo("Chinese");
        assertThat(submission.getBody()).isEqualTo("a b c d\nc d e f");
        assertThat(submission.getBodyHTML()).isEqualTo("<h1>markup</h1>");
    }
}
