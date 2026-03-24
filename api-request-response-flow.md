# API Request Response Flow Logic

## 1. Bundle Management API

### Create Bundle
- **Request**: `POST /api/bundles` (Multipart Form Data)
  - `name`: String (required)
  - `description`: String
  - `price`: BigDecimal
  - `isCustom`: boolean (default: false)
  - `products`: List of objects (productId, quantity)
  - `image`: MultipartFile
- **Logic**:
  - Validates input.
  - Maps `BundleRequest` to `BundleEntity`.
  - Links `BundleProductEntity` records back to the newly created `BundleEntity` and fetches the actual `ProductEntity` references from DB.
  - Calculates `totalPrice` if `isCustom` is false (sum of (product price * quantity)).
  - Uploads `image` to Cloudinary if provided and sets the URL.
  - Saves to database.
- **Response**: `200 OK` (returns ID of created bundle as Long)

### Get Bundle By ID
- **Request**: `GET /api/bundles/{id}`
- **Logic**:
  - Fetches `BundleEntity` by ID where `isActive` is true.
  - Throws `ResourceNotFoundException` if not found.
  - Maps to `BundleResponse` containing bundle details and a list of `BundleProductResponse` (id, productId, productName, productPrice, quantity).
- **Response**: `200 OK` (BundleResponse)

### Get All Bundles (Paginated)
- **Request**: `GET /api/bundles` (QueryParams: `page`, `size`, `sortBy`, `sortDir`)
- **Logic**:
  - Fetches paginated `BundleEntity` where `isActive` is true.
  - Maps results to `BundleResponse` objects.
- **Response**: `200 OK` (PageResponse<BundleResponse>)

### Update Bundle
- **Request**: `PUT /api/bundles/{id}` (Multipart Form Data)
  - Same fields as Create Bundle.
- **Logic**:
  - Fetches existing `BundleEntity` by ID.
  - Updates fields using `BundleMapper`.
  - Uploads new `image` to Cloudinary if provided.
  - Replaces existing `BundleProductEntity` list if `products` provided in request. Verifies products are active.
  - Recalculates `totalPrice` if `isCustom` is false.
  - Saves updated entity.
- **Response**: `200 OK` (returns ID of updated bundle as Long)

### Delete Bundle
- **Request**: `DELETE /api/bundles/{id}`
- **Logic**:
  - Fetches existing `BundleEntity` by ID.
  - Soft deletes by setting `isActive` = false.
  - Saves entity.
- **Response**: `200 OK`

---

## 2. Cart Management API

### Get My Cart
- **Request**: `GET /api/cart`
- **Logic**:
  - Fetches current authenticated user. Throws `ForBiddenException` if not authenticated.
  - Fetches or creates a `CartEntity` for the user.
  - Maps `CartEntity` and its `CartItemEntity` list to `CartResponse` and `CartItemResponse`.
  - Calculates total price and total items.
- **Response**: `200 OK` (CartResponse)

### Add Item To Cart
- **Request**: `POST /api/cart/items`
  - `CartItemRequest` (JSON)
    - `itemType`: "PRODUCT" or "BUNDLE"
    - `productId`: Long (if type PRODUCT)
    - `bundleId`: Long (if type BUNDLE)
    - `quantity`: Integer (default: 1)
    - `isCustomCombo`: boolean (default: false, only for BUNDLE)
    - `customComboData`: String (JSON string containing structured data about customized items, packaging, ribbons, notes, etc.)
- **Logic**:
  - Fetches or creates current user's `CartEntity`.
  - Depending on `itemType`:
    - **PRODUCT**: Fetches product. Checks if same product exists in cart; if yes, increments quantity, else creates new `CartItemEntity`.
    - **BUNDLE**: Fetches bundle. Checks if same bundle exists in cart; if yes, increments quantity, else creates new `CartItemEntity`. Stores `isCustomCombo` and `customComboData`.
  - Saves `CartEntity`.
- **Response**: `200 OK` (CartResponse with updated items and totals)

### Update Cart Item Quantity
- **Request**: `PUT /api/cart/items/{itemId}?quantity={newQuantity}`
- **Logic**:
  - Fetches user's `CartEntity`.
  - Finds `CartItemEntity` by `itemId`.
  - If `quantity` <= 0, removes the item. Else, updates quantity.
  - Saves `CartEntity`.
- **Response**: `200 OK` (CartResponse with updated items and totals)

### Remove Cart Item
- **Request**: `DELETE /api/cart/items/{itemId}`
- **Logic**:
  - Fetches user's `CartEntity`.
  - Removes `CartItemEntity` matching `itemId`.
  - Saves `CartEntity`.
- **Response**: `200 OK`

### Clear Cart
- **Request**: `DELETE /api/cart`
- **Logic**:
  - Fetches user's `CartEntity`.
  - Clears all items.
  - Saves `CartEntity`.
- **Response**: `200 OK`

---

## 3. Order Management API

### Create Order
- **Request**: `POST /api/orders`
  - `OrderRequest` (JSON)
    - `addressId`: Long (ID of delivery address)
    - `discountCode`: String (optional)
    - `vatCompanyName`, `vatTaxCode`, `vatPhone`, `vatAddress`: Strings (optional)
- **Logic**:
  - Authenticates user.
  - Fetches user's `CartEntity`. Checks if empty.
  - Fetches `Address` by `addressId`.
  - Creates `OrderEntity` with user, address snapshot (name, phone, detail), VAT info, and `CREATED` status.
  - Iterates over `CartItemEntity` to create `OrderItemEntity`:
    - Checks stock for PRODUCTS or component products within BUNDLES. Deducts stock immediately. Throws `InvalidDataException` if not enough stock.
    - Snapshots prices (`priceSnapshot`), `isCustomCombo`, and `customComboData` into `OrderItemEntity`.
  - Calculates Subtotal.
  - Applies Tier Discount (10% for >= 50M, 8% for >= 30M, 5% for >= 15M, 3% for >= 10M).
  - Applies Discount Code if provided (validates expiry, usage limits, minimum order amount).
  - Calculates final `totalAmount`.
  - Saves `OrderEntity` and associated items. (Cart is NOT cleared immediately here to allow retries on failed payments).
- **Response**: `200 OK` (OrderResponse)

### Get Order By ID
- **Request**: `GET /api/orders/{id}`
- **Logic**:
  - Fetches `OrderEntity` by ID.
  - Maps to `OrderResponse` including calculated subtotal, discounts applied, VAT info, and list of `OrderItemResponse` (includes `isCustomCombo` and `customComboData`).
- **Response**: `200 OK` (OrderResponse)

### Get My Orders (Paginated)
- **Request**: `GET /api/orders/my-orders` (QueryParams: `page`, `size`)
- **Logic**:
  - Fetches order history for current authenticated user, descending by `createdAt`.
- **Response**: `200 OK` (PageResponse<OrderResponse>)

### Get All Orders (Admin - Paginated)
- **Request**: `GET /api/orders` (QueryParams: `page`, `size`)
- **Logic**:
  - Fetches all orders across all users.
- **Response**: `200 OK` (PageResponse<OrderResponse>)

### Update Order Status (Admin)
- **Request**: `PUT /api/orders/{id}/status?status={newStatus}`
- **Logic**:
  - Updates `status` field of `OrderEntity`.
  - Sends a WebSocket notification to the user (`/queue/order-status`).
- **Response**: `200 OK` (OrderResponse)

### Cancel Order
- **Request**: `PUT /api/orders/{id}/cancel`
- **Logic**:
  - Verifies current user owns the order.
  - Verifies order status is either `CREATED` or `WAITING_PAYMENT`.
  - Updates status to `CANCELLED`.
- **Response**: `200 OK` (OrderResponse)

