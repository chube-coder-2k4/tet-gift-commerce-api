-- =============================================
-- V3: Refactor User-Role, Address, Category
-- =============================================

-- 1. User-Role: ManyToMany -> ManyToOne
-- Add role_id column to users table
ALTER TABLE users ADD COLUMN IF NOT EXISTS role_id BIGINT;

-- Migrate data: pick ONE role per user from the user_roles junction table
UPDATE users u
SET role_id = (
    SELECT ur.role_id FROM user_roles ur WHERE ur.user_id = u.id LIMIT 1
)
WHERE u.role_id IS NULL;

-- Set default role (USER role) for users without any role
UPDATE users u
SET role_id = (SELECT id FROM role WHERE name = 'USER' LIMIT 1)
WHERE u.role_id IS NULL;

-- Add foreign key constraint
ALTER TABLE users ADD CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES role(id);

-- Drop the old junction table
DROP TABLE IF EXISTS user_roles CASCADE;

-- 2. Address: simplify fields
ALTER TABLE address ADD COLUMN IF NOT EXISTS address_detail VARCHAR(500);
ALTER TABLE address ADD COLUMN IF NOT EXISTS is_default BOOLEAN DEFAULT false;

-- Migrate existing address data into address_detail
UPDATE address
SET address_detail = CONCAT_WS(', ', street_address, ward, district, province)
WHERE address_detail IS NULL;

-- Drop old columns
ALTER TABLE address DROP COLUMN IF EXISTS street_address;
ALTER TABLE address DROP COLUMN IF EXISTS ward;
ALTER TABLE address DROP COLUMN IF EXISTS district;
ALTER TABLE address DROP COLUMN IF EXISTS province;

-- 3. Category: add is_active
ALTER TABLE category ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT true;
