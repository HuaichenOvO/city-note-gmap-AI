#!/bin/bash

# MySQL连接配置
HOST="50.18.226.67"
PORT="3306"
USER="root"
PASSWORD="root"
DATABASE="citynote"

echo "=== 加载数据库脚本 ==="
echo "主机: $HOST:$PORT"
echo "数据库: $DATABASE"
echo ""

# SQL文件路径
SQL_DIR="backend/src/main/resources/sql-history"

# 按顺序执行SQL文件
echo "1. 创建用户表..."
mysql -h $HOST -P $PORT -u $USER -p$PASSWORD $DATABASE < $SQL_DIR/0_create_users.sql

echo "2. 创建用户档案表..."
mysql -h $HOST -P $PORT -u $USER -p$PASSWORD $DATABASE < $SQL_DIR/1_c_user_profiles.sql

echo "3. 创建counties表..."
mysql -h $HOST -P $PORT -u $USER -p$PASSWORD $DATABASE < $SQL_DIR/2_c_counties.sql

echo "4. 创建events表..."
mysql -h $HOST -P $PORT -u $USER -p$PASSWORD $DATABASE < $SQL_DIR/3_c_events.sql

echo "5. 创建blobs表..."
mysql -h $HOST -P $PORT -u $USER -p$PASSWORD $DATABASE < $SQL_DIR/4_c_blobs.sql

echo "6. 插入counties数据..."
mysql -h $HOST -P $PORT -u $USER -p$PASSWORD $DATABASE < $SQL_DIR/5_insert_counties.sql

echo "7. 插入用户数据..."
mysql -h $HOST -P $PORT -u $USER -p$PASSWORD $DATABASE < $SQL_DIR/6_i_users.sql

echo "8. 添加事件点赞唯一约束..."
mysql -h $HOST -P $PORT -u $USER -p$PASSWORD $DATABASE < $SQL_DIR/7_add_event_likes_unique_constraint.sql

echo ""
echo "✅ 数据库加载完成！"
echo ""
echo "验证数据："
echo "用户数量: $(mysql -h $HOST -P $PORT -u $USER -p$PASSWORD $DATABASE -e "SELECT COUNT(*) FROM users;" -s -N)"
echo "County数量: $(mysql -h $HOST -P $PORT -u $USER -p$PASSWORD $DATABASE -e "SELECT COUNT(*) FROM counties;" -s -N)"
echo "事件数量: $(mysql -h $HOST -P $PORT -u $USER -p$PASSWORD $DATABASE -e "SELECT COUNT(*) FROM events;" -s -N)" 