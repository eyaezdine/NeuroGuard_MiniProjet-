package com.neuroguard.forumservice.services.impl;

import com.neuroguard.forumservice.entities.Post;
import com.neuroguard.forumservice.entities.PostStatus;
import com.neuroguard.forumservice.exceptions.BadRequestException;
import com.neuroguard.forumservice.exceptions.NotFoundException;
import com.neuroguard.forumservice.repositories.PostRepository;
import com.neuroguard.forumservice.services.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepo;

    @Override
    public Post create(Post post) {
        if (post.getTitle() == null || post.getTitle().isBlank())
            throw new BadRequestException("title is required");
        if (post.getContent() == null || post.getContent().isBlank())
            throw new BadRequestException("content is required");
        if (post.getAuthorUserId() == null)
            throw new BadRequestException("authorUserId is required");
        if (post.getAuthorRole() == null || post.getAuthorRole().isBlank())
            throw new BadRequestException("authorRole is required");

        post.setStatus(PostStatus.PENDING);
        return postRepo.save(post);
    }

    @Override
    public List<Post> getPublished() {
        return postRepo.findByStatus(PostStatus.PUBLISHED);
    }

    @Override
    public List<Post> getAllAdmin() {
        return postRepo.findAll();
    }

    @Override
    public Post getById(Long id) {
        Post p = postRepo.findById(id).orElseThrow(() -> new NotFoundException("Post not found"));
        if (p.getStatus() == PostStatus.DELETED) throw new NotFoundException("Post not found");
        return p;
    }

    @Override
    public Post update(Long id, Post post) {
        Post existing = getById(id);

        if (existing.getStatus() == PostStatus.DELETED)
            throw new NotFoundException("Post not found");

        if (post.getTitle() != null && !post.getTitle().isBlank()) existing.setTitle(post.getTitle());
        if (post.getContent() != null && !post.getContent().isBlank()) existing.setContent(post.getContent());
        if (post.getCategory() != null) existing.setCategory(post.getCategory());
        if (post.getTags() != null) existing.setTags(post.getTags());

        return postRepo.save(existing);
    }

    @Override
    public void delete(Long id) {
        Post existing = getById(id);
        existing.setStatus(PostStatus.DELETED);
        postRepo.save(existing);
    }

    @Override
    public List<Post> search(String q) {
        if (q == null || q.isBlank()) throw new BadRequestException("q is required");
        return postRepo.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(q, q);
    }

    @Override
    public Post publish(Long id) {
        Post p = getById(id);
        p.setStatus(PostStatus.PUBLISHED);
        return postRepo.save(p);
    }

    @Override
    public Post hide(Long id) {
        Post p = getById(id);
        p.setStatus(PostStatus.HIDDEN);
        return postRepo.save(p);
    }
}