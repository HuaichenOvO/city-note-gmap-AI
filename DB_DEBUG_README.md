# 远程EC2 MySQL数据库调试指南

## 概述
这个项目包含了连接到远程EC2实例上Docker容器中MySQL数据库的脚本和工具。

## 连接信息
- **主机**: 50.18.226.67
- **端口**: 3306
- **数据库**: citynote
- **用户名**: root
- **密码**: root

## 可用的脚本

### 1. 快速连接脚本
```bash
./connect_mysql.sh
```
直接连接到MySQL数据库，进入交互式命令行。


### 2. 数据库调试脚本
```bash
./db_debug.sh
```
运行一系列预定义的查询，显示数据库状态和基本信息。

## 常用MySQL命令

### 查看所有表
```sql
SHOW TABLES;
```

### 查看表结构
```sql
DESCRIBE users;
DESCRIBE events;
```

### 查看数据
```sql
SELECT * FROM users LIMIT 10;
SELECT * FROM events LIMIT 10;
```

### 查看用户数量
```sql
SELECT COUNT(*) FROM users;
```

### 查看事件数量
```sql
SELECT COUNT(*) FROM events;
```

### 查看最近的事件
```sql
SELECT event_id, title, event_type, create_date 
FROM events 
ORDER BY create_date DESC 
LIMIT 5;
```

## 注意事项
1. 确保EC2实例的3306端口对本地IP开放
2. 如果连接失败，检查EC2安全组设置
3. 建议使用 `connect_mysql_safe.sh` 避免密码暴露
4. 在生产环境中，建议使用更安全的认证方式

## 故障排除
如果遇到连接问题：
1. 检查网络连接：`ping 50.18.226.67`
2. 检查端口是否开放：`telnet 50.18.226.67 3306`
3. 确认EC2安全组允许你的IP访问3306端口 