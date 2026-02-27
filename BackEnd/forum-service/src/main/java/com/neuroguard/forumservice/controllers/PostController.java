package com.neuroguard.forumservice.controllers;

import com.neuroguard.forumservice.entities.Post;
import com.neuroguard.forumservice.services.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public Post create(@RequestBody Post post) {
        return postService.create(post);
    }

    @GetMapping
    public List<Post> getPublished() {
        return postService.getPublished();
    }

    @GetMapping("/admin")
    public List<Post> getAllAdmin() {
        return postService.getAllAdmin();
    }

    @GetMapping("/{id}")
    public Post getById(@PathVariable Long id) {
        return postService.getById(id);
    }

    @PutMapping("/{id}")
    public Post update(@PathVariable Long id, @RequestBody Post post) {
        return postService.update(id, post);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        postService.delete(id);
    }

    @GetMapping("/search")
    public List<Post> search(@RequestParam String q) {
        return postService.search(q);
    }

    @PutMapping("/{id}/publish")
    public Post publish(@PathVariable Long id) {
        return postService.publish(id);
    }

    @PutMapping("/{id}/hide")
    public Post hide(@PathVariable Long id) {
        return postService.hide(id);
    }
}