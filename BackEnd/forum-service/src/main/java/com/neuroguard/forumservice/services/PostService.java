package com.neuroguard.forumservice.services;

import com.neuroguard.forumservice.entities.Post;

import java.util.List;

public interface PostService {
    Post create(Post post);
    List<Post> getPublished();
    List<Post> getAllAdmin();
    Post getById(Long id);
    Post update(Long id, Post post);
    void delete(Long id); // soft delete
    List<Post> search(String q);
    Post publish(Long id);
    Post hide(Long id);
}