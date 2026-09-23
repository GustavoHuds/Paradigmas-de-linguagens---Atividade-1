@echo off
REM Compila e executa o Sistema de Estoque de Produtos (Windows).
REM   executar.bat        -> versao de console (EstoqueApp, exigida no enunciado)
REM   executar.bat gui    -> interface grafica (EstoqueGUI)
chcp 65001 >nul
cd /d "%~dp0"

if exist out rmdir /s /q out
javac -encoding UTF-8 -d out src\estoque\*.java src\estoque\excecoes\*.java src\estoque\produtos\*.java src\estoque\gui\*.java
if errorlevel 1 (
    echo.
    echo Falha na compilacao. Verifique se o JDK 8 ou superior esta instalado e no PATH.
    pause
    exit /b 1
)

if /i "%~1"=="gui" (
    start "" javaw -cp out estoque.gui.EstoqueGUI
) else (
    java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -cp out estoque.EstoqueApp
    echo.
    pause
)
