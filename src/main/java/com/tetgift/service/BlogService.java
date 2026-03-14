package com.tetgift.service;

import com.tetgift.dto.request.BlogRequest;
import com.tetgift.dto.request.BlogTopicRequest;
import com.tetgift.dto.response.BlogResponse;
import com.tetgift.dto.response.BlogTopicResponse;
import com.tetgift.dto.response.PageResponse;

import java.util.List;

public interface BlogService {
    // Topics
    BlogTopicResponse createTopic(BlogTopicRequest request);

    List<BlogTopicResponse> getAllTopics();

    BlogTopicResponse updateTopic(Long id, BlogTopicRequest request);

    void deleteTopic(Long id);

    // Blogs
    BlogResponse createBlog(BlogRequest request);

    BlogResponse getBlogById(Long id);

    PageResponse<BlogResponse> getAllBlogs(int page, int size);

    BlogResponse updateBlog(Long id, BlogRequest request);

    void deleteBlog(Long id);
}
