#!/usr/bin/env sh
# Compila e executa o Sistema de Estoque de Produtos (Linux/macOS).
#   ./executar.sh        -> versão de console (EstoqueApp, exigida no enunciado)
#   ./executar.sh gui    -> interface gráfica (EstoqueGUI)
set -e
cd "$(dirname "$0")"

rm -rf out
javac -encoding UTF-8 -d out $(find src -name "*.java")

if [ "$1" = "gui" ]; then
    java -cp out estoque.gui.EstoqueGUI
else
    java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -cp out estoque.EstoqueApp
fi
