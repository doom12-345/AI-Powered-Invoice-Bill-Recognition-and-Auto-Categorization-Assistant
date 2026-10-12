-- 发票记录表（本步先建好结构，AI 相关字段暂留空）
CREATE TABLE IF NOT EXISTS invoice (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_id VARCHAR(64),
    file_path VARCHAR(255),
    original_filename VARCHAR(255),
    amount DECIMAL(10, 2),
    invoice_date DATE,
    company VARCHAR(255),
    category VARCHAR(20),
    confidence DECIMAL(4, 3),
    status VARCHAR(20),
    need_manual_review TINYINT DEFAULT 0,
    corrected TINYINT DEFAULT 0,
    raw_text VARCHAR(2000),
    user_id BIGINT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
