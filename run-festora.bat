
@echo off
title Festora Application Launcher

set "ROOT=F:\Projects\Festora"

where mvn >nul 2>nul
if errorlevel 1 (
    echo Maven is not installed or not configured in PATH.
    pause
    exit /b 1
)

where npm >nul 2>nul
if errorlevel 1 (
    echo Node.js and npm are not installed or not configured in PATH.
    pause
    exit /b 1
)

if not exist "%ROOT%\backend\billing-service\pom.xml" (
    echo Billing service not found.
    pause
    exit /b 1
)

if not exist "%ROOT%\backend\festora-backend\pom.xml" (
    echo Main backend not found.
    pause
    exit /b 1
)

if not exist "%ROOT%\backend\festora-gateway\pom.xml" (
    echo Gateway not found.
    pause
    exit /b 1
)

if not exist "%ROOT%\frontend\package.json" (
    echo Frontend package.json not found.
    pause
    exit /b 1
)

start "Festora Billing" cmd /k "cd /d %ROOT%\backend\billing-service && mvn spring-boot:run"

start "Festora Backend" cmd /k "cd /d %ROOT%\backend\festora-backend && mvn spring-boot:run"

start "Festora Gateway" cmd /k "cd /d %ROOT%\backend\festora-gateway && mvn spring-boot:run"

start "Festora Frontend" cmd /k "cd /d %ROOT%\frontend && npm run dev"

echo.
echo Festora startup commands launched.
echo Frontend: http://localhost:5173
echo Gateway:  http://localhost:8080
echo Backend:  http://localhost:8081
echo Billing:  http://localhost:8091
echo.
pause
