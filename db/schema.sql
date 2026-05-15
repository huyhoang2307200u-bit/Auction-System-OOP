CREATE DATABASE IF NOT EXISTS auction_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE auction_system;

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    display_name VARCHAR(120),
    role ENUM('BIDDER','SELLER','ADMIN') NOT NULL,
    balance DECIMAL(15,2) NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS auctions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    seller_username VARCHAR(64),
    item_name VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(32) DEFAULT 'ELECTRONICS',
    starting_price DECIMAL(15,2) NOT NULL DEFAULT 0,
    current_price DECIMAL(15,2) NOT NULL DEFAULT 0,
    start_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    end_time DATETIME NULL,
    status ENUM('PENDING_APPROVAL','REJECTED','OPEN','RUNNING','FINISHED','PAID','CANCELED') DEFAULT 'PENDING_APPROVAL',
    reviewed_by VARCHAR(64),
    reviewed_at DATETIME NULL,
    rejection_reason TEXT,
    winner_username VARCHAR(64),
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS bids (
    id INT AUTO_INCREMENT PRIMARY KEY,
    auction_id INT NOT NULL,
    username VARCHAR(64) NOT NULL,
    bid_amount DECIMAL(15,2) NOT NULL,
    bid_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    auto_generated BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (auction_id) REFERENCES auctions(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS auto_bids (
    id INT AUTO_INCREMENT PRIMARY KEY,
    auction_id INT NOT NULL,
    username VARCHAR(64) NOT NULL,
    max_bid DECIMAL(15,2) NOT NULL,
    increment_amount DECIMAL(15,2) NOT NULL,
    priority_order BIGINT NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (auction_id) REFERENCES auctions(id) ON DELETE CASCADE
);


CREATE TABLE IF NOT EXISTS wallet_deposit_requests (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(64) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    status ENUM('PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING',
    requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reviewed_by VARCHAR(64),
    reviewed_at DATETIME NULL,
    rejection_reason TEXT,
    FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS wallet_transactions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(64) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    transaction_type ENUM('DEPOSIT','BID_HOLD','REFUND_BID_HOLD','PAYMENT','ADMIN_ADJUSTMENT') NOT NULL,
    note VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT IGNORE INTO users (username, password, display_name, role, balance) VALUES
('admin', '123', 'Quản trị viên', 'ADMIN', 0),
('seller', '123', 'Người bán', 'SELLER', 0),
('user', '123', 'Người đấu giá', 'BIDDER', 5000);

-- Quy định nghiệp vụ: Admin duy nhất được seed sẵn.
-- Ứng dụng server-side cấm mọi request REGISTER với role = ADMIN.
-- Người dùng gửi yêu cầu nạp tiền; Admin phải duyệt thì số dư ví mới được cộng.
-- Người dùng cần có số dư ví đủ trước khi đặt giá vượt quá số dư khả dụng.
