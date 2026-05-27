@echo off
echo ============================================
echo  Shopping Orchestrator AI - Starting All Services
echo ============================================
echo.

echo [1/6] Starting Eureka Server (port 8761)...
start "Eureka Server" cmd /k "cd /d %~dp0backend\eureka-server && mvn spring-boot:run"
timeout /t 15 /nobreak > nul

echo [2/6] Starting Product Research Agent (port 8082)...
start "Product Research Agent" cmd /k "cd /d %~dp0backend\product-research-agent && mvn spring-boot:run"
timeout /t 5 /nobreak > nul

echo [3/6] Starting Comparison Agent (port 8083)...
start "Comparison Agent" cmd /k "cd /d %~dp0backend\comparison-agent && mvn spring-boot:run"
timeout /t 5 /nobreak > nul

echo [4/6] Starting Budget Agent (port 8084)...
start "Budget Agent" cmd /k "cd /d %~dp0backend\budget-agent && mvn spring-boot:run"
timeout /t 5 /nobreak > nul

echo [5/6] Starting Recommendation Agent (port 8085)...
start "Recommendation Agent" cmd /k "cd /d %~dp0backend\recommendation-agent && mvn spring-boot:run"
timeout /t 5 /nobreak > nul

echo [6/6] Starting Orchestrator Service (port 8081)...
start "Orchestrator Service" cmd /k "cd /d %~dp0backend\orchestrator-service && mvn spring-boot:run"
timeout /t 10 /nobreak > nul

echo [7/7] Starting API Gateway (port 8080)...
start "API Gateway" cmd /k "cd /d %~dp0backend\api-gateway && mvn spring-boot:run"

echo.
echo ============================================
echo  All backend services started!
echo.
echo  Eureka Dashboard:  http://localhost:8761
echo  API Gateway:       http://localhost:8080
echo  Orchestrator:      http://localhost:8081
echo  Research Agent:    http://localhost:8082
echo  Comparison Agent:  http://localhost:8083
echo  Budget Agent:      http://localhost:8084
echo  Recommendation:    http://localhost:8085
echo ============================================
echo.
echo  To start Angular frontend:
echo  cd frontend ^&^& npm install ^&^& npm start
echo  Frontend URL: http://localhost:4200
echo ============================================
pause
