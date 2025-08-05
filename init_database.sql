-- 数据库初始化脚本
-- 用于新EC2实例的MySQL数据库

-- 创建数据库
CREATE DATABASE IF NOT EXISTS citynote;
USE citynote;

-- 创建用户表
CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    create_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 创建用户资料表
CREATE TABLE IF NOT EXISTS user_profiles (
    profile_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    bio TEXT,
    avatar_url VARCHAR(255),
    create_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 创建地区表
CREATE TABLE IF NOT EXISTS counties (
    county_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    county_name VARCHAR(100) NOT NULL,
    state VARCHAR(50),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    create_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建事件表
CREATE TABLE IF NOT EXISTS events (
    event_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    event_type ENUM('TEXT', 'IMAGE') DEFAULT 'TEXT',
    county_id BIGINT,
    user_id BIGINT NOT NULL,
    create_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (county_id) REFERENCES counties(county_id) ON DELETE SET NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 创建事件点赞表
CREATE TABLE IF NOT EXISTS event_likes (
    like_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    create_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (event_id) REFERENCES events(event_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    UNIQUE KEY unique_event_user (event_id, user_id)
);

-- 创建文件存储表
CREATE TABLE IF NOT EXISTS blobs (
    blob_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT,
    file_name VARCHAR(255) NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    file_size BIGINT,
    mime_type VARCHAR(100),
    create_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (event_id) REFERENCES events(event_id) ON DELETE CASCADE
);

-- 插入示例数据
INSERT INTO counties (county_name, state, latitude, longitude) VALUES
('New York County', 'NY', 40.7589, -73.9851),
('Kings County', 'NY', 40.6782, -73.9442),
('Queens County', 'NY', 40.7282, -73.7949),
('Bronx County', 'NY', 40.8448, -73.8648),
('Richmond County', 'NY', 40.5795, -74.1502);

-- 创建索引
CREATE INDEX idx_events_county ON events(county_id);
CREATE INDEX idx_events_user ON events(user_id);
CREATE INDEX idx_events_create_date ON events(create_date);
CREATE INDEX idx_blobs_event ON blobs(event_id);
CREATE INDEX idx_event_likes_event ON event_likes(event_id);
CREATE INDEX idx_event_likes_user ON event_likes(user_id); 