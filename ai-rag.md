<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.5.10</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <groupId>com.ragvector</groupId>
    <artifactId>rag-pgvector-ai</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>rag-pgvector-ai</name>
    <description>rag-pgvector-ai</description>
    <url/>
    <licenses>
        <license/>
    </licenses>
    <developers>
        <developer/>
    </developers>
    <scm>
        <connection/>
        <developerConnection/>
        <tag/>
        <url/>
    </scm>
    <properties>
        <java.version>21</java.version>
        <spring-ai.version>1.1.2</spring-ai.version>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-starter-model-google-genai</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-starter-model-transformers</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-starter-vector-store-pgvector</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-rag</artifactId>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>


        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

    </dependencies>
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.ai</groupId>
                <artifactId>spring-ai-bom</artifactId>
                <version>${spring-ai.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <annotationProcessorPaths>
                        <path>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </path>
                    </annotationProcessorPaths>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>

</project>


spring.application.name=rag-pgvector-ai
server.port=8084

# PostgreSQL Database (Docker with pgvector on port 5433)
spring.datasource.url=jdbc:postgresql://localhost:5433/ragvector
spring.datasource.username=postgres
spring.datasource.password=12345
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# pgvector
spring.ai.vectorstore.pgvector.initialize-schema=true
spring.ai.vectorstore.pgvector.dimensions=384
spring.ai.vectorstore.pgvector.index-type=HNSW
spring.ai.vectorstore.pgvector.distance-type=COSINE_DISTANCE

# Google AI Gemini (API Key) - Chat only
spring.ai.google.genai.api-key=${GEMINI_API_KEY:AIzaSyBm4Yg4LW_1hfupCQoJ9EcB16J5_4aNeK4}
spring.ai.google.genai.transport=rest
spring.ai.google.genai.chat.options.model=gemma-3-4b-it
spring.ai.google.genai.chat.options.temperature=0.7

# Transformers Embedding Model (Local ONNX - all-MiniLM-L6-v2)
spring.ai.transformers.embedding.onnx.modelUri=https://huggingface.co/sentence-transformers/all-MiniLM-L6-v2/resolve/main/onnx/model.onnx
spring.ai.transformers.embedding.tokenizer.uri=https://huggingface.co/sentence-transformers/all-MiniLM-L6-v2/resolve/main/tokenizer.json

# Logging
logging.level.org.springframework.ai=DEBUG


package com.ragvector.service;

import com.ragvector.dto.ChatRequest;
import com.ragvector.dto.ChatResponse;
import com.ragvector.dto.IntentResult;
import com.ragvector.dto.IntentResult.IntentType;
import com.ragvector.entity.ChatMessage;
import com.ragvector.entity.ChatMessage.MessageRole;
import com.ragvector.entity.ChatSession;
import com.ragvector.entity.Product;
import com.ragvector.repository.ChatMessageRepository;
import com.ragvector.repository.ChatSessionRepository;
import com.ragvector.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Core Chat Service - RAG Pipeline Orchestrator
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ChatClient.Builder chatClientBuilder;
    private final IntentClassifierService intentClassifierService;
    private final EmbeddingService embeddingService;
    private final ProductRepository productRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;

    private static final String SYSTEM_PROMPT = """
        Bạn là nhân viên tư vấn bán hàng của Shop Quà Tết. Nhiệm vụ của bạn:
        
        1. Tư vấn và gợi ý các sản phẩm quà Tết phù hợp với nhu cầu khách hàng
        2. Trả lời câu hỏi về sản phẩm, giá cả, tồn kho
        3. Hỗ trợ thông tin về chính sách giảm giá, đặt hàng số lượng lớn
        
        QUY TẮC QUAN TRỌNG:
        - KHÔNG được bịa thông tin về sản phẩm, giá, tồn kho
        - CHỈ sử dụng thông tin từ context được cung cấp
        - Nếu không có thông tin, hãy nói rõ và đề xuất liên hệ hotline
        - Trả lời lịch sự, thân thiện, bằng tiếng Việt
        
        Thông tin cửa hàng:
        - Địa chỉ: 123 Đường Nguyễn Huệ, Quận 1, TP.HCM
        - Hotline: 1900-1234
        - Giờ mở cửa: 8:00 - 21:00 hàng ngày
        
        Chính sách giảm giá:
        - Mua từ 10 sản phẩm: Giảm 5%
        - Mua từ 50 sản phẩm: Giảm 10%
        - Mua từ 100 sản phẩm: Giảm 15% + Free ship
        """;

    @Transactional
    public ChatResponse chat(ChatRequest request) {
        log.info("Processing chat request: {}", request.getMessage());

        try {
            // 1. Get or create session
            ChatSession session = getOrCreateSession(request);

            // 2. Save user message
            saveMessage(session, MessageRole.USER, request.getMessage());

            // 3. Classify intent
            IntentResult intent = intentClassifierService.classifyIntent(request.getMessage());
            log.info("Detected intent: {}", intent.getIntent());

            // 4. Retrieve relevant data based on intent
            String context = retrieveContext(intent);

            // 5. Build RAG prompt and call LLM
            String response = generateResponse(request.getMessage(), context, intent);

            // 6. Save assistant message
            saveMessage(session, MessageRole.ASSISTANT, response);

            return ChatResponse.success(session.getId(), response, intent.getIntent());

        } catch (Exception e) {
            log.error("Error processing chat request", e);
            return ChatResponse.error(e.getMessage());
        }
    }

    private ChatSession getOrCreateSession(ChatRequest request) {
        if (request.getSessionId() != null) {
            return chatSessionRepository.findById(request.getSessionId())
                .orElseGet(() -> createNewSession(request.getUserId()));
        }
        return createNewSession(request.getUserId());
    }

    private ChatSession createNewSession(String userId) {
        ChatSession session = ChatSession.builder()
            .userId(userId)
            .build();
        return chatSessionRepository.save(session);
    }

    private void saveMessage(ChatSession session, MessageRole role, String content) {
        ChatMessage message = ChatMessage.builder()
            .session(session)
            .role(role)
            .content(content)
            .build();
        chatMessageRepository.save(message);
    }

    private String retrieveContext(IntentResult intent) {
        StringBuilder context = new StringBuilder();

        switch (intent.getIntent()) {
            case PRODUCT_SEARCH -> {
                // Combine semantic search with structured query
                List<Product> products = retrieveProducts(intent);
                List<Document> semanticResults = embeddingService.searchSimilar(intent.getRawQuery(), 5);

                context.append("=== SẢN PHẨM TÌM THẤY ===\n");

                // Add products from structured query
                if (!products.isEmpty()) {
                    context.append("\nKết quả từ database:\n");
                    for (Product p : products) {
                        context.append(formatProduct(p)).append("\n");
                    }
                }

                // Add semantic search results
                if (!semanticResults.isEmpty()) {
                    context.append("\nKết quả tìm kiếm liên quan:\n");
                    for (Document doc : semanticResults) {
                        context.append(doc.getText()).append("\n---\n");
                    }
                }

                if (products.isEmpty() && semanticResults.isEmpty()) {
                    context.append("Không tìm thấy sản phẩm phù hợp.\n");
                }
            }

            case STOCK_CHECK -> {
                List<Product> products = retrieveProducts(intent);
                context.append("=== THÔNG TIN TỒN KHO ===\n");
                for (Product p : products) {
                    context.append(String.format("- %s: Còn %d sản phẩm (Giá: %s VNĐ)\n",
                        p.getName(), p.getStockQuantity(), p.getPrice()));
                }
            }

            case BULK_ORDER -> {
                context.append("=== CHÍNH SÁCH ĐẶT HÀNG SỐ LƯỢNG LỚN ===\n");
                context.append("- Mua từ 10 sản phẩm: Giảm 5%\n");
                context.append("- Mua từ 50 sản phẩm: Giảm 10%\n");
                context.append("- Mua từ 100 sản phẩm: Giảm 15% + Free ship\n");
                context.append("- Đơn hàng doanh nghiệp: Liên hệ hotline để được báo giá riêng\n");

                // Include relevant products if quantity specified
                if (intent.getQuantity() != null) {
                    List<Product> availableProducts = productRepository
                        .findByStockQuantityGreaterThanEqual(intent.getQuantity());
                    context.append("\nSản phẩm có đủ số lượng:\n");
                    for (Product p : availableProducts) {
                        context.append(formatProduct(p)).append("\n");
                    }
                }
            }

            case DISCOUNT_POLICY -> {
                context.append("=== CHÍNH SÁCH GIẢM GIÁ ===\n");
                context.append("1. Giảm giá theo số lượng:\n");
                context.append("   - 10-49 sản phẩm: Giảm 5%\n");
                context.append("   - 50-99 sản phẩm: Giảm 10%\n");
                context.append("   - 100+ sản phẩm: Giảm 15% + Free ship\n\n");
                context.append("2. Khuyến mãi Tết 2026:\n");
                context.append("   - Giảm thêm 5% cho đơn hàng thanh toán trước\n");
                context.append("   - Tặng thiệp chúc Tết cao cấp cho mỗi đơn hàng\n");
            }

            case SHOP_INFO -> {
                context.append("=== THÔNG TIN CỬA HÀNG ===\n");
                context.append("Tên: Shop Quà Tết\n");
                context.append("Địa chỉ: 123 Đường Nguyễn Huệ, Quận 1, TP.HCM\n");
                context.append("Hotline: 1900-1234\n");
                context.append("Email: contact@quatet.vn\n");
                context.append("Giờ mở cửa: 8:00 - 21:00 hàng ngày\n");
                context.append("Giao hàng: Toàn quốc\n");
            }

            case GENERAL_CHAT -> {
                // For general chat, use semantic search to find relevant context
                List<Document> results = embeddingService.searchSimilar(intent.getRawQuery(), 3);
                if (!results.isEmpty()) {
                    context.append("=== THÔNG TIN LIÊN QUAN ===\n");
                    for (Document doc : results) {
                        context.append(doc.getText()).append("\n---\n");
                    }
                }
            }
        }

        return context.toString();
    }

    private List<Product> retrieveProducts(IntentResult intent) {
        // Apply filters based on intent constraints
        if (intent.getMaxPrice() != null && intent.getTargetAudience() != null) {
            return productRepository.findByAudienceAndMaxPrice(
                intent.getTargetAudience(), intent.getMaxPrice());
        }

        if (intent.getMaxPrice() != null && intent.getQuantity() != null) {
            return productRepository.findByPriceAndAvailability(
                intent.getMaxPrice(), intent.getQuantity());
        }

        if (intent.getMaxPrice() != null) {
            return productRepository.findByPriceLessThanEqual(intent.getMaxPrice());
        }

        if (intent.getTargetAudience() != null) {
            return productRepository.findByTargetAudience(intent.getTargetAudience());
        }

        if (intent.getKeyword() != null) {
            return productRepository.searchByKeyword(intent.getKeyword());
        }

        // Default: return top products
        return productRepository.findAll().stream().limit(10).collect(Collectors.toList());
    }

    private String formatProduct(Product p) {
        return String.format("• %s - Giá: %s VNĐ - Phù hợp: %s - Còn: %d",
            p.getName(),
            p.getPrice(),
            p.getTargetAudience() != null ? p.getTargetAudience() : "Mọi đối tượng",
            p.getStockQuantity());
    }

    private String generateResponse(String userMessage, String context, IntentResult intent) {
        ChatClient chatClient = chatClientBuilder.build();

        String augmentedPrompt = String.format("""
            %s
            
            === CONTEXT ===
            %s
            
            === CÂU HỎI KHÁCH HÀNG ===
            %s
            
            Hãy trả lời câu hỏi dựa trên context ở trên. Nếu không có thông tin trong context, hãy nói rõ và gợi ý liên hệ hotline.
            """, SYSTEM_PROMPT, context, userMessage);

        return chatClient.prompt()
            .user(augmentedPrompt)
            .call()
            .content();
    }

    /**
     * Get conversation history for a session
     */
    public List<ChatMessage> getHistory(Long sessionId) {
        return chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }
}


package com.ragvector.service;

import com.ragvector.entity.Product;
import com.ragvector.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Service for managing product embeddings in pgvector
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmbeddingService {

    private final VectorStore vectorStore;
    private final ProductRepository productRepository;

    /**
     * Generate and store embedding for a product
     */
    @Transactional
    public void embedProduct(Product product) {
        log.info("Generating embedding for product: {}", product.getName());

        Document document = new Document(
            product.toEmbeddingText(),
            Map.of(
                "productId", product.getId().toString(),
                "name", product.getName(),
                "price", product.getPrice().toString(),
                "category", product.getCategory() != null ? product.getCategory() : "",
                "targetAudience", product.getTargetAudience() != null ? product.getTargetAudience() : "",
                "stockQuantity", product.getStockQuantity().toString()
            )
        );

        vectorStore.add(List.of(document));
        log.info("Embedding stored for product ID: {}", product.getId());
    }

    /**
     * Generate embeddings for all products
     */
    @Transactional
    public void embedAllProducts() {
        log.info("Generating embeddings for all products...");
        List<Product> products = productRepository.findAll();

        List<Document> documents = products.stream()
            .map(product -> new Document(
                product.toEmbeddingText(),
                Map.of(
                    "productId", product.getId().toString(),
                    "name", product.getName(),
                    "price", product.getPrice().toString(),
                    "category", product.getCategory() != null ? product.getCategory() : "",
                    "targetAudience", product.getTargetAudience() != null ? product.getTargetAudience() : "",
                    "stockQuantity", product.getStockQuantity().toString()
                )
            ))
            .toList();

        if (!documents.isEmpty()) {
            vectorStore.add(documents);
            log.info("Generated embeddings for {} products", products.size());
        }
    }

    /**
     * Search similar products using semantic search
     */
    public List<Document> searchSimilar(String query, int topK) {
        log.info("Searching for similar products: {}", query);
        return vectorStore.similaritySearch(query);
    }
}


package com.ragvector.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ragvector.dto.IntentResult;
import com.ragvector.dto.IntentResult.IntentType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Service for classifying user intent using LLM
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IntentClassifierService {

    private final ChatClient.Builder chatClientBuilder;
    private final ObjectMapper objectMapper;

    private static final String INTENT_CLASSIFICATION_PROMPT = """
        Bạn là một AI phân loại intent cho shop quà Tết. Hãy phân tích câu hỏi của khách hàng và trả về JSON với format sau:
        
        {
            "intent": "PRODUCT_SEARCH | STOCK_CHECK | BULK_ORDER | DISCOUNT_POLICY | SHOP_INFO | GENERAL_CHAT",
            "maxPrice": số tiền tối đa (nếu có, đơn vị VNĐ),
            "minPrice": số tiền tối thiểu (nếu có, đơn vị VNĐ),
            "targetAudience": "elderly | boss | family | corporate | null" (đối tượng được nhắc đến),
            "quantity": số lượng cần mua (nếu có),
            "keyword": từ khóa chính (nếu có)
        }
        
        Quy tắc phân loại:
        - PRODUCT_SEARCH: Tìm kiếm sản phẩm, gợi ý quà, hỏi về sản phẩm
        - STOCK_CHECK: Hỏi về số lượng tồn kho, còn hàng không
        - BULK_ORDER: Đặt hàng số lượng lớn (>10), mua sỉ
        - DISCOUNT_POLICY: Hỏi về giảm giá, khuyến mãi, chính sách
        - SHOP_INFO: Hỏi địa chỉ, giờ mở cửa, thông tin shop
        - GENERAL_CHAT: Các câu hỏi chung khác
        
        Chỉ trả về JSON, không có text khác.
        
        Câu hỏi: %s
        """;

    public IntentResult classifyIntent(String userMessage) {
        log.info("Classifying intent for: {}", userMessage);

        try {
            ChatClient chatClient = chatClientBuilder.build();

            String response = chatClient.prompt()
                .user(String.format(INTENT_CLASSIFICATION_PROMPT, userMessage))
                .call()
                .content();

            log.debug("Intent classification response: {}", response);

            return parseIntentResponse(response, userMessage);

        } catch (Exception e) {
            log.error("Error classifying intent", e);
            return IntentResult.builder()
                .intent(IntentType.GENERAL_CHAT)
                .rawQuery(userMessage)
                .build();
        }
    }

    private IntentResult parseIntentResponse(String response, String originalQuery) {
        try {
            // Clean response - remove markdown code blocks if present
            String cleanResponse = response.trim();
            if (cleanResponse.startsWith("```json")) {
                cleanResponse = cleanResponse.substring(7);
            }
            if (cleanResponse.startsWith("```")) {
                cleanResponse = cleanResponse.substring(3);
            }
            if (cleanResponse.endsWith("```")) {
                cleanResponse = cleanResponse.substring(0, cleanResponse.length() - 3);
            }
            cleanResponse = cleanResponse.trim();

            JsonNode jsonNode = objectMapper.readTree(cleanResponse);

            IntentResult.IntentResultBuilder builder = IntentResult.builder()
                .rawQuery(originalQuery);

            // Parse intent
            String intentStr = jsonNode.has("intent") ? jsonNode.get("intent").asText() : "GENERAL_CHAT";
            builder.intent(IntentType.valueOf(intentStr));

            // Parse optional fields
            if (jsonNode.has("maxPrice") && !jsonNode.get("maxPrice").isNull()) {
                builder.maxPrice(new BigDecimal(jsonNode.get("maxPrice").asText()));
            }

            if (jsonNode.has("minPrice") && !jsonNode.get("minPrice").isNull()) {
                builder.minPrice(new BigDecimal(jsonNode.get("minPrice").asText()));
            }

            if (jsonNode.has("targetAudience") && !jsonNode.get("targetAudience").isNull()
                && !"null".equals(jsonNode.get("targetAudience").asText())) {
                builder.targetAudience(jsonNode.get("targetAudience").asText());
            }

            if (jsonNode.has("quantity") && !jsonNode.get("quantity").isNull()) {
                builder.quantity(jsonNode.get("quantity").asInt());
            }

            if (jsonNode.has("keyword") && !jsonNode.get("keyword").isNull()) {
                builder.keyword(jsonNode.get("keyword").asText());
            }

            return builder.build();

        } catch (Exception e) {
            log.error("Error parsing intent response: {}", response, e);
            return IntentResult.builder()
                .intent(IntentType.GENERAL_CHAT)
                .rawQuery(originalQuery)
                .build();
        }
    }
}


package com.ragvector.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "product")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    private String category;

    @Column(name = "target_audience")
    private String targetAudience; // elderly, boss, family, corporate

    @Column(name = "stock_quantity")
    private Integer stockQuantity;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Generate text for embedding
     */
    public String toEmbeddingText() {
        return String.format("""
            Tên sản phẩm: %s
            Mô tả: %s
            Giá: %s VNĐ
            Danh mục: %s
            Đối tượng phù hợp: %s
            """,
            name,
            description,
            price,
            category != null ? category : "Không xác định",
            targetAudience != null ? targetAudience : "Mọi đối tượng"
        );
    }
}


package com.ragvector.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chat_session")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ChatMessage> messages = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}


package com.ragvector.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_message")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ChatSession session;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageRole role;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum MessageRole {
        USER, ASSISTANT, SYSTEM
    }
}


package com.ragvector.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Intent classification result from LLM
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntentResult {

    private IntentType intent;
    private BigDecimal maxPrice;
    private BigDecimal minPrice;
    private String targetAudience;
    private Integer quantity;
    private String keyword;
    private String rawQuery;

    public enum IntentType {
        PRODUCT_SEARCH,      // Tìm sản phẩm
        STOCK_CHECK,         // Kiểm tra tồn kho
        BULK_ORDER,          // Đơn hàng số lượng lớn
        DISCOUNT_POLICY,     // Chính sách giảm giá
        SHOP_INFO,           // Thông tin cửa hàng
        GENERAL_CHAT         // Trò chuyện chung
    }
}



package com.ragvector.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    private Long sessionId;
    private String message;
    private LocalDateTime timestamp;
    private IntentResult.IntentType detectedIntent;
    private boolean success;
    private String errorMessage;

    public static ChatResponse success(Long sessionId, String message, IntentResult.IntentType intent) {
        return ChatResponse.builder()
                .sessionId(sessionId)
                .message(message)
                .timestamp(LocalDateTime.now())
                .detectedIntent(intent)
                .success(true)
                .build();
    }

    public static ChatResponse error(String errorMessage) {
        return ChatResponse.builder()
                .message("Xin lỗi, đã có lỗi xảy ra. Vui lòng thử lại sau.")
                .timestamp(LocalDateTime.now())
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }
}



package com.ragvector.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

    @NotBlank(message = "Message cannot be blank")
    private String message;

    private Long sessionId;

    private String userId;
}



-- Script để cài đặt pgvector vào PostgreSQL local
-- Chạy script này trong pgAdmin hoặc psql

-- 1. Tạo database nếu chưa có
CREATE DATABASE ragvector;

-- 2. Kết nối vào database ragvector rồi chạy lệnh sau:
-- CREATE EXTENSION IF NOT EXISTS vector;

-- LƯU Ý: Nếu bạn thấy lỗi "extension vector is not available",
-- bạn cần cài đặt pgvector extension vào PostgreSQL:

-- Cách 1: Dùng Docker (khuyến nghị)
-- docker-compose up -d

-- Cách 2: Cài pgvector vào PostgreSQL Windows
-- 1. Tải pgvector từ: https://github.com/pgvector/pgvector/releases
-- 2. Copy các file vào thư mục PostgreSQL:
--    - vector.dll -> C:\Program Files\PostgreSQL\17\lib
--    - vector.control -> C:\Program Files\PostgreSQL\17\share\extension
--    - vector--*.sql -> C:\Program Files\PostgreSQL\17\share\extension
-- 3. Restart PostgreSQL service
-- 4. Chạy: CREATE EXTENSION vector;




package com.ragvector.controller;

import com.ragvector.entity.Product;
import com.ragvector.repository.ProductRepository;
import com.ragvector.service.EmbeddingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Product Management
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductRepository productRepository;
    private final EmbeddingService embeddingService;

    /**
     * Get all products
     */
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productRepository.findAll());
    }

    /**
     * Get product by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        return productRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create a new product
     */
    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        Product saved = productRepository.save(product);
        // Generate embedding for the new product
        embeddingService.embedProduct(saved);
        return ResponseEntity.ok(saved);
    }

    /**
     * Update a product
     */
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        return productRepository.findById(id)
            .map(existing -> {
                existing.setName(product.getName());
                existing.setDescription(product.getDescription());
                existing.setPrice(product.getPrice());
                existing.setCategory(product.getCategory());
                existing.setTargetAudience(product.getTargetAudience());
                existing.setStockQuantity(product.getStockQuantity());
                Product saved = productRepository.save(existing);
                // Re-generate embedding
                embeddingService.embedProduct(saved);
                return ResponseEntity.ok(saved);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Delete a product
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Re-generate embeddings for all products
     */
    @PostMapping("/embeddings/regenerate")
    public ResponseEntity<Map<String, String>> regenerateEmbeddings() {
        log.info("Regenerating all product embeddings...");
        embeddingService.embedAllProducts();
        return ResponseEntity.ok(Map.of("status", "Embeddings regenerated successfully"));
    }
}


package com.ragvector.controller;

import com.ragvector.dto.ChatRequest;
import com.ragvector.dto.ChatResponse;
import com.ragvector.entity.ChatMessage;
import com.ragvector.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Chat API
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatService chatService;

    /**
     * Send a message and get AI response
     */
    @PostMapping
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        log.info("Received chat request: {}", request.getMessage());
        ChatResponse response = chatService.chat(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get conversation history
     */
    @GetMapping("/history/{sessionId}")
    public ResponseEntity<List<ChatMessage>> getHistory(@PathVariable Long sessionId) {
        List<ChatMessage> history = chatService.getHistory(sessionId);
        return ResponseEntity.ok(history);
    }
}



package com.ragvector.config;

import com.ragvector.entity.Product;
import com.ragvector.repository.ProductRepository;
import com.ragvector.service.EmbeddingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.util.List;

/**
 * Data Initializer - Seeds sample products
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final ProductRepository productRepository;
    private final EmbeddingService embeddingService;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            if (productRepository.count() == 0) {
                log.info("Initializing sample product data...");

                List<Product> products = List.of(
                    Product.builder()
                        .name("Giỏ quà Tết An Khang")
                        .description("Giỏ quà Tết cao cấp gồm bánh kẹo, mứt Tết, trà, rượu vang. Phù hợp biếu tặng người lớn tuổi, bố mẹ.")
                        .price(new BigDecimal("450000"))
                        .category("Giỏ quà Tết")
                        .targetAudience("elderly")
                        .stockQuantity(100)
                        .build(),

                    Product.builder()
                        .name("Giỏ quà Tết Phú Quý")
                        .description("Giỏ quà sang trọng với rượu vang Pháp, hạt điều, chocola Bỉ. Thích hợp tặng sếp, đối tác kinh doanh.")
                        .price(new BigDecimal("1200000"))
                        .category("Giỏ quà cao cấp")
                        .targetAudience("boss")
                        .stockQuantity(50)
                        .build(),

                    Product.builder()
                        .name("Hộp bánh Tết Truyền Thống")
                        .description("Hộp bánh Tết các loại: bánh chưng, bánh tét, bánh in. Hương vị truyền thống Việt Nam.")
                        .price(new BigDecimal("280000"))
                        .category("Bánh Tết")
                        .targetAudience("family")
                        .stockQuantity(200)
                        .build(),

                    Product.builder()
                        .name("Set quà Tết Doanh Nghiệp")
                        .description("Bộ quà tặng doanh nghiệp gồm hộp quà cao cấp, có thể in logo công ty. Phù hợp tặng khách hàng, nhân viên.")
                        .price(new BigDecimal("800000"))
                        .category("Quà doanh nghiệp")
                        .targetAudience("corporate")
                        .stockQuantity(300)
                        .build(),

                    Product.builder()
                        .name("Hộp mứt Tết Đặc Sản")
                        .description("Mứt dừa, mứt gừng, mứt bí các loại. Làm từ nguyên liệu tự nhiên, không chất bảo quản.")
                        .price(new BigDecimal("150000"))
                        .category("Mứt Tết")
                        .targetAudience("family")
                        .stockQuantity(500)
                        .build(),

                    Product.builder()
                        .name("Rượu vang Chile Reserva")
                        .description("Rượu vang đỏ Chile nhập khẩu, hương vị đậm đà. Thích hợp làm quà biếu sang trọng.")
                        .price(new BigDecimal("650000"))
                        .category("Rượu vang")
                        .targetAudience("boss")
                        .stockQuantity(80)
                        .build(),

                    Product.builder()
                        .name("Trà Ô Long Đài Loan")
                        .description("Trà Ô Long cao cấp từ Đài Loan, hộp quà tặng sang trọng. Phù hợp người lớn tuổi yêu thích trà.")
                        .price(new BigDecimal("350000"))
                        .category("Trà cao cấp")
                        .targetAudience("elderly")
                        .stockQuantity(120)
                        .build(),

                    Product.builder()
                        .name("Hạt điều rang muối Bình Phước")
                        .description("Hạt điều loại 1 Bình Phước, rang muối vừa ăn. Đóng hộp quà tặng đẹp mắt.")
                        .price(new BigDecimal("200000"))
                        .category("Hạt khô")
                        .targetAudience("family")
                        .stockQuantity(400)
                        .build(),

                    Product.builder()
                        .name("Giỏ trái cây nhập khẩu")
                        .description("Giỏ trái cây nhập khẩu cao cấp gồm táo Mỹ, nho Úc, lê Hàn Quốc. Tươi ngon, bổ dưỡng.")
                        .price(new BigDecimal("550000"))
                        .category("Trái cây")
                        .targetAudience("elderly")
                        .stockQuantity(60)
                        .build(),

                    Product.builder()
                        .name("Yến sào cao cấp")
                        .description("Yến sào Khánh Hòa chính gốc, hộp quà tặng sang trọng. Bổ dưỡng cho sức khỏe.")
                        .price(new BigDecimal("2500000"))
                        .category("Yến sào")
                        .targetAudience("elderly")
                        .stockQuantity(30)
                        .build()
                );

                productRepository.saveAll(products);
                log.info("Saved {} sample products", products.size());

                // Generate embeddings for all products
                log.info("Generating embeddings for products...");
                embeddingService.embedAllProducts();
                log.info("Data initialization complete!");
            }
        };
    }
}



package com.ragvector.config;

import org.springframework.context.annotation.Configuration;

/**
 * Spring AI Configuration
 * ChatClient.Builder is auto-configured by Spring AI
 */
@Configuration
public class AiConfig {
    // ChatClient.Builder is automatically provided by Spring AI autoconfiguration
}
