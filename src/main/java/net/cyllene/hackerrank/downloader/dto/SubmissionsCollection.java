package net.cyllene.hackerrank.downloader.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class SubmissionsCollection {
    private List<SubmissionSummary> models;
    private int total;
}
