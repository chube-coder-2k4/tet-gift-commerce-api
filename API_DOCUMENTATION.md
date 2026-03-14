# 🎁 Tet Gift Commerce API — Frontend Integration Guide

> **Cập nhật:** 2026-03-11  
> **Version:** 2.1  
> **Base URL (Dev):** `http://localhost:8080/api/v1`  
> **Base URL (Prod):** `https://api.shophuypro.store/api/v1`  
> **Swagger UI:** `http://localhost:8080/swagger-ui.html`

---

## 📋 Mục lục

1. [Server & Cấu hình](#1-server--cấu-hình)
2. [Authentication & Headers](#2-authentication--headers)
3. [Response Wrapper](#3-response-wrapper)
4. [Pagination](#4-pagination)
5. [Error Handling](#5-error-handling)
6. [Enums](#6-enums)
7. [Validation Rules](#7-validation-rules)
8. [API Endpoints](#8-api-endpoints)
   - [8.1 Authentication](#81-authentication)
   - [8.2 User](#82-user)
   - [8.3 Role (ADMIN)](#83-role-admin)
   - [8.4 Address](#84-address)
   - [8.5 Category](#85-category)
   - [8.6 Product](#86-product)
   - [8.7 Bundle](#87-bundle)
   - [8.8 Cart](#88-cart)
   - [8.9 Order](#89-order)
   - [8.10 Payment](#810-payment)
   - [8.11 Discount](#811-discount)
   - [8.12 Blog](#812-blog)
   - [8.13 Product Review](#813-product-review)
   - [8.14 AI Chatbot (RAG)](#814-ai-chatbot-rag)
9. [WebSocket — Realtime](#9-websocket--realtime)
10. [Tổng hợp nhanh](#10-tổng-hợp-nhanh--tất-cả-endpoints)

---

## 1. Server & Cấu hình

| Thuộc tính | Dev | Prod |
|---|---|---|
| **Port** | `8080` | `443 (HTTPS)` |
| **Base URL** | `http://localhost:8080` | `https://api.shophuypro.store` |
| **API Prefix** | `/api/v1` | `/api/v1` |
| **Swagger UI** | `http://localhost:8080/swagger-ui.html` | Disabled |
| **API Docs** | `http://localhost:8080/v3/api-docs` | Disabled |
| **WebSocket** | `ws://localhost:8080/ws` | `wss://api.shophuypro.store/ws` |

### CORS Allowed Origins

```
http://localhost:3000
http://localhost:5173
https://shophuypro.store
```

### Allowed Methods

```
GET, POST, PUT, DELETE, OPTIONS
```

### Services Yêu cầu

| Service | Port | Mô tả |
|---|---|---|
| **PostgreSQL** | `5432` | Database chính |
| **Redis** | `6379` | Cache OTP, Refresh Token |

---

## 2. Authentication & Headers

### JWT Bearer Token

Tất cả API yêu cầu xác thực đều cần header:

```
Authorization: Bearer <access_token>
```

### Refresh Token

Khi cần refresh access token hoặc logout:

```
x-refresh-token: <refresh_token>
```

### Content-Type

```
Content-Type: application/json
```

### Xử lý Token hết hạn

Khi access token hết hạn, server sẽ:
- Trả về HTTP `401`
- Kèm response header: `X-Token-Expired: true`

**Frontend phải:**
1. Check header `X-Token-Expired: true`
2. Gọi `POST /auth/refresh-token` với `x-refresh-token` header
3. Lưu access token mới, retry request ban đầu

```
Access token hết hạn → nhận 401 + X-Token-Expired: true
→ gọi POST /auth/refresh-token (x-refresh-token: <refresh_token>)
→ nhận accessToken mới → retry request
```

### Public Endpoints (không cần token)

| Method | URL |
|---|---|
| `POST` | `/api/v1/auth/**` |
| `POST` | `/api/v1/user/register` |
| `GET` | `/api/v1/products/**` |
| `GET` | `/api/v1/bundles/**` |
| `GET` | `/api/v1/categories/**` |
| `GET` | `/api/v1/blogs/**` |
| `GET` | `/api/v1/blog-topics/**` |
| `GET` | `/api/v1/products/{id}/reviews` |
| `GET` | `/api/v1/payments/vnpay-callback` |
| `GET` | `/swagger-ui/**` |

### OAuth2 Login URLs

| Provider | URL |
|---|---|
| Google | `http://localhost:8080/oauth2/authorization/google` |
| GitHub | `http://localhost:8080/oauth2/authorization/github` |
| Callback redirect | `http://localhost:3000/oauth2/redirect?token=<jwt>&email=<email>` |

> [!NOTE]
> Sau khi OAuth2 thành công, token được gửi qua **query param `token`** trong URL redirect.  
> Frontend đọc `token` từ URL, lưu vào storage và dùng như access token bình thường.  
> Token **không** có refresh token tương ứng — khi hết hạn, user cần login lại qua OAuth2.

---

## 3. Response Wrapper

**Tất cả** response đều được wrap trong format chung:

### Success Response

```json
{
  "status": 200,
  "message": "Description of what happened",
  "data": { ... }
}
```

### Success Response (no data)

```json
{
  "status": 200,
  "message": "Action completed successfully"
}
```

> [!NOTE]
> Field `data` sẽ **không xuất hiện** trong response nếu giá trị là `null` (do `@JsonInclude(NON_NULL)`)

---

## 4. Pagination

Các API trả về danh sách có phân trang sử dụng wrapper `PageResponse`:

### Query Parameters

| Param | Type | Default | Description |
|---|---|---|---|
| `page` | int | `0` | Trang hiện tại (0-indexed) |
| `size` | int | `10` | Số item mỗi trang |
| `sortBy` | string | `createdAt` | Field để sort |
| `sortDir` | string | `desc` | `asc` hoặc `desc` |

### Response Format

```json
{
  "status": 200,
  "message": "Items fetched successfully",
  "data": {
    "pageNo": 0,
    "pageSize": 10,
    "totalPages": 5,
    "totalItems": 48,
    "data": [ ... ]
  }
}
```

---

## 5. Error Handling

### Error Response Format

```json
{
  "timestamp": "2026-03-11 12:00:00",
  "status": 400,
  "path": "/api/v1/auth/login",
  "error": "Invalid Payload",
  "message": "Username or email is required"
}
```

### HTTP Status Codes

| Code | Khi nào |
|---|---|
| `200` | Thành công |
| `201` | Tạo mới thành công |
| `400` | Dữ liệu không hợp lệ (validation) |
| `401` | Chưa đăng nhập / Token hết hạn → check header `X-Token-Expired` |
| `403` | Không có quyền truy cập |
| `404` | Không tìm thấy tài nguyên |
| `409` | Xung đột dữ liệu (email/username đã tồn tại, logic error) |
| `500` | Lỗi server |

---

## 6. Enums

### OrderStatus

```
CREATED → WAITING_PAYMENT → PAID → PROCESSING → SHIPPED → COMPLETED
                                                         → CANCELLED
```

| Value | Mô tả | User có thể set? |
|---|---|---|
| `CREATED` | Đơn hàng vừa tạo | ❌ (tự động) |
| `WAITING_PAYMENT` | Đang chờ thanh toán | ❌ (tự động sau createPayment) |
| `PAID` | Đã thanh toán | ❌ (tự động sau VNPay callback) |
| `PROCESSING` | Đang xử lý | ✅ ADMIN only |
| `SHIPPED` | Đang giao hàng | ✅ ADMIN only |
| `COMPLETED` | Hoàn thành | ✅ ADMIN only |
| `CANCELLED` | Đã hủy | ✅ User (chỉ khi CREATED/WAITING_PAYMENT) |

### PaymentMethod

| Value | Mô tả |
|---|---|
| `COD` | Thanh toán khi nhận hàng |
| `VN_PAY` | Thanh toán qua VNPay |

### PaymentStatus

| Value | Mô tả |
|---|---|
| `PENDING` | Đang chờ |
| `SUCCESS` | Thành công |
| `FAILED` | Thất bại |
| `CANCELLED` | Đã hủy |
| `EXPIRED` | Hết hạn |

### Cart/Order Item Type

| Value | Mô tả |
|---|---|
| `PRODUCT` | Sản phẩm đơn lẻ |
| `BUNDLE` | Combo/giỏ quà |

---

## 7. Validation Rules

### Password

| Field | Rule |
|---|---|
| `password` (đăng ký) | Tối thiểu **6** ký tự |
| `oldPassword`, `newPassword` (đổi/reset mật khẩu) | Tối thiểu **8** ký tự |

### Các field bắt buộc theo endpoint

| Endpoint | Field bắt buộc |
|---|---|
| `POST /user/register` | `fullName`, `email`, `password` (≥6), `username` (3-50 ký tự) |
| `POST /auth/login` | `usernameOrEmail`, `password` (≥6) |
| `POST /auth/forgot-password` | `email` (valid email format) |
| `POST /auth/reset-password` | `token`, `newPassword` (≥8), `confirmPassword` |
| `POST /auth/change-password` | `oldPassword` (≥8), `newPassword` (≥8), `confirmPassword` |
| `POST /addresses` | `receiverName`, `phone`, `addressDetail` |
| `POST /categories` | `name` |
| `POST /products/register` | `name`, `price` (>0) |
| `POST /bundles` | `name`, `price` (>0) |
| `POST /discounts` | `code`, `discountValue` (>0) |
| `POST /products/{id}/reviews` | `rating` (1-5) |
| `POST /blog-topics` | `name` |
| `POST /blogs` | `title` |
| `POST /payments/create` | `orderId`, `method` |

---

## 8. API Endpoints

---

### 8.1 Authentication

Base: `/api/v1/auth`

#### 🔓 POST `/auth/login` — Đăng nhập

**Request:**
```json
{
  "usernameOrEmail": "admin@tetgift.com",
  "password": "12345678"
}
```

> [!NOTE]
> Field `usernameOrEmail` chấp nhận cả **username** lẫn **email**.

**Response:**
```json
{
  "status": 200,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
    "userId": 1
  }
}
```

---

#### 🔓 POST `/auth/refresh-token` — Làm mới access token

**Header:**
```
x-refresh-token: <refresh_token>
```

**Response:**
```json
{
  "status": 200,
  "message": "Token refreshed successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
    "userId": 1
  }
}
```

---

#### 🔐 POST `/auth/logout` — Đăng xuất

**Header:**
```
Authorization: Bearer <access_token>
x-refresh-token: <refresh_token>
```

**Response:**
```json
{
  "status": 200,
  "message": "Logout successful",
  "data": "Logout successful"
}
```

---

#### 🔓 POST `/auth/forgot-password` — Quên mật khẩu

**Request:**
```json
{
  "email": "user@example.com"
}
```

**Response:**
```json
{
  "status": 200,
  "message": "If the email is registered, a password reset token has been sent",
  "data": "Password reset token has been sent to your email"
}
```

> [!IMPORTANT]
> Reset token được gửi qua **email**, **KHÔNG** trả về trong response body.  
> Người dùng mở email → copy token → điền vào form reset password.

---

#### 🔓 POST `/auth/reset-password` — Đặt lại mật khẩu

**Request:**
```json
{
  "token": "<reset_token_từ_email>",
  "newPassword": "newpassword123",
  "confirmPassword": "newpassword123"
}
```

> Password tối thiểu **8 ký tự**. Token có hiệu lực **10 phút**.

**Response:**
```json
{
  "status": 200,
  "message": "Password reset successfully",
  "data": "Password reset successful"
}
```

---

#### 🔐 POST `/auth/change-password` — Đổi mật khẩu

**Header:** `Authorization: Bearer <access_token>`

**Request:**
```json
{
  "oldPassword": "currentpass123",
  "newPassword": "newpassword123",
  "confirmPassword": "newpassword123"
}
```

> Password tối thiểu **8 ký tự**.  
> Sau khi đổi thành công, **tất cả refresh token cũ bị vô hiệu hóa** — user cần login lại.

**Response:**
```json
{
  "status": 200,
  "message": "Password changed successfully",
  "data": "Change password successful"
}
```

---

#### 🔓 POST `/auth/verify-otp` — Xác thực OTP

**Request:**
```json
{
  "email": "user@example.com",
  "otp": "123456"
}
```

**Response:**
```json
{
  "status": 200,
  "message": "OTP verified successfully"
}
```

---

#### 🔓 POST `/auth/resend-otp` — Gửi lại OTP

**Request:**
```json
{
  "email": "user@example.com"
}
```

**Response:**
```json
{
  "status": 200,
  "message": "OTP resent successfully if the email is registered and not yet verified"
}
```

> [!NOTE]
> Nếu user đã verify, API trả về `409 Conflict`.

---

### 8.2 User

Base: `/api/v1/user`

#### 🔓 POST `/user/register` — Đăng ký tài khoản

**Request:**
```json
{
  "email": "user@example.com",
  "password": "123456",
  "fullName": "Nguyễn Văn A",
  "phone": "0901234567",
  "username": "nguyenvana"
}
```

> Password tối thiểu **6 ký tự**. Username **3-50 ký tự**.  
> Sau khi đăng ký, một email OTP sẽ được gửi — cần verify trước khi đăng nhập.

**Response:**
```json
{
  "status": 201,
  "message": "User registered successfully",
  "data": 1
}
```

`data` là `userId` vừa tạo.

---

#### 🔐 GET `/user?page=0&size=10&sortBy=createdAt&sortDir=desc` — Lấy danh sách users (ADMIN)

**Response:**
```json
{
  "status": 200,
  "message": "Fetched users successfully",
  "data": {
    "pageNo": 0,
    "pageSize": 10,
    "totalPages": 1,
    "totalItems": 3,
    "data": [
      {
        "id": 1,
        "fullName": "Nguyễn Văn A",
        "email": "user@example.com",
        "phone": "0901234567",
        "username": "nguyenvana",
        "roleName": "USER",
        "createdAt": "2026-03-01T10:00:00",
        "updatedAt": "2026-03-01T10:00:00"
      }
    ]
  }
}
```

---

#### 🔐 GET `/user/{id}` — Lấy thông tin user

**Response:**
```json
{
  "status": 200,
  "message": "Fetched user successfully",
  "data": {
    "id": 1,
    "fullName": "Nguyễn Văn A",
    "email": "user@example.com",
    "phone": "0901234567",
    "username": "nguyenvana",
    "roleName": "USER",
    "createdAt": "2026-03-01T10:00:00",
    "updatedAt": "2026-03-01T10:00:00"
  }
}
```

---

#### 🔐 PUT `/user/{id}` — Cập nhật user

**Request:** *(tất cả fields đều optional)*
```json
{
  "fullName": "Nguyễn Văn B",
  "email": "newemail@example.com",
  "phone": "0987654321",
  "username": "newusername"
}
```

**Response:**
```json
{
  "status": 200,
  "message": "User updated successfully",
  "data": 1
}
```

`data` là `userId` vừa cập nhật.

---

#### 🔐 DELETE `/user/{id}` — Xóa user (ADMIN)

**Response:**
```json
{
  "status": 200,
  "message": "User deleted successfully",
  "data": "User with ID 1 has been deleted"
}
```

---

### 8.3 Role (ADMIN)

Base: `/api/v1/role`

> [!WARNING]
> Tất cả write endpoints (`POST`, `PUT`, `DELETE`) yêu cầu role **ADMIN**.

#### 🔐 POST `/role` — Tạo role (ADMIN)

**Request:**
```json
{
  "name": "ADMIN",
  "description": "Administrator role"
}
```

**Response:**
```json
{
  "status": 201,
  "message": "Role created successfully",
  "data": {
    "id": 1,
    "name": "ADMIN",
    "description": "Administrator role"
  }
}
```

---

#### 🔐 GET `/role` — Lấy tất cả roles

**Response:**
```json
{
  "status": 200,
  "message": "Roles retrieved successfully",
  "data": [
    { "id": 1, "name": "ADMIN", "description": "Administrator role" },
    { "id": 2, "name": "USER", "description": "Regular user" }
  ]
}
```

---

#### 🔐 GET `/role/{id}` — Lấy role theo ID

**Response:**
```json
{
  "status": 200,
  "message": "Role retrieved successfully",
  "data": {
    "id": 1,
    "name": "ADMIN",
    "description": "Administrator role"
  }
}
```

#### 🔐 PUT `/role?id=1` — Cập nhật role (ADMIN)

**Request:**
```json
{
  "name": "MODERATOR",
  "description": "Moderator role"
}
```

#### 🔐 DELETE `/role?id=1` — Xóa role (ADMIN)

---

### 8.4 Address

Base: `/api/v1/addresses`

> [!IMPORTANT]
> Tất cả endpoints cần **Bearer Token**. User chỉ xem/sửa/xóa được address **của mình** — nếu cố truy cập address của người khác sẽ nhận `403 Forbidden`.

#### 🔐 POST `/addresses` — Tạo địa chỉ mới

**Request:**
```json
{
  "receiverName": "Nguyễn Văn A",
  "phone": "0901234567",
  "addressDetail": "123 Đường ABC, Phường 1, Quận 1, TP.HCM",
  "isDefault": true
}
```

> Nếu `isDefault: true`, tất cả địa chỉ mặc định cũ sẽ tự động bị bỏ mặc định.

**Response:**
```json
{
  "status": 201,
  "message": "Address created successfully",
  "data": {
    "id": 1,
    "receiverName": "Nguyễn Văn A",
    "phone": "0901234567",
    "addressDetail": "123 Đường ABC, Phường 1, Quận 1, TP.HCM",
    "isDefault": true
  }
}
```

---

#### 🔐 GET `/addresses` — Lấy danh sách địa chỉ

> Địa chỉ mặc định (`isDefault: true`) sẽ được **trả về đầu tiên**.

**Response:**
```json
{
  "status": 200,
  "message": "Addresses fetched successfully",
  "data": [
    {
      "id": 1,
      "receiverName": "Nguyễn Văn A",
      "phone": "0901234567",
      "addressDetail": "123 Đường ABC, Phường 1, Quận 1, TP.HCM",
      "isDefault": true
    },
    {
      "id": 2,
      "receiverName": "Trần Thị B",
      "phone": "0912345678",
      "addressDetail": "456 Đường XYZ, Quận 3, TP.HCM",
      "isDefault": false
    }
  ]
}
```

---

#### 🔐 GET `/addresses/{id}` — Lấy chi tiết 1 địa chỉ

**Response:**
```json
{
  "status": 200,
  "message": "Address fetched",
  "data": {
    "id": 1,
    "receiverName": "Nguyễn Văn A",
    "phone": "0901234567",
    "addressDetail": "123 Đường ABC, Phường 1, Quận 1, TP.HCM",
    "isDefault": true
  }
}
```

---

#### 🔐 PUT `/addresses/{id}` — Cập nhật địa chỉ

**Request:** *(giống POST, tất cả fields bắt buộc)*

#### 🔐 DELETE `/addresses/{id}` — Xóa địa chỉ

**Response:**
```json
{
  "status": 200,
  "message": "Address deleted successfully"
}
```

#### 🔐 PUT `/addresses/{id}/default` — Đặt làm địa chỉ mặc định

**Response:**
```json
{
  "status": 200,
  "message": "Default address set successfully",
  "data": {
    "id": 1,
    "receiverName": "Nguyễn Văn A",
    "phone": "0901234567",
    "addressDetail": "123 Đường ABC",
    "isDefault": true
  }
}
```

---

### 8.5 Category

Base: `/api/v1/categories`

#### 🔓 GET `/categories` — Lấy tất cả danh mục (active)

**Response:**
```json
{
  "status": 200,
  "message": "Categories retrieved successfully",
  "data": [
    {
      "id": 1,
      "name": "Bánh kẹo Tết",
      "description": "Các loại bánh kẹo truyền thống",
      "isActive": true
    },
    {
      "id": 2,
      "name": "Mứt Tết",
      "description": "Mứt truyền thống Việt Nam",
      "isActive": true
    }
  ]
}
```

---

#### 🔓 GET `/categories/{id}` — Lấy danh mục theo ID

**Response:**
```json
{
  "status": 200,
  "message": "Category retrieved successfully",
  "data": {
    "id": 1,
    "name": "Bánh kẹo Tết",
    "description": "Các loại bánh kẹo truyền thống",
    "isActive": true
  }
}
```

#### 🔐 POST `/categories` — Tạo danh mục (ADMIN)

**Request:**
```json
{
  "name": "Trái cây nhập khẩu",
  "description": "Trái cây cao cấp nhập khẩu"
}
```

**Response:**
```json
{
  "status": 201,
  "message": "Category created successfully",
  "data": {
    "id": 3,
    "name": "Trái cây nhập khẩu",
    "description": "Trái cây cao cấp nhập khẩu",
    "isActive": true
  }
}
```

#### 🔐 PUT `/categories/{id}` — Cập nhật danh mục (ADMIN)

**Request:** *(giống POST)*

#### 🔐 DELETE `/categories/{id}` — Xóa danh mục (soft delete, ADMIN)

**Response:**
```json
{
  "status": 200,
  "message": "Category deleted successfully"
}
```

---

### 8.6 Product

Base: `/api/v1/products`

#### 🔓 GET `/products?page=0&size=10&sortBy=createdAt&sortDir=desc` — Lấy danh sách sản phẩm

**Response:**
```json
{
  "status": 200,
  "message": "Products fetched successfully",
  "data": {
    "pageNo": 0,
    "pageSize": 10,
    "totalPages": 2,
    "totalItems": 15,
    "data": [
      {
        "id": 1,
        "name": "Bánh Chưng Truyền Thống",
        "description": "Bánh chưng vuông truyền thống 1kg",
        "price": 150000.00,
        "stock": 100,
        "categoryName": "Bánh kẹo Tết",
        "categoryId": 1,
        "isActive": true,
        "manufactureDate": "2026-01-10",
        "expDate": "2026-03-10",
        "images": [
          {
            "id": 1,
            "imageUrl": "https://res.cloudinary.com/xxx/image.jpg",
            "imageType": "jpg",
            "publicId": "products/banh-chung-1",
            "isPrimary": true
          }
        ]
      }
    ]
  }
}
```

> `isActive: false` items **không** được trả về trong list — chỉ sản phẩm active.

---

#### 🔓 GET `/products/{id}` — Lấy chi tiết sản phẩm

**Response:** *(cấu trúc giống 1 item trong list trên)*

> Nếu sản phẩm không tồn tại hoặc đã xóa → `404 Not Found`

---

#### 🔐 POST `/products/register` — Tạo sản phẩm mới (ADMIN)

**Request:**
```json
{
  "name": "Bánh Chưng Truyền Thống",
  "description": "Bánh chưng vuông truyền thống 1kg",
  "price": 150000.00,
  "stock": 100,
  "categoryId": 1,
  "manufactureDate": "2026-01-10",
  "expDate": "2026-03-10",
  "images": [
    {
      "imageUrl": "https://res.cloudinary.com/xxx/image.jpg",
      "imageType": "jpg",
      "publicId": "products/banh-chung-1",
      "isPrimary": true
    }
  ]
}
```

> `categoryId`, `manufactureDate`, `expDate`, `images` đều **optional**.  
> `stock` mặc định là `0` nếu không truyền.  
> Ngày theo định dạng `yyyy-MM-dd`.

**Response:**
```json
{
  "status": 201,
  "message": "Product registered successfully",
  "data": 1
}
```

`data` là `productId` vừa tạo.

---

#### 🔐 PUT `/products/{id}` — Cập nhật sản phẩm (ADMIN)

**Request:** *(giống POST, tất cả fields bắt buộc khi update)*

> Khi truyền `images`, **toàn bộ ảnh cũ bị thay thế** bởi danh sách mới.

#### 🔐 DELETE `/products/{id}` — Xóa sản phẩm (soft delete, ADMIN)

> Soft delete: `isActive` được set thành `false`. Sản phẩm không còn xuất hiện trong danh sách.

---

### 8.7 Bundle

Base: `/api/v1/bundles`

#### 🔓 GET `/bundles?page=0&size=10&sortBy=createdAt&sortDir=desc` — Lấy danh sách combo

**Response:**
```json
{
  "status": 200,
  "message": "Bundles fetched successfully",
  "data": {
    "pageNo": 0,
    "pageSize": 10,
    "totalPages": 1,
    "totalItems": 5,
    "data": [
      {
        "id": 1,
        "name": "Combo Tết An Khang",
        "price": 500000.00,
        "isCustom": false,
        "isActive": true,
        "products": [
          {
            "id": 1,
            "productId": 1,
            "productName": "Bánh Chưng Truyền Thống",
            "productPrice": 150000.00,
            "quantity": 2
          },
          {
            "id": 2,
            "productId": 3,
            "productName": "Mứt Gừng",
            "productPrice": 50000.00,
            "quantity": 3
          }
        ]
      }
    ]
  }
}
```

---

#### 🔓 GET `/bundles/{id}` — Lấy chi tiết combo

**Response:** *(cấu trúc giống 1 item trong list trên)*

---

#### 🔐 POST `/bundles` — Tạo combo mới (ADMIN)

**Request:**
```json
{
  "name": "Combo Tết An Khang",
  "price": 500000.00,
  "isCustom": false,
  "products": [
    { "productId": 1, "quantity": 2 },
    { "productId": 3, "quantity": 3 }
  ]
}
```

> `isCustom`: `false` = combo cố định do admin tạo, `true` = combo khách tự chọn.  
> `products` là **optional**. `quantity` mặc định `1` nếu không truyền.

**Response:**
```json
{
  "status": 201,
  "message": "Bundle created successfully",
  "data": 1
}
```

`data` là `bundleId`.

#### 🔐 PUT `/bundles/{id}` — Cập nhật combo (ADMIN)

**Request:** *(giống POST)*

#### 🔐 DELETE `/bundles/{id}` — Xóa combo (soft delete, ADMIN)

---

### 8.8 Cart

Base: `/api/v1/cart`

> [!IMPORTANT]
> Tất cả endpoints cần **Bearer Token**. Cart được tạo tự động khi user chưa có.

#### 🔐 GET `/cart` — Lấy giỏ hàng

**Response:**
```json
{
  "status": 200,
  "message": "Cart fetched successfully",
  "data": {
    "id": 1,
    "items": [
      {
        "id": 1,
        "itemType": "PRODUCT",
        "itemId": 1,
        "itemName": "Bánh Chưng Truyền Thống",
        "itemPrice": 150000.00,
        "quantity": 2,
        "subtotal": 300000.00
      },
      {
        "id": 2,
        "itemType": "BUNDLE",
        "itemId": 1,
        "itemName": "Combo Tết An Khang",
        "itemPrice": 500000.00,
        "quantity": 1,
        "subtotal": 500000.00
      }
    ],
    "totalPrice": 800000.00,
    "totalItems": 2
  }
}
```

---

#### 🔐 POST `/cart/items` — Thêm item vào giỏ

**Request (thêm sản phẩm):**
```json
{
  "itemType": "PRODUCT",
  "productId": 1,
  "quantity": 2
}
```

**Request (thêm combo):**
```json
{
  "itemType": "BUNDLE",
  "bundleId": 1,
  "quantity": 1
}
```

> `quantity` mặc định `1` nếu không truyền.  
> `itemType` **bắt buộc**, giá trị là `"PRODUCT"` hoặc `"BUNDLE"` (case-insensitive).

**Response:** *(CartResponse như trên, trả về giỏ hàng mới)*

---

#### 🔐 PUT `/cart/items/{itemId}?quantity=3` — Cập nhật số lượng

> Nếu `quantity <= 0`, item sẽ bị **xóa khỏi giỏ hàng**.

**Response:** *(CartResponse như trên)*

---

#### 🔐 DELETE `/cart/items/{itemId}` — Xóa item khỏi giỏ

**Response:**
```json
{
  "status": 200,
  "message": "Item removed from cart"
}
```

#### 🔐 DELETE `/cart` — Xóa toàn bộ giỏ hàng

**Response:**
```json
{
  "status": 200,
  "message": "Cart cleared"
}
```

---

### 8.9 Order

Base: `/api/v1/orders`

> [!IMPORTANT]
> Tất cả endpoints cần **Bearer Token**. Đơn hàng được tạo từ giỏ hàng hiện tại.

#### 🔐 POST `/orders` — Tạo đơn hàng từ giỏ hàng

**Request:**
```json
{
  "addressId": 1,
  "discountCode": "TET2026",
  "vatCompanyName": "Công ty ABC",
  "vatTaxCode": "0123456789",
  "vatPhone": "0901234567",
  "vatAddress": "123 Đường ABC, TP.HCM"
}
```

> Tất cả fields đều **optional**.  
> `addressId` là ID địa chỉ giao hàng (từ danh sách address của user).  
> Nếu `discountCode` hợp lệ, giá trị discount sẽ được trừ vào `totalAmount`.  
> Các field `vat*` chỉ cần khi muốn xuất hóa đơn VAT.

> [!WARNING]
> **Giỏ hàng phải không được rỗng** khi tạo đơn.

**Response:**
```json
{
  "status": 201,
  "message": "Order created successfully",
  "data": {
    "id": 1,
    "status": "CREATED",
    "totalAmount": 700000.00,
    "vatCompanyName": "Công ty ABC",
    "vatTaxCode": "0123456789",
    "vatPhone": "0901234567",
    "vatAddress": "123 Đường ABC, TP.HCM",
    "items": [
      {
        "id": 1,
        "itemType": "PRODUCT",
        "itemName": "Bánh Chưng Truyền Thống",
        "priceSnapshot": 150000.00,
        "quantity": 2,
        "subtotal": 300000.00
      },
      {
        "id": 2,
        "itemType": "BUNDLE",
        "itemName": "Combo Tết An Khang",
        "priceSnapshot": 500000.00,
        "quantity": 1,
        "subtotal": 500000.00
      }
    ],
    "createdAt": "2026-03-11T12:00:00"
  }
}
```

> [!NOTE]
> Sau khi tạo đơn thành công, **giỏ hàng sẽ bị xóa sạch** tự động.  
> `priceSnapshot` là giá tại thời điểm đặt hàng, **KHÔNG** thay đổi khi sản phẩm đổi giá.

---

#### 🔐 GET `/orders?page=0&size=10` — Lấy danh sách đơn hàng của tôi

**Response:**
```json
{
  "status": 200,
  "message": "Orders fetched",
  "data": {
    "pageNo": 0,
    "pageSize": 10,
    "totalPages": 1,
    "totalItems": 3,
    "data": [
      {
        "id": 1,
        "status": "PAID",
        "totalAmount": 700000.00,
        "items": [ ... ],
        "createdAt": "2026-03-11T12:00:00"
      }
    ]
  }
}
```

---

#### 🔐 GET `/orders/{id}` — Lấy chi tiết đơn hàng

**Response:** *(cấu trúc đầy đủ như khi tạo đơn)*

---

#### 🔐 PUT `/orders/{id}/cancel` — Hủy đơn hàng

> Chỉ hủy được khi status là `CREATED` hoặc `WAITING_PAYMENT`.  
> Nếu đơn đã `PAID` hoặc xa hơn → `409 Conflict`.

**Response:**
```json
{
  "status": 200,
  "message": "Order cancelled",
  "data": { ... }
}
```

---

#### 🔐 PUT `/orders/{id}/status?status=PROCESSING` — Cập nhật trạng thái (ADMIN)

**Query param:** `status` — giá trị hợp lệ: `CREATED`, `WAITING_PAYMENT`, `PAID`, `PROCESSING`, `SHIPPED`, `COMPLETED`, `CANCELLED`

**Response:**
```json
{
  "status": 200,
  "message": "Order status updated",
  "data": { ... }
}
```

---

### 8.10 Payment

Base: `/api/v1/payments`

#### 🔐 POST `/payments/create` — Tạo thanh toán cho đơn hàng

**Request (COD):**
```json
{
  "orderId": 1,
  "method": "COD"
}
```

**Request (VNPay):**
```json
{
  "orderId": 1,
  "method": "VN_PAY"
}
```

> `method` chỉ chấp nhận `"COD"` hoặc `"VN_PAY"` (case-sensitive).  
> Đơn phải ở trạng thái `CREATED` — nếu không sẽ nhận `409 Conflict`.

**Response (VNPay):**
```json
{
  "status": 201,
  "message": "Payment created",
  "data": {
    "id": 1,
    "orderId": 1,
    "method": "VN_PAY",
    "status": "PENDING",
    "amount": 700000.00,
    "transactionId": null,
    "paidAt": null,
    "paymentUrl": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_Amount=70000000&..."
  }
}
```

> [!TIP]
> Với VNPay: redirect user tới `paymentUrl`.  
> Sau thanh toán, VNPay redirect về `VNPAY_RETURN_URL` bạn cấu hình.  
> `paymentUrl` là `null` với method `COD`.

**Response (COD):**
```json
{
  "status": 201,
  "message": "Payment created",
  "data": {
    "id": 1,
    "orderId": 1,
    "method": "COD",
    "status": "PENDING",
    "amount": 700000.00,
    "transactionId": null,
    "paidAt": null,
    "paymentUrl": null
  }
}
```

---

#### 🔓 GET `/payments/vnpay-callback?vnp_TxnRef=1&vnp_ResponseCode=00` — VNPay callback

> Endpoint này được **VNPay gọi tự động**. Frontend **KHÔNG** cần gọi trực tiếp.  
> `vnp_ResponseCode = "00"` → thanh toán thành công → order chuyển sang `PAID`.  
> `vnp_ResponseCode` khác → thanh toán thất bại → payment `FAILED`.

---

#### 🔐 GET `/payments/{orderId}` — Lấy thông tin thanh toán theo order ID

**Response:**
```json
{
  "status": 200,
  "message": "Payment fetched",
  "data": {
    "id": 1,
    "orderId": 1,
    "method": "VN_PAY",
    "status": "SUCCESS",
    "amount": 700000.00,
    "transactionId": "1",
    "paidAt": "2026-03-11T12:05:00",
    "paymentUrl": null
  }
}
```

---

### 8.11 Discount

Base: `/api/v1/discounts`

#### 🔐 POST `/discounts` — Tạo mã giảm giá (ADMIN)

**Request:**
```json
{
  "code": "TET2026",
  "discountValue": 100000.00,
  "startDate": "2026-01-01T00:00:00",
  "endDate": "2026-03-31T23:59:59"
}
```

> `code` sẽ tự động được chuyển thành **chữ hoa**.  
> `startDate`, `endDate` đều **optional**. Định dạng: `yyyy-MM-dd'T'HH:mm:ss`.

**Response:**
```json
{
  "status": 201,
  "message": "Discount created",
  "data": {
    "id": 1,
    "code": "TET2026",
    "discountValue": 100000.00,
    "startDate": "2026-01-01T00:00:00",
    "endDate": "2026-03-31T23:59:59",
    "isActive": true
  }
}
```

---

#### 🔐 GET `/discounts` — Lấy tất cả mã giảm giá (ADMIN)

#### 🔐 GET `/discounts/{id}` — Lấy chi tiết mã giảm giá

#### 🔐 PUT `/discounts/{id}` — Cập nhật mã giảm giá (ADMIN)

**Request:** *(giống POST)*

#### 🔐 DELETE `/discounts/{id}` — Xóa mã giảm giá (soft delete, ADMIN)

---

#### 🔐 POST `/discounts/validate?code=TET2026` — Kiểm tra mã giảm giá

> [!NOTE]
> Đây là `POST` request với `code` là **query parameter**, không phải request body.

**Response (hợp lệ):**
```json
{
  "status": 200,
  "message": "Discount code is valid",
  "data": {
    "id": 1,
    "code": "TET2026",
    "discountValue": 100000.00,
    "startDate": "2026-01-01T00:00:00",
    "endDate": "2026-03-31T23:59:59",
    "isActive": true
  }
}
```

**Response (không hợp lệ / hết hạn):**
```json
{
  "timestamp": "2026-03-11 12:00:00",
  "status": 404,
  "path": "/api/v1/discounts/validate",
  "error": "Not Found",
  "message": "Discount code not found or inactive"
}
```

**Response (chưa đến ngày hiệu lực):**
```json
{
  "timestamp": "2026-03-11 12:00:00",
  "status": 409,
  "path": "/api/v1/discounts/validate",
  "error": "Conflict",
  "message": "Discount code is not yet active"
}
```

---

### 8.12 Blog

#### Blog Topics

Base: `/api/v1/blog-topics`

#### 🔓 GET `/blog-topics` — Lấy tất cả chủ đề

**Response:**
```json
{
  "status": 200,
  "message": "Topics fetched",
  "data": [
    { "id": 1, "name": "Ẩm thực Tết" },
    { "id": 2, "name": "Phong tục Tết" },
    { "id": 3, "name": "Quà Tết ý nghĩa" }
  ]
}
```

---

#### 🔐 POST `/blog-topics` — Tạo chủ đề (ADMIN)

**Request:**
```json
{
  "name": "Ẩm thực Tết"
}
```

**Response:**
```json
{
  "status": 201,
  "message": "Topic created",
  "data": {
    "id": 1,
    "name": "Ẩm thực Tết"
  }
}
```

#### 🔐 PUT `/blog-topics/{id}` — Cập nhật chủ đề (ADMIN)

**Request:** *(giống POST)*

#### 🔐 DELETE `/blog-topics/{id}` — Xóa chủ đề (ADMIN)

---

#### Blog Posts

Base: `/api/v1/blogs`

#### 🔓 GET `/blogs?page=0&size=10` — Lấy danh sách bài viết

**Response:**
```json
{
  "status": 200,
  "message": "Blogs fetched",
  "data": {
    "pageNo": 0,
    "pageSize": 10,
    "totalPages": 1,
    "totalItems": 3,
    "data": [
      {
        "id": 1,
        "title": "Top 10 Món Quà Tết Ý Nghĩa 2026",
        "content": "<p>Tết Nguyên Đán...</p>",
        "topicName": "Quà Tết ý nghĩa",
        "topicId": 3,
        "createdAt": "2026-02-15T10:00:00"
      }
    ]
  }
}
```

---

#### 🔓 GET `/blogs/{id}` — Lấy chi tiết bài viết

**Response:**
```json
{
  "status": 200,
  "message": "Blog fetched",
  "data": {
    "id": 1,
    "title": "Top 10 Món Quà Tết Ý Nghĩa 2026",
    "content": "<p>Tết Nguyên Đán là dịp...</p>",
    "topicName": "Quà Tết ý nghĩa",
    "topicId": 3,
    "createdAt": "2026-02-15T10:00:00"
  }
}
```

---

#### 🔐 POST `/blogs` — Tạo bài viết (ADMIN)

**Request:**
```json
{
  "title": "Top 10 Món Quà Tết Ý Nghĩa 2026",
  "content": "<p>Tết Nguyên Đán là dịp...</p>",
  "topicId": 3
}
```

> `content` và `topicId` đều **optional**.

**Response:**
```json
{
  "status": 201,
  "message": "Blog created",
  "data": {
    "id": 1,
    "title": "Top 10 Món Quà Tết Ý Nghĩa 2026",
    "content": "<p>Tết Nguyên Đán là dịp...</p>",
    "topicName": "Quà Tết ý nghĩa",
    "topicId": 3,
    "createdAt": "2026-03-11T10:00:00"
  }
}
```

#### 🔐 PUT `/blogs/{id}` — Cập nhật bài viết (ADMIN)

**Request:** *(giống POST)*

#### 🔐 DELETE `/blogs/{id}` — Xóa bài viết (ADMIN)

---

### 8.13 Product Review

#### 🔓 GET `/products/{productId}/reviews?page=0&size=10` — Lấy đánh giá sản phẩm

**Response:**
```json
{
  "status": 200,
  "message": "Reviews fetched",
  "data": {
    "pageNo": 0,
    "pageSize": 10,
    "totalPages": 1,
    "totalItems": 3,
    "data": [
      {
        "id": 1,
        "productId": 1,
        "userId": 5,
        "userName": "Nguyễn Văn A",
        "rating": 5,
        "comment": "Bánh rất ngon, giao hàng nhanh!",
        "createdAt": "2026-02-20T14:30:00"
      }
    ]
  }
}
```

---

#### 🔐 POST `/products/{productId}/reviews` — Tạo đánh giá

> [!WARNING]
> Mỗi user chỉ được **đánh giá 1 lần** cho mỗi sản phẩm.

**Request:**
```json
{
  "rating": 5,
  "comment": "Bánh rất ngon, giao hàng nhanh!"
}
```

> `rating` **bắt buộc**, giá trị từ **1 đến 5**.  
> `comment` là **optional**.

**Response:**
```json
{
  "status": 201,
  "message": "Review created",
  "data": {
    "id": 1,
    "productId": 1,
    "userId": 5,
    "userName": "Nguyễn Văn A",
    "rating": 5,
    "comment": "Bánh rất ngon, giao hàng nhanh!",
    "createdAt": "2026-02-20T14:30:00"
  }
}
```

---

#### 🔐 PUT `/reviews/{reviewId}` — Sửa đánh giá (chỉ chủ sở hữu)

**Request:**
```json
{
  "rating": 4,
  "comment": "Cập nhật: Bánh vẫn ngon nhưng đóng gói hơi đơn giản"
}
```

**Response:**
```json
{
  "status": 200,
  "message": "Review updated",
  "data": {
    "id": 1,
    "productId": 1,
    "userId": 5,
    "userName": "Nguyễn Văn A",
    "rating": 4,
    "comment": "Cập nhật: Bánh vẫn ngon nhưng đóng gói hơi đơn giản",
    "createdAt": "2026-02-20T14:30:00"
  }
}
```

#### 🔐 DELETE `/reviews/{reviewId}` — Xóa đánh giá (chỉ chủ sở hữu)

**Response:**
```json
{
  "status": 200,
  "message": "Review deleted"
}
```

---

### 8.14 AI Chatbot (RAG)

Base: `/api/v1/chatbot`

> [!NOTE]
> Chatbot sử dụng RAG (Retrieval-Augmented Generation) để tư vấn sản phẩm dựa trên dữ liệu thực, **không bịa thông tin**.

#### 🔓 POST `/chatbot/chat` — Chat với AI

**Request:**
```json
{
  "message": "Tôi cần tìm quà Tết dưới 500k cho bố mẹ",
  "sessionId": "abc123-uuid" 
}
```

> `sessionId` là **optional**. Nếu không truyền, server tự tạo session mới.  
> Nếu user đã đăng nhập, session sẽ được liên kết với tài khoản.

**Response:**
```json
{
  "status": 200,
  "message": "Chat processed successfully",
  "data": {
    "sessionId": "abc123-uuid",
    "message": "Dạ, shop có nhiều sản phẩm phù hợp với ngân sách dưới 500.000đ để tặng bố mẹ ạ:\n\n1. **Hộp bánh Tết Truyền Thống** - 280.000đ - Còn 200 sản phẩm\n2. **Trà Ô Long Đài Loan** - 350.000đ - Còn 120 sản phẩm\n...",
    "timestamp": "2026-03-11T14:30:00",
    "detectedIntent": "PRODUCT_SEARCH",
    "success": true,
    "suggestions": [
      {
        "id": 3,
        "type": "PRODUCT",
        "name": "Hộp bánh Tết Truyền Thống",
        "price": "280000",
        "stock": 200,
        "imageUrl": "https://res.cloudinary.com/xxx/image.jpg"
      },
      {
        "id": 7,
        "type": "PRODUCT",
        "name": "Trà Ô Long Đài Loan",
        "price": "350000",
        "stock": 120,
        "imageUrl": null
      }
    ]
  }
}
```

**Các loại Intent được phát hiện:**

| Intent | Mô tả |
|---|---|
| `PRODUCT_SEARCH` | Tìm sản phẩm theo giá, danh mục, từ khóa |
| `BUNDLE_SEARCH` | Tìm combo/giỏ quà |
| `CATEGORY_BROWSE` | Duyệt theo danh mục |
| `STOCK_CHECK` | Hỏi tồn kho, còn hàng không |
| `DISCOUNT_POLICY` | Hỏi giảm giá, khuyến mãi |
| `SHOP_INFO` | Thông tin cửa hàng, liên hệ |
| `GENERAL_CHAT` | Câu hỏi chung khác |

**Ví dụ câu hỏi:**
- "Có sản phẩm nào dưới 500k không?"
- "Cho tôi xem các combo quà Tết"
- "Bánh chưng còn hàng không?"
- "Mua nhiều có được giảm giá không?"
- "Hotline của shop là gì?"

---

#### 🔓 GET `/chatbot/history/{sessionId}` — Lấy lịch sử chat

**Response:**
```json
{
  "status": 200,
  "message": "History fetched successfully",
  "data": [
    {
      "id": 1,
      "role": "USER",
      "content": "Có sản phẩm nào dưới 500k không?",
      "intent": null,
      "createdAt": "2026-03-11T14:30:00"
    },
    {
      "id": 2,
      "role": "ASSISTANT",
      "content": "Dạ, shop có nhiều sản phẩm phù hợp...",
      "intent": "PRODUCT_SEARCH",
      "createdAt": "2026-03-11T14:30:02"
    }
  ]
}
```

---

#### 🔐 POST `/chatbot/embeddings/sync` — Đồng bộ embeddings (ADMIN)

> Endpoint này dùng để re-generate embeddings cho tất cả sản phẩm và combo.  
> Chạy sau khi thêm/sửa nhiều sản phẩm để AI tìm kiếm chính xác hơn.

**Response:**
```json
{
  "status": 200,
  "message": "Embeddings synced successfully",
  "data": {
    "productsEmbedded": 50,
    "bundlesEmbedded": 10
  }
}
```

---

## 9. WebSocket — Realtime

### Kết nối (SockJS + STOMP)

```javascript
// Dùng sockjs-client + stompjs
const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
  // Subscribe order status của user hiện tại
  stompClient.subscribe('/user/{username}/queue/order-status', function(message) {
    console.log('Order update:', message.body);
  });
});
```

### Subscribe Channels

| Channel | Mô tả |
|---|---|
| `/user/{username}/queue/order-status` | Nhận thông báo cập nhật trạng thái đơn hàng |
| `/topic/...` | Broadcast toàn bộ (chưa dùng) |

> `{username}` là username của user đang đăng nhập.

### Message nhận được

Khi admin update trạng thái đơn hàng, user sẽ nhận message dạng:

```
Order #1 status updated to PROCESSING
```

---

## 10. Tổng hợp nhanh — Tất cả Endpoints

| # | Method | URL | Auth | Role | Mô tả |
|---|---|---|---|---|---|
| **Auth** |
| 1 | POST | `/auth/login` | 🔓 | - | Đăng nhập |
| 2 | POST | `/auth/refresh-token` | 🔓 | - | Refresh token |
| 3 | POST | `/auth/logout` | 🔐 | Any | Đăng xuất |
| 4 | POST | `/auth/forgot-password` | 🔓 | - | Gửi token reset qua email |
| 5 | POST | `/auth/reset-password` | 🔓 | - | Đặt lại mật khẩu |
| 6 | POST | `/auth/change-password` | 🔐 | Any | Đổi mật khẩu |
| 7 | POST | `/auth/verify-otp` | 🔓 | - | Xác thực OTP |
| 8 | POST | `/auth/resend-otp` | 🔓 | - | Gửi lại OTP |
| **User** |
| 9 | POST | `/user/register` | 🔓 | - | Đăng ký |
| 10 | GET | `/user` | 🔐 | ADMIN | Danh sách users |
| 11 | GET | `/user/{id}` | 🔐 | Any | Chi tiết user |
| 12 | PUT | `/user/{id}` | 🔐 | Any | Cập nhật user |
| 13 | DELETE | `/user/{id}` | 🔐 | ADMIN | Xóa user |
| **Role** |
| 14 | POST | `/role` | 🔐 | ADMIN | Tạo role |
| 15 | GET | `/role` | 🔐 | Any | Tất cả roles |
| 16 | GET | `/role/{id}` | 🔐 | Any | Chi tiết role |
| 17 | PUT | `/role?id={id}` | 🔐 | ADMIN | Cập nhật role |
| 18 | DELETE | `/role?id={id}` | 🔐 | ADMIN | Xóa role |
| **Address** |
| 19 | POST | `/addresses` | 🔐 | Any | Tạo địa chỉ |
| 20 | GET | `/addresses` | 🔐 | Any | Danh sách địa chỉ của tôi |
| 21 | GET | `/addresses/{id}` | 🔐 | Any | Chi tiết địa chỉ |
| 22 | PUT | `/addresses/{id}` | 🔐 | Any | Cập nhật địa chỉ |
| 23 | DELETE | `/addresses/{id}` | 🔐 | Any | Xóa địa chỉ |
| 24 | PUT | `/addresses/{id}/default` | 🔐 | Any | Đặt mặc định |
| **Category** |
| 25 | GET | `/categories` | 🔓 | - | Tất cả danh mục |
| 26 | GET | `/categories/{id}` | 🔓 | - | Chi tiết danh mục |
| 27 | POST | `/categories` | 🔐 | Any | Tạo danh mục |
| 28 | PUT | `/categories/{id}` | 🔐 | Any | Cập nhật danh mục |
| 29 | DELETE | `/categories/{id}` | 🔐 | Any | Xóa danh mục |
| **Product** |
| 30 | GET | `/products` | 🔓 | - | Danh sách sản phẩm |
| 31 | GET | `/products/{id}` | 🔓 | - | Chi tiết sản phẩm |
| 32 | POST | `/products/register` | 🔐 | ADMIN | Tạo sản phẩm |
| 33 | PUT | `/products/{id}` | 🔐 | ADMIN | Cập nhật sản phẩm |
| 34 | DELETE | `/products/{id}` | 🔐 | ADMIN | Xóa sản phẩm |
| **Bundle** |
| 35 | GET | `/bundles` | 🔓 | - | Danh sách combo |
| 36 | GET | `/bundles/{id}` | 🔓 | - | Chi tiết combo |
| 37 | POST | `/bundles` | 🔐 | Any | Tạo combo |
| 38 | PUT | `/bundles/{id}` | 🔐 | Any | Cập nhật combo |
| 39 | DELETE | `/bundles/{id}` | 🔐 | Any | Xóa combo |
| **Cart** |
| 40 | GET | `/cart` | 🔐 | Any | Lấy giỏ hàng |
| 41 | POST | `/cart/items` | 🔐 | Any | Thêm item |
| 42 | PUT | `/cart/items/{id}?quantity=N` | 🔐 | Any | Cập nhật số lượng |
| 43 | DELETE | `/cart/items/{id}` | 🔐 | Any | Xóa item |
| 44 | DELETE | `/cart` | 🔐 | Any | Xóa giỏ hàng |
| **Order** |
| 45 | POST | `/orders` | 🔐 | Any | Tạo đơn hàng |
| 46 | GET | `/orders` | 🔐 | Any | Đơn hàng của tôi |
| 47 | GET | `/orders/{id}` | 🔐 | Any | Chi tiết đơn |
| 48 | PUT | `/orders/{id}/cancel` | 🔐 | Any | Hủy đơn |
| 49 | PUT | `/orders/{id}/status?status=X` | 🔐 | ADMIN | Cập nhật trạng thái |
| **Payment** |
| 50 | POST | `/payments/create` | 🔐 | Any | Tạo thanh toán |
| 51 | GET | `/payments/vnpay-callback` | 🔓 | - | VNPay callback (do VNPay gọi) |
| 52 | GET | `/payments/{orderId}` | 🔐 | Any | Thanh toán theo order |
| **Discount** |
| 53 | POST | `/discounts` | 🔐 | ADMIN | Tạo mã giảm giá |
| 54 | GET | `/discounts` | 🔐 | ADMIN | Tất cả mã |
| 55 | GET | `/discounts/{id}` | 🔐 | Any | Chi tiết mã |
| 56 | PUT | `/discounts/{id}` | 🔐 | ADMIN | Cập nhật mã |
| 57 | DELETE | `/discounts/{id}` | 🔐 | ADMIN | Xóa mã |
| 58 | POST | `/discounts/validate?code=X` | 🔐 | Any | Kiểm tra mã (query param) |
| **Blog** |
| 59 | GET | `/blog-topics` | 🔓 | - | Tất cả chủ đề |
| 60 | POST | `/blog-topics` | 🔐 | Any | Tạo chủ đề |
| 61 | PUT | `/blog-topics/{id}` | 🔐 | Any | Cập nhật chủ đề |
| 62 | DELETE | `/blog-topics/{id}` | 🔐 | Any | Xóa chủ đề |
| 63 | GET | `/blogs` | 🔓 | - | Danh sách bài viết |
| 64 | GET | `/blogs/{id}` | 🔓 | - | Chi tiết bài viết |
| 65 | POST | `/blogs` | 🔐 | Any | Tạo bài viết |
| 66 | PUT | `/blogs/{id}` | 🔐 | Any | Cập nhật bài viết |
| 67 | DELETE | `/blogs/{id}` | 🔐 | Any | Xóa bài viết |
| **Review** |
| 68 | GET | `/products/{id}/reviews` | 🔓 | - | Đánh giá sản phẩm |
| 69 | POST | `/products/{id}/reviews` | 🔐 | Any | Tạo đánh giá |
| 70 | PUT | `/reviews/{id}` | 🔐 | Any | Sửa đánh giá (owner) |
| 71 | DELETE | `/reviews/{id}` | 🔐 | Any | Xóa đánh giá (owner) |
| **AI Chatbot** |
| 72 | POST | `/chatbot/chat` | 🔓 | - | Chat với AI tư vấn |
| 73 | GET | `/chatbot/history/{sessionId}` | 🔓 | - | Lịch sử chat |
| 74 | POST | `/chatbot/embeddings/sync` | 🔐 | ADMIN | Đồng bộ embeddings |

> 🔓 = Public (không cần token) | 🔐 = Cần Bearer Token | ADMIN = Cần role ADMIN | Any = Bất kỳ authenticated user
