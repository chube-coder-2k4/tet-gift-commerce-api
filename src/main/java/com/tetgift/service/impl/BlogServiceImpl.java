package com.tetgift.service.impl;

import com.tetgift.dto.request.BlogRequest;
import com.tetgift.dto.request.BlogTopicRequest;
import com.tetgift.dto.response.BlogResponse;
import com.tetgift.dto.response.BlogTopicResponse;
import com.tetgift.dto.response.PageResponse;
import com.tetgift.exception.ResourceNotFoundException;
import com.tetgift.model.entity.BlogEntity;
import com.tetgift.model.entity.BlogTopicEntity;
import com.tetgift.repository.jpa.BlogRepository;
import com.tetgift.repository.jpa.BlogTopicRepository;
import com.tetgift.service.BlogService;
import com.tetgift.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BlogServiceImpl implements BlogService {

    private final BlogTopicRepository topicRepository;
    private final BlogRepository blogRepository;
    private final CloudinaryService cloudinaryService;

    // === TOPICS ===

    @Override
    @Transactional
    public BlogTopicResponse createTopic(BlogTopicRequest request) {
        BlogTopicEntity topic = BlogTopicEntity.builder().name(request.getName()).build();
        BlogTopicEntity saved = topicRepository.save(topic);
        return toTopicResponse(saved);
    }

    @Override
    public List<BlogTopicResponse> getAllTopics() {
        return topicRepository.findAll().stream().map(this::toTopicResponse).toList();
    }

    @Override
    @Transactional
    public BlogTopicResponse updateTopic(Long id, BlogTopicRequest request) {
        BlogTopicEntity topic = topicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found"));
        topic.setName(request.getName());
        return toTopicResponse(topicRepository.save(topic));
    }

    @Override
    @Transactional
    public void deleteTopic(Long id) {
        if (!topicRepository.existsById(id)) {
            throw new ResourceNotFoundException("Topic not found");
        }
        topicRepository.deleteById(id);
    }

    // === BLOGS ===

    @Override
    @Transactional
    public BlogResponse createBlog(BlogRequest request) {
        BlogEntity blog = BlogEntity.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .build();

        if (request.getTopicId() != null) {
            BlogTopicEntity topic = topicRepository.findById(request.getTopicId())
                    .orElseThrow(() -> new ResourceNotFoundException("Topic not found"));
            blog.setTopic(topic);
        }

        return toBlogResponse(blogRepository.save(blog));
    }

    @Override
    @Transactional
    public BlogResponse createBlog(BlogRequest request, MultipartFile image) throws IOException {
        BlogEntity blog = BlogEntity.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .build();

        if (request.getTopicId() != null) {
            BlogTopicEntity topic = topicRepository.findById(request.getTopicId())
                    .orElseThrow(() -> new ResourceNotFoundException("Topic not found"));
            blog.setTopic(topic);
        }

        if (image != null && !image.isEmpty()) {
            String imageUrl = cloudinaryService.uploadFile(image, "blogs");
            blog.setImage(imageUrl);
        }

        return toBlogResponse(blogRepository.save(blog));
    }

    @Override
    public BlogResponse getBlogById(Long id) {
        BlogEntity blog = blogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog not found"));
        return toBlogResponse(blog);
    }

    @Override
    public PageResponse<BlogResponse> getAllBlogs(int page, int size) {
        Page<BlogEntity> blogs = blogRepository.findAll(
                PageRequest.of(Math.max(page, 0), size, Sort.by("createdAt").descending()));

        return PageResponse.<BlogResponse>builder()
                .data(blogs.getContent().stream().map(this::toBlogResponse).toList())
                .pageNo(page)
                .pageSize(size)
                .totalItems(blogs.getTotalElements())
                .totalPages(blogs.getTotalPages())
                .build();
    }

    @Override
    @Transactional
    public BlogResponse updateBlog(Long id, BlogRequest request) {
        BlogEntity blog = blogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog not found"));
        blog.setTitle(request.getTitle());
        blog.setContent(request.getContent());

        if (request.getTopicId() != null) {
            BlogTopicEntity topic = topicRepository.findById(request.getTopicId())
                    .orElseThrow(() -> new ResourceNotFoundException("Topic not found"));
            blog.setTopic(topic);
        }

        return toBlogResponse(blogRepository.save(blog));
    }

    @Override
    @Transactional
    public BlogResponse updateBlog(Long id, BlogRequest request, MultipartFile image) throws IOException {
        BlogEntity blog = blogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog not found"));
        blog.setTitle(request.getTitle());
        blog.setContent(request.getContent());

        if (request.getTopicId() != null) {
            BlogTopicEntity topic = topicRepository.findById(request.getTopicId())
                    .orElseThrow(() -> new ResourceNotFoundException("Topic not found"));
            blog.setTopic(topic);
        }

        if (image != null && !image.isEmpty()) {
            String imageUrl = cloudinaryService.uploadFile(image, "blogs");
            blog.setImage(imageUrl);
        }

        return toBlogResponse(blogRepository.save(blog));
    }

    @Override
    @Transactional
    public void deleteBlog(Long id) {
        if (!blogRepository.existsById(id)) {
            throw new ResourceNotFoundException("Blog not found");
        }
        blogRepository.deleteById(id);
    }

    private BlogTopicResponse toTopicResponse(BlogTopicEntity topic) {
        return BlogTopicResponse.builder().id(topic.getId()).name(topic.getName()).build();
    }

    private BlogResponse toBlogResponse(BlogEntity blog) {
        return BlogResponse.builder()
                .id(blog.getId())
                .title(blog.getTitle())
                .content(blog.getContent())
                .image(blog.getImage())
                .topicName(blog.getTopic() != null ? blog.getTopic().getName() : null)
                .topicId(blog.getTopic() != null ? blog.getTopic().getId() : null)
                .createdAt(blog.getCreatedAt())
                .build();
    }
}
