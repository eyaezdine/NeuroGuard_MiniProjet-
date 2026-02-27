package com.neuroguard.forumservice.services.impl;

import com.neuroguard.forumservice.entities.Comment;
import com.neuroguard.forumservice.entities.Post;
import com.neuroguard.forumservice.entities.PostStatus;
import com.neuroguard.forumservice.exceptions.BadRequestException;
import com.neuroguard.forumservice.exceptions.NotFoundException;
import com.neuroguard.forumservice.repositories.CommentRepository;
import com.neuroguard.forumservice.repositories.PostRepository;
import com.neuroguard.forumservice.services.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepo;
    private final PostRepository postRepo;

    @Override
    public Comment addToPost(Long postId, Comment comment) {
        Post post = postRepo.findById(postId).orElseThrow(() -> new NotFoundException("Post not found"));
        if (post.getStatus() != PostStatus.PUBLISHED)
            throw new BadRequestException("You can comment only on published posts");

        if (comment.getContent() == null || comment.getContent().isBlank())
            throw new BadRequestException("content is required");
        if (comment.getAuthorUserId() == null)
            throw new BadRequestException("authorUserId is required");
        if (comment.getAuthorRole() == null || comment.getAuthorRole().isBlank())
            throw new BadRequestException("authorRole is required");

        comment.setPost(post);
        return commentRepo.save(comment);
    }

    @Override
    public List<Comment> getByPost(Long postId) {
        return commentRepo.findByPostId(postId);
    }

    @Override
    public void delete(Long id) {
        Comment c = commentRepo.findById(id).orElseThrow(() -> new NotFoundException("Comment not found"));
        commentRepo.delete(c);
    }
}