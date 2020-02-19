package net.cyllene.hackerrank.downloader.dto;

import lombok.Data;

import java.util.List;

@Data
public class SubmissionsCollection {
    private List<Submission> models;
    private int total;
}
