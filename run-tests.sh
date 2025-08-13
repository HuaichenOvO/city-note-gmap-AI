#!/bin/bash

# City Note Test Runner Script
# Runs all tests for both frontend and backend

set -e

# Color output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${GREEN}Running City Note Tests...${NC}"

# Function to run backend tests
run_backend_tests() {
    echo -e "${YELLOW}Running backend tests...${NC}"
    cd backend
    
    # Check if Maven is available
    if ! command -v mvn &> /dev/null; then
        echo -e "${RED}Error: Maven not found${NC}"
        exit 1
    fi
    
    # Run tests
    mvn clean test
    if [ $? -ne 0 ]; then
        echo -e "${RED}Backend tests failed!${NC}"
        exit 1
    fi
    echo -e "${GREEN}Backend tests passed!${NC}"
    cd ..
}

# Function to run frontend tests
run_frontend_tests() {
    echo -e "${YELLOW}Running frontend tests...${NC}"
    cd frontend
    
    # Check if Node.js is available
    if ! command -v node &> /dev/null; then
        echo -e "${RED}Error: Node.js not found${NC}"
        exit 1
    fi
    
    # Install dependencies if needed
    if [ ! -d "node_modules" ]; then
        echo "Installing frontend dependencies..."
        npm install
    fi
    
    # Run tests
    npm test -- --run --reporter=verbose
    if [ $? -ne 0 ]; then
        echo -e "${RED}Frontend tests failed!${NC}"
        exit 1
    fi
    echo -e "${GREEN}Frontend tests passed!${NC}"
    cd ..
}

# Function to run integration tests (placeholder)
run_integration_tests() {
    echo -e "${YELLOW}Running integration tests...${NC}"
    echo -e "${YELLOW}Note: Integration tests not implemented yet${NC}"
    echo -e "${YELLOW}You can add end-to-end tests here using tools like Playwright or Cypress${NC}"
}

# Main execution
main() {
    # Run backend tests
    run_backend_tests
    
    # Run frontend tests
    run_frontend_tests
    
    # Run integration tests (optional)
    # run_integration_tests
    
    echo -e "${GREEN}All tests completed successfully!${NC}"
}

# Check command line arguments
case "${1:-all}" in
    "backend")
        run_backend_tests
        ;;
    "frontend")
        run_frontend_tests
        ;;
    "integration")
        run_integration_tests
        ;;
    "all"|*)
        main
        ;;
esac 