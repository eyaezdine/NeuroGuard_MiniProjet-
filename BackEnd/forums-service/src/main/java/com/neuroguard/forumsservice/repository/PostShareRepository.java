package com.neuroguard.forumsservice.repository;

import com.neuroguard.forumsservice.entity.PostShare;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PostShareRepository extends JpaRepository<PostShare, Long> {
    Optional<PostShare> findByPostIdAndUserId(Long postId, String userId);
    boolean existsByPostIdAndUserId(Long postId, String userId);
    int countByPostId(Long postId);
}