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

# Function to cleanup processes
cleanup() {
    echo -e "\n${YELLOW}Stopping services...${NC}"
    
    # Kill backend process
    if [ ! -z "$BACKEND_PID" ]; then
        kill $BACKEND_PID 2>/dev/null || true
    fi
    
    # Kill frontend process
    if [ ! -z "$FRONTEND_PID" ]; then
        kill $FRONTEND_PID 2>/dev/null || true
    fi
    
    # Kill any remaining processes
    pkill -f "spring-boot:run" 2>/dev/null || true
    pkill -f "npm start" 2>/dev/null || true
    pkill -f "vite" 2>/dev/null || true
    
    echo -e "${GREEN}Services stopped.${NC}"
    exit 0
}

# Set trap for cleanup
trap cleanup INT TERM

# Start backend
echo -e "${YELLOW}Starting backend service...${NC}"
cd backend

# Kill any existing backend process
pkill -f "spring-boot:run" 2>/dev/null || true

# Start backend
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
    cleanup
fi

# Start frontend
echo -e "${YELLOW}Starting frontend service...${NC}"
cd frontend

# Kill any existing frontend process
pkill -f "npm start" 2>/dev/null || true
pkill -f "vite" 2>/dev/null || true

# Check if node_modules exists, if not install dependencies
if [ ! -d "node_modules" ]; then
    echo "Installing frontend dependencies..."
    npm install
fi

# Start frontend
echo "Starting frontend development server..."
# Use npx to ensure we use the local vite installation
npx vite --host 0.0.0.0 --port 5173 &
FRONTEND_PID=$!
cd ..

# Wait for frontend to start
echo -e "${YELLOW}Waiting for frontend service to start...${NC}"
sleep 5

# Check if frontend is running
if kill -0 $FRONTEND_PID 2>/dev/null; then
    echo -e "${GREEN}Frontend service started successfully!${NC}"
else
    echo -e "${RED}Frontend service failed to start!${NC}"
    cleanup
fi

echo -e "${GREEN}Local development environment startup completed!${NC}"
echo -e "${GREEN}Frontend: http://localhost:5173${NC}"
echo -e "${GREEN}Backend: http://localhost:8080${NC}"
echo ""
echo -e "${YELLOW}Press Ctrl+C to stop services${NC}"

# Keep script running
echo -e "${YELLOW}Services are running. Press Ctrl+C to stop.${NC}"

# Simple monitoring
while true; do
    sleep 10
    
    # Check if processes are still running
    if ! kill -0 $BACKEND_PID 2>/dev/null; then
        echo -e "${RED}Backend service stopped unexpectedly!${NC}"
        break
    fi
    
    if ! kill -0 $FRONTEND_PID 2>/dev/null; then
        echo -e "${RED}Frontend service stopped unexpectedly!${NC}"
        break
    fi
    
    echo -e "${GREEN}✓ Both services are running normally${NC}"
done

# If we reach here, one of the services stopped
cleanup
