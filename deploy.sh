#!/bin/bash

# City Note Unified Deployment Script
# Handles local build and EC2 deployment

set -e  # Exit on error

# Configuration variables - Please modify these variables according to your environment
EC2_HOST="44.243.89.138"
EC2_USER="ec2-user"
PROJECT_NAME="city-note"
BACKEND_PORT="8080"
DEPLOY_DIR="deploy_package"

# SSH Key Configuration - Please modify with your pem filename
# Example: SSH_KEY="~/.ssh/my-key.pem" or SSH_KEY="~/.ssh/aws-key.pem"
SSH_KEY="~/.ssh/OR-key.pem"

# Color output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Function: Check SSH key
check_ssh_key() {
    echo -e "${YELLOW}Checking SSH key...${NC}"
    
    # Expand ~ symbol in path
    SSH_KEY_EXPANDED=$(eval echo "$SSH_KEY")
    
    if [ ! -f "$SSH_KEY_EXPANDED" ]; then
        echo -e "${RED}Error: SSH key file does not exist: $SSH_KEY_EXPANDED${NC}"
        echo -e "${YELLOW}Please modify the SSH_KEY variable at the top of the script to specify the correct pem file path${NC}"
        echo -e "${YELLOW}Example: SSH_KEY=\"~/.ssh/your-key.pem\"${NC}"
        exit 1
    fi
    
    # Check file permissions
    if [ "$(stat -c %a "$SSH_KEY_EXPANDED" 2>/dev/null || stat -f %Lp "$SSH_KEY_EXPANDED" 2>/dev/null)" != "600" ]; then
        echo -e "${YELLOW}Setting SSH key file permissions to 600...${NC}"
        chmod 600 "$SSH_KEY_EXPANDED" 2>/dev/null || true
    fi
    
    echo -e "${GREEN}SSH key check passed: $SSH_KEY_EXPANDED${NC}"
}

# Function: Check local environment
check_local_env() {
    echo -e "${YELLOW}1. Checking local environment...${NC}"
    
    # Check Java
    if ! command -v java &> /dev/null; then
        echo -e "${RED}Error: Java not found${NC}"
        exit 1
    fi
    
    # Check Node.js
    if ! command -v node &> /dev/null; then
        echo -e "${RED}Error: Node.js not found${NC}"
        exit 1
    fi
    
    # Check Maven
    if ! command -v mvn &> /dev/null; then
        echo -e "${RED}Error: Maven not found${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}Local environment check completed${NC}"
}

# Function: Build application
build_app() {
    echo -e "${YELLOW}2. Building application...${NC}"
    
    # Run tests
    echo "Running tests..."
    ./run-tests.sh
    if [ $? -ne 0 ]; then
        echo -e "${RED}Tests failed! Deployment stopped${NC}"
        exit 1
    fi
    echo -e "${GREEN}Tests passed!${NC}"
    
    # Build backend
    echo "Building backend..."
    cd backend
    mvn clean package -DskipTests
    cd ..
    
    # Build frontend
    echo "Building frontend..."
    cd frontend
    npm install
    npm run build
    cd ..
}

# Function: Prepare deployment package
prepare_deploy() {
    echo -e "${YELLOW}3. Preparing deployment package...${NC}"
    
    rm -rf $DEPLOY_DIR
    mkdir -p $DEPLOY_DIR
    
    # Read local .env file
    if [ -f "backend/.env" ]; then
        echo "Reading local .env file..."
        OPENAI_API_KEY=$(grep "openai.api-key=" backend/.env | cut -d'=' -f2)
        JWT_SECRET=$(grep "jwt.secret=" backend/.env | cut -d'=' -f2)
        
        if [ -z "$OPENAI_API_KEY" ] || [ -z "$JWT_SECRET" ]; then
            echo -e "${RED}Error: Unable to read configuration from .env file${NC}"
            exit 1
        fi
        
        echo "Configuration read: OpenAI API Key: ${OPENAI_API_KEY:0:20}..., JWT Secret: ${JWT_SECRET:0:20}..."
    else
        echo -e "${YELLOW}Warning: backend/.env file not found, using default configuration${NC}"
        OPENAI_API_KEY="your-openai-api-key-here"
        JWT_SECRET="your-secret-key-here-make-it-long-and-secure-for-production"
    fi
    
    # Copy files
    cp backend/target/*.jar $DEPLOY_DIR/city-note-backend.jar
    cp -r frontend/dist $DEPLOY_DIR/frontend
    cp nginx.conf $DEPLOY_DIR/
    cp systemd-backend.service $DEPLOY_DIR/
    
    # Create updated configuration files
    echo "Creating updated configuration files..."
    
    # Update application.properties
    cat > $DEPLOY_DIR/application.properties << EOF
# Common Configuration
server.servlet.context-path=/api
server.port=8080

# Database Configuration - Using EC2 Database for all environments
spring.datasource.url=jdbc:mysql://44.243.89.138:3306/citynote?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=root
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# MyBatis configuration
mybatis.mapper-locations=classpath*:mapper/*.xml
mybatis.configuration.map-underscore-to-camel-case=true

# JPA Common Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
spring.jpa.open-in-view=false

# Common CORS Configuration
cors.allowed-origins=http://localhost:5173,http://localhost:5174,http://44.243.89.138
cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
cors.allowed-headers=*
cors.allow-credentials=true

# File Upload Common Configuration
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=50MB

# OpenAI Base Configuration
openai.base-url=https://api.openai.com/v1

# JWT Configuration
jwt.secret=$JWT_SECRET
jwt.expiration=86400
EOF

    # Update application-prod.properties
    cat > $DEPLOY_DIR/application-prod.properties << EOF
# Production Settings
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=false

# Production CORS
cors.allowed-origins=http://44.243.89.138,https://44.243.89.138

# Production File Upload
file.upload.path=/opt/city-note/uploads/

# Production Logging
logging.level.com.citynote=INFO
logging.level.org.springframework.web=INFO
logging.level.org.hibernate.SQL=WARN
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=WARN

# Performance Configuration
spring.jpa.properties.hibernate.jdbc.batch_size=20
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true

# JWT Configuration for Production
jwt.secret=$JWT_SECRET
jwt.expiration=86400

# OpenAI Configuration for Production
openai.api-key=$OPENAI_API_KEY
openai.base-url=https://api.openai.com/v1
EOF
}

# Function: Configure and start services on EC2
configure_ec2() {
    echo -e "${YELLOW}4. Configuring EC2 server...${NC}"
    
    # Create remote command
    cat > $DEPLOY_DIR/setup.sh << 'EOF'
#!/bin/bash
set -e

# 1. Update system and install dependencies
sudo dnf update -y
sudo dnf install -y nginx java-17-amazon-corretto

# 2. Create application directories
sudo mkdir -p /opt/city-note
sudo mkdir -p /opt/city-note/uploads
sudo chown -R ec2-user:ec2-user /opt/city-note

# 3. Copy files
sudo cp city-note-backend.jar /opt/city-note/
sudo cp -r frontend/* /opt/city-note/frontend/
sudo cp application.properties /opt/city-note/
sudo cp application-prod.properties /opt/city-note/
sudo chown -R ec2-user:ec2-user /opt/city-note

# 4. Configure Nginx
sudo mkdir -p /etc/nginx/conf.d
sudo cp nginx.conf /etc/nginx/conf.d/city-note.conf
sudo rm -f /etc/nginx/nginx.conf
sudo mv /etc/nginx/conf.d/city-note.conf /etc/nginx/nginx.conf
sudo nginx -t
sudo systemctl restart nginx

# 5. Configure backend service
sudo cp systemd-backend.service /etc/systemd/system/city-note-backend.service
sudo systemctl daemon-reload
sudo systemctl enable city-note-backend
sudo systemctl restart city-note-backend

# 6. Configure security group
# Note: In AWS, port access should be configured through EC2 security groups
# Here we ensure iptables doesn't block any traffic
sudo iptables -P INPUT ACCEPT
sudo iptables -P FORWARD ACCEPT
sudo iptables -P OUTPUT ACCEPT
sudo iptables -F

echo "Deployment completed!"
EOF
    
    chmod +x $DEPLOY_DIR/setup.sh
}

# Function: Deploy to EC2
deploy_to_ec2() {
    echo -e "${YELLOW}5. Deploying to EC2...${NC}"
    
    # Expand SSH key path
    SSH_KEY_EXPANDED=$(eval echo "$SSH_KEY")
    
    # Upload deployment package
    echo "Uploading files..."
    scp -i "$SSH_KEY_EXPANDED" -r $DEPLOY_DIR/* $EC2_USER@$EC2_HOST:~/deploy_package/
    
    # Execute remote configuration
    echo "Executing remote configuration..."
    ssh -i "$SSH_KEY_EXPANDED" $EC2_USER@$EC2_HOST "cd ~/deploy_package && chmod +x setup.sh && ./setup.sh"
}

# Function: Cleanup
cleanup() {
    echo -e "${YELLOW}6. Cleaning up temporary files...${NC}"
    rm -rf $DEPLOY_DIR
}

# Main process
main() {
    echo -e "${GREEN}Starting City Note project deployment...${NC}"
    
    check_ssh_key
    check_local_env
    build_app
    prepare_deploy
    configure_ec2
    deploy_to_ec2
    cleanup
    
    echo -e "${GREEN}Deployment completed!${NC}"
    echo -e "${GREEN}Frontend access address: http://$EC2_HOST${NC}"
    echo -e "${GREEN}Backend API address: http://$EC2_HOST/api${NC}"
}

# Execute main process
main