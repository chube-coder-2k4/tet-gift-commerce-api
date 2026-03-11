-- =============================================
-- V5: Create Bundle, Cart, Order, Payment, Discount, Blog tables
-- =============================================

-- 1. Bundle
CREATE TABLE IF NOT EXISTS bundle (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(15, 2),
    is_custom BOOLEAN DEFAULT false,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT now(),
    updated_at TIMESTAMP DEFAULT now(),
    created_by BIGINT,
    updated_by BIGINT
);

CREATE TABLE IF NOT EXISTS bundle_product (
    id BIGSERIAL PRIMARY KEY,
    bundle_id BIGINT NOT NULL REFERENCES bundle(id),
    product_id BIGINT NOT NULL REFERENCES products(id),
    quantity INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT now(),
    updated_at TIMESTAMP DEFAULT now(),
    created_by BIGINT,
    updated_by BIGINT
);

-- 2. Product Review
CREATE TABLE IF NOT EXISTS product_review (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT now(),
    updated_at TIMESTAMP DEFAULT now(),
    created_by BIGINT,
    updated_by BIGINT,
    UNIQUE(product_id, user_id)
);

-- 3. Cart
CREATE TABLE IF NOT EXISTS cart (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id),
    created_at TIMESTAMP DEFAULT now(),
    updated_at TIMESTAMP DEFAULT now(),
    created_by BIGINT,
    updated_by BIGINT
);

CREATE TABLE IF NOT EXISTS cart_item (
    id BIGSERIAL PRIMARY KEY,
    cart_id BIGINT NOT NULL REFERENCES cart(id),
    item_type VARCHAR(20) NOT NULL,
    product_id BIGINT REFERENCES products(id),
    bundle_id BIGINT REFERENCES bundle(id),
    quantity INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT now(),
    updated_at TIMESTAMP DEFAULT now(),
    created_by BIGINT,
    updated_by BIGINT
);

-- 4. Order
CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    total_amount DECIMAL(15, 2) NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL DEFAULT 'CREATED',
    vat_company_name VARCHAR(255),
    vat_tax_code VARCHAR(50),
    vat_phone VARCHAR(20),
    vat_address VARCHAR(500),
    created_at TIMESTAMP DEFAULT now(),
    updated_at TIMESTAMP DEFAULT now(),
    created_by BIGINT,
    updated_by BIGINT
);

CREATE TABLE IF NOT EXISTS order_item (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id),
    item_type VARCHAR(20) NOT NULL,
    product_id BIGINT REFERENCES products(id),
    bundle_id BIGINT REFERENCES bundle(id),
    price_snapshot DECIMAL(15, 2) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT now(),
    updated_at TIMESTAMP DEFAULT now(),
    created_by BIGINT,
    updated_by BIGINT
);

-- 5. Payment
CREATE TABLE IF NOT EXISTS payment (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id),
    method VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    amount DECIMAL(15, 2) NOT NULL,
    transaction_id VARCHAR(255),
    paid_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT now(),
    updated_at TIMESTAMP DEFAULT now(),
    created_by BIGINT,
    updated_by BIGINT
);

-- 6. Discount
CREATE TABLE IF NOT EXISTS discount (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    discount_value DECIMAL(15, 2) NOT NULL,
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT now(),
    updated_at TIMESTAMP DEFAULT now(),
    created_by BIGINT,
    updated_by BIGINT
);

-- 7. Blog
CREATE TABLE IF NOT EXISTS blog_topic (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT now(),
    updated_at TIMESTAMP DEFAULT now(),
    created_by BIGINT,
    updated_by BIGINT
);

CREATE TABLE IF NOT EXISTS blog (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    topic_id BIGINT REFERENCES blog_topic(id),
    created_at TIMESTAMP DEFAULT now(),
    updated_at TIMESTAMP DEFAULT now(),
    created_by BIGINT,
    updated_by BIGINT
);
