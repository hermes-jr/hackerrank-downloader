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

public class HRSubmissionTest {
    @Test
    public void isHRSubmissionBuilderBuildingFull() {
        HRSubmission submission = HRSubmission.builder()
                .id(1)
                .ctime(2L)
                .statusCode(3)
                .score(4.0)
                .sourceCode("abc")
                .hackerId(5)
                .kind("bcd")
                .language("cde")
                .status("def")
                .build();

        System.out.println("Inspecting " + submission);

        assertThat(submission.getId()).isEqualTo(1);
        assertThat(submission.getCtime()).isEqualTo(2L);
        assertThat(submission.getStatusCode()).isEqualTo(3);
        assertThat(submission.getScore()).isEqualTo(4.0);
        assertThat(submission.getSourceCode()).isEqualTo("abc");
        assertThat(submission.getHackerId()).isEqualTo(5);
        assertThat(submission.getKind()).isEqualTo("bcd");
        assertThat(submission.getLanguage()).isEqualTo("cde");
        assertThat(submission.getStatus()).isEqualTo("def");
    }

    @Test
    public void isHRSubmissionBuilderBuildingPartial() {
        HRSubmission submission = HRSubmission.builder()
                .id(10)
                .ctime(100L)
                .statusCode(1)
                .sourceCode("abc cba")
                .score(20.0)
                .build();

        System.out.println("Inspecting " + submission);

        assertThat(submission.getId()).isEqualTo(10);
        assertThat(submission.getCtime()).isEqualTo(100L);
        assertThat(submission.getStatusCode()).isEqualTo(1);
        assertThat(submission.getScore()).isEqualTo(20.0);
        assertThat(submission.getSourceCode()).isEqualTo("abc cba");
        assertThat(submission.getHackerId()).isEqualTo(0);
        assertThat(submission.getKind()).isNull();
        assertThat(submission.getLanguage()).isNull();
        assertThat(submission.getStatus()).isNull();
    }
}
