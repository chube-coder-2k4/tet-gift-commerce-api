-- ============================================
-- RUN THIS MANUALLY IN PostgreSQL ONCE
-- Required for AI Chatbot pgvector support
-- ============================================

-- 1. Enable pgvector extension (requires pgvector installed on server)
CREATE EXTENSION IF NOT EXISTS vector;

-- Note: The following tables are auto-created by:
--   - chat_session, chat_message → JPA ddl-auto: update
--   - vector_store → Spring AI pgvector (initialize-schema: true)
--
-- You do NOT need to run anything else manually.


