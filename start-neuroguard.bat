@echo off
setlocal EnableExtensions
pushd "%~dp0"

echo Starting NeuroGuard Microservices and Frontend...
echo.
echo [1/3] Freeing required ports...

call :KillPort 8761
call :KillPort 8083
call :KillPort 8089
call :KillPort 8090
call :KillPort 8084
call :KillPort 8082
call :KillPort 4200

echo.
echo [2/3] Launching services...

echo Starting Eureka Server...
start "Eureka Server" cmd /k "cd /d BackEnd\eureka-server && mvnw.cmd -DskipTests spring-boot:run"

echo Waiting for Eureka to become available on http://localhost:8761 ...
call :WaitForEureka

echo Starting Gateway Service...
start "Gateway Service" cmd /k "cd /d BackEnd\gateway && mvnw.cmd spring-boot:run"

echo Starting User Service...
start "User Service" cmd /k "cd /d BackEnd\user-service && npm start"

echo Starting Consultation Service...
start "Consultation Service" cmd /k "cd /d BackEnd\consultation-service && mvnw.cmd -DskipTests spring-boot:run"

echo Starting Careplan Service...
start "Careplan Service" cmd /k "cd /d BackEnd\careplan-service && mvnw.cmd -DskipTests spring-boot:run"

echo Starting Medical History Service...
start "Medical History Service" cmd /k "cd /d BackEnd\medical-history-service && mvnw.cmd -DskipTests spring-boot:run"

echo Starting Angular Frontend...
start "Angular Frontend" cmd /k "cd /d FrontEnd && npm start"

echo.
echo [3/3] Startup commands sent.
echo All services have been launched in separate windows.
pause
popd
exit /b 0

:KillPort
set "PORT=%~1"
set "FOUND="
for /f "tokens=5" %%P in ('netstat -ano ^| findstr /R /C:":%PORT% .*"') do (
	if not "%%P"=="0" (
		set "FOUND=1"
		echo   - Killing PID %%P on port %PORT%
		taskkill /F /PID %%P >nul 2>&1
	)
)
if not defined FOUND (
	echo   - Port %PORT% is already free
)
exit /b 0

:WaitForEureka
set /a RETRIES=45
:WaitForEurekaLoop
powershell -NoProfile -Command "try { $r = Invoke-WebRequest -Uri 'http://localhost:8761/' -UseBasicParsing -TimeoutSec 2; if ($r.StatusCode -ge 200 -and $r.StatusCode -lt 500) { exit 0 } else { exit 1 } } catch { exit 1 }" >nul 2>&1
if %errorlevel%==0 (
	echo   - Eureka is reachable
	exit /b 0
)
set /a RETRIES-=1
if %RETRIES% LEQ 0 (
	echo   - WARNING: Eureka is still not reachable. Continuing startup anyway.
	exit /b 0
)
echo   - Waiting for Eureka... (%RETRIES% retries left)
timeout /t 2 /nobreak >nul
goto :WaitForEurekaLoop
