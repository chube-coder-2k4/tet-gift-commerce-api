package com.tetgift.controller;

import com.tetgift.dto.request.BlogRequest;
import com.tetgift.dto.request.BlogTopicRequest;
import com.tetgift.dto.response.*;
import com.tetgift.service.BlogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Tag(name = "Blog Management", description = "APIs for managing blogs and blog topics")
public class BlogController {
    private final BlogService blogService;

    // === TOPICS ===

    @PostMapping("/blog-topics")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create blog topic (ADMIN)")
    public ResponseEntity<ResponseData<BlogTopicResponse>> createTopic(@RequestBody @Valid BlogTopicRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseData<>(HttpStatus.CREATED.value(), "Topic created",
                        blogService.createTopic(request)));
    }

    @GetMapping("/blog-topics")
    @Operation(summary = "Get all blog topics")
    public ResponseEntity<ResponseData<List<BlogTopicResponse>>> getAllTopics() {
        return ResponseEntity
                .ok(new ResponseData<>(HttpStatus.OK.value(), "Topics fetched", blogService.getAllTopics()));
    }

    @PutMapping("/blog-topics/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update blog topic (ADMIN)")
    public ResponseEntity<ResponseData<BlogTopicResponse>> updateTopic(@PathVariable Long id,
            @RequestBody @Valid BlogTopicRequest request) {
        return ResponseEntity
                .ok(new ResponseData<>(HttpStatus.OK.value(), "Topic updated", blogService.updateTopic(id, request)));
    }

    @DeleteMapping("/blog-topics/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete blog topic (ADMIN)")
    public ResponseEntity<ResponseData<Void>> deleteTopic(@PathVariable Long id) {
        blogService.deleteTopic(id);
        return ResponseEntity.ok(new ResponseData<>(HttpStatus.OK.value(), "Topic deleted", null));
    }

    // === BLOGS ===

    @PostMapping("/blogs")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create blog post (ADMIN)")
    public ResponseEntity<ResponseData<BlogResponse>> createBlog(@RequestBody @Valid BlogRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseData<>(HttpStatus.CREATED.value(), "Blog created", blogService.createBlog(request)));
    }

    @GetMapping("/blogs")
    @Operation(summary = "Get all blog posts (PUBLIC)")
    public ResponseEntity<ResponseData<PageResponse<BlogResponse>>> getAllBlogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity
                .ok(new ResponseData<>(HttpStatus.OK.value(), "Blogs fetched", blogService.getAllBlogs(page, size)));
    }

    @GetMapping("/blogs/{id}")
    @Operation(summary = "Get blog post by ID (PUBLIC)")
    public ResponseEntity<ResponseData<BlogResponse>> getBlog(@PathVariable Long id) {
        return ResponseEntity
                .ok(new ResponseData<>(HttpStatus.OK.value(), "Blog fetched", blogService.getBlogById(id)));
    }

    @PutMapping("/blogs/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update blog post (ADMIN)")
    public ResponseEntity<ResponseData<BlogResponse>> updateBlog(@PathVariable Long id,
            @RequestBody @Valid BlogRequest request) {
        return ResponseEntity
                .ok(new ResponseData<>(HttpStatus.OK.value(), "Blog updated", blogService.updateBlog(id, request)));
    }

    @DeleteMapping("/blogs/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete blog post (ADMIN)")
    public ResponseEntity<ResponseData<Void>> deleteBlog(@PathVariable Long id) {
        blogService.deleteBlog(id);
        return ResponseEntity.ok(new ResponseData<>(HttpStatus.OK.value(), "Blog deleted", null));
    }
}
