-- 检查counties表
USE citynote;

-- 显示所有表
SHOW TABLES;

-- 检查counties表结构
DESCRIBE counties;

-- 查看counties数量
SELECT COUNT(*) as county_count FROM counties;

-- 查看所有counties
SELECT county_id, county_name, state_name FROM counties LIMIT 20;

-- 查找特定的county ID 6085
SELECT * FROM counties WHERE county_id = 6085;

-- 查看county_id的范围
SELECT MIN(county_id) as min_id, MAX(county_id) as max_id FROM counties;

-- 查看最近的几个counties
SELECT county_id, county_name, state_name FROM counties ORDER BY county_id DESC LIMIT 10; 