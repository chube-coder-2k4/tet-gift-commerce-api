# 🎁 Tet Gift Commerce API - Frontend Integration Guide.

> **Base URL**: `http://localhost:8080/api/v1`
> **Swagger UI**: `http://localhost:8080/swagger-ui/index.html`
> **Auth**: JWT Bearer Token (header `Authorization: Bearer <access_token>`)
> **Refresh Token**: header `x-refresh-token: <refresh_token>`

---

## 📦 Response Format:

### Success Response
```json
{
  "status": 200,
  "message": "Success message",
  "data": { ... }
}
```

### Error Response (HTTP 400/401/403/404/500)
```json
{
  "timestamp": "2026-03-15T00:00:00",
  "status": 400,
  "error": "Invalid Payload",
  "message": "email must not be blank",
  "path": "/api/v1/user/register"
}
```

### Paginated Response
```json
{
  "status": 200,
  "message": "...",
  "data": {
    "pageNo": 0,
    "pageSize": 10,
    "totalPages": 5,
    "totalItems": 50,
    "data": [ ... ]
  }
}
```

---

## 🔐 1. Authentication Module:

**Base**: `/api/v1/auth`

### 1.1 Login
```
POST /api/v1/auth/login
Auth: ❌ None
```

**Request Body:**
```json
{
  "usernameOrEmail": "string (required)",
  "password": "string (required, min 6 chars)"
}
```

**Response:**
```json
{
  "status": 200,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGci...",
    "refreshToken": "eyJhbGci...",
    "userId": 1
  }
}
```

### 1.2 Register User
```
POST /api/v1/user/register
Auth: ❌ None
```

**Request Body:**
```json
{
  "fullName": "string (required, 2-100 chars)",
  "email": "string (required, valid email)",
  "password": "string (required, min 8 chars, must contain: 1 digit, 1 lowercase, 1 uppercase, 1 special char @#$%^&+=)",
  "phone": "string (optional, VN phone format: 0xxxxxxxxx or 84xxxxxxxxx)",
  "username": "string (required, 3-50 chars, alphanumeric + . _ -)"
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

> ⚠️ Sau khi register, user cần verify OTP qua email trước khi login.

### 1.3 Verify OTP (Email Verification)
```
POST /api/v1/auth/verify-otp
Auth: ❌ None
```

**Request Body:**
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

### 1.4 Resend OTP
```
POST /api/v1/auth/resend-otp
Auth: ❌ None
```

**Request Body:**
```json
{
  "email": "user@example.com"
}
```

### 1.5 Refresh Token
```
POST /api/v1/auth/refresh-token
Auth: ❌ None
Headers: x-refresh-token: <refresh_token>
```

**Response:**
```json
{
  "status": 200,
  "message": "Token refreshed successfully",
  "data": {
    "accessToken": "new_access_token",
    "refreshToken": "new_refresh_token",
    "userId": 1
  }
}
```

### 1.6 Logout
```
POST /api/v1/auth/logout
Auth: ✅ Bearer Token
Headers: x-refresh-token: <refresh_token>
```

### 1.7 Forgot Password
```
POST /api/v1/auth/forgot-password
Auth: ❌ None
```

**Request Body:**
```json
{
  "email": "user@example.com"
}
```

### 1.8 Reset Password
```
POST /api/v1/auth/reset-password
Auth: ❌ None
```

**Request Body:**
```json
{
  "token": "reset_token_from_email",
  "newPassword": "string (min 8 chars)",
  "confirmPassword": "string (must match newPassword)"
}
```

### 1.9 Change Password
```
POST /api/v1/auth/change-password
Auth: ✅ Bearer Token
```

**Request Body:**
```json
{
  "oldPassword": "string (min 8 chars)",
  "newPassword": "string (min 8 chars)",
  "confirmPassword": "string (must match newPassword)"
}
```

---

## 👤 2. User Module

**Base**: `/api/v1/user`

### 2.1 Get User by ID
```
GET /api/v1/user/{id}
Auth: ✅ Bearer Token
```

**Response:**
```json
{
  "status": 200,
  "message": "Fetched user successfully",
  "data": {
    "id": 1,
    "fullName": "Nguyễn Văn A",
    "email": "user@example.com",
    "phone": "0912345678",
    "username": "user123",
    "roleName": "USER",
    "createdBy": null,
    "updatedBy": null,
    "createdAt": "2026-03-15T00:00:00",
    "updatedAt": "2026-03-15T00:00:00"
  }
}
```

### 2.2 Update User
```
PUT /api/v1/user/{id}
Auth: ✅ Bearer Token
```

**Request Body:**
```json
{
  "fullName": "string (optional)",
  "email": "string (optional, valid email)",
  "phone": "string (optional)",
  "username": "string (optional, 3-50 chars)"
}
```

### 2.3 Get All Users (ADMIN)
```
GET /api/v1/user?page=0&size=10&sortBy=createdAt&sortDir=desc
Auth: ✅ Bearer Token (ADMIN only)
```

**Response:** Paginated `UserResponse`

### 2.4 Delete User (ADMIN)
```
DELETE /api/v1/user/{id}
Auth: ✅ Bearer Token (ADMIN only)
```

---

## 📍 3. Address Module

**Base**: `/api/v1/addresses`
**Auth**: ✅ All endpoints require Bearer Token

### 3.1 Create Address
```
POST /api/v1/addresses
```

**Request Body:**
```json
{
  "receiverName": "string (required)",
  "phone": "string (required)",
  "addressDetail": "string (required)",
  "isDefault": false
}
```

**Response:**
```json
{
  "status": 201,
  "data": {
    "id": 1,
    "receiverName": "Nguyễn Văn A",
    "phone": "0912345678",
    "addressDetail": "123 Lê Lợi, Q1, TP.HCM",
    "isDefault": false
  }
}
```

### 3.2 Get My Addresses
```
GET /api/v1/addresses
```

**Response:** `List<AddressResponse>`

### 3.3 Get Address by ID
```
GET /api/v1/addresses/{id}
```

### 3.4 Update Address
```
PUT /api/v1/addresses/{id}
```

**Request Body:** Same as Create

### 3.5 Delete Address
```
DELETE /api/v1/addresses/{id}
```

### 3.6 Set Default Address
```
PUT /api/v1/addresses/{id}/default
```

---

## 📂 4. Category Module

**Base**: `/api/v1/categories`

### 4.1 Get All Categories
```
GET /api/v1/categories
Auth: ❌ None
```

**Response:**
```json
{
  "status": 200,
  "data": [
    {
      "id": 1,
      "name": "Bánh Kẹo",
      "description": "Bánh kẹo truyền thống Tết",
      "isActive": true
    }
  ]
}
```

### 4.2 Get Category by ID
```
GET /api/v1/categories/{id}
Auth: ❌ None
```

### 4.3 Create Category (ADMIN)
```
POST /api/v1/categories
Auth: ✅ Bearer Token
```

**Request Body:**
```json
{
  "name": "string (required, max 100 chars)",
  "description": "string (optional, max 1000 chars)"
}
```

### 4.4 Update Category (ADMIN)
```
PUT /api/v1/categories/{id}
Auth: ✅ Bearer Token
```

### 4.5 Delete Category (ADMIN)
```
DELETE /api/v1/categories/{id}
Auth: ✅ Bearer Token
```

---

## 🛍️ 5. Product Module

**Base**: `/api/v1/products`

### 5.1 Get All Products (PUBLIC)
```
GET /api/v1/products?page=0&size=10&sortBy=createdAt&sortDir=desc
Auth: ❌ None
```

**Response:**
```json
{
  "status": 200,
  "data": {
    "pageNo": 0,
    "pageSize": 10,
    "totalPages": 5,
    "totalItems": 50,
    "data": [
      {
        "id": 1,
        "name": "Mứt Dừa Tết",
        "description": "Mứt dừa truyền thống, thơm ngon",
        "price": 120000,
        "stock": 100,
        "categoryName": "Bánh Kẹo",
        "categoryId": 1,
        "isActive": true,
        "manufactureDate": "2026-01-01",
        "expDate": "2026-06-01",
        "images": [
          {
            "id": 1,
            "imageUrl": "https://res.cloudinary.com/xxx/image.jpg",
            "imageType": "MAIN",
            "publicId": "product_images/abc123",
            "isPrimary": true
          }
        ]
      }
    ]
  }
}
```

### 5.2 Get Product by ID (PUBLIC)
```
GET /api/v1/products/{id}
Auth: ❌ None
```

**Response:** Single `ProductResponse`

### 5.3 Create Product (ADMIN)
```
POST /api/v1/products/register
Auth: ✅ Bearer Token (ADMIN)
Content-Type: multipart/form-data
```

**Form Parts:**
- `request` (JSON):
```json
{
  "name": "string (required, max 255 chars)",
  "description": "string (optional, max 5000 chars)",
  "price": 120000,
  "stock": 100,
  "categoryId": 1,
  "manufactureDate": "2026-01-01",
  "expDate": "2026-06-01",
  "images": [
    {
      "imageUrl": "https://...",
      "imageType": "MAIN",
      "publicId": "abc123",
      "isPrimary": true
    }
  ]
}
```
- `images` (files): MultipartFile[] (optional)

### 5.4 Update Product (ADMIN)
```
PUT /api/v1/products/{id}
Auth: ✅ Bearer Token (ADMIN)
Content-Type: multipart/form-data
```

### 5.5 Delete Product (ADMIN)
```
DELETE /api/v1/products/{id}
Auth: ✅ Bearer Token (ADMIN)
```

---

## 🎁 6. Bundle (Gift Set) Module

**Base**: `/api/v1/bundles`

### 6.1 Get All Bundles (PUBLIC)
```
GET /api/v1/bundles?page=0&size=10&sortBy=createdAt&sortDir=desc
Auth: ❌ None
```

**Response:**
```json
{
  "status": 200,
  "data": {
    "pageNo": 0,
    "pageSize": 10,
    "totalPages": 1,
    "totalItems": 5,
    "data": [
      {
        "id": 1,
        "name": "Giỏ Quà Tết An Khang",
        "price": 500000,
        "isCustom": false,
        "isActive": true,
        "products": [
          {
            "id": 1,
            "productId": 10,
            "productName": "Mứt Dừa",
            "productPrice": 120000,
            "quantity": 2
          },
          {
            "id": 2,
            "productId": 11,
            "productName": "Rượu Vang",
            "productPrice": 260000,
            "quantity": 1
          }
        ]
      }
    ]
  }
}
```

### 6.2 Get Bundle by ID (PUBLIC)
```
GET /api/v1/bundles/{id}
Auth: ❌ None
```

### 6.3 Create Bundle
```
POST /api/v1/bundles
Auth: ✅ Bearer Token
Content-Type: multipart/form-data
```

**Form Parts:**
- `request` (JSON):
```json
{
  "name": "string (required, max 255 chars)",
  "price": 500000,
  "isCustom": false,
  "description": "string (optional, max 5000 chars)",
  "products": [
    { "productId": 10, "quantity": 2 },
    { "productId": 11, "quantity": 1 }
  ]
}
```
- `image` (file): MultipartFile (optional)

> 📌 Nếu `isCustom = false`, price sẽ tự động được tính từ tổng giá products.

### 6.4 Update Bundle
```
PUT /api/v1/bundles/{id}
Auth: ✅ Bearer Token
Content-Type: multipart/form-data
```

### 6.5 Delete Bundle (Soft Delete)
```
DELETE /api/v1/bundles/{id}
Auth: ✅ Bearer Token
```

---

## ⭐ 7. Product Review Module

### 7.1 Get Reviews for Product (PUBLIC)
```
GET /api/v1/products/{productId}/reviews?page=0&size=10
Auth: ❌ None
```

**Response:**
```json
{
  "status": 200,
  "data": {
    "pageNo": 0,
    "pageSize": 10,
    "totalPages": 1,
    "totalItems": 3,
    "data": [
      {
        "id": 1,
        "productId": 10,
        "userId": 5,
        "userName": "Nguyễn Văn A",
        "rating": 5,
        "comment": "Sản phẩm rất ngon!",
        "createdAt": "2026-03-15T00:00:00"
      }
    ]
  }
}
```

### 7.2 Create Review
```
POST /api/v1/products/{productId}/reviews
Auth: ✅ Bearer Token
```

**Request Body:**
```json
{
  "rating": 5,
  "comment": "string (optional)"
}
```

> ⚠️ Mỗi user chỉ được review 1 lần/sản phẩm. Rating: 1-5.

### 7.3 Update Review
```
PUT /api/v1/reviews/{reviewId}
Auth: ✅ Bearer Token (chỉ owner của review)
```

**Request Body:** Same as Create

### 7.4 Delete Review
```
DELETE /api/v1/reviews/{reviewId}
Auth: ✅ Bearer Token (chỉ owner)
```

---

## 🛒 8. Cart Module

**Base**: `/api/v1/cart`
**Auth**: ✅ All endpoints require Bearer Token

### 8.1 Get My Cart
```
GET /api/v1/cart
```

**Response:**
```json
{
  "status": 200,
  "data": {
    "id": 1,
    "items": [
      {
        "id": 101,
        "itemType": "PRODUCT",
        "itemId": 10,
        "itemName": "Mứt Dừa Tết",
        "itemPrice": 120000,
        "quantity": 2,
        "subtotal": 240000
      },
      {
        "id": 102,
        "itemType": "BUNDLE",
        "itemId": 1,
        "itemName": "Giỏ Quà Tết An Khang",
        "itemPrice": 500000,
        "quantity": 1,
        "subtotal": 500000
      }
    ],
    "totalPrice": 740000,
    "totalItems": 3
  }
}
```

### 8.2 Add Item to Cart
```
POST /api/v1/cart/items
```

**Request Body:**
```json
{
  "itemType": "PRODUCT",
  "productId": 10,
  "bundleId": null,
  "quantity": 2
}
```

> 📌 `itemType` phải là `"PRODUCT"` hoặc `"BUNDLE"`. Nếu `PRODUCT` thì set `productId`, nếu `BUNDLE` thì set `bundleId`.

### 8.3 Update Cart Item Quantity
```
PUT /api/v1/cart/items/{itemId}?quantity=3
```

### 8.4 Remove Cart Item
```
DELETE /api/v1/cart/items/{itemId}
```

### 8.5 Clear Cart
```
DELETE /api/v1/cart
```

---

## 📋 9. Order Module

**Base**: `/api/v1/orders`
**Auth**: ✅ All endpoints require Bearer Token

### Order Status Flow:
```
CREATED → WAITING_PAYMENT → PAID → PROCESSING → SHIPPED → COMPLETED
                    ↘                                       
                  CANCELLED (chỉ khi CREATED hoặc WAITING_PAYMENT)
```

### 9.1 Create Order (from Cart)
```
POST /api/v1/orders
Auth: ✅ Bearer Token
```

**Request Body:**
```json
{
  "addressId": 1,
  "discountCode": "TETGIFT2026",
  "vatCompanyName": "string (optional)",
  "vatTaxCode": "string (optional)",
  "vatPhone": "string (optional, VN phone format)",
  "vatAddress": "string (optional)"
}
```

> 📌 Tạo order sẽ tự động snapshot cart items → order items + clear cart.
> 📌 `discountCode` (optional) – mã giảm giá được apply vào tổng đơn.

**Response:**
```json
{
  "status": 200,
  "data": {
    "id": 1,
    "status": "CREATED",
    "totalAmount": 690000,
    "customerName": "Nguyễn Văn A",
    "customerEmail": "user@example.com",
    "receiverName": "Trần Thị B",
    "receiverPhone": "0912345678",
    "shippingAddress": "123 Lê Lợi, Q1, TP.HCM",
    "discountCode": "TETGIFT2026",
    "discountAmount": 50000,
    "vatCompanyName": null,
    "vatTaxCode": null,
    "vatPhone": null,
    "vatAddress": null,
    "items": [
      {
        "id": 1,
        "itemType": "PRODUCT",
        "itemName": "Mứt Dừa Tết",
        "priceSnapshot": 120000,
        "quantity": 2,
        "subtotal": 240000
      },
      {
        "id": 2,
        "itemType": "BUNDLE",
        "itemName": "Giỏ Quà Tết An Khang",
        "priceSnapshot": 500000,
        "quantity": 1,
        "subtotal": 500000
      }
    ],
    "createdAt": "2026-03-15T00:00:00"
  }
}
```

> 📌 `customerName/Email` = thông tin người đặt hàng (từ User).
> 📌 `receiverName/Phone/shippingAddress` = snapshot từ Address tại thời điểm đặt hàng.

### 9.2 Get Order by ID
```
GET /api/v1/orders/{id}
Auth: ✅ Bearer Token
```

### 9.3 Get My Orders (Paginated)
```
GET /api/v1/orders?page=0&size=10
Auth: ✅ Bearer Token
```

### 9.4 Get All Orders (ADMIN)
```
GET /api/v1/orders/all?page=0&size=10
Auth: ✅ Bearer Token (ADMIN)
```

### 9.5 Update Order Status (ADMIN)
```
PUT /api/v1/orders/{id}/status?status=PROCESSING
Auth: ✅ Bearer Token (ADMIN)
```

> 📌 Valid statuses: `CREATED`, `WAITING_PAYMENT`, `PAID`, `PROCESSING`, `SHIPPED`, `COMPLETED`, `CANCELLED`

### 9.6 Cancel Order
```
PUT /api/v1/orders/{id}/cancel
Auth: ✅ Bearer Token (owner only)
```

> ⚠️ Chỉ cancel được khi status là `CREATED` hoặc `WAITING_PAYMENT`.

---

## 💳 10. Payment Module

**Base**: `/api/v1/payments`

### 10.1 Create Payment
```
POST /api/v1/payments/create
Auth: ✅ Bearer Token
```

**Request Body:**
```json
{
  "orderId": 1,
  "method": "VN_PAY"
}
```

> 📌 `method` phải là `"COD"` hoặc `"VN_PAY"`.

**Response (VN_PAY):**
```json
{
  "status": 201,
  "data": {
    "id": 1,
    "orderId": 1,
    "method": "VN_PAY",
    "status": "PENDING",
    "amount": 690000,
    "transactionId": null,
    "paidAt": null,
    "paymentUrl": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_Amount=..."
  }
}
```

> 📌 Với `VN_PAY`, FE cần redirect user đến `paymentUrl`. Sau khi thanh toán, VNPay redirect về callback URL.

**Response (COD):**
```json
{
  "status": 201,
  "data": {
    "id": 1,
    "orderId": 1,
    "method": "COD",
    "status": "PENDING",
    "amount": 690000,
    "transactionId": null,
    "paidAt": null,
    "paymentUrl": null
  }
}
```

### 10.2 VNPay Callback (handled by backend, FE reads result)
```
GET /api/v1/payments/vnpay-callback?vnp_Amount=...&vnp_ResponseCode=00&...
Auth: ❌ None (VNPay redirects here)
```

### 10.3 Get Payment by Order ID
```
GET /api/v1/payments/{orderId}
Auth: ✅ Bearer Token
```

---

## 🏷️ 11. Discount Module

**Base**: `/api/v1/discounts`

### 11.1 Validate Discount Code (USER)
```
POST /api/v1/discounts/validate?code=TETGIFT2026
Auth: ✅ Bearer Token
```

**Response:**
```json
{
  "status": 200,
  "data": {
    "id": 1,
    "code": "TETGIFT2026",
    "discountValue": 50000,
    "minOrderAmount": 200000,
    "usageLimit": 100,
    "usageCount": 45,
    "startDate": "2026-01-01T00:00:00",
    "endDate": "2026-03-31T23:59:59",
    "isActive": true
  }
}
```

### 11.2 Get All Discounts (ADMIN)
```
GET /api/v1/discounts
Auth: ✅ Bearer Token
```

### 11.3 Get Discount by ID (ADMIN)
```
GET /api/v1/discounts/{id}
Auth: ✅ Bearer Token
```

### 11.4 Create Discount (ADMIN)
```
POST /api/v1/discounts
Auth: ✅ Bearer Token
```

**Request Body:**
```json
{
  "code": "TETGIFT2026",
  "discountValue": 50000,
  "minOrderAmount": 200000,
  "usageLimit": 100,
  "startDate": "2026-01-01T00:00:00",
  "endDate": "2026-03-31T23:59:59"
}
```

> 📌 `minOrderAmount` (optional): Giá trị đơn tối thiểu để sử dụng mã.
> 📌 `usageLimit` (optional): Số lần sử dụng tối đa (null = vô hạn).

### 11.5 Update Discount (ADMIN)
```
PUT /api/v1/discounts/{id}
Auth: ✅ Bearer Token
```

### 11.6 Delete Discount (ADMIN, soft delete)
```
DELETE /api/v1/discounts/{id}
Auth: ✅ Bearer Token
```

---

## 📝 12. Blog Module

### Blog Topics

### 12.1 Get All Blog Topics (PUBLIC)
```
GET /api/v1/blog-topics
Auth: ❌ None
```

**Response:**
```json
{
  "status": 200,
  "data": [
    { "id": 1, "name": "Phong Tục Tết" },
    { "id": 2, "name": "Hướng Dẫn Chọn Quà" }
  ]
}
```

### 12.2 Create Blog Topic (ADMIN)
```
POST /api/v1/blog-topics
Auth: ✅ Bearer Token
```

**Request Body:**
```json
{ "name": "string (required)" }
```

### 12.3 Update Blog Topic (ADMIN)
```
PUT /api/v1/blog-topics/{id}
Auth: ✅ Bearer Token
```

### 12.4 Delete Blog Topic (ADMIN)
```
DELETE /api/v1/blog-topics/{id}
Auth: ✅ Bearer Token
```

### Blog Posts

### 12.5 Get All Blogs (PUBLIC)
```
GET /api/v1/blogs?page=0&size=10
Auth: ❌ None
```

**Response:**
```json
{
  "status": 200,
  "data": {
    "pageNo": 0,
    "pageSize": 10,
    "totalPages": 1,
    "totalItems": 5,
    "data": [
      {
        "id": 1,
        "title": "Top 10 Quà Tết Ý Nghĩa",
        "content": "Nội dung bài viết...",
        "topicName": "Hướng Dẫn Chọn Quà",
        "topicId": 2,
        "createdAt": "2026-03-15T00:00:00"
      }
    ]
  }
}
```

### 12.6 Get Blog by ID (PUBLIC)
```
GET /api/v1/blogs/{id}
Auth: ❌ None
```

### 12.7 Create Blog (ADMIN)
```
POST /api/v1/blogs
Auth: ✅ Bearer Token
```

**Request Body:**
```json
{
  "title": "string (required)",
  "content": "string (optional)",
  "topicId": 2
}
```

### 12.8 Update Blog (ADMIN)
```
PUT /api/v1/blogs/{id}
Auth: ✅ Bearer Token
```

### 12.9 Delete Blog (ADMIN)
```
DELETE /api/v1/blogs/{id}
Auth: ✅ Bearer Token
```

---

## 🤖 13. AI Chatbot Module

**Base**: `/api/v1/chatbot`

### 13.1 Send Message to Chatbot
```
POST /api/v1/chatbot/chat
Auth: ❌ None (nhưng nếu authenticated, userId sẽ tự động set)
```

**Request Body:**
```json
{
  "message": "string (required, max 2000 chars)",
  "sessionId": "string (optional, để tiếp tục conversation)"
}
```

**Response:**
```json
{
  "status": 200,
  "data": {
    "sessionId": "abc-123-def",
    "message": "Dạ, em xin giới thiệu một số sản phẩm mứt Tết:\n1. Mứt Dừa - 120.000đ\n2. Mứt Gừng - 95.000đ",
    "timestamp": "2026-03-15T00:00:00",
    "detectedIntent": "PRODUCT_SEARCH",
    "success": true,
    "errorMessage": null,
    "suggestions": [
      {
        "id": 10,
        "type": "PRODUCT",
        "name": "Mứt Dừa",
        "price": "120000",
        "stock": 100,
        "imageUrl": "https://..."
      }
    ]
  }
}
```

> 📌 **Intent types**: `PRODUCT_SEARCH`, `BUNDLE_SEARCH`, `CATEGORY_BROWSE`, `STOCK_CHECK`, `DISCOUNT_POLICY`, `SHOP_INFO`, `GENERAL_CHAT`

### 13.2 Get Chat History
```
GET /api/v1/chatbot/history/{sessionId}
Auth: ❌ None
```

**Response:** Chat messages list

### 13.3 Sync Embeddings (ADMIN)
```
POST /api/v1/chatbot/embeddings/sync
Auth: ✅ Bearer Token (ADMIN)
```

---

## 📤 14. Upload Module

**Base**: `/api/v1/upload`

### 14.1 Upload Image
```
POST /api/v1/upload/image
Auth: ✅ Bearer Token
Content-Type: multipart/form-data
```

**Form Data:**
- `file`: MultipartFile (image file)

**Response:**
```json
{
  "status": 200,
  "message": "Upload successful",
  "data": "https://res.cloudinary.com/xxx/image/upload/v123/product_images/abc.jpg"
}
```

---

## 🔑 15. Role Module (ADMIN only)

**Base**: `/api/v1/role`
**Auth**: ✅ All endpoints require Bearer Token (ADMIN)

### 15.1 Get All Roles
```
GET /api/v1/role
```

**Response:**
```json
{
  "status": 200,
  "data": [
    { "id": 1, "name": "ADMIN", "description": "Administrator" },
    { "id": 2, "name": "USER", "description": "Regular user" }
  ]
}
```

### 15.2 Get Role by ID
```
GET /api/v1/role/{id}
```

### 15.3 Create Role
```
POST /api/v1/role
```

**Request Body:**
```json
{
  "name": "string",
  "description": "string"
}
```

### 15.4 Update Role
```
PUT /api/v1/role?id=1
```

### 15.5 Delete Role
```
DELETE /api/v1/role?id=1
```

---

## 🔄 Business Flows

### Flow 1: User Registration & Login
```
1. POST /api/v1/user/register       → Register (trả về userId)
2. [Email OTP được gửi tự động]
3. POST /api/v1/auth/verify-otp     → Verify email
4. POST /api/v1/auth/login          → Login (trả về accessToken + refreshToken)
5. Lưu accessToken + refreshToken vào localStorage/cookie
6. Gửi accessToken trong header: Authorization: Bearer <accessToken>
```

### Flow 2: Shopping Flow (Product → Cart → Order → Payment)
```
1. GET /api/v1/products             → Browse products
2. GET /api/v1/products/{id}        → View product detail
3. POST /api/v1/cart/items          → Add to cart (itemType: "PRODUCT" or "BUNDLE")
4. GET /api/v1/cart                 → View cart
5. PUT /api/v1/cart/items/{id}      → Update quantity
6. [Optional] POST /api/v1/discounts/validate?code=XXX → Validate discount
7. POST /api/v1/orders              → Create order from cart (truyền discountCode nếu có)
8. POST /api/v1/payments/create     → Create payment
   - Nếu VN_PAY: redirect user đến paymentUrl
   - Nếu COD: order chờ xác nhận
9. GET /api/v1/orders               → Check order status
```

### Flow 3: Password Recovery
```
1. POST /api/v1/auth/forgot-password  → Gửi email chứa reset token
2. POST /api/v1/auth/reset-password   → Reset password bằng token từ email
```

### Flow 4: Token Refresh
```
1. Access token hết hạn → 401 Unauthorized
2. POST /api/v1/auth/refresh-token    → Gửi refresh token qua header x-refresh-token
3. Nhận lại cặp token mới
4. Retry request ban đầu
```

### Flow 5: Admin Product Management
```
1. POST /api/v1/categories           → Create category
2. POST /api/v1/upload/image         → Upload product image (lấy URL)
3. POST /api/v1/products/register    → Create product (multipart: request JSON + image files)
4. PUT /api/v1/products/{id}         → Update product
5. DELETE /api/v1/products/{id}      → Soft delete product
```

### Flow 6: Admin Order Management
```
1. GET /api/v1/orders/all            → View all orders
2. PUT /api/v1/orders/{id}/status?status=PROCESSING → Update status
3. → SHIPPED → COMPLETED
```

### Flow 7: VNPay Payment
```
1. POST /api/v1/payments/create      → method: "VN_PAY"
2. Response → paymentUrl (VNPay sandbox URL)
3. FE redirect user → paymentUrl
4. User thanh toán trên VNPay
5. VNPay redirect về: GET /api/v1/payments/vnpay-callback
6. VNPay gọi server: GET /api/v1/payments/vnpay-ipn (server-to-server)
7. Order status: WAITING_PAYMENT → PAID
```

---

## 📊 Enums Reference

### OrderStatus
| Value | Mô tả |
|-------|--------|
| `CREATED` | Đơn vừa tạo |
| `WAITING_PAYMENT` | Chờ thanh toán (VNPay) |
| `PAID` | Đã thanh toán |
| `PROCESSING` | Đang xử lý |
| `SHIPPED` | Đang giao hàng |
| `COMPLETED` | Hoàn thành |
| `CANCELLED` | Đã hủy |

### PaymentMethod
| Value | Mô tả |
|-------|--------|
| `COD` | Thanh toán khi nhận hàng |
| `VN_PAY` | Thanh toán qua VNPay |

### PaymentStatus
| Value | Mô tả |
|-------|--------|
| `PENDING` | Chờ thanh toán |
| `SUCCESS` | Thành công |
| `FAILED` | Thất bại |
| `CANCELLED` | Đã hủy |
| `EXPIRED` | Hết hạn |

### ChatBot IntentType
| Value | Mô tả |
|-------|--------|
| `PRODUCT_SEARCH` | Tìm sản phẩm |
| `BUNDLE_SEARCH` | Tìm combo/giỏ quà |
| `CATEGORY_BROWSE` | Duyệt theo danh mục |
| `STOCK_CHECK` | Kiểm tra tồn kho |
| `DISCOUNT_POLICY` | Chính sách giảm giá |
| `SHOP_INFO` | Thông tin cửa hàng |
| `GENERAL_CHAT` | Trò chuyện chung |

---

## 🔒 Public vs Protected Endpoints

### Public (không cần token):
- `POST /api/v1/auth/**` (login, register, forgot/reset password, verify OTP)
- `POST /api/v1/user/register`
- `GET /api/v1/products/**` (list, detail)
- `GET /api/v1/bundles/**` (list, detail)
- `GET /api/v1/categories/**` (list, detail)
- `GET /api/v1/products/{id}/reviews` (list reviews)
- `GET /api/v1/blogs/**` (list, detail)
- `GET /api/v1/blog-topics` (list)
- `POST /api/v1/chatbot/chat`
- `GET /api/v1/chatbot/history/**`
- `GET /api/v1/payments/vnpay-callback`
- `GET /api/v1/payments/vnpay-ipn`

### Protected (cần Bearer Token):
- Cart, Order, Payment, Address, Discount, Upload, User (update/get)
- Product Review (create, update, delete)

### Admin Only (cần ADMIN role):
- User list/delete
- Product/Category/Bundle CRUD
- Discount CRUD
- Order status update, view all orders
- Blog/Topic CRUD
- Role CRUD
- Chatbot embeddings sync

---

## ⚙️ CORS Allowed Origins
```
http://localhost:3000
http://localhost:5173
https://shophuypro.store
```

## 🗄️ Tech Stack
- **Backend**: Spring Boot 3.5.9, Java 21
- **Database**: PostgreSQL 16 + pgvector
- **Cache**: Redis 7
- **Auth**: JWT + OAuth2 Google
- **Payment**: VNPay (sandbox)
- **File Storage**: Cloudinary
- **AI Chatbot**: Spring AI + Gemini + RAG

---

## 📝 Changelog (2026-03-15)

### Order Module - Thêm thông tin giao hàng

**OrderResponse** giờ đây bao gồm đầy đủ thông tin để admin quản lý đơn hàng:

| Field | Type | Mô tả |
|-------|------|--------|
| `customerName` | String | Tên người đặt hàng (từ User) |
| `customerEmail` | String | Email người đặt hàng (từ User) |
| `receiverName` | String | Tên người nhận hàng (snapshot từ Address) |
| `receiverPhone` | String | SĐT người nhận (snapshot từ Address) |
| `shippingAddress` | String | Địa chỉ giao hàng (snapshot từ Address) |
| `discountCode` | String | Mã giảm giá đã dùng |
| `discountAmount` | BigDecimal | Số tiền đã giảm |

> 📌 Thông tin giao hàng (receiver) được **snapshot** tại thời điểm tạo đơn, không thay đổi khi user sửa/xóa address.

### Cart Module - Fix duplicate item bug

Khi add cùng 1 sản phẩm nhiều lần vào giỏ hàng, hệ thống sẽ **tăng quantity** của item đã có thay vì tạo dòng mới.

### Discount ↔ Order Integration

- `DiscountEntity` thêm: `usageLimit`, `usageCount`, `minOrderAmount`
- `OrderEntity` thêm: `discount` (FK), `discountCode`, `discountAmount` (snapshots)
- Khi tạo order với discount: validate hạn, validate usage limit, validate min order amount → apply → tăng usageCount

---

## 🖼️ Hướng dẫn Upload Ảnh (Product, Bundle, Blog)

### Quy tắc chung

**File validation (backend tự kiểm tra):**
| Rule | Chi tiết |
|------|----------|
| Định dạng cho phép | `image/jpeg`, `image/jpg`, `image/png`, `image/gif`, `image/webp` |
| Kích thước tối đa | **10MB** |
| Upload engine | **Cloudinary** (HTTPS secure_url, auto quality optimization) |

**Folder tổ chức trên Cloudinary:**
| Module | Folder | Ví dụ URL |
|--------|--------|-----------|
| Product | `products/` | `https://res.cloudinary.com/xxx/image/upload/products/abc.jpg` |
| Bundle | `bundles/` | `https://res.cloudinary.com/xxx/image/upload/bundles/xyz.jpg` |
| Blog | `blogs/` | `https://res.cloudinary.com/xxx/image/upload/blogs/def.jpg` |
| Upload API | `general/` hoặc custom | tùy param `folder` |

**Mỗi endpoint create/update đều hỗ trợ 2 Content-Type:**
- `application/json` → gửi data thuần, không upload ảnh
- `multipart/form-data` → gửi data + file ảnh

---

### 📦 1. Product - Upload ảnh

#### Cách A: JSON (không ảnh hoặc đã có URL)
```
POST /api/v1/products/register
Content-Type: application/json
Auth: ✅ Bearer Token (ADMIN)
```

```json
{
  "name": "Mứt Dừa Tết",
  "description": "Mứt dừa truyền thống",
  "price": 120000,
  "stock": 100,
  "categoryId": 1,
  "manufactureDate": "2026-01-01",
  "expDate": "2026-06-01",
  "images": [
    {
      "imageUrl": "https://res.cloudinary.com/xxx/products/abc.jpg",
      "imageType": "MAIN",
      "publicId": "products/abc",
      "isPrimary": true
    }
  ]
}
```

#### Cách B: Multipart (upload ảnh trực tiếp)
```
POST /api/v1/products/register
Content-Type: multipart/form-data
Auth: ✅ Bearer Token (ADMIN)
```

| Part | Type | Bắt buộc | Mô tả |
|------|------|----------|-------|
| `request` | Blob (JSON) | ✅ | ProductRequest JSON |
| `image` | File | ❌ | Ảnh sản phẩm chính |

```javascript
const formData = new FormData();
formData.append('request', new Blob([JSON.stringify({
  name: "Mứt Dừa Tết",
  price: 120000,
  stock: 100,
  categoryId: 1,
  manufactureDate: "2026-01-01",
  expDate: "2026-06-01",
  images: []
})], { type: 'application/json' }));
formData.append('image', fileInput.files[0]);

await axios.post('/api/v1/products/register', formData, {
  headers: { 'Authorization': `Bearer ${token}` }
  // ❌ KHÔNG set Content-Type
});
```

#### Update: `PUT /api/v1/products/{id}` (JSON hoặc Multipart tương tự)

**ProductResponse:**
```json
{
  "id": 1,
  "name": "Mứt Dừa Tết",
  "image": "https://res.cloudinary.com/xxx/products/abc.jpg",
  "images": [{ "id": 1, "imageUrl": "...", "imageType": "MAIN", "publicId": "...", "isPrimary": true }]
}
```

> 📌 `image` = ảnh chính (upload qua multipart). `images` = danh sách ảnh chi tiết (metadata).

---

### 🎁 2. Bundle - Upload ảnh

#### Cách A: JSON
```
POST /api/v1/bundles
Content-Type: application/json
Auth: ✅ Bearer Token (ADMIN)
```

```json
{
  "name": "Giỏ Quà Tết An Khang",
  "description": "Giỏ quà sang trọng",
  "price": 500000,
  "isCustom": false,
  "products": [
    { "productId": 1, "quantity": 2 },
    { "productId": 3, "quantity": 1 }
  ]
}
```

#### Cách B: Multipart
```
POST /api/v1/bundles
Content-Type: multipart/form-data
```

| Part | Type | Bắt buộc | Mô tả |
|------|------|----------|-------|
| `request` | Blob (JSON) | ✅ | BundleRequest JSON |
| `image` | File | ❌ | Ảnh giỏ quà |

```javascript
const formData = new FormData();
formData.append('request', new Blob([JSON.stringify({
  name: "Giỏ Quà Tết An Khang",
  description: "Giỏ quà sang trọng",
  price: 500000,
  isCustom: false,
  products: [{ productId: 1, quantity: 2 }]
})], { type: 'application/json' }));
formData.append('image', fileInput.files[0]);

await axios.post('/api/v1/bundles', formData, {
  headers: { 'Authorization': `Bearer ${token}` }
});
```

#### Update: `PUT /api/v1/bundles/{id}` (JSON hoặc Multipart tương tự)

**BundleResponse:**
```json
{
  "id": 1,
  "name": "Giỏ Quà Tết An Khang",
  "description": "Giỏ quà sang trọng",
  "price": 500000,
  "image": "https://res.cloudinary.com/xxx/bundles/xyz.jpg",
  "isCustom": false,
  "isActive": true,
  "products": [...]
}
```

---

### 📝 3. Blog - Upload ảnh thumbnail

#### Cách A: JSON
```
POST /api/v1/blogs
Content-Type: application/json
Auth: ✅ Bearer Token (ADMIN)
```

```json
{
  "title": "Top 10 Quà Tết Ý Nghĩa",
  "content": "Nội dung bài viết...",
  "topicId": 2
}
```

#### Cách B: Multipart
```
POST /api/v1/blogs
Content-Type: multipart/form-data
```

| Part | Type | Bắt buộc | Mô tả |
|------|------|----------|-------|
| `request` | Blob (JSON) | ✅ | BlogRequest JSON |
| `image` | File | ❌ | Ảnh thumbnail bài viết |

```javascript
const formData = new FormData();
formData.append('request', new Blob([JSON.stringify({
  title: "Top 10 Quà Tết Ý Nghĩa",
  content: "Nội dung bài viết...",
  topicId: 2
})], { type: 'application/json' }));
formData.append('image', fileInput.files[0]);

await axios.post('/api/v1/blogs', formData, {
  headers: { 'Authorization': `Bearer ${token}` }
});
```

#### Update: `PUT /api/v1/blogs/{id}` (JSON hoặc Multipart tương tự)

**BlogResponse:**
```json
{
  "id": 1,
  "title": "Top 10 Quà Tết Ý Nghĩa",
  "content": "Nội dung bài viết...",
  "image": "https://res.cloudinary.com/xxx/blogs/def.jpg",
  "topicName": "Hướng Dẫn Chọn Quà",
  "topicId": 2,
  "createdAt": "2026-03-15T00:00:00"
}
```

---

### 📤 4. Upload API độc lập

Upload ảnh riêng (lấy URL trước, rồi gán vào entity sau):

```
POST /api/v1/upload/image?folder=products
Content-Type: multipart/form-data
Auth: ✅ Bearer Token
```

| Param | Type | Default | Mô tả |
|-------|------|---------|-------|
| `file` | MultipartFile (form part) | ✅ bắt buộc | File ảnh |
| `folder` | String (query param) | `general` | Folder trên Cloudinary |

**Response:**
```json
{
  "status": 200,
  "message": "Upload successful",
  "data": "https://res.cloudinary.com/xxx/image/upload/products/abc.jpg"
}
```

---

### ⚠️ Lưu ý quan trọng cho FE

1. **Khi gửi multipart**, phần `request` **PHẢI** wrap trong `new Blob([JSON.stringify(data)], { type: 'application/json' })`
2. **KHÔNG set `Content-Type` header** thủ công — browser/axios tự thêm `boundary`
3. **Backend tự validate file** — nếu sai định dạng hoặc quá 10MB sẽ trả `400 Bad Request`
4. **Ảnh trả về luôn là HTTPS** (`secure_url` từ Cloudinary)
5. **Ảnh được tối ưu tự động** bởi Cloudinary (`quality: auto`)

