#!/bin/bash

# City Note Local Development Script
# Used to start local development environment

set -e

# Color output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${GREEN}Starting City Note local development environment...${NC}"

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

# Start backend
echo -e "${YELLOW}Starting backend service...${NC}"
cd backend
./mvnw spring-boot:run -Dspring.profiles.active=local > backend.log 2>&1 &
BACKEND_PID=$!
cd ..

# Wait for backend service to fully start
echo -e "${YELLOW}Waiting for backend service to start...${NC}"
MAX_ATTEMPTS=30
ATTEMPT=0
BACKEND_READY=false

while [ $ATTEMPT -lt $MAX_ATTEMPTS ] && [ "$BACKEND_READY" = false ]; do
    ATTEMPT=$((ATTEMPT + 1))
    echo -n "."
    
    # Check if backend is running on port 8080
    if curl -s http://localhost:8080/api/health > /dev/null 2>&1 || curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
        BACKEND_READY=true
        echo ""
        echo -e "${GREEN}Backend service started successfully!${NC}"
    else
        # Check if process is still running
        if ! kill -0 $BACKEND_PID 2>/dev/null; then
            echo ""
            echo -e "${RED}Backend service failed to start! Please check backend.log file${NC}"
            exit 1
        fi
        sleep 2
    fi
done

if [ "$BACKEND_READY" = false ]; then
    echo ""
    echo -e "${RED}Backend service startup timeout! Please check backend.log file${NC}"
    kill $BACKEND_PID 2>/dev/null
    exit 1
fi

# Start frontend
echo -e "${YELLOW}Starting frontend service...${NC}"
cd frontend
npm install
npm start &
FRONTEND_PID=$!
cd ..

echo -e "${GREEN}Local development environment startup completed!${NC}"
echo -e "${GREEN}Frontend: http://localhost:5173${NC}"
echo -e "${GREEN}Backend: http://localhost:8080${NC}"
echo ""
echo -e "${YELLOW}Press Ctrl+C to stop services${NC}"

# Wait for user interrupt
trap "echo -e '\n${YELLOW}Stopping services...${NC}'; kill $BACKEND_PID $FRONTEND_PID 2>/dev/null; exit 0" INT

wait
