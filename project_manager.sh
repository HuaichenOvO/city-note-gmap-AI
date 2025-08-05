#!/bin/bash

# City Note GMap AI 项目管理脚本
# 整合了连接、部署、数据库管理等功能

EC2_IP="44.243.89.138"
KEY_FILE="~/.ssh/OR-key.pem"

show_menu() {
    echo "=== City Note GMap AI 项目管理 ==="
    echo "1. 连接到EC2实例"
    echo "2. 部署项目到EC2"
    echo "3. 启动本地开发环境"
    echo "4. 初始化数据库"
    echo "5. 查看项目状态"
    echo "6. 清理项目"
    echo "0. 退出"
    echo ""
    read -p "请选择操作 (0-6): " choice
}

connect_ec2() {
    echo "连接到EC2实例..."
    ssh -i $KEY_FILE ec2-user@$EC2_IP
}

deploy_to_ec2() {
    echo "开始部署项目到EC2..."
    
    # 更新配置文件
    sed -i '' 's/50.18.226.67/44.243.89.138/g' backend/src/main/resources/application.properties
    sed -i '' 's|http://50.18.226.67:8081|http://44.243.89.138:8081|g' backend/src/main/resources/application.properties
    
    # 上传并部署
    scp -i $KEY_FILE -r backend/ ec2-user@$EC2_IP:~/city-note-app/
    scp -i $KEY_FILE -r frontend/ ec2-user@$EC2_IP:~/city-note-app/
    scp -i $KEY_FILE init_database.sql ec2-user@$EC2_IP:~/city-note-app/
    
    ssh -i $KEY_FILE ec2-user@$EC2_IP << 'DEPLOY'
        cd ~/city-note-app
        
        # 设置环境
        sudo yum install -y java-17-amazon-corretto mysql mysql-server firewalld
        sudo systemctl start mysqld
        sudo systemctl enable mysqld
        
        # 安装Node.js
        curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash
        source ~/.bashrc
        nvm install 18
        nvm use 18
        
        # 配置防火墙
        sudo systemctl start firewalld
        sudo firewall-cmd --permanent --add-port=8080/tcp
        sudo firewall-cmd --permanent --add-port=5173/tcp
        sudo firewall-cmd --reload
        
        # 初始化数据库
        mysql -u root < init_database.sql
        
        # 部署后端
        cd backend
        ./mvnw clean package -DskipTests
        nohup java -jar target/city-note-backend-0.0.1-SNAPSHOT.jar > backend.log 2>&1 &
        
        # 部署前端
        cd ../frontend
        npm install
        npm run build
        npm install -g serve
        nohup serve -s dist -l 5173 > frontend.log 2>&1 &
        
        echo "部署完成！"
DEPLOY
    
    echo "✅ 部署完成！"
    echo "后端: http://44.243.89.138:8080"
    echo "前端: http://44.243.89.138:5173"
}

start_local_dev() {
    echo "启动本地开发环境..."
    
    # 检查MySQL是否运行
    if ! pgrep -x "mysqld" > /dev/null; then
        echo "⚠️  MySQL未运行，请先启动MySQL"
        echo "macOS: brew services start mysql"
        return
    fi
    
    # 启动后端
    echo "启动Spring Boot后端..."
    cd backend
    ./mvnw spring-boot:run -Dspring.profiles.active=local &
    cd ..
    
    # 启动前端
    echo "启动React前端..."
    cd frontend
    npm start &
    cd ..
    
    echo "✅ 本地开发环境启动完成！"
    echo "前端: http://localhost:5173"
    echo "后端: http://localhost:8080"
}

init_database() {
    echo "初始化数据库..."
    
    if command -v mysql &> /dev/null; then
        mysql -u root < init_database.sql
        echo "✅ 数据库初始化完成！"
    else
        echo "❌ MySQL未安装或未在PATH中"
    fi
}

show_status() {
    echo "=== 项目状态 ==="
    
    # 检查本地服务
    echo "本地服务状态:"
    if pgrep -f "spring-boot" > /dev/null; then
        echo "✅ Spring Boot后端运行中"
    else
        echo "❌ Spring Boot后端未运行"
    fi
    
    if pgrep -f "vite" > /dev/null; then
        echo "✅ React前端运行中"
    else
        echo "❌ React前端未运行"
    fi
    
    # 检查EC2连接
    echo ""
    echo "EC2连接状态:"
    if ssh -o ConnectTimeout=5 -o BatchMode=yes -i $KEY_FILE ec2-user@$EC2_IP "echo 'connected'" 2>/dev/null; then
        echo "✅ EC2连接正常"
    else
        echo "❌ EC2连接失败"
    fi
}

cleanup() {
    echo "清理项目..."
    
    # 停止本地服务
    pkill -f "spring-boot"
    pkill -f "vite"
    
    # 清理构建文件
    rm -rf backend/target
    rm -rf frontend/dist
    rm -rf frontend/node_modules
    
    echo "✅ 清理完成！"
}

# 主循环
while true; do
    show_menu
    case $choice in
        1) connect_ec2 ;;
        2) deploy_to_ec2 ;;
        3) start_local_dev ;;
        4) init_database ;;
        5) show_status ;;
        6) cleanup ;;
        0) echo "再见！"; exit 0 ;;
        *) echo "无效选择，请重试" ;;
    esac
    
    echo ""
    read -p "按回车键继续..."
done 