package com.neuroguard.forumservice.services;

import com.neuroguard.forumservice.entities.Comment;

import java.util.List;

public interface CommentService {
    Comment addToPost(Long postId, Comment comment);
    List<Comment> getByPost(Long postId);
    void delete(Long id);
}