@echo off
REM Compila e abre a interface grafica do Sistema de Estoque de Produtos.
REM Basta dar dois cliques neste arquivo.
cd /d "%~dp0"

if exist out rmdir /s /q out
javac -encoding UTF-8 -d out src\estoque\*.java src\estoque\excecoes\*.java src\estoque\produtos\*.java src\estoque\gui\*.java
if errorlevel 1 (
    echo.
    echo Falha na compilacao. Verifique se o JDK 8 ou superior esta instalado e no PATH.
    pause
    exit /b 1
)

start "" javaw -cp out estoque.gui.EstoqueGUI
