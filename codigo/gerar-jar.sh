#!/bin/sh
# Gera o executável dist/JogoBalao.jar no Linux ou macOS, com o jogo, as imagens, os sons e as bibliotecas juntos.
# O jar é compilado para Java 8, então roda mesmo em computadores com Java antigo.
cd "$(dirname "$0")" || exit 1
rm -rf out/jar
mkdir -p out/jar/classes dist
FONTES=$(find core/src/main/java lwjgl3/src/main/java -name '*.java')
javac -encoding UTF-8 --release 8 -nowarn -cp "libs/*" -d out/jar/classes $FONTES 2>/dev/null \
  || javac -encoding UTF-8 --release 17 -cp "libs/*" -d out/jar/classes $FONTES || exit 1
(cd out/jar/classes && for lib in ../../../libs/*.jar; do jar xf "$lib"; done)
rm -f out/jar/classes/META-INF/*.SF out/jar/classes/META-INF/*.RSA out/jar/classes/META-INF/*.DSA out/jar/classes/META-INF/INDEX.LIST
cp -R assets/. out/jar/classes/
printf 'Main-Class: br.mackenzie.lwjgl3.Lwjgl3Launcher\nMulti-Release: true\n' > out/jar/manifest.txt
rm -f dist/JogoBalao.jar
jar cfm dist/JogoBalao.jar out/jar/manifest.txt -C out/jar/classes . || exit 1
rm -rf out/jar
echo "Pronto: dist/JogoBalao.jar (rode com: java -jar dist/JogoBalao.jar)"
