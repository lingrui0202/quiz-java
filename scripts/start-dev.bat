@echo off
REM 一键起本机开发环境：MySQL（新窗口）+ Spring Boot 后端
REM 依赖：E:\Dev\tools\mysql.bat、E:\Dev\tools\maven、E:\Dev\tools\jdk17

setlocal
set PROJECT=%~dp0..
set JAVA_HOME=E:\Dev\tools\jdk17

echo [1/2] 启动 MySQL（会开一个新窗口，关掉那个窗口即停止 MySQL）
start "MySQL 8.4.6" cmd /c "E:\Dev\tools\mysql.bat start"

echo [2/2] 等待 MySQL 就绪...
timeout /t 6 /nobreak >nul

echo 启动 Spring Boot（端口 8081）...
cd /d "%PROJECT%"
call E:\Dev\tools\maven\bin\mvn.cmd spring-boot:run

endlocal
