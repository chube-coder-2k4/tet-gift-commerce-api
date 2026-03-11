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

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    /**
     * Generate a deterministic UUID from a string key (e.g. "PRODUCT_11").
     * pgvector requires document IDs to be valid UUIDs.
     */
    private String toUUID(String key) {
        return UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8)).toString();
    }

    /**
     * Generate and store embedding for a product.
     * NOT @Transactional — called within saveProduct's transaction,
     * failures here must not roll back the product save.
     */
    public void embedProduct(ProductEntity product) {
        log.info("Generating embedding for product: {}", product.getName());

        // Remove old embedding first (if exists)
        removeProductEmbedding(product.getId());

        String embeddingText = buildProductEmbeddingText(product);
        String documentId = toUUID("PRODUCT_" + product.getId());

        Document document = new Document(
            documentId,
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
     * Generate and store embedding for a bundle.
     * NOT @Transactional — same reason as embedProduct.
     */
    public void embedBundle(BundleEntity bundle) {
        log.info("Generating embedding for bundle: {}", bundle.getName());

        // Remove old embedding first (if exists)
        removeBundleEmbedding(bundle.getId());

        String embeddingText = buildBundleEmbeddingText(bundle);
        String documentId = toUUID("BUNDLE_" + bundle.getId());

        Document document = new Document(
            documentId,
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
                toUUID("PRODUCT_" + product.getId()),
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
                toUUID("BUNDLE_" + bundle.getId()),
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
     * Remove embedding for a product from vector store
     */
    public void removeProductEmbedding(Long productId) {
        try {
            String documentId = toUUID("PRODUCT_" + productId);
            vectorStore.delete(List.of(documentId));
            log.info("Removed embedding for product ID: {}", productId);
        } catch (Exception e) {
            log.warn("Failed to remove embedding for product ID {}: {}", productId, e.getMessage());
        }
    }

    /**
     * Remove embedding for a bundle from vector store
     */
    public void removeBundleEmbedding(Long bundleId) {
        try {
            String documentId = toUUID("BUNDLE_" + bundleId);
            vectorStore.delete(List.of(documentId));
            log.info("Removed embedding for bundle ID: {}", bundleId);
        } catch (Exception e) {
            log.warn("Failed to remove embedding for bundle ID {}: {}", bundleId, e.getMessage());
        }
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


