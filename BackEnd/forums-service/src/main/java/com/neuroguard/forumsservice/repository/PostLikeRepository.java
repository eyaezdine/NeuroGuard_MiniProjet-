package com.neuroguard.forumsservice.repository;

import com.neuroguard.forumsservice.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    Optional<PostLike> findByPostIdAndUserId(Long postId, String userId);
    boolean existsByPostIdAndUserId(Long postId, String userId);
    void deleteByPostIdAndUserId(Long postId, String userId);
}