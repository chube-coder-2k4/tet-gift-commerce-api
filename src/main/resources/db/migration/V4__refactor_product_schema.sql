-- =============================================
-- V4: Refactor Product schema
-- =============================================

-- 1. Product: remove product_badge_map, product_category_map (many-to-many)
--    Add category_id (many-to-one), stock, manufacture_date, exp_date, is_active
--    Remove original_price, status enum

-- Remove many-to-many junction tables
DROP TABLE IF EXISTS product_badge_map CASCADE;
DROP TABLE IF EXISTS product_category_map CASCADE;

-- Remove related tables
DROP TABLE IF EXISTS product_rating_summary CASCADE;
DROP TABLE IF EXISTS product_rating CASCADE;
DROP TABLE IF EXISTS product_inventory CASCADE;

-- Add new columns to products
ALTER TABLE products ADD COLUMN IF NOT EXISTS category_id BIGINT;
ALTER TABLE products ADD COLUMN IF NOT EXISTS stock INT DEFAULT 0;
ALTER TABLE products ADD COLUMN IF NOT EXISTS manufacture_date DATE;
ALTER TABLE products ADD COLUMN IF NOT EXISTS exp_date DATE;
ALTER TABLE products ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT true;

-- Add FK for category
ALTER TABLE products ADD CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES category(id);

-- Migrate status to is_active
UPDATE products SET is_active = (status != 'DELETED') WHERE is_active IS NULL;

-- Migrate stock from product_inventory if exists
-- (handled by Hibernate since we're dropping product_inventory)

-- Remove old columns
ALTER TABLE products DROP COLUMN IF EXISTS original_price;
ALTER TABLE products DROP COLUMN IF EXISTS status;

-- 2. Product Images: update columns
ALTER TABLE product_images ADD COLUMN IF NOT EXISTS image_type VARCHAR(50);
ALTER TABLE product_images ADD COLUMN IF NOT EXISTS public_id VARCHAR(255);
ALTER TABLE product_images ADD COLUMN IF NOT EXISTS is_primary BOOLEAN DEFAULT false;

-- Migrate isThumbnail to is_primary
UPDATE product_images SET is_primary = is_thumbnail WHERE is_primary IS NULL;

-- Remove old columns
ALTER TABLE product_images DROP COLUMN IF EXISTS is_thumbnail;
ALTER TABLE product_images DROP COLUMN IF EXISTS sort_order;
