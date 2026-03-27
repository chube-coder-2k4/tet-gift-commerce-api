package com.tetgift.service;

import com.tetgift.dto.request.HomeSlideRequest;
import com.tetgift.dto.response.HomeSlideResponse;

import java.util.List;

public interface HomeSlideService {
    List<HomeSlideResponse> getAllActiveSlides();
    List<HomeSlideResponse> getAllSlidesAdmin();
    HomeSlideResponse createSlide(HomeSlideRequest request);
    HomeSlideResponse updateSlide(Long id, HomeSlideRequest request);
    void deleteSlide(Long id);
    HomeSlideResponse getSlideById(Long id);
}
