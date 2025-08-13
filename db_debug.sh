#!/bin/bash

# MySQL连接配置
HOST="44.243.89.138"
PORT="3306"
USER="root"
PASSWORD="root"
DATABASE="citynote"

echo "=== 远程EC2 MySQL数据库调试工具 ==="
echo "主机: $HOST:$PORT"
echo "数据库: $DATABASE"
echo ""

# 显示所有表
echo "1. 显示所有表:"
mysql -h $HOST -P $PORT -u $USER -p$PASSWORD $DATABASE -e "SHOW TABLES;"
echo ""

# 显示用户表结构
echo "2. 用户表结构:"
mysql -h $HOST -P $PORT -u $USER -p$PASSWORD $DATABASE -e "DESCRIBE users;"
echo ""

# 显示事件表结构
echo "3. 事件表结构:"
mysql -h $HOST -P $PORT -u $USER -p$PASSWORD $DATABASE -e "DESCRIBE events;"
echo ""

# 显示用户数量
echo "4. 用户数量:"
mysql -h $HOST -P $PORT -u $USER -p$PASSWORD $DATABASE -e "SELECT COUNT(*) as user_count FROM users;"
echo ""

# 显示事件数量
echo "5. 事件数量:"
mysql -h $HOST -P $PORT -u $USER -p$PASSWORD $DATABASE -e "SELECT COUNT(*) as event_count FROM events;"
echo ""

# 显示最近的5个事件
echo "6. 最近的5个事件:"
mysql -h $HOST -P $PORT -u $USER -p$PASSWORD $DATABASE -e "SELECT event_id, title, event_type, create_date FROM events ORDER BY create_date DESC LIMIT 5;"
echo ""

echo "=== 调试完成 ===" 