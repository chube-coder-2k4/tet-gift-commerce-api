-- =====================================================
-- 🌸 TET GIFT COMMERCE - SEED DATA SCRIPT
-- Database: PostgreSQL
-- Chạy SQL này trong pgAdmin hoặc psql console
-- =====================================================

-- ==================== 1. ROLES ====================
INSERT INTO role (id, name, description, created_at, updated_at) VALUES
(1, 'ADMIN', 'Quản trị viên hệ thống', NOW(), NOW()),
(2, 'USER', 'Khách hàng', NOW(), NOW());

-- Reset sequence
SELECT setval(pg_get_serial_sequence('role', 'id'), (SELECT MAX(id) FROM role));

-- ==================== 2. USERS ====================
-- Password mặc định: "password123" (BCrypt encoded)
-- $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
INSERT INTO users (id, full_name, email, password, phone, username, is_verify, is_active, is_locked, role_id, created_at, updated_at) VALUES
(1,  'Admin Hệ Thống',     'admin@tetgift.com',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '0901000001', 'admin',      true, true, false, 1, NOW(), NOW()),
(2,  'Nguyễn Văn An',      'an.nguyen@gmail.com',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '0901000002', 'annguyen',   true, true, false, 2, NOW(), NOW()),
(3,  'Trần Thị Bình',      'binh.tran@gmail.com',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '0901000003', 'binhtran',   true, true, false, 2, NOW(), NOW()),
(4,  'Lê Hoàng Cường',     'cuong.le@gmail.com',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '0901000004', 'cuongle',    true, true, false, 2, NOW(), NOW()),
(5,  'Phạm Minh Duy',      'duy.pham@gmail.com',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '0901000005', 'duypham',    true, true, false, 2, NOW(), NOW()),
(6,  'Hoàng Thị Em',       'em.hoang@gmail.com',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '0901000006', 'emhoang',    true, true, false, 2, NOW(), NOW()),
(7,  'Võ Đức Phúc',        'phuc.vo@gmail.com',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '0901000007', 'phucvo',     true, true, false, 2, NOW(), NOW()),
(8,  'Đỗ Thanh Giang',     'giang.do@gmail.com',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '0901000008', 'giangdo',    true, true, false, 2, NOW(), NOW()),
(9,  'Bùi Quốc Hưng',      'hung.bui@gmail.com',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '0901000009', 'hungbui',    true, true, false, 2, NOW(), NOW()),
(10, 'Ngô Thị Inh',        'inh.ngo@gmail.com',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '0901000010', 'inhngo',     true, true, false, 2, NOW(), NOW()),
(11, 'Lý Văn Khánh',       'khanh.ly@gmail.com',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '0901000011', 'khanhly',    true, true, false, 2, NOW(), NOW());

SELECT setval(pg_get_serial_sequence('users', 'id'), (SELECT MAX(id) FROM users));

-- ==================== 3. ADDRESSES ====================
INSERT INTO address (id, receiver_name, phone, address_detail, is_default, user_id, created_at, updated_at) VALUES
(1,  'Nguyễn Văn An',      '0901000002', '123 Nguyễn Huệ, Q.1, TP.HCM',            true,  2, NOW(), NOW()),
(2,  'Nguyễn Văn An',      '0901000002', '456 Lê Lợi, Q.3, TP.HCM',                false, 2, NOW(), NOW()),
(3,  'Trần Thị Bình',      '0901000003', '789 Trần Hưng Đạo, Q.5, TP.HCM',         true,  3, NOW(), NOW()),
(4,  'Lê Hoàng Cường',     '0901000004', '12 Phạm Ngũ Lão, Q.1, TP.HCM',           true,  4, NOW(), NOW()),
(5,  'Phạm Minh Duy',      '0901000005', '34 Nguyễn Trãi, Q.1, TP.HCM',            true,  5, NOW(), NOW()),
(6,  'Hoàng Thị Em',       '0901000006', '56 Lý Tự Trọng, Q.1, TP.HCM',            true,  6, NOW(), NOW()),
(7,  'Võ Đức Phúc',        '0901000007', '78 Hai Bà Trưng, Q.1, TP.HCM',           true,  7, NOW(), NOW()),
(8,  'Đỗ Thanh Giang',     '0901000008', '90 Điện Biên Phủ, Q.Bình Thạnh, TP.HCM', true,  8, NOW(), NOW()),
(9,  'Bùi Quốc Hưng',      '0901000009', '11 Cách Mạng Tháng 8, Q.3, TP.HCM',      true,  9, NOW(), NOW()),
(10, 'Ngô Thị Inh',        '0901000010', '22 Võ Văn Tần, Q.3, TP.HCM',             true, 10, NOW(), NOW()),
(11, 'Lý Văn Khánh',       '0901000011', '33 Pasteur, Q.1, TP.HCM',                true, 11, NOW(), NOW());

SELECT setval(pg_get_serial_sequence('address', 'id'), (SELECT MAX(id) FROM address));

-- ==================== 4. CATEGORIES ====================
INSERT INTO category (id, name, description, is_active, created_at, updated_at) VALUES
(1, 'Bánh Kẹo Tết',        'Các loại bánh kẹo truyền thống ngày Tết',          true, NOW(), NOW()),
(2, 'Mứt Tết',             'Mứt dừa, mứt gừng, mứt bí và các loại mứt khác',  true, NOW(), NOW()),
(3, 'Hạt & Trái Cây Sấy',  'Hạt điều, hạt dưa, hạt hướng dương...',            true, NOW(), NOW()),
(4, 'Trà & Cà Phê',        'Trà hoa, trà xanh, cà phê đặc biệt',              true, NOW(), NOW()),
(5, 'Rượu & Nước Giải Khát','Rượu vang, bia thủ công, nước ép trái cây',        true, NOW(), NOW()),
(6, 'Giỏ Quà Tết',         'Giỏ quà được sắp xếp sẵn theo chủ đề',             true, NOW(), NOW()),
(7, 'Hoa Tết',              'Hoa mai, hoa đào, hoa lan trang trí Tết',          true, NOW(), NOW()),
(8, 'Đồ Trang Trí',        'Câu đối, đèn lồng, bao lì xì, đồ trang trí',      true, NOW(), NOW());

SELECT setval(pg_get_serial_sequence('category', 'id'), (SELECT MAX(id) FROM category));

-- ==================== 5. PRODUCTS ====================
INSERT INTO products (id, name, description, image, price, stock, is_active, manufacture_date, exp_date, category_id, created_at, updated_at) VALUES
(1,  'Bánh Chưng Truyền Thống',    'Bánh chưng gạo nếp cái hoa vàng, nhân đậu xanh thịt heo', 'https://res.cloudinary.com/demo/image/upload/products/banh-chung.jpg',       150000,  200, true, '2026-01-15', '2026-02-28', 1, NOW(), NOW()),
(2,  'Bánh Tét Lá Chuối',           'Bánh tét nhân chuối, đậu xanh, thịt mỡ',                  'https://res.cloudinary.com/demo/image/upload/products/banh-tet.jpg',         120000,  150, true, '2026-01-15', '2026-02-28', 1, NOW(), NOW()),
(3,  'Mứt Dừa Non',                 'Mứt dừa non dẻo thơm, nhiều màu sắc',                     'https://res.cloudinary.com/demo/image/upload/products/mut-dua.jpg',          85000,   300, true, '2026-01-01', '2026-06-01', 2, NOW(), NOW()),
(4,  'Mứt Gừng Dẻo',               'Mứt gừng cay nồng, tốt cho sức khỏe',                     'https://res.cloudinary.com/demo/image/upload/products/mut-gung.jpg',         65000,   250, true, '2026-01-01', '2026-06-01', 2, NOW(), NOW()),
(5,  'Mứt Bí Đao',                  'Mứt bí đao trong suốt, giòn ngọt thanh',                  'https://res.cloudinary.com/demo/image/upload/products/mut-bi.jpg',           75000,   200, true, '2026-01-01', '2026-06-01', 2, NOW(), NOW()),
(6,  'Hạt Điều Rang Muối',          'Hạt điều Bình Phước rang muối vàng ươm',                   'https://res.cloudinary.com/demo/image/upload/products/hat-dieu.jpg',         180000,  180, true, '2026-01-01', '2026-12-01', 3, NOW(), NOW()),
(7,  'Hạt Dưa Rang',                'Hạt dưa rang giòn đỏ tươi, không phẩm màu',               'https://res.cloudinary.com/demo/image/upload/products/hat-dua.jpg',          55000,   400, true, '2026-01-01', '2026-12-01', 3, NOW(), NOW()),
(8,  'Hạt Hướng Dương',             'Hạt hướng dương rang bơ thơm ngon',                        'https://res.cloudinary.com/demo/image/upload/products/hat-huong-duong.jpg',  45000,   350, true, '2026-01-01', '2026-12-01', 3, NOW(), NOW()),
(9,  'Trà Sen Tây Hồ',              'Trà ướp sen Tây Hồ hương thơm thanh nhã',                  'https://res.cloudinary.com/demo/image/upload/products/tra-sen.jpg',          250000,  100, true, '2026-01-01', '2026-12-01', 4, NOW(), NOW()),
(10, 'Cà Phê Weasel Premium',       'Cà phê chồn nguyên chất Đắk Lắk',                         'https://res.cloudinary.com/demo/image/upload/products/ca-phe-chon.jpg',      450000,   80, true, '2026-01-01', '2026-12-01', 4, NOW(), NOW()),
(11, 'Rượu Vang Đà Lạt',            'Rượu vang đỏ Đà Lạt 750ml',                               'https://res.cloudinary.com/demo/image/upload/products/ruou-vang.jpg',        320000,  120, true, '2025-06-01', '2028-06-01', 5, NOW(), NOW()),
(12, 'Nước Ép Táo Mèo',             'Nước ép táo mèo Sapa nguyên chất',                         'https://res.cloudinary.com/demo/image/upload/products/nuoc-ep-tao.jpg',      95000,   200, true, '2026-01-01', '2026-12-01', 5, NOW(), NOW()),
(13, 'Kẹo Dừa Bến Tre',             'Kẹo dừa sữa Bến Tre thơm béo',                            'https://res.cloudinary.com/demo/image/upload/products/keo-dua.jpg',          35000,   500, true, '2026-01-01', '2026-12-01', 1, NOW(), NOW()),
(14, 'Bao Lì Xì Rồng Vàng',         'Bao lì xì cao cấp họa tiết rồng vàng, set 10 cái',        'https://res.cloudinary.com/demo/image/upload/products/li-xi.jpg',            55000,   600, true, '2026-01-01', '2027-01-01', 8, NOW(), NOW()),
(15, 'Đèn Lồng Trang Trí',          'Đèn lồng đỏ treo trang trí Tết, set 2 cái',               'https://res.cloudinary.com/demo/image/upload/products/den-long.jpg',         120000,  150, true, '2026-01-01', '2027-01-01', 8, NOW(), NOW());

SELECT setval(pg_get_serial_sequence('products', 'id'), (SELECT MAX(id) FROM products));

-- ==================== 6. PRODUCT IMAGES ====================
INSERT INTO product_images (id, image_type, public_id, image_url, is_primary, product_id) VALUES
(1,  'MAIN',   'products/banh-chung',       'https://res.cloudinary.com/demo/image/upload/products/banh-chung.jpg',       true,  1),
(2,  'DETAIL', 'products/banh-chung-2',     'https://res.cloudinary.com/demo/image/upload/products/banh-chung-2.jpg',     false, 1),
(3,  'MAIN',   'products/banh-tet',         'https://res.cloudinary.com/demo/image/upload/products/banh-tet.jpg',         true,  2),
(4,  'MAIN',   'products/mut-dua',          'https://res.cloudinary.com/demo/image/upload/products/mut-dua.jpg',          true,  3),
(5,  'MAIN',   'products/mut-gung',         'https://res.cloudinary.com/demo/image/upload/products/mut-gung.jpg',         true,  4),
(6,  'MAIN',   'products/mut-bi',           'https://res.cloudinary.com/demo/image/upload/products/mut-bi.jpg',           true,  5),
(7,  'MAIN',   'products/hat-dieu',         'https://res.cloudinary.com/demo/image/upload/products/hat-dieu.jpg',         true,  6),
(8,  'MAIN',   'products/hat-dua',          'https://res.cloudinary.com/demo/image/upload/products/hat-dua.jpg',          true,  7),
(9,  'MAIN',   'products/hat-huong-duong',  'https://res.cloudinary.com/demo/image/upload/products/hat-huong-duong.jpg',  true,  8),
(10, 'MAIN',   'products/tra-sen',          'https://res.cloudinary.com/demo/image/upload/products/tra-sen.jpg',          true,  9),
(11, 'MAIN',   'products/ca-phe-chon',      'https://res.cloudinary.com/demo/image/upload/products/ca-phe-chon.jpg',      true,  10),
(12, 'MAIN',   'products/ruou-vang',        'https://res.cloudinary.com/demo/image/upload/products/ruou-vang.jpg',        true,  11),
(13, 'MAIN',   'products/nuoc-ep-tao',      'https://res.cloudinary.com/demo/image/upload/products/nuoc-ep-tao.jpg',      true,  12),
(14, 'MAIN',   'products/keo-dua',          'https://res.cloudinary.com/demo/image/upload/products/keo-dua.jpg',          true,  13),
(15, 'MAIN',   'products/li-xi',            'https://res.cloudinary.com/demo/image/upload/products/li-xi.jpg',            true,  14),
(16, 'MAIN',   'products/den-long',         'https://res.cloudinary.com/demo/image/upload/products/den-long.jpg',         true,  15);

SELECT setval(pg_get_serial_sequence('product_images', 'id'), (SELECT MAX(id) FROM product_images));

-- ==================== 7. BUNDLES ====================
INSERT INTO bundle (id, name, description, image, price, total_price, is_custom, is_active, created_at, updated_at) VALUES
(1,  'Giỏ Quà Tết An Khang',       'Giỏ quà sang trọng gồm bánh chưng, mứt, hạt điều',      'https://res.cloudinary.com/demo/image/upload/bundles/an-khang.jpg',      599000,  599000,  false, true, NOW(), NOW()),
(2,  'Giỏ Quà Tết Phú Quý',        'Giỏ quà cao cấp với rượu vang, cà phê, trà sen',         'https://res.cloudinary.com/demo/image/upload/bundles/phu-quy.jpg',       1200000, 1200000, false, true, NOW(), NOW()),
(3,  'Giỏ Quà Tết Bình An',        'Giỏ quà trung cấp gồm mứt, kẹo dừa, hạt dưa',          'https://res.cloudinary.com/demo/image/upload/bundles/binh-an.jpg',       350000,  350000,  false, true, NOW(), NOW()),
(4,  'Giỏ Quà Tết Vạn Lộc',        'Giỏ quà đầy đủ bánh kẹo truyền thống',                   'https://res.cloudinary.com/demo/image/upload/bundles/van-loc.jpg',       450000,  450000,  false, true, NOW(), NOW()),
(5,  'Giỏ Quà Tết Như Ý',           'Giỏ quà sức khỏe với trà, cà phê, nước ép',              'https://res.cloudinary.com/demo/image/upload/bundles/nhu-y.jpg',         780000,  780000,  false, true, NOW(), NOW()),
(6,  'Giỏ Quà Tết Thịnh Vượng',    'Giỏ quà premium gồm toàn hàng đặc sản',                  'https://res.cloudinary.com/demo/image/upload/bundles/thinh-vuong.jpg',   1500000, 1500000, false, true, NOW(), NOW()),
(7,  'Giỏ Quà Tết Hạnh Phúc',      'Giỏ quà gia đình với đa dạng sản phẩm',                  'https://res.cloudinary.com/demo/image/upload/bundles/hanh-phuc.jpg',     680000,  680000,  false, true, NOW(), NOW()),
(8,  'Giỏ Quà Tết Tài Lộc',        'Giỏ quà doanh nghiệp tặng đối tác',                      'https://res.cloudinary.com/demo/image/upload/bundles/tai-loc.jpg',       950000,  950000,  false, true, NOW(), NOW()),
(9,  'Giỏ Quà Tết Mini',            'Giỏ quà nhỏ xinh, phù hợp tặng bạn bè',                  'https://res.cloudinary.com/demo/image/upload/bundles/mini.jpg',          250000,  250000,  false, true, NOW(), NOW()),
(10, 'Giỏ Quà Custom',              'Giỏ quà tùy chỉnh theo ý khách hàng',                    'https://res.cloudinary.com/demo/image/upload/bundles/custom.jpg',        0,       0,       true,  true, NOW(), NOW());

SELECT setval(pg_get_serial_sequence('bundle', 'id'), (SELECT MAX(id) FROM bundle));

-- ==================== 8. BUNDLE_PRODUCTS ====================
-- Giỏ An Khang: Bánh chưng + Mứt dừa + Hạt điều
INSERT INTO bundle_product (id, bundle_id, product_id, quantity, created_at, updated_at) VALUES
(1,  1, 1,  1, NOW(), NOW()),
(2,  1, 3,  2, NOW(), NOW()),
(3,  1, 6,  1, NOW(), NOW()),
-- Giỏ Phú Quý: Rượu vang + Cà phê + Trà sen + Hạt điều
(4,  2, 11, 1, NOW(), NOW()),
(5,  2, 10, 1, NOW(), NOW()),
(6,  2, 9,  1, NOW(), NOW()),
(7,  2, 6,  1, NOW(), NOW()),
-- Giỏ Bình An: Mứt dừa + Kẹo dừa + Hạt dưa
(8,  3, 3,  1, NOW(), NOW()),
(9,  3, 13, 2, NOW(), NOW()),
(10, 3, 7,  1, NOW(), NOW()),
-- Giỏ Vạn Lộc: Bánh chưng + Bánh tét + Mứt gừng + Kẹo dừa
(11, 4, 1,  1, NOW(), NOW()),
(12, 4, 2,  1, NOW(), NOW()),
(13, 4, 4,  1, NOW(), NOW()),
(14, 4, 13, 1, NOW(), NOW()),
-- Giỏ Như Ý: Trà sen + Cà phê + Nước ép
(15, 5, 9,  1, NOW(), NOW()),
(16, 5, 10, 1, NOW(), NOW()),
(17, 5, 12, 2, NOW(), NOW()),
-- Giỏ Thịnh Vượng: Rượu vang + Cà phê + Trà sen + Hạt điều + Bánh chưng + Mứt dừa
(18, 6, 11, 1, NOW(), NOW()),
(19, 6, 10, 1, NOW(), NOW()),
(20, 6, 9,  1, NOW(), NOW()),
(21, 6, 6,  1, NOW(), NOW()),
(22, 6, 1,  1, NOW(), NOW()),
(23, 6, 3,  1, NOW(), NOW()),
-- Giỏ Hạnh Phúc: Bánh tét + Mứt bí + Hạt dưa + Hạt hướng dương + Kẹo dừa
(24, 7, 2,  1, NOW(), NOW()),
(25, 7, 5,  1, NOW(), NOW()),
(26, 7, 7,  1, NOW(), NOW()),
(27, 7, 8,  1, NOW(), NOW()),
(28, 7, 13, 1, NOW(), NOW()),
-- Giỏ Tài Lộc: Rượu vang + Hạt điều + Trà sen + Bao lì xì
(29, 8, 11, 1, NOW(), NOW()),
(30, 8, 6,  1, NOW(), NOW()),
(31, 8, 9,  1, NOW(), NOW()),
(32, 8, 14, 2, NOW(), NOW()),
-- Giỏ Mini: Mứt dừa + Kẹo dừa + Hạt hướng dương
(33, 9, 3,  1, NOW(), NOW()),
(34, 9, 13, 1, NOW(), NOW()),
(35, 9, 8,  1, NOW(), NOW());

SELECT setval(pg_get_serial_sequence('bundle_product', 'id'), (SELECT MAX(id) FROM bundle_product));

-- ==================== 9. DISCOUNTS ====================
INSERT INTO discount (id, code, discount_value, start_date, end_date, min_order_amount, usage_limit, usage_count, is_active, created_at, updated_at) VALUES
(1,  'TET2026',       50000,   '2026-01-01 00:00:00', '2026-02-28 23:59:59', 300000,  100,  5,  true,  NOW(), NOW()),
(2,  'XUANMOI',       100000,  '2026-01-15 00:00:00', '2026-02-15 23:59:59', 500000,  50,   12, true,  NOW(), NOW()),
(3,  'GIAMGIA10',     80000,   '2026-01-01 00:00:00', '2026-03-31 23:59:59', 200000,  200,  30, true,  NOW(), NOW()),
(4,  'FREESHIP',      30000,   '2026-01-01 00:00:00', '2026-12-31 23:59:59', 150000,  500,  45, true,  NOW(), NOW()),
(5,  'VIP50',         200000,  '2026-02-01 00:00:00', '2026-02-28 23:59:59', 1000000, 20,   3,  true,  NOW(), NOW()),
(6,  'TETSUM',        150000,  '2026-01-20 00:00:00', '2026-02-10 23:59:59', 700000,  30,   8,  true,  NOW(), NOW()),
(7,  'WELCOME',       25000,   '2026-01-01 00:00:00', '2026-12-31 23:59:59', 100000,  1000, 120, true, NOW(), NOW()),
(8,  'EXPIRED2025',   50000,   '2025-01-01 00:00:00', '2025-12-31 23:59:59', 200000,  100,  88, false, NOW(), NOW()),
(9,  'VALENTINE',     70000,   '2026-02-10 00:00:00', '2026-02-14 23:59:59', 250000,  80,   0,  true,  NOW(), NOW()),
(10, 'DOANHNGHIEP',   500000,  '2026-01-01 00:00:00', '2026-03-31 23:59:59', 3000000, 10,   2,  true,  NOW(), NOW());

SELECT setval(pg_get_serial_sequence('discount', 'id'), (SELECT MAX(id) FROM discount));

-- ==================== 10. BLOG TOPICS ====================
INSERT INTO blog_topic (id, name, created_at, updated_at) VALUES
(1, 'Phong Tục Tết',         NOW(), NOW()),
(2, 'Hướng Dẫn Chọn Quà',   NOW(), NOW()),
(3, 'Công Thức Nấu Ăn',     NOW(), NOW()),
(4, 'Trang Trí Nhà Cửa',    NOW(), NOW()),
(5, 'Sức Khỏe Ngày Tết',    NOW(), NOW());

SELECT setval(pg_get_serial_sequence('blog_topic', 'id'), (SELECT MAX(id) FROM blog_topic));

-- ==================== 11. BLOGS ====================
INSERT INTO blog (id, title, content, image, topic_id, created_at, updated_at) VALUES
(1,  'Top 10 Quà Tết Ý Nghĩa Nhất 2026',          'Tết Nguyên Đán là dịp để mọi người sum vầy, gửi gắm những lời chúc tốt đẹp. Dưới đây là top 10 món quà Tết ý nghĩa nhất cho gia đình và bạn bè...', 'https://res.cloudinary.com/demo/image/upload/blogs/top-qua-tet.jpg',    2, NOW(), NOW()),
(2,  'Cách Gói Bánh Chưng Truyền Thống',           'Bánh chưng là biểu tượng không thể thiếu trong mâm cỗ Tết. Hãy cùng tìm hiểu cách gói bánh chưng vuông đẹp theo phương pháp truyền thống...', 'https://res.cloudinary.com/demo/image/upload/blogs/goi-banh-chung.jpg', 3, NOW(), NOW()),
(3,  'Ý Nghĩa Hoa Mai Hoa Đào Ngày Tết',          'Hoa mai vàng và hoa đào hồng là hai loại hoa biểu tượng cho Tết miền Nam và miền Bắc. Mỗi loại hoa mang một ý nghĩa phong thủy khác nhau...', 'https://res.cloudinary.com/demo/image/upload/blogs/hoa-mai-dao.jpg',    1, NOW(), NOW()),
(4,  'Trang Trí Nhà Đón Tết Đẹp Và Tiết Kiệm',   'Không cần chi nhiều tiền, bạn vẫn có thể trang trí nhà cửa lung linh đón Tết. Dưới đây là 8 ý tưởng trang trí đơn giản nhưng ấn tượng...', 'https://res.cloudinary.com/demo/image/upload/blogs/trang-tri-tet.jpg',  4, NOW(), NOW()),
(5,  'Bí Quyết Ăn Tết Không Lo Tăng Cân',          'Mùa Tết với bao nhiêu món ngon, làm sao ăn uống thoải mái mà vẫn giữ dáng? Bài viết chia sẻ 7 bí quyết ăn Tết khỏe mạnh...', 'https://res.cloudinary.com/demo/image/upload/blogs/an-tet-khoe.jpg',    5, NOW(), NOW()),
(6,  'Phong Tục Chúc Tết Và Mừng Tuổi',            'Chúc Tết và mừng tuổi là phong tục đẹp của người Việt. Tìm hiểu những câu chúc Tết hay và quy tắc mừng tuổi đúng cách...', 'https://res.cloudinary.com/demo/image/upload/blogs/chuc-tet.jpg',       1, NOW(), NOW()),
(7,  'Cách Làm Mứt Dừa Non Đẹp Mắt',              'Mứt dừa non là món ăn vặt không thể thiếu ngày Tết. Hướng dẫn chi tiết cách làm mứt dừa non nhiều màu sắc, dẻo thơm...', 'https://res.cloudinary.com/demo/image/upload/blogs/lam-mut-dua.jpg',    3, NOW(), NOW()),
(8,  'Chọn Giỏ Quà Tết Theo Ngân Sách',            'Với mỗi mức ngân sách khác nhau, bạn có thể chọn được giỏ quà Tết phù hợp. Từ giỏ quà 200k đến giỏ quà triệu đồng...', 'https://res.cloudinary.com/demo/image/upload/blogs/gio-qua-ngan-sach.jpg', 2, NOW(), NOW()),
(9,  'Mâm Ngũ Quả Ngày Tết Miền Nam',              'Mâm ngũ quả miền Nam thường có mãng cầu, dừa, đu đủ, xoài, sung - tượng trưng cho câu "Cầu Dừa Đủ Xài Sung"...', 'https://res.cloudinary.com/demo/image/upload/blogs/mam-ngu-qua.jpg',    1, NOW(), NOW()),
(10, 'Lịch Sử Tết Nguyên Đán Việt Nam',            'Tết Nguyên Đán có lịch sử hàng nghìn năm, gắn liền với nền văn minh lúa nước. Bài viết đi sâu vào nguồn gốc và sự biến đổi của Tết qua các thời kỳ...', 'https://res.cloudinary.com/demo/image/upload/blogs/lich-su-tet.jpg', 1, NOW(), NOW());

SELECT setval(pg_get_serial_sequence('blog', 'id'), (SELECT MAX(id) FROM blog));

-- ==================== 12. CARTS ====================
INSERT INTO cart (id, user_id, created_at, updated_at) VALUES
(1,  2,  NOW(), NOW()),
(2,  3,  NOW(), NOW()),
(3,  4,  NOW(), NOW()),
(4,  5,  NOW(), NOW()),
(5,  6,  NOW(), NOW()),
(6,  7,  NOW(), NOW()),
(7,  8,  NOW(), NOW()),
(8,  9,  NOW(), NOW()),
(9,  10, NOW(), NOW()),
(10, 11, NOW(), NOW());

SELECT setval(pg_get_serial_sequence('cart', 'id'), (SELECT MAX(id) FROM cart));

-- ==================== 13. CART ITEMS ====================
-- Một vài user có sản phẩm trong giỏ hàng
INSERT INTO cart_item (id, cart_id, item_type, product_id, bundle_id, quantity, created_at, updated_at) VALUES
(1,  1, 'PRODUCT', 1,    NULL, 2, NOW(), NOW()),
(2,  1, 'PRODUCT', 3,    NULL, 3, NOW(), NOW()),
(3,  1, 'BUNDLE',  NULL, 1,    1, NOW(), NOW()),
(4,  2, 'PRODUCT', 6,    NULL, 1, NOW(), NOW()),
(5,  2, 'PRODUCT', 9,    NULL, 1, NOW(), NOW()),
(6,  3, 'BUNDLE',  NULL, 2,    1, NOW(), NOW()),
(7,  4, 'PRODUCT', 11,   NULL, 2, NOW(), NOW()),
(8,  5, 'PRODUCT', 13,   NULL, 5, NOW(), NOW()),
(9,  5, 'PRODUCT', 7,    NULL, 2, NOW(), NOW());

SELECT setval(pg_get_serial_sequence('cart_item', 'id'), (SELECT MAX(id) FROM cart_item));

-- ==================== 14. ORDERS ====================
INSERT INTO orders (id, user_id, total_amount, status, receiver_name, receiver_phone, shipping_address, discount_id, discount_code, discount_amount, created_at, updated_at) VALUES
(1,  2,  599000,   'COMPLETED',      'Nguyễn Văn An',      '0901000002', '123 Nguyễn Huệ, Q.1, TP.HCM',             NULL, NULL,        0,      '2026-01-20 10:30:00', '2026-01-25 15:00:00'),
(2,  3,  1150000,  'COMPLETED',      'Trần Thị Bình',      '0901000003', '789 Trần Hưng Đạo, Q.5, TP.HCM',          1,    'TET2026',   50000,  '2026-01-22 14:00:00', '2026-01-27 09:00:00'),
(3,  4,  350000,   'SHIPPED',        'Lê Hoàng Cường',     '0901000004', '12 Phạm Ngũ Lão, Q.1, TP.HCM',            NULL, NULL,        0,      '2026-01-25 09:15:00', '2026-01-28 10:00:00'),
(4,  5,  485000,   'PROCESSING',     'Phạm Minh Duy',      '0901000005', '34 Nguyễn Trãi, Q.1, TP.HCM',             3,    'GIAMGIA10', 80000,  '2026-01-28 16:45:00', '2026-01-29 08:00:00'),
(5,  6,  780000,   'PAID',           'Hoàng Thị Em',       '0901000006', '56 Lý Tự Trọng, Q.1, TP.HCM',             NULL, NULL,        0,      '2026-02-01 11:00:00', '2026-02-01 11:30:00'),
(6,  7,  250000,   'CREATED',        'Võ Đức Phúc',        '0901000007', '78 Hai Bà Trưng, Q.1, TP.HCM',            4,    'FREESHIP',  30000,  '2026-02-02 08:30:00', '2026-02-02 08:30:00'),
(7,  8,  1500000,  'COMPLETED',      'Đỗ Thanh Giang',     '0901000008', '90 Điện Biên Phủ, Q.Bình Thạnh, TP.HCM',  2,    'XUANMOI',   100000, '2026-01-18 10:00:00', '2026-01-23 14:00:00'),
(8,  9,  680000,   'WAITING_PAYMENT','Bùi Quốc Hưng',      '0901000009', '11 Cách Mạng Tháng 8, Q.3, TP.HCM',       NULL, NULL,        0,      '2026-02-03 15:20:00', '2026-02-03 15:20:00'),
(9,  10, 950000,   'COMPLETED',      'Ngô Thị Inh',        '0901000010', '22 Võ Văn Tần, Q.3, TP.HCM',              NULL, NULL,        0,      '2026-01-15 12:00:00', '2026-01-20 16:00:00'),
(10, 11, 320000,   'CANCELLED',      'Lý Văn Khánh',       '0901000011', '33 Pasteur, Q.1, TP.HCM',                 NULL, NULL,        0,      '2026-01-30 09:00:00', '2026-01-31 10:00:00'),
(11, 2,  450000,   'PROCESSING',     'Nguyễn Văn An',      '0901000002', '456 Lê Lợi, Q.3, TP.HCM',                 NULL, NULL,        0,      '2026-02-05 10:00:00', '2026-02-06 08:00:00'),
(12, 3,  250000,   'PAID',           'Trần Thị Bình',      '0901000003', '789 Trần Hưng Đạo, Q.5, TP.HCM',          7,    'WELCOME',   25000,  '2026-02-06 14:30:00', '2026-02-06 15:00:00');

SELECT setval(pg_get_serial_sequence('orders', 'id'), (SELECT MAX(id) FROM orders));

-- ==================== 15. ORDER ITEMS ====================
INSERT INTO order_item (id, order_id, item_type, product_id, bundle_id, price_snapshot, quantity, created_at, updated_at) VALUES
-- Order 1: Giỏ An Khang
(1,  1,  'BUNDLE',  NULL, 1,  599000,   1, NOW(), NOW()),
-- Order 2: Rượu vang + Cà phê + Trà sen
(2,  2,  'PRODUCT', 11,   NULL, 320000, 1, NOW(), NOW()),
(3,  2,  'PRODUCT', 10,   NULL, 450000, 1, NOW(), NOW()),
(4,  2,  'PRODUCT', 9,    NULL, 250000, 1, NOW(), NOW()),
-- Order 3: Giỏ Bình An
(5,  3,  'BUNDLE',  NULL, 3,  350000,   1, NOW(), NOW()),
-- Order 4: Mứt dừa x3 + Hạt dưa x2 + Mứt bí
(6,  4,  'PRODUCT', 3,    NULL, 85000,  3, NOW(), NOW()),
(7,  4,  'PRODUCT', 7,    NULL, 55000,  2, NOW(), NOW()),
(8,  4,  'PRODUCT', 5,    NULL, 75000,  1, NOW(), NOW()),
-- Order 5: Giỏ Như Ý
(9,  5,  'BUNDLE',  NULL, 5,  780000,   1, NOW(), NOW()),
-- Order 6: Giỏ Mini
(10, 6,  'BUNDLE',  NULL, 9,  250000,   1, NOW(), NOW()),
-- Order 7: Giỏ Thịnh Vượng
(11, 7,  'BUNDLE',  NULL, 6,  1500000,  1, NOW(), NOW()),
-- Order 8: Giỏ Hạnh Phúc
(12, 8,  'BUNDLE',  NULL, 7,  680000,   1, NOW(), NOW()),
-- Order 9: Giỏ Tài Lộc
(13, 9,  'BUNDLE',  NULL, 8,  950000,   1, NOW(), NOW()),
-- Order 10: Rượu vang
(14, 10, 'PRODUCT', 11,   NULL, 320000, 1, NOW(), NOW()),
-- Order 11: Giỏ Vạn Lộc
(15, 11, 'BUNDLE',  NULL, 4,  450000,   1, NOW(), NOW()),
-- Order 12: Giỏ Mini
(16, 12, 'BUNDLE',  NULL, 9,  250000,   1, NOW(), NOW());

SELECT setval(pg_get_serial_sequence('order_item', 'id'), (SELECT MAX(id) FROM order_item));

-- ==================== 16. PAYMENTS ====================
INSERT INTO payment (id, order_id, method, status, amount, transaction_id, paid_at, created_at, updated_at) VALUES
(1,  1,  'COD',    'SUCCESS',   599000,   NULL,                    '2026-01-25 15:00:00', '2026-01-20 10:30:00', '2026-01-25 15:00:00'),
(2,  2,  'VN_PAY', 'SUCCESS',   1150000,  'VNP20260122140012345',  '2026-01-22 14:05:00', '2026-01-22 14:00:00', '2026-01-22 14:05:00'),
(3,  3,  'COD',    'PENDING',   350000,   NULL,                    NULL,                  '2026-01-25 09:15:00', '2026-01-25 09:15:00'),
(4,  4,  'VN_PAY', 'SUCCESS',   485000,   'VNP20260128164567890',  '2026-01-28 16:50:00', '2026-01-28 16:45:00', '2026-01-28 16:50:00'),
(5,  5,  'VN_PAY', 'SUCCESS',   780000,   'VNP20260201110098765',  '2026-02-01 11:05:00', '2026-02-01 11:00:00', '2026-02-01 11:05:00'),
(6,  6,  'COD',    'PENDING',   250000,   NULL,                    NULL,                  '2026-02-02 08:30:00', '2026-02-02 08:30:00'),
(7,  7,  'VN_PAY', 'SUCCESS',   1500000,  'VNP20260118100011111',  '2026-01-18 10:05:00', '2026-01-18 10:00:00', '2026-01-18 10:05:00'),
(8,  8,  'VN_PAY', 'PENDING',   680000,   NULL,                    NULL,                  '2026-02-03 15:20:00', '2026-02-03 15:20:00'),
(9,  9,  'COD',    'SUCCESS',   950000,   NULL,                    '2026-01-20 16:00:00', '2026-01-15 12:00:00', '2026-01-20 16:00:00'),
(10, 10, 'VN_PAY', 'CANCELLED', 320000,   NULL,                    NULL,                  '2026-01-30 09:00:00', '2026-01-31 10:00:00'),
(11, 11, 'COD',    'PENDING',   450000,   NULL,                    NULL,                  '2026-02-05 10:00:00', '2026-02-05 10:00:00'),
(12, 12, 'VN_PAY', 'SUCCESS',   250000,   'VNP20260206143022222',  '2026-02-06 14:35:00', '2026-02-06 14:30:00', '2026-02-06 14:35:00');

SELECT setval(pg_get_serial_sequence('payment', 'id'), (SELECT MAX(id) FROM payment));

-- ==================== 17. PRODUCT REVIEWS ====================
INSERT INTO product_review (id, product_id, user_id, rating, comment, created_at, updated_at) VALUES
(1,  1,  2, 5, 'Bánh chưng ngon lắm, gạo nếp dẻo, nhân đậm đà!',                NOW(), NOW()),
(2,  1,  3, 4, 'Bánh ngon nhưng hơi nhỏ so với giá tiền',                         NOW(), NOW()),
(3,  3,  4, 5, 'Mứt dừa dẻo thơm, nhiều màu sắc bắt mắt, ăn hoài không ngán',    NOW(), NOW()),
(4,  6,  5, 5, 'Hạt điều rang muối giòn tan, vừa miệng, rất ngon!',               NOW(), NOW()),
(5,  6,  6, 4, 'Hạt điều chất lượng tốt, đóng gói đẹp',                            NOW(), NOW()),
(6,  9,  7, 5, 'Trà sen Tây Hồ thơm lắm, uống thanh mát',                          NOW(), NOW()),
(7,  10, 8, 5, 'Cà phê chồn đậm, thơm, xứng đáng với giá tiền',                   NOW(), NOW()),
(8,  11, 9, 4, 'Rượu vang Đà Lạt ngọt dịu, dễ uống',                               NOW(), NOW()),
(9,  13, 10, 5, 'Kẹo dừa Bến Tre dẻo thơm, mua tặng ai cũng thích',               NOW(), NOW()),
(10, 13, 11, 4, 'Kẹo ngon, giá rẻ, rất phù hợp làm quà Tết',                       NOW(), NOW()),
(11, 3,  2, 4, 'Mứt dừa ngon, nhưng hơi ngọt',                                      NOW(), NOW()),
(12, 7,  3, 5, 'Hạt dưa giòn, không bị mặn quá, rất vừa vặn',                       NOW(), NOW()),
(13, 14, 4, 5, 'Bao lì xì đẹp xuất sắc, giấy dày, in sắc nét!',                    NOW(), NOW()),
(14, 15, 5, 4, 'Đèn lồng đẹp, treo lên rất lung linh, hơi mỏng',                   NOW(), NOW()),
(15, 4,  6, 3, 'Mứt gừng hơi cay quá, không hợp khẩu vị lắm',                      NOW(), NOW());

SELECT setval(pg_get_serial_sequence('product_review', 'id'), (SELECT MAX(id) FROM product_review));

-- =====================================================
-- ✅ SEED DATA HOÀN TẤT!
-- Tổng: 2 roles, 11 users, 11 addresses, 8 categories,
--        15 products, 16 product_images, 10 bundles,
--        35 bundle_products, 10 discounts, 5 blog_topics,
--        10 blogs, 10 carts, 9 cart_items, 12 orders,
--        16 order_items, 12 payments, 15 reviews
-- 
-- Login: username/password
--   Admin: admin / password123
--   User:  annguyen / password123 (và các user khác)
-- =====================================================
