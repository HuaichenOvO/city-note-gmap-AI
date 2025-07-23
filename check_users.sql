-- 检查用户表
USE citynote;

-- 显示所有表
SHOW TABLES;

-- 检查用户表结构
DESCRIBE users;

-- 查看用户数量
SELECT COUNT(*) as user_count FROM users;

-- 查看所有用户（不显示密码）
SELECT id, username, email, enabled, create_date FROM users LIMIT 10;

-- 检查是否有测试用户
SELECT * FROM users WHERE username LIKE '%test%' OR username LIKE '%admin%'; 