#!/bin/bash

# MySQL连接配置
HOST="50.18.226.67"
PORT="3306"
USER="root"
PASSWORD="root"
DATABASE="citynote"

echo "连接到远程EC2 MySQL数据库..."
echo "主机: $HOST:$PORT"
echo "数据库: $DATABASE"
echo "用户: $USER"
echo ""

# 连接到MySQL数据库
mysql -h $HOST -P $PORT -u $USER -p$PASSWORD $DATABASE 