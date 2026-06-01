@echo off
set JAVA_BIN=C:\eclipse\plugins\org.eclipse.justj.openjdk.hotspot.jre.full.win32.x86_64_17.0.1.v20211116-1657\jre\bin
set MVN=C:\maven\apache-maven-3.9.6\bin\mvn.cmd
set BACKEND=%~dp0backend
set PATH=%JAVA_BIN%;%PATH%

if not exist "%MVN%" (
    where /q mvn
    if errorlevel 1 (
        echo ============================================
        echo  ERROR: Maven executable not found.
        echo  Install Maven or add it to your PATH, or update the MVN path in start-backend.bat.
        echo  Example: set MVN=C:\Program Files\apache-maven-3.9.6\bin\mvn.cmd
        echo ============================================
        pause
        exit /b 1
    ) else (
        set MVN=mvn
    )
)

echo ============================================
echo  Shopping Orchestrator AI - Starting All Services
echo  Java bin: %JAVA_BIN%
echo  Maven: %MVN%
echo ============================================
echo.

echo [1/6] Starting Eureka Server (port 8761)...
start "Eureka Server" cmd /c "cd /d %BACKEND%\eureka-server && %MVN% spring-boot:run"
timeout /t 20 /nobreak > nul

echo [2/6] Starting Product Research Agent (port 8082)...
start "Product Research Agent" cmd /c "cd /d %BACKEND%\product-research-agent && %MVN% spring-boot:run"
timeout /t 5 /nobreak > nul

echo [3/6] Starting Comparison Agent (port 8083)...
start "Comparison Agent" cmd /c "cd /d %BACKEND%\comparison-agent && %MVN% spring-boot:run"
timeout /t 5 /nobreak > nul

echo [4/6] Starting Budget Agent (port 8084)...
start "Budget Agent" cmd /c "cd /d %BACKEND%\budget-agent && %MVN% spring-boot:run"
timeout /t 5 /nobreak > nul

echo [5/6] Starting Recommendation Agent (port 8085)...
start "Recommendation Agent" cmd /c "cd /d %BACKEND%\recommendation-agent && %MVN% spring-boot:run"
timeout /t 5 /nobreak > nul

echo [6/6] Starting Orchestrator Service (port 8081)...
start "Orchestrator Service" cmd /c "cd /d %BACKEND%\orchestrator-service && %MVN% spring-boot:run"
timeout /t 10 /nobreak > nul

echo [7/7] Starting API Gateway (port 8090)...
start "API Gateway" cmd /c "cd /d %BACKEND%\api-gateway && %MVN% spring-boot:run"

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
echo  Frontend URL: http://localhost:4200
echo ============================================
pause
