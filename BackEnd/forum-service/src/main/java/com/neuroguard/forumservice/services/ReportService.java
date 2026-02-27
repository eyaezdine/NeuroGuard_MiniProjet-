package com.neuroguard.forumservice.services;

import com.neuroguard.forumservice.entities.Report;

import java.util.List;

public interface ReportService {
    Report reportPost(Long postId, Report report);
    List<Report> getAll();
}