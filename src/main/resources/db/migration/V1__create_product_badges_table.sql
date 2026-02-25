DROP TABLE IF EXISTS product_badges CASCADE;

CREATE TABLE product_badges (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL,
    label VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT now()
);

CREATE UNIQUE INDEX uk_product_badges_code
    ON product_badges (code);
