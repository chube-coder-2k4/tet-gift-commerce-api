package com.tetgift.service.ai;

import com.tetgift.dto.response.ChatbotResponse.ProductSuggestion;
import com.tetgift.dto.response.ConversationState;
import com.tetgift.model.entity.BundleEntity;
import com.tetgift.model.entity.ProductEntity;
import com.tetgift.model.entity.ProductImageEntity;
import com.tetgift.repository.jpa.BundleRepository;
import com.tetgift.repository.jpa.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Recommendation Engine combining Rule-Based (Budget Filter) and Vector Semantic Search
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {

    private final EmbeddingService embeddingService;
    private final ProductRepository productRepository;
    private final BundleRepository bundleRepository;

    public List<ProductSuggestion> recommendProducts(String query, ConversationState state) {
        log.info("Recommendation Engine started matching for Budget: {}", state.getMaxBudget());
        
        List<ProductSuggestion> allSuggestions = new ArrayList<>(buildFromVectorSearch(query));

        // Keyword Fallback if Vector failed or returned empty
        if (allSuggestions.isEmpty() && query != null && !query.trim().isEmpty()) {
            List<ProductEntity> kws = productRepository.searchByKeyword(query);
            for (ProductEntity p : kws) {
                allSuggestions.add(ProductSuggestion.builder()
                    .id(p.getId()).type("PRODUCT").name(p.getName())
                    .price(p.getPrice().toString()).stock(p.getStock())
                    .imageUrl(extractPrimaryImage(p)).build());
            }
        }

        // Apply Rule-based Filtering (Price filtering)
        List<ProductSuggestion> filtered = allSuggestions.stream()
            .filter(s -> isUnderBudget(s.getPrice(), state.getMaxBudget()))
            // Remove duplicates
            .collect(Collectors.toMap(s -> s.getType() + "_" + s.getId(), s -> s, (s1, s2) -> s1))
            .values().stream()
            .collect(Collectors.toList());

        // Advanced Feature: Fallback to Auto Recommend Best Sellers under budget
        if (filtered.isEmpty() && query.length() > 0) {
            log.info("Semantic matches dropped by budget rule. Falling back to global best sellers...");
            filtered = fallbackBestSellers(state.getMaxBudget());
        }

        return filtered.stream().limit(3).collect(Collectors.toList());
    }

    private boolean isUnderBudget(String priceStr, BigDecimal maxBudget) {
        if (maxBudget == null) return true;
        try {
            BigDecimal price = new BigDecimal(priceStr.replaceAll("[^0-9.]", ""));
            return price.compareTo(maxBudget) <= 0;
        } catch (Exception e) {
            return true;
        }
    }

    private List<ProductSuggestion> buildFromVectorSearch(String query) {
        List<ProductSuggestion> suggestions = new ArrayList<>();
        // Fetch Top 10 to afford space for filtering later
        List<Document> results = embeddingService.searchSimilar(query, 10); 

        for (Document doc : results) {
            String type = doc.getMetadata().getOrDefault("type", "PRODUCT").toString();
            String idStr = doc.getMetadata().getOrDefault("id", "0").toString();
            try {
                Long id = Long.parseLong(idStr);
                if ("PRODUCT".equals(type)) {
                    productRepository.findByIdAndIsActiveTrue(id).ifPresent(p -> {
                        suggestions.add(ProductSuggestion.builder()
                            .id(p.getId())
                            .type("PRODUCT")
                            .name(p.getName())
                            .price(p.getPrice().toString())
                            .stock(p.getStock())
                            .imageUrl(extractPrimaryImage(p))
                            .build());
                    });
                } else if ("BUNDLE".equals(type)) {
                    bundleRepository.findById(id).filter(BundleEntity::isActive).ifPresent(b -> {
                        suggestions.add(ProductSuggestion.builder()
                            .id(b.getId())
                            .type("BUNDLE")
                            .name(b.getName())
                            .price(b.getPrice().toString())
                            .imageUrl(b.getImage())
                            .build());
                    });
                }
            } catch (Exception ignored) {}
        }
        return suggestions;
    }

    public List<ProductSuggestion> fallbackBestSellers(BigDecimal maxBudget) {
        List<ProductSuggestion> fb = new ArrayList<>();
        List<ProductEntity> tops = productRepository.findTopActiveProducts(PageRequest.of(0, 15));
        
        for (ProductEntity p : tops) {
            if (isUnderBudget(p.getPrice().toString(), maxBudget)) {
                fb.add(ProductSuggestion.builder()
                    .id(p.getId()).type("PRODUCT")
                    .name(p.getName())
                    .price(p.getPrice().toString())
                    .stock(p.getStock())
                    .imageUrl(extractPrimaryImage(p))
                    .build());
            }
        }
        return fb.stream().limit(3).collect(Collectors.toList());
    }

    private String extractPrimaryImage(ProductEntity product) {
        if (product.getProductImages() != null && !product.getProductImages().isEmpty()) {
            return product.getProductImages().stream()
                .filter(ProductImageEntity::isPrimary)
                .findFirst()
                .map(ProductImageEntity::getImageUrl)
                .orElse(product.getProductImages().get(0).getImageUrl());
        }
        return product.getImage();
    }
}
