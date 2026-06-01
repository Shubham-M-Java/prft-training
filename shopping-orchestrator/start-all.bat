@echo off
set JAVA_HOME=C:\eclipse\plugins\org.eclipse.justj.openjdk.hotspot.jre.full.win32.x86_64_17.0.1.v20211116-1657\jre
set MVN=C:\maven\apache-maven-3.9.6\bin\mvn.cmd
set BACKEND=%~dp0backend
set PATH=%JAVA_HOME%\bin;%PATH%

echo ============================================
echo  Shopping Orchestrator AI - Starting All Services
echo  Java: %JAVA_HOME%
echo  Maven: %MVN%
echo ============================================
echo.

echo [1/7] Starting Eureka Server (port 8761)...
start "Eureka Server" cmd /k "set JAVA_HOME=%JAVA_HOME% && set PATH=%JAVA_HOME%\bin;%PATH% && cd /d %BACKEND%\eureka-server && %MVN% spring-boot:run"
ping -n 21 127.0.0.1 > nul

echo [2/7] Starting Product Research Agent (port 8082)...
start "Product Research Agent" cmd /k "set JAVA_HOME=%JAVA_HOME% && set PATH=%JAVA_HOME%\bin;%PATH% && cd /d %BACKEND%\product-research-agent && %MVN% spring-boot:run"
ping -n 6 127.0.0.1 > nul

echo [3/7] Starting Comparison Agent (port 8083)...
start "Comparison Agent" cmd /k "set JAVA_HOME=%JAVA_HOME% && set PATH=%JAVA_HOME%\bin;%PATH% && cd /d %BACKEND%\comparison-agent && %MVN% spring-boot:run"
ping -n 6 127.0.0.1 > nul

echo [4/7] Starting Budget Agent (port 8084)...
start "Budget Agent" cmd /k "set JAVA_HOME=%JAVA_HOME% && set PATH=%JAVA_HOME%\bin;%PATH% && cd /d %BACKEND%\budget-agent && %MVN% spring-boot:run"
ping -n 6 127.0.0.1 > nul

echo [5/7] Starting Recommendation Agent (port 8085)...
start "Recommendation Agent" cmd /k "set JAVA_HOME=%JAVA_HOME% && set PATH=%JAVA_HOME%\bin;%PATH% && cd /d %BACKEND%\recommendation-agent && %MVN% spring-boot:run"
ping -n 6 127.0.0.1 > nul

echo [6/7] Starting Orchestrator Service (port 8081)...
start "Orchestrator Service" cmd /k "set JAVA_HOME=%JAVA_HOME% && set PATH=%JAVA_HOME%\bin;%PATH% && cd /d %BACKEND%\orchestrator-service && %MVN% spring-boot:run"
ping -n 11 127.0.0.1 > nul

echo [7/7] Starting API Gateway (port 8090)...
start "API Gateway" cmd /k "set JAVA_HOME=%JAVA_HOME% && set PATH=%JAVA_HOME%\bin;%PATH% && cd /d %BACKEND%\api-gateway && %MVN% spring-boot:run"

echo.
echo ============================================
echo  All backend services started!
echo.
echo  Eureka Dashboard:  http://localhost:8761
echo  API Gateway:       http://localhost:8090
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
