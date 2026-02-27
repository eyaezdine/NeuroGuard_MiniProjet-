package com.neuroguard.forumservice.services.impl;

import com.neuroguard.forumservice.entities.Post;
import com.neuroguard.forumservice.entities.Report;
import com.neuroguard.forumservice.exceptions.BadRequestException;
import com.neuroguard.forumservice.exceptions.NotFoundException;
import com.neuroguard.forumservice.repositories.PostRepository;
import com.neuroguard.forumservice.repositories.ReportRepository;
import com.neuroguard.forumservice.services.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepo;
    private final PostRepository postRepo;

    @Override
    public Report reportPost(Long postId, Report report) {
        Post post = postRepo.findById(postId).orElseThrow(() -> new NotFoundException("Post not found"));

        if (report.getReason() == null)
            throw new BadRequestException("reason is required");
        if (report.getReporterUserId() == null)
            throw new BadRequestException("reporterUserId is required");

        report.setPost(post);
        return reportRepo.save(report);
    }

    @Override
    public List<Report> getAll() {
        return reportRepo.findAll();
    }
}