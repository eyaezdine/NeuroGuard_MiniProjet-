package com.neuroguard.forumservice.controllers;

import com.neuroguard.forumservice.entities.Comment;
import com.neuroguard.forumservice.services.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/api/posts/{postId}/comments")
    public Comment addComment(@PathVariable Long postId, @RequestBody Comment comment) {
        return commentService.addToPost(postId, comment);
    }

    @GetMapping("/api/posts/{postId}/comments")
    public List<Comment> getComments(@PathVariable Long postId) {
        return commentService.getByPost(postId);
    }

    @DeleteMapping("/api/comments/{id}")
    public void delete(@PathVariable Long id) {
        commentService.delete(id);
    }
}