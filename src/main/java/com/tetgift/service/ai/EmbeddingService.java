package com.tetgift.service.ai;

import com.tetgift.model.entity.BundleEntity;
import com.tetgift.model.entity.ProductEntity;
import com.tetgift.repository.jpa.BundleRepository;
import com.tetgift.repository.jpa.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

/**
 * Service for managing product/bundle embeddings in pgvector
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "chatbot.enabled", havingValue = "true", matchIfMissing = true)
public class EmbeddingService {

    private final VectorStore vectorStore;
    private final ProductRepository productRepository;
    private final BundleRepository bundleRepository;
    private final JdbcTemplate jdbcTemplate;

    /**
     * Clear all embeddings from vector store
     */
    @Transactional
    public void clearAllEmbeddings() {
        log.info("Clearing all embeddings from vector_store...");
        try {
            jdbcTemplate.execute("TRUNCATE TABLE vector_store CASCADE");
            log.info("Cleared vector_store table successfully.");
        } catch (Exception e) {
            log.warn("Failed to truncate vector_store, attempting DELETE... Error: {}", e.getMessage());
            jdbcTemplate.execute("DELETE FROM vector_store");
        }
    }

    /**
     * Generate and store embedding for a product
     */
    @Transactional
    public void embedProduct(ProductEntity product) {
        log.info("Generating embedding for product: {}", product.getName());

        String embeddingText = buildProductEmbeddingText(product);

        Document document = new Document(
            embeddingText,
            Map.of(
                "type", "PRODUCT",
                "id", product.getId().toString(),
                "name", product.getName(),
                "price", product.getPrice().toString(),
                "category", product.getCategory() != null ? product.getCategory().getName() : "",
                "stock", product.getStock().toString()
            )
        );

        vectorStore.add(List.of(document));
        log.info("Embedding stored for product ID: {}", product.getId());
    }

    /**
     * Generate and store embedding for a bundle
     */
    @Transactional
    public void embedBundle(BundleEntity bundle) {
        log.info("Generating embedding for bundle: {}", bundle.getName());

        String embeddingText = buildBundleEmbeddingText(bundle);

        Document document = new Document(
            embeddingText,
            Map.of(
                "type", "BUNDLE",
                "id", bundle.getId().toString(),
                "name", bundle.getName(),
                "price", bundle.getPrice().toString()
            )
        );

        vectorStore.add(List.of(document));
        log.info("Embedding stored for bundle ID: {}", bundle.getId());
    }

    /**
     * Generate embeddings for all active products and bundles
     */
    @Transactional
    public int embedAllProducts() {
        log.info("Generating embeddings for all products...");

        List<ProductEntity> products = productRepository.findAll().stream()
            .filter(ProductEntity::isActive)
            .toList();

        List<Document> documents = products.stream()
            .map(product -> new Document(
                buildProductEmbeddingText(product),
                Map.of(
                    "type", "PRODUCT",
                    "id", product.getId().toString(),
                    "name", product.getName(),
                    "price", product.getPrice().toString(),
                    "category", product.getCategory() != null ? product.getCategory().getName() : "",
                    "stock", product.getStock().toString()
                )
            ))
            .toList();

        if (!documents.isEmpty()) {
            vectorStore.add(documents);
            log.info("Generated embeddings for {} products", products.size());
        }

        return documents.size();
    }

    /**
     * Generate embeddings for all active bundles
     */
    @Transactional
    public int embedAllBundles() {
        log.info("Generating embeddings for all bundles...");

        List<BundleEntity> bundles = bundleRepository.findAll().stream()
            .filter(BundleEntity::isActive)
            .toList();

        List<Document> documents = bundles.stream()
            .map(bundle -> new Document(
                buildBundleEmbeddingText(bundle),
                Map.of(
                    "type", "BUNDLE",
                    "id", bundle.getId().toString(),
                    "name", bundle.getName(),
                    "price", bundle.getPrice().toString()
                )
            ))
            .toList();

        if (!documents.isEmpty()) {
            vectorStore.add(documents);
            log.info("Generated embeddings for {} bundles", bundles.size());
        }

        return documents.size();
    }

    /**
     * Search similar products/bundles using semantic search
     */
    public List<Document> searchSimilar(String query, int topK) {
        log.info("Searching for similar items: {}", query);
        return vectorStore.similaritySearch(
            SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(0.55)
                .build()
        );
    }

    private String buildProductEmbeddingText(ProductEntity product) {
        StringBuilder sb = new StringBuilder();
        sb.append("Sản phẩm: ").append(product.getName()).append("\n");
        if (product.getDescription() != null) {
            sb.append("Mô tả: ").append(product.getDescription()).append("\n");
        }
        sb.append("Giá: ").append(product.getPrice()).append(" VNĐ\n");
        if (product.getCategory() != null) {
            sb.append("Danh mục: ").append(product.getCategory().getName()).append("\n");
        }
        sb.append("Tồn kho: ").append(product.getStock()).append(" sản phẩm\n");
        return sb.toString();
    }

    private String buildBundleEmbeddingText(BundleEntity bundle) {
        StringBuilder sb = new StringBuilder();
        sb.append("Combo/Giỏ quà: ").append(bundle.getName()).append("\n");
        sb.append("Giá: ").append(bundle.getPrice()).append(" VNĐ\n");
        sb.append("Loại: ").append(bundle.isCustom() ? "Combo tùy chỉnh" : "Combo có sẵn").append("\n");

        if (bundle.getBundleProducts() != null && !bundle.getBundleProducts().isEmpty()) {
            sb.append("Bao gồm: ");
            bundle.getBundleProducts().forEach(bp -> {
                if (bp.getProduct() != null) {
                    sb.append(bp.getProduct().getName()).append(" (x").append(bp.getQuantity()).append("), ");
                }
            });
        }
        return sb.toString();
    }
}


