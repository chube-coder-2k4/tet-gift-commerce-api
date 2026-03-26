# Hướng Dẫn Tích Hợp API Chức Năng Bundle và Custom Combo

Tài liệu này mô tả chi tiết các thay đổi về luồng (flow), payload request/response của các API để hỗ trợ tính năng Bundle và Custom Combo (Combo tuỳ chỉnh) từ frontend.

---

## 1. Cart (Giỏ hàng)

### 1.1 Thêm vào giỏ hàng (`POST /api/v1/cart/items`)

Để hỗ trợ thêm combo tuỳ chỉnh vào giỏ hàng bên cạnh các sản phẩm và bundle cố định, backend đã mở rộng payload `CartItemRequest` cho API thêm sản phẩm vào giỏ.

**Payload Request (Thêm mới 2 trường):**
```json
{
  "itemType": "BUNDLE", // Hoặc "PRODUCT"
  "productId": null, // Cần truyền nếu itemType="PRODUCT"
  "bundleId": 1, // Cần truyền nếu itemType="BUNDLE"
  "quantity": 1,
  
  // 2 trường mới cho CUSTOM COMBO
  "isCustomCombo": true, // Bật cờ này bằng true nếu là combo tự chọn
  "customComboData": "Dữ liệu JSON stringify chứa chi tiết combo tuỳ chỉnh" // Ví dụ: "{\"color\": \"red\", \"note\": \"Gói viền đỏ\"}"
}
```

### 1.2 Lấy danh sách giỏ hàng (`GET /api/v1/cart`)

**Payload Response (Nhận thêm trường cho list item):**
Dữ liệu sẽ trả về bao gồm thông tin chi tiết combo nếu `isCustomCombo` là `true`.

```json
{
  "id": 1,
  "items": [
    {
      "id": 10,
      "itemType": "BUNDLE",
      "itemId": 1,
      "itemName": "Tết Đong Đầy",
      "itemPrice": 250000,
      "quantity": 1,
      "subtotal": 250000,
      
      // 2 trường mới cho các item Custom Combo
      "isCustomCombo": true,
      "customComboData": "Dữ liệu JSON stringify chứa chi tiết combo tuỳ chỉnh"
    }
  ],
  "totalPrice": 250000,
  "totalItems": 1
}
```

---

## 2. Order (Đặt hàng)

Khi user tạo đơn hàng (`POST /api/v1/orders`), các thông tin `isCustomCombo` và `customComboData` từ giỏ hàng sẽ tự động được copy (snapshot) vào từng mặt hàng (Order Item) lưu trong DB.

### 2.1 Lấy chi tiết đơn hàng (`GET /api/v1/orders/{id}` và `GET /api/v1/orders/me`)

**Payload Response:**
Ở mục `items` trong `OrderResponse`, bạn sẽ nhận được thông tin custom combo đã lưu từ trước.

```json
{
  "id": 1001,
  "status": "WAITING_PAYMENT",
  "totalAmount": 250000,
  // ... (các trường khác)
  "items": [
    {
      "id": 50,
      "itemType": "BUNDLE",
      "itemName": "Tết Đong Đầy",
      "priceSnapshot": 250000,
      "quantity": 1,
      "subtotal": 250000,
      "isCustomCombo": true,
      "customComboData": "Dữ liệu JSON stringify chứa chi tiết combo tuỳ chỉnh"
    }
  ],
  "createdAt": "2026-03-24T18:00:00"
}
```

---

## 3. Quản lý Bundle (`/api/v1/bundles`)

### Optimistic Locking
Table `bundle` đã được thêm `stock` và `version` để sử dụng cơ chế Optimistic Locking tránh race condition khi cập nhật số lượng tồn kho. 
Trường hợp gặp lỗi xung đột khi 2 luồng cập nhật cùng 1 lúc, backend sẽ trả về HTTP Status `409 Conflict` (bắt bởi `ObjectOptimisticLockingFailureException`). 
Frontend cần xử lý bắt lỗi HTTP 409 này và báo cho người dùng "Dữ liệu đang được cập nhật bởi thao tác khác, vui lòng thử lại.".

### Payload tính giá Bundle
Ở backend đã được tính toán lại cách cộng dồn giá bundle.
Khi thêm hoặc cập nhật 1 Fixed Bundle (`is_custom = false`), tổng giá (`price`) của Bundle sẽ tự động được cập nhật trên backend bằng tổng `price * quantity` của từng product bên trong:

Ví dụ Product A giá 100k (số lượng 2), Product B giá 50k (số lượng 1): Tổng giá Bundle sẽ là 250k. Frontend không cần phải truyền giá trị `price` lên, backend sẽ tự cân đối lại.

```json
{
  "name": "Bundle Test",
  "description": "Mô tả bundle",
  "isCustom": false,
  "products": [
    {
      "productId": 1,
      "quantity": 2
    },
    {
      "productId": 2,
      "quantity": 1
    }
  ]
}
```
