package io.github.vishalmysore.controller;

import io.github.vishalmysore.model.MoltbookPost;
import io.github.vishalmysore.service.MoltbookService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for direct Moltbook operations
 */
@RestController
@RequestMapping("/api/moltbook")
@Slf4j
public class MoltbookController {

    private final MoltbookService moltbookService;

    public MoltbookController(MoltbookService moltbookService) {
        this.moltbookService = moltbookService;
    }

    @GetMapping("/status")
    public ResponseEntity<String> getStatus() {
        try {
            String status = moltbookService.getClaimStatus();
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("Failed to get status", e);
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/feed")
    public ResponseEntity<List<MoltbookPost>> getFeed(
            @RequestParam(defaultValue = "hot") String sort,
            @RequestParam(defaultValue = "25") int limit) {
        try {
            List<MoltbookPost> posts = moltbookService.getFeed(sort, limit);
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            log.error("Failed to get feed", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/posts")
    public ResponseEntity<MoltbookPost> createPost(@RequestBody CreatePostRequest request) {
        try {
            MoltbookPost post = moltbookService.createPost(
                    request.getSubmolt(),
                    request.getTitle(),
                    request.getContent(),
                    request.getUrl());
            return ResponseEntity.ok(post);
        } catch (Exception e) {
            log.error("Failed to create post", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/posts/{postId}/upvote")
    public ResponseEntity<String> upvotePost(@PathVariable String postId) {
        try {
            moltbookService.upvotePost(postId);
            return ResponseEntity.ok("Upvoted!");
        } catch (Exception e) {
            log.error("Failed to upvote", e);
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<String> createComment(
            @PathVariable String postId,
            @RequestBody CreateCommentRequest request) {
        try {
            moltbookService.createComment(postId, request.getContent());
            return ResponseEntity.ok("Comment created!");
        } catch (Exception e) {
            log.error("Failed to create comment", e);
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<MoltbookPost>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "20") int limit) {
        try {
            List<MoltbookPost> posts = moltbookService.searchPosts(q, limit);
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            log.error("Failed to search", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Data
    public static class CreatePostRequest {
        private String submolt;
        private String title;
        private String content;
        private String url;
    }

    @Data
    public static class CreateCommentRequest {
        private String content;
    }
}
