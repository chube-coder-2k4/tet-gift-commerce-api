# 🎯 ĐẶC TẢ CHUẨN VIẾT CODE - SPRING BOOT BACKEND

**Phiên bản:** 1.0  
**Áp dụng cho:** Tất cả dự án Spring Boot trong team  
**Mục đích:** Đảm bảo code nhất quán, dễ bảo trì, có khả năng mở rộng

---

## 📋 MỤC LỤC

1. [Quy tắc đặt tên](#1-quy-tắc-đặt-tên)
2. [Kiến trúc & Thiết kế](#2-kiến-trúc--thiết-kế)
3. [Cấu trúc dự án](#3-cấu-trúc-dự-án)
4. [API Design](#4-api-design)
5. [Response Chuẩn](#5-response-chuẩn)
6. [Exception Handling](#6-exception-handling)
7. [Validation](#7-validation)
8. [Database & Entity](#8-database--entity)
9. [Mapping & DTO](#9-mapping--dto)
10. [Configuration](#10-configuration)
11. [Documentation](#11-documentation)
12. [Best Practices](#12-best-practices)

---

## 1️⃣ QUY TẮC ĐẶT TÊN

### 1.1 Biến (Variables)

**Quy tắc:**
- Sử dụng `camelCase`
- Tên phải thể hiện rõ ý nghĩa nghiệp vụ
- Boolean: bắt đầu bằng `is`, `has`, `can`, `should`

```java
// ✅ GOOD
private String userName;
private Long userId;
private boolean isActive;
private boolean hasPermission;
private boolean canEdit;

// ❌ BAD
private String un;
private Long uid;
private boolean flag;
private boolean temp;
private String data;
```

### 1.2 Method / Function

**Quy tắc:**
- Sử dụng `camelCase`
- Cấu trúc: **Verb + Noun**
- Thể hiện rõ hành động

```java
// ✅ GOOD
public User createUser(CreateUserRequest request)
public void updateUserProfile(Long userId, UpdateProfileRequest request)
public User findUserByEmail(String email)
public List<User> getUsersByStatus(UserStatus status)
public boolean deleteUser(Long userId)

// ❌ BAD
public User user()
public void update()
public User get(String s)
public void process()
```

### 1.3 Class & Interface

**Quy tắc:**
- Sử dụng `PascalCase`
- Tên rõ ràng, thể hiện mục đích

```java
// Interface
public interface UserService { }
public interface ProductRepository { }

// Implementation
public class UserServiceImpl implements UserService { }

// Controller
public class UserController { }

// DTO
public class CreateUserRequest { }
public class UserResponse { }

// Entity
public class User extends BaseEntity<Long> { }
```

### 1.4 Constants

**Quy tắc:**
- Sử dụng `UPPER_SNAKE_CASE`
- Tập trung trong class riêng

```java
public class AppConstants {
    public static final int MAX_PAGE_SIZE = 100;
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
}

public class ValidationMessages {
    public static final String EMAIL_INVALID = "Email không hợp lệ";
    public static final String REQUIRED_FIELD = "Trường này bắt buộc";
}
```

### 1.5 Package

**Quy tắc:**
- Tất cả chữ thường
- Phân cách bằng dấu chấm

```
com.example.project.controller
com.example.project.service.impl
com.example.project.dto.request
```

### 1.6 Enum

```java
// ✅ GOOD
public enum UserStatus {
    ACTIVE,
    INACTIVE,
    BLOCKED,
    PENDING
}

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    SHIPPING,
    DELIVERED,
    CANCELLED
}
```

---

## 2️⃣ KIẾN TRÚC & THIẾT KẾ

### 2.1 Layered Architecture (BẮT BUỘC)

```
Controller → Service (Interface) → ServiceImpl → Repository → Database
```

**Nguyên tắc:**
- **Controller**: Xử lý HTTP request/response, validation input
- **Service**: Chứa business logic
- **Repository**: Truy vấn database
- **Entity**: Mapping với bảng database
- **DTO**: Transfer data giữa các layer

### 2.2 Interface & Implementation

**BẮT BUỘC: Tất cả Service phải có Interface**

```java
// Interface - Định nghĩa contract
public interface UserService {
    User createUser(CreateUserRequest request);
    User updateUser(Long id, UpdateUserRequest request);
    User getUserById(Long id);
    void deleteUser(Long id);
}

// Implementation - Chứa business logic
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    
    @Override
    @Transactional
    public User createUser(CreateUserRequest request) {
        // Business logic here
    }
}
```

### 2.3 Single Responsibility Principle

**Mỗi class chỉ làm MỘT việc**

```java
// ✅ GOOD - Mỗi class có trách nhiệm rõ ràng
public class UserService { }           // Quản lý user
public class EmailService { }          // Gửi email
public class NotificationService { }   // Gửi thông báo

// ❌ BAD - Class làm quá nhiều việc
public class UserService {
    public void createUser() { }
    public void sendEmail() { }
    public void generateReport() { }
    public void processPayment() { }
}
```

### 2.4 Dependency Injection

**Sử dụng Constructor Injection với Lombok**

```java
@Service
@RequiredArgsConstructor  // Lombok tự tạo constructor
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final UserMapper userMapper;
    
    // Không cần viết constructor
}
```

---

## 3️⃣ CẤU TRÚC DỰ ÁN

### 3.1 Package Structure (BẮT BUỘC)

```
src/main/java/com/example/project/
│
├── 📁 config/                    # Configuration classes
│   ├── SecurityConfig.java
│   ├── SwaggerConfig.java
│   └── WebConfig.java
│
├── 📁 controller/                # REST Controllers
│   ├── UserController.java
│   └── ProductController.java
│
├── 📁 service/                   # Service interfaces
│   ├── UserService.java
│   └── impl/                     # Service implementations
│       └── UserServiceImpl.java
│
├── 📁 repository/                # JPA Repositories
│   └── UserRepository.java
│
├── 📁 entity/                    # Database entities
│   └── User.java
│
├── 📁 dto/                       # Data Transfer Objects
│   ├── request/
│   │   ├── CreateUserRequest.java
│   │   └── UpdateUserRequest.java
│   └── response/
│       └── UserResponse.java
│
├── 📁 mapper/                    # MapStruct mappers
│   └── UserMapper.java
│
├── 📁 exception/                 # Custom exceptions
│   ├── ResourceNotFoundException.java
│   ├── BusinessException.java
│   └── GlobalExceptionHandler.java
│
├── 📁 common/                    # Common utilities
│   ├── constants/
│   │   ├── AppConstants.java
│   │   └── ValidationMessages.java
│   └── response/
│       ├── ResponseData.java
│       ├── ResponseError.java
│       └── PageResponse.java
│
├── 📁 security/                  # Security components
│   ├── JwtTokenProvider.java
│   └── UserDetailsServiceImpl.java
│
└── 📁 util/                      # Utility classes
    ├── DateUtils.java
    └── StringUtils.java
```

---

## 4️⃣ API DESIGN

### 4.1 Versioning (BẮT BUỘC)

```java
@RestController
@RequestMapping("/api/v1/users")
public class UserController { }
```

### 4.2 RESTful Convention

| HTTP Method | Endpoint | Mục đích |
|------------|----------|----------|
| **GET** | `/api/v1/users` | Lấy danh sách users |
| **GET** | `/api/v1/users/{id}` | Lấy user theo ID |
| **POST** | `/api/v1/users` | Tạo user mới |
| **PUT** | `/api/v1/users/{id}` | Cập nhật toàn bộ user |
| **PATCH** | `/api/v1/users/{id}` | Cập nhật một phần user |
| **DELETE** | `/api/v1/users/{id}` | Xóa user |

### 4.3 Controller Template

```java
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "APIs for user operations")
public class UserController {
    
    private final UserService userService;
    
    @PostMapping
    @Operation(summary = "Create new user")
    public ResponseEntity<ResponseData<UserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        UserResponse user = userService.createUser(request);
        return ResponseEntity.ok(
            new ResponseData<>(HttpStatus.OK.value(), "User created successfully", user)
        );
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<ResponseData<UserResponse>> getUserById(
            @PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(
            new ResponseData<>(HttpStatus.OK.value(), "Success", user)
        );
    }
    
    @GetMapping
    @Operation(summary = "Get all users with pagination")
    public ResponseEntity<PageResponse<List<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "20") int pageSize) {
        PageResponse<List<UserResponse>> response = userService.getAllUsers(pageNo, pageSize);
        return ResponseEntity.ok(response);
    }
}
```

**QUY TẮC:**
- ❌ KHÔNG viết business logic trong Controller
- ✅ Controller chỉ làm: nhận request → gọi service → trả response
- ✅ Validation ở Controller level
- ✅ Exception handling qua `@RestControllerAdvice`

---

## 5️⃣ RESPONSE CHUẨN

### 5.1 Success Response

```java
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResponseData<T> implements Serializable {
    
    private int status;
    private String message;
    private T data;
    
    public ResponseData(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
```

**Ví dụ sử dụng:**
```java
// Single object
return ResponseEntity.ok(
    new ResponseData<>(200, "Success", userResponse)
);

// List
return ResponseEntity.ok(
    new ResponseData<>(200, "Success", userList)
);
```

### 5.2 Pagination Response

```java
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PageResponse<T> implements Serializable {
    
    private int pageNo;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean last;
    private T items;
    
    public static <T> PageResponse<T> of(Page<?> page, T items) {
        return new PageResponse<>(
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isLast(),
            items
        );
    }
}
```

### 5.3 Error Response

```java
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse implements Serializable {
    
    private LocalDateTime timestamp;
    private int status;
    private String path;
    private String error;
    private String message;
    private List<String> details;
}
```

---

## 6️⃣ EXCEPTION HANDLING

### 6.1 Custom Exceptions

```java
// Base exception
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}

// Specific exceptions
public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String resource, String field, Object value) {
        super(String.format("%s not found with %s: '%s'", resource, field, value));
    }
}

public class DuplicateResourceException extends BusinessException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
```

### 6.2 Global Exception Handler (BẮT BUỘC)

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex,
            WebRequest request) {
        
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.NOT_FOUND.value(),
            request.getDescription(false).replace("uri=", ""),
            "Not Found",
            ex.getMessage(),
            null
        );
        
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            WebRequest request) {
        
        List<String> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.toList());
        
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            request.getDescription(false).replace("uri=", ""),
            "Validation Failed",
            "Invalid input data",
            errors
        );
        
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex,
            WebRequest request) {
        
        log.error("Unhandled exception occurred", ex);
        
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            request.getDescription(false).replace("uri=", ""),
            "Internal Server Error",
            ex.getMessage(),
            null
        );
        
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
```

**QUY TẮC:**
- ❌ KHÔNG dùng `try-catch` trong Controller
- ✅ Throw exception, để `@RestControllerAdvice` xử lý
- ✅ Log error ở global handler

---

## 7️⃣ VALIDATION

### 7.1 Request Validation (BẮT BUỘC)

```java
@Getter
@Setter
public class CreateUserRequest {
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).*$",
        message = "Password must contain at least one digit, one lowercase, one uppercase, and one special character"
    )
    private String password;
    
    @NotNull(message = "Age is required")
    @Min(value = 18, message = "Age must be at least 18")
    @Max(value = 100, message = "Age must not exceed 100")
    private Integer age;
    
    @NotNull(message = "Status is required")
    private UserStatus status;
}
```

### 7.2 Custom Validator

```java
// Annotation
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueEmailValidator.class)
public @interface UniqueEmail {
    String message() default "Email already exists";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

// Validator
@Component
@RequiredArgsConstructor
public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {
    
    private final UserRepository userRepository;
    
    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null) return true;
        return !userRepository.existsByEmail(email);
    }
}
```

---

## 8️⃣ DATABASE & ENTITY

### 8.1 Base Entity (BẮT BUỘC)

```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class BaseEntity<T> implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private T id;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @CreatedBy
    @Column(updatable = false)
    private String createdBy;
    
    @LastModifiedBy
    private String updatedBy;
}
```

### 8.2 Entity Example

```java
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity<Long> {
    
    @Column(nullable = false, unique = true, length = 50)
    private String username;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;
    
    @Column(length = 15)
    private String phoneNumber;
    
    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders = new ArrayList<>();
}
```

**QUY TẮC:**
- ✅ Tất cả entity extends `BaseEntity`
- ✅ Sử dụng `@Column` để định nghĩa constraints
- ✅ Lazy loading cho relationships
- ❌ KHÔNG expose Entity ra ngoài (dùng DTO)

### 8.3 Repository

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    List<User> findByStatus(UserStatus status);
    
    @Query("SELECT u FROM User u WHERE u.username LIKE %:keyword% OR u.email LIKE %:keyword%")
    Page<User> searchUsers(@Param("keyword") String keyword, Pageable pageable);
}
```

---

## 9️⃣ MAPPING & DTO

### 9.1 MapStruct (BẮT BUỘC)

```java
@Mapper(componentModel = "spring")
public interface UserMapper {
    
    // Entity -> Response DTO
    UserResponse toResponse(User user);
    
    List<UserResponse> toResponseList(List<User> users);
    
    // Request DTO -> Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(CreateUserRequest request);
    
    // Update Entity from Request DTO
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntityFromRequest(UpdateUserRequest request, @MappingTarget User user);
}
```

### 9.2 DTO Design

**Request DTO:**
```java
@Getter
@Setter
public class CreateUserRequest {
    @NotBlank
    private String username;
    
    @Email
    private String email;
    
    @NotBlank
    private String password;
}
```

**Response DTO:**
```java
@Getter
@Setter
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private UserStatus status;
    private LocalDateTime createdAt;
    // NO PASSWORD!
}
```

**QUY TẮC:**
- ❌ KHÔNG dùng Entity làm request/response
- ✅ Mỗi use-case tạo DTO riêng
- ✅ Response DTO không chứa thông tin nhạy cảm

---

## 🔟 CONFIGURATION

### 10.1 Application Properties (BẮT BUỘC)

**❌ KHÔNG hardcode config**

```yaml
# application.yml
spring:
  datasource:
    url: ${DB_URL:jdbc:mysql://localhost:3306/mydb}
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:password}
  
  jpa:
    hibernate:
      ddl-auto: ${DDL_AUTO:validate}
    show-sql: ${SHOW_SQL:false}
    properties:
      hibernate:
        format_sql: true
        
server:
  port: ${PORT:8080}
  
app:
  jwt:
    secret: ${JWT_SECRET}
    expiration: ${JWT_EXPIRATION:86400000}
```

### 10.2 Environment Variables

```properties
# .env (local development)
DB_URL=jdbc:mysql://localhost:3306/mydb
DB_USERNAME=root
DB_PASSWORD=password
JWT_SECRET=your-secret-key
```

---

## 1️⃣1️⃣ DOCUMENTATION

### 11.1 Swagger/OpenAPI (BẮT BUỘC)

```java
@Configuration
public class SwaggerConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("User Management API")
                .version("1.0")
                .description("API documentation for User Management System"));
    }
}
```

**Controller Documentation:**
```java
@Tag(name = "User Management", description = "APIs for user operations")
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    
    @Operation(
        summary = "Create new user",
        description = "Creates a new user account with the provided information"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    @PostMapping
    public ResponseEntity<ResponseData<UserResponse>> createUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "User creation request",
                required = true
            )
            @Valid @RequestBody CreateUserRequest request) {
        // Implementation
    }
}
```

### 11.2 JavaDoc

```java
/**
 * Service for managing user operations.
 * Handles user creation, update, retrieval, and deletion.
 *
 * @author Team Backend
 * @version 1.0
 */
public interface UserService {
    
    /**
     * Creates a new user account.
     *
     * @param request the user creation request containing user details
     * @return the created user response
     * @throws DuplicateResourceException if email already exists
     */
    UserResponse createUser(CreateUserRequest request);
}
```

---

## 1️⃣2️⃣ BEST PRACTICES

### 12.1 Transaction Management

```java
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    @Transactional  // Đặt ở Service layer
    public UserResponse createUser(CreateUserRequest request) {
        // All DB operations in one transaction
        User user = userMapper.toEntity(request);
        user = userRepository.save(user);
        
        // Send email (non-transactional work should be after commit)
        return userMapper.toResponse(user);
    }
    
    @Transactional(readOnly = true)  // For read operations
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return userMapper.toResponse(user);
    }
}
```

### 12.2 Logging

```java
@Slf4j  // Lombok annotation
@Service
public class UserServiceImpl implements UserService {
    
    public UserResponse createUser(CreateUserRequest request) {
        log.info("Creating user with email: {}", request.getEmail());
        
        try {
            // Business logic
            log.info("User created successfully: {}", user.getId());
            return userMapper.toResponse(user);
        } catch (Exception e) {
            log.error("Error creating user: {}", e.getMessage(), e);
            throw e;
        }
    }
}
```

**QUY TẮC:**
- ✅ Sử dụng SLF4J với Lombok `@Slf4j`
- ❌ KHÔNG dùng `System.out.println()`
- ✅ Log level: INFO (success), ERROR (exceptions), DEBUG (details)

### 12.3 Enum Usage

```java
public enum UserStatus {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    BLOCKED("Blocked"),
    PENDING("Pending Verification");
    
    private final String displayName;
    
    UserStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
```

### 12.4 Utility Classes

```java
@UtilityClass  // Lombok - prevents instantiation
public class DateUtils {
    
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    
    public static String formatDate(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT));
    }
    
    public static LocalDate parseDate(String dateString) {
        return LocalDate.parse(dateString, DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT));
    }
}
```

### 12.5 Security Best Practices

```java
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final PasswordEncoder passwordEncoder;
    
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        // ✅ Always encode passwords
        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        
        user = userRepository.save(user);
        
        // ✅ Never return password in response
        return userMapper.toResponse(user);
    }
}
```

---

## 📌 CHECKLIST TRƯỚC KHI COMMIT

- [ ] Code tuân thủ naming convention
- [ ] Service có interface
- [ ] Không có business logic trong Controller
- [ ] Sử dụng DTO, không expose Entity
- [ ] Exception handling qua `@RestControllerAdvice`
- [ ] Có validation cho request
- [ ] Response theo chuẩn `ResponseData` / `PageResponse`
- [ ] Sử dụng `@Transactional` đúng chỗ
- [ ] Có logging với SLF4J
- [ ] Swagger documentation đầy đủ
- [ ] Không hardcode config
- [ ] Code format (Ctrl + Alt + L)

---

## 🚫 NHỮNG ĐIỀU TUYỆT ĐỐI KHÔNG LÀM

1. ❌ Viết business logic trong Controller
2. ❌ Dùng Entity làm request/response
3. ❌ Hardcode configuration values
4. ❌ Dùng `System.out.println()` thay vì logger
5. ❌ Try-catch trong Controller (dùng global exception handler)
6. ❌ Trả về password trong response
7. ❌ Skip validation
8. ❌