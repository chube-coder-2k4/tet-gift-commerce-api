-- Flyway migration to update orders_status_check to match Java enum OrderStatus
-- This migration drops the existing check constraint (if exists) and recreates it with up-to-date values.

BEGIN;

ALTER TABLE IF EXISTS public.orders
DROP
CONSTRAINT IF EXISTS orders_status_check;

ALTER TABLE IF EXISTS public.orders
    ADD CONSTRAINT orders_status_check CHECK (status IN (
    'CREATED','WAITING_PAYMENT','PAID','PROCESSING','SHIPPED','COMPLETED','CANCELLED','CANCELLED_PENDING_REFUND','CANCELLED_REFUNDED'
    ));

COMMIT;

