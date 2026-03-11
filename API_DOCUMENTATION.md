# 🎁 Tet Gift Commerce API — Frontend Integration Guide

> **Cập nhật:** 2026-03-07  
> **Version:** 2.0  
> **Base URL:** `http://localhost:8080/api/v1`  
> **Swagger UI:** `http://localhost:8080/swagger-ui.html`

---

## 📋 Mục lục

1. [Server & Cấu hình](#1-server--cấu-hình)
2. [Authentication & Headers](#2-authentication--headers)
3. [Response Wrapper](#3-response-wrapper)
4. [Pagination](#4-pagination)
5. [Error Handling](#5-error-handling)
6. [Enums](#6-enums)
7. [API Endpoints](#7-api-endpoints)
   - [7.1 Authentication](#71-authentication)
   - [7.2 User](#72-user)
   - [7.3 Role (ADMIN)](#73-role-admin)
   - [7.4 Address](#74-address)
   - [7.5 Category](#75-category)
   - [7.6 Product](#76-product)
   - [7.7 Bundle](#77-bundle)
   - [7.8 Cart](#78-cart)
   - [7.9 Order](#79-order)
   - [7.10 Payment](#710-payment)
   - [7.11 Discount](#711-discount)
   - [7.12 Blog](#712-blog)
   - [7.13 Product Review](#713-product-review)

---

## 1. Server & Cấu hình

| Thuộc tính | Giá trị |
|---|---|
| **Port** | `8080` |
| **Base URL** | `http://localhost:8080` |
| **API Prefix** | `/api/v1` |
| **Swagger UI** | `http://localhost:8080/swagger-ui.html` |
| **API Docs** | `http://localhost:8080/v3/api-docs` |
| **WebSocket** | `ws://localhost:8080/ws` |

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

### Environment Variables cần thiết (cho Backend)

```env
# Database
DATASOURCE_URL=jdbc:postgresql://localhost:5432/tetgift
POSTGRES_USER=postgres
POSTGRES_PASSWORD=your_password

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# JWT
JWT_SECRET=your_jwt_secret
JWT_REFRESH_SECRET=your_refresh_secret
JWT_RESET_PASSWORD_SECRET=your_reset_secret
JWT_TIME_OUT=3600000

# VNPay
VNPAY_TMN_CODE=your_tmn_code
VNPAY_SECRET_KEY=your_secret
VNPAY_PAY_URL=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
VNPAY_RETURN_URL=http://localhost:3000/payment/callback

# OAuth2
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
```

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
| Callback | `http://localhost:3000/oauth2/redirect?token=<jwt>&email=<email>` |

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
  "timestamp": "2026-03-07 12:00:00",
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
| `401` | Chưa đăng nhập / Token hết hạn |
| `403` | Không có quyền truy cập |
| `404` | Không tìm thấy tài nguyên |
| `409` | Xung đột dữ liệu (duplicate, logic error) |
| `500` | Lỗi server |

---

## 6. Enums

### OrderStatus

```
CREATED → WAITING_PAYMENT → PAID → PROCESSING → SHIPPED → COMPLETED
                                                         → CANCELLED
```

| Value | Mô tả |
|---|---|
| `CREATED` | Đơn hàng vừa tạo |
| `WAITING_PAYMENT` | Đang chờ thanh toán |
| `PAID` | Đã thanh toán |
| `PROCESSING` | Đang xử lý |
| `SHIPPED` | Đang giao hàng |
| `COMPLETED` | Hoàn thành |
| `CANCELLED` | Đã hủy |

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

## 7. API Endpoints

---

### 7.1 Authentication

Base: `/api/v1/auth`

#### 🔓 POST `/auth/login` — Đăng nhập

**Request:**
```json
{
  "usernameOrEmail": "admin@tetgift.com",
  "password": "12345678"
}
```

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
  "data": "<reset_token>"
}
```

---

#### 🔓 POST `/auth/reset-password` — Đặt lại mật khẩu

**Request:**
```json
{
  "token": "<reset_token>",
  "newPassword": "newpassword123",
  "confirmPassword": "newpassword123"
}
```

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

**Request:**
```json
{
  "oldPassword": "currentpass123",
  "newPassword": "newpassword123",
  "confirmPassword": "newpassword123"
}
```

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

---

### 7.2 User

Base: `/api/v1/user`

#### 🔓 POST `/user/register` — Đăng ký tài khoản

**Request:**
```json
{
  "email": "user@example.com",
  "password": "12345678",
  "fullName": "Nguyễn Văn A",
  "phone": "0901234567",
  "username": "nguyenvana"
}
```

**Response:**
```json
{
  "status": 201,
  "message": "User registered successfully",
  "data": 1
}
```

---

#### 🔐 GET `/user?page=0&size=10&sortBy=createdAt&sortDir=desc` — Lấy danh sách users

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
        "roleName": "USER"
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
    "roleName": "USER"
  }
}
```

---

#### 🔐 PUT `/user/{id}` — Cập nhật user

**Request:**
```json
{
  "fullName": "Nguyễn Văn B",
  "phone": "0987654321"
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

---

#### 🔐 DELETE `/user/{id}` — Xóa user

**Response:**
```json
{
  "status": 200,
  "message": "User deleted successfully",
  "data": "User with ID 1 has been deleted"
}
```

---

### 7.3 Role (ADMIN)

Base: `/api/v1/role`

#### 🔐 POST `/role` — Tạo role

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

#### 🔐 PUT `/role?id=1` — Cập nhật role

**Request:**
```json
{
  "name": "MODERATOR",
  "description": "Moderator role"
}
```

#### 🔐 DELETE `/role?id=1` — Xóa role

---

### 7.4 Address

Base: `/api/v1/addresses`

> [!IMPORTANT]
> Tất cả endpoints cần **Bearer Token**. User chỉ xem/sửa/xóa được address **của mình**.

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

#### 🔐 PUT `/addresses/{id}` — Cập nhật địa chỉ

**Request:** *(giống POST)*

#### 🔐 DELETE `/addresses/{id}` — Xóa địa chỉ

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

### 7.5 Category

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

#### 🔐 POST `/categories` — Tạo danh mục

**Request:**
```json
{
  "name": "Trái cây nhập khẩu",
  "description": "Trái cây cao cấp nhập khẩu"
}
```

#### 🔐 PUT `/categories/{id}` — Cập nhật danh mục

**Request:** *(giống POST)*

#### 🔐 DELETE `/categories/{id}` — Xóa danh mục (soft delete)

---

### 7.6 Product

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
          },
          {
            "id": 2,
            "imageUrl": "https://res.cloudinary.com/xxx/image2.jpg",
            "imageType": "jpg",
            "publicId": "products/banh-chung-2",
            "isPrimary": false
          }
        ]
      }
    ]
  }
}
```

---

#### 🔓 GET `/products/{id}` — Lấy chi tiết sản phẩm

**Response:** *(cấu trúc giống 1 item trong list trên)*

---

#### 🔐 POST `/products/register` — Tạo sản phẩm mới

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

**Response:**
```json
{
  "status": 201,
  "message": "Product registered successfully",
  "data": 1
}
```

---

#### 🔐 PUT `/products/{id}` — Cập nhật sản phẩm

**Request:** *(giống POST)*

#### 🔐 DELETE `/products/{id}` — Xóa sản phẩm (soft delete)

---

### 7.7 Bundle

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

#### 🔐 POST `/bundles` — Tạo combo mới

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

**Response:**
```json
{
  "status": 201,
  "message": "Bundle created successfully",
  "data": 1
}
```

#### 🔐 PUT `/bundles/{id}` — Cập nhật combo

**Request:** *(giống POST)*

#### 🔐 DELETE `/bundles/{id}` — Xóa combo (soft delete)

---

### 7.8 Cart

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

---

#### 🔐 PUT `/cart/items/{itemId}?quantity=3` — Cập nhật số lượng

> Nếu `quantity <= 0`, item sẽ bị xóa khỏi giỏ hàng.

---

#### 🔐 DELETE `/cart/items/{itemId}` — Xóa item khỏi giỏ

#### 🔐 DELETE `/cart` — Xóa toàn bộ giỏ hàng

---

### 7.9 Order

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

> [!TIP]
> Các field VAT là **tùy chọn**. Nếu không cần xuất hóa đơn VAT, bỏ qua.
> 
> `discountCode` cũng **tùy chọn**. Nếu có, sẽ trừ giá trị discount vào tổng đơn.

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
    "createdAt": "2026-03-07T12:00:00"
  }
}
```

> [!NOTE]
> Sau khi tạo đơn, **giỏ hàng sẽ bị xóa sạch** tự động.
> 
> `priceSnapshot` là giá tại thời điểm đặt hàng, **KHÔNG** thay đổi khi sản phẩm đổi giá.

---

#### 🔐 GET `/orders?page=0&size=10` — Lấy danh sách đơn hàng của tôi

#### 🔐 GET `/orders/{id}` — Lấy chi tiết đơn hàng

#### 🔐 PUT `/orders/{id}/cancel` — Hủy đơn hàng

> Chỉ hủy được khi status là `CREATED` hoặc `WAITING_PAYMENT`

#### 🔐 PUT `/orders/{id}/status?status=PROCESSING` — Cập nhật trạng thái (ADMIN)

> Giá trị status hợp lệ: `CREATED`, `WAITING_PAYMENT`, `PAID`, `PROCESSING`, `SHIPPED`, `COMPLETED`, `CANCELLED`

---

### 7.10 Payment

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
    "paymentUrl": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_Amount=70000000&vnp_Command=pay&..."
  }
}
```

> [!TIP]
> Với VNPay, redirect user tới `paymentUrl` để thanh toán. Sau khi thanh toán xong, VNPay sẽ redirect về `VNPAY_RETURN_URL`.

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

> Endpoint này được VNPay gọi tự động. Frontend **KHÔNG** cần gọi trực tiếp.
> 
> `vnp_ResponseCode = "00"` nghĩa là thành công.

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
    "paidAt": "2026-03-07T12:05:00",
    "paymentUrl": null
  }
}
```

---

### 7.11 Discount

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

#### 🔐 POST `/discounts/validate?code=TET2026` — Kiểm tra mã giảm giá

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

**Response (không hợp lệ):**
```json
{
  "timestamp": "2026-03-07 12:00:00",
  "status": 404,
  "path": "/api/v1/discounts/validate",
  "error": "Not Found",
  "message": "Discount code not found or inactive"
}
```

---

### 7.12 Blog

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

#### 🔐 POST `/blog-topics` — Tạo chủ đề (ADMIN)

**Request:**
```json
{
  "name": "Ẩm thực Tết"
}
```

#### 🔐 PUT `/blog-topics/{id}` — Cập nhật chủ đề (ADMIN)

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

#### 🔐 POST `/blogs` — Tạo bài viết (ADMIN)

**Request:**
```json
{
  "title": "Top 10 Món Quà Tết Ý Nghĩa 2026",
  "content": "<p>Tết Nguyên Đán là dịp...</p>",
  "topicId": 3
}
```

#### 🔐 PUT `/blogs/{id}` — Cập nhật bài viết (ADMIN)

**Request:** *(giống POST)*

#### 🔐 DELETE `/blogs/{id}` — Xóa bài viết (ADMIN)

---

### 7.13 Product Review

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
      },
      {
        "id": 2,
        "productId": 1,
        "userId": 8,
        "userName": "Trần Thị B",
        "rating": 4,
        "comment": "Chất lượng tốt",
        "createdAt": "2026-02-19T10:00:00"
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

#### 🔐 DELETE `/reviews/{reviewId}` — Xóa đánh giá (chỉ chủ sở hữu)

---

## 📌 WebSocket — Realtime Order Status

### Kết nối

```
ws://localhost:8080/ws
```

### Subscribe channel

```
/user/{username}/queue/order-status
```

### Nhận message

Khi admin update trạng thái đơn hàng, user sẽ nhận:

```
Order #1 status updated to PROCESSING
```

---

## 📌 Tổng hợp nhanh — Tất cả Endpoints

| # | Method | URL | Auth | Mô tả |
|---|---|---|---|---|
| **Auth** |
| 1 | POST | `/auth/login` | 🔓 | Đăng nhập |
| 2 | POST | `/auth/refresh-token` | 🔓 | Refresh token |
| 3 | POST | `/auth/logout` | 🔐 | Đăng xuất |
| 4 | POST | `/auth/forgot-password` | 🔓 | Quên mật khẩu |
| 5 | POST | `/auth/reset-password` | 🔓 | Đặt lại mật khẩu |
| 6 | POST | `/auth/change-password` | 🔐 | Đổi mật khẩu |
| 7 | POST | `/auth/verify-otp` | 🔓 | Xác thực OTP |
| 8 | POST | `/auth/resend-otp` | 🔓 | Gửi lại OTP |
| **User** |
| 9 | POST | `/user/register` | 🔓 | Đăng ký |
| 10 | GET | `/user` | 🔐 | Danh sách users |
| 11 | GET | `/user/{id}` | 🔐 | Chi tiết user |
| 12 | PUT | `/user/{id}` | 🔐 | Cập nhật user |
| 13 | DELETE | `/user/{id}` | 🔐 | Xóa user |
| **Role** |
| 14 | POST | `/role` | 🔐 | Tạo role |
| 15 | GET | `/role` | 🔐 | Tất cả roles |
| 16 | GET | `/role/{id}` | 🔐 | Chi tiết role |
| 17 | PUT | `/role?id={id}` | 🔐 | Cập nhật role |
| 18 | DELETE | `/role?id={id}` | 🔐 | Xóa role |
| **Address** |
| 19 | POST | `/addresses` | 🔐 | Tạo địa chỉ |
| 20 | GET | `/addresses` | 🔐 | Danh sách địa chỉ |
| 21 | GET | `/addresses/{id}` | 🔐 | Chi tiết địa chỉ |
| 22 | PUT | `/addresses/{id}` | 🔐 | Cập nhật địa chỉ |
| 23 | DELETE | `/addresses/{id}` | 🔐 | Xóa địa chỉ |
| 24 | PUT | `/addresses/{id}/default` | 🔐 | Đặt mặc định |
| **Category** |
| 25 | GET | `/categories` | 🔓 | Tất cả danh mục |
| 26 | GET | `/categories/{id}` | 🔓 | Chi tiết danh mục |
| 27 | POST | `/categories` | 🔐 | Tạo danh mục |
| 28 | PUT | `/categories/{id}` | 🔐 | Cập nhật danh mục |
| 29 | DELETE | `/categories/{id}` | 🔐 | Xóa danh mục |
| **Product** |
| 30 | GET | `/products` | 🔓 | Danh sách sản phẩm |
| 31 | GET | `/products/{id}` | 🔓 | Chi tiết sản phẩm |
| 32 | POST | `/products/register` | 🔐 | Tạo sản phẩm |
| 33 | PUT | `/products/{id}` | 🔐 | Cập nhật sản phẩm |
| 34 | DELETE | `/products/{id}` | 🔐 | Xóa sản phẩm |
| **Bundle** |
| 35 | GET | `/bundles` | 🔓 | Danh sách combo |
| 36 | GET | `/bundles/{id}` | 🔓 | Chi tiết combo |
| 37 | POST | `/bundles` | 🔐 | Tạo combo |
| 38 | PUT | `/bundles/{id}` | 🔐 | Cập nhật combo |
| 39 | DELETE | `/bundles/{id}` | 🔐 | Xóa combo |
| **Cart** |
| 40 | GET | `/cart` | 🔐 | Lấy giỏ hàng |
| 41 | POST | `/cart/items` | 🔐 | Thêm item |
| 42 | PUT | `/cart/items/{id}?quantity=N` | 🔐 | Cập nhật số lượng |
| 43 | DELETE | `/cart/items/{id}` | 🔐 | Xóa item |
| 44 | DELETE | `/cart` | 🔐 | Xóa giỏ hàng |
| **Order** |
| 45 | POST | `/orders` | 🔐 | Tạo đơn hàng |
| 46 | GET | `/orders` | 🔐 | Đơn hàng của tôi |
| 47 | GET | `/orders/{id}` | 🔐 | Chi tiết đơn |
| 48 | PUT | `/orders/{id}/cancel` | 🔐 | Hủy đơn |
| 49 | PUT | `/orders/{id}/status?status=X` | 🔐 | Cập nhật trạng thái |
| **Payment** |
| 50 | POST | `/payments/create` | 🔐 | Tạo thanh toán |
| 51 | GET | `/payments/vnpay-callback` | 🔓 | VNPay callback |
| 52 | GET | `/payments/{orderId}` | 🔐 | Thanh toán theo order |
| **Discount** |
| 53 | POST | `/discounts` | 🔐 | Tạo mã giảm giá |
| 54 | GET | `/discounts` | 🔐 | Tất cả mã |
| 55 | GET | `/discounts/{id}` | 🔐 | Chi tiết mã |
| 56 | PUT | `/discounts/{id}` | 🔐 | Cập nhật mã |
| 57 | DELETE | `/discounts/{id}` | 🔐 | Xóa mã |
| 58 | POST | `/discounts/validate?code=X` | 🔐 | Kiểm tra mã |
| **Blog** |
| 59 | GET | `/blog-topics` | 🔓 | Tất cả chủ đề |
| 60 | POST | `/blog-topics` | 🔐 | Tạo chủ đề |
| 61 | PUT | `/blog-topics/{id}` | 🔐 | Cập nhật chủ đề |
| 62 | DELETE | `/blog-topics/{id}` | 🔐 | Xóa chủ đề |
| 63 | GET | `/blogs` | 🔓 | Danh sách bài viết |
| 64 | GET | `/blogs/{id}` | 🔓 | Chi tiết bài viết |
| 65 | POST | `/blogs` | 🔐 | Tạo bài viết |
| 66 | PUT | `/blogs/{id}` | 🔐 | Cập nhật bài viết |
| 67 | DELETE | `/blogs/{id}` | 🔐 | Xóa bài viết |
| **Review** |
| 68 | GET | `/products/{id}/reviews` | 🔓 | Đánh giá sản phẩm |
| 69 | POST | `/products/{id}/reviews` | 🔐 | Tạo đánh giá |
| 70 | PUT | `/reviews/{id}` | 🔐 | Sửa đánh giá |
| 71 | DELETE | `/reviews/{id}` | 🔐 | Xóa đánh giá |

> 🔓 = Public (không cần token) | 🔐 = Cần Bearer Token
