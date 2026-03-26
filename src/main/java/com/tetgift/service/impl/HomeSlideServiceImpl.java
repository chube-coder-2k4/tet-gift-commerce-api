package com.tetgift.service.impl;

import com.tetgift.dto.request.HomeSlideRequest;
import com.tetgift.dto.response.HomeSlideResponse;
import com.tetgift.exception.ResourceNotFoundException;
import com.tetgift.model.entity.HomeSlideEntity;
import com.tetgift.repository.jpa.HomeSlideRepository;
import com.tetgift.service.HomeSlideService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HomeSlideServiceImpl implements HomeSlideService {

    private final HomeSlideRepository homeSlideRepository;

    @Override
    public List<HomeSlideResponse> getAllActiveSlides() {
        return homeSlideRepository.findAllByIsActiveTrueOrderBySlideOrderAsc()
                .stream().map(this::toResponse).toList();
    }

    @Override
    public List<HomeSlideResponse> getAllSlidesAdmin() {
        return homeSlideRepository.findAllByOrderBySlideOrderAsc()
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public HomeSlideResponse createSlide(HomeSlideRequest request) {
        HomeSlideEntity slide = HomeSlideEntity.builder()
                .image(request.getImage())
                .title(request.getTitle())
                .subtitle(request.getSubtitle())
                .cta(request.getCta())
                .link(request.getLink())
                .slideOrder(request.getSlideOrder() != null ? request.getSlideOrder() : 0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
        return toResponse(homeSlideRepository.save(slide));
    }

    @Override
    @Transactional
    public HomeSlideResponse updateSlide(Long id, HomeSlideRequest request) {
        HomeSlideEntity slide = homeSlideRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Slide not found: " + id));
        
        if (request.getImage() != null) slide.setImage(request.getImage());
        if (request.getTitle() != null) slide.setTitle(request.getTitle());
        if (request.getSubtitle() != null) slide.setSubtitle(request.getSubtitle());
        if (request.getCta() != null) slide.setCta(request.getCta());
        if (request.getLink() != null) slide.setLink(request.getLink());
        if (request.getSlideOrder() != null) slide.setSlideOrder(request.getSlideOrder());
        if (request.getIsActive() != null) slide.setActive(request.getIsActive());
        
        return toResponse(homeSlideRepository.save(slide));
    }

    @Override
    @Transactional
    public void deleteSlide(Long id) {
        if (!homeSlideRepository.existsById(id)) {
            throw new ResourceNotFoundException("Slide not found: " + id);
        }
        homeSlideRepository.deleteById(id);
    }

    @Override
    public HomeSlideResponse getSlideById(Long id) {
        return homeSlideRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Slide not found: " + id));
    }

    private HomeSlideResponse toResponse(HomeSlideEntity slide) {
        return HomeSlideResponse.builder()
                .id(slide.getId())
                .image(slide.getImage())
                .title(slide.getTitle())
                .subtitle(slide.getSubtitle())
                .cta(slide.getCta())
                .link(slide.getLink())
                .slideOrder(slide.getSlideOrder())
                .isActive(slide.isActive())
                .createdAt(slide.getCreatedAt())
                .updatedAt(slide.getUpdatedAt())
                .build();
    }
}
