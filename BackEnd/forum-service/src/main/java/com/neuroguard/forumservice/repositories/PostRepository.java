package com.neuroguard.forumservice.repositories;

import com.neuroguard.forumservice.entities.Post;
import com.neuroguard.forumservice.entities.PostStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByStatus(PostStatus status);
    List<Post> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(String t, String c);
}