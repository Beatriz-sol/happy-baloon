#!/bin/sh
# Compila e roda o Jogo do Balão no Linux ou macOS. Precisa apenas do JDK 17 ou mais novo.
cd "$(dirname "$0")" || exit 1
rm -rf out/rodar
javac -encoding UTF-8 -cp "libs/*" -d out/rodar $(find core/src/main/java lwjgl3/src/main/java -name '*.java') || exit 1
cd assets || exit 1
if [ "$(uname)" = "Darwin" ]; then
  exec java -XstartOnFirstThread -cp "../out/rodar:../libs/*" br.mackenzie.lwjgl3.Lwjgl3Launcher
fi
exec java -cp "../out/rodar:../libs/*" br.mackenzie.lwjgl3.Lwjgl3Launcher
