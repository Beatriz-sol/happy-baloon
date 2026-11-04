@echo off
setlocal enabledelayedexpansion
rem Gera o executavel dist\JogoBalao.jar, com o jogo, as imagens, os sons e as bibliotecas juntos.
rem O jar e compilado para Java 8, entao abre com dois cliques mesmo em PCs com Java antigo.
cd /d "%~dp0"

set "JDK_BIN="
for /f "delims=" %%j in ('where javac 2^>nul') do if not defined JDK_BIN set "JDK_BIN=%%~dpj"
if not defined JDK_BIN (
  echo O comando javac nao foi encontrado. Instale o JDK 21 ou 25 em https://adoptium.net
  pause
  exit /b 1
)
echo Usando o Java de !JDK_BIN!

if exist out\jar rmdir /s /q out\jar
mkdir out\jar\classes
if exist fontes.txt del fontes.txt
for /r "core\src\main\java" %%f in (*.java) do (
  set "arquivo=%%f"
  echo "!arquivo:\=/!">> fontes.txt
)
for /r "lwjgl3\src\main\java" %%f in (*.java) do (
  set "arquivo=%%f"
  echo "!arquivo:\=/!">> fontes.txt
)

echo Compilando...
set "VERSAO=8"
"!JDK_BIN!javac.exe" -encoding UTF-8 --release 8 -nowarn -cp "libs\*" -d out\jar\classes @fontes.txt 2>nul
if errorlevel 1 (
  rem JDKs muito novos podem nao compilar mais para Java 8; nesse caso o jar exige Java 17
  set "VERSAO=17"
  "!JDK_BIN!javac.exe" -encoding UTF-8 --release 17 -cp "libs\*" -d out\jar\classes @fontes.txt
)
set erro=!errorlevel!
del fontes.txt
if not "!erro!"=="0" (
  echo.
  echo Nao foi possivel compilar. Veja as mensagens acima.
  pause
  exit /b 1
)

echo Juntando as bibliotecas...
pushd out\jar\classes
for %%l in (..\..\..\libs\*.jar) do "!JDK_BIN!jar.exe" xf "%%l"
popd
rem Assinaturas e indices das bibliotecas nao valem para o jar novo
del /q out\jar\classes\META-INF\*.SF out\jar\classes\META-INF\*.RSA out\jar\classes\META-INF\*.DSA out\jar\classes\META-INF\INDEX.LIST 2>nul
xcopy /e /i /q /y assets out\jar\classes >nul

> out\jar\manifest.txt echo Main-Class: br.mackenzie.lwjgl3.Lwjgl3Launcher
>> out\jar\manifest.txt echo Multi-Release: true
if not exist dist mkdir dist
if exist dist\JogoBalao.jar del dist\JogoBalao.jar
"!JDK_BIN!jar.exe" cfm dist\JogoBalao.jar out\jar\manifest.txt -C out\jar\classes .
if errorlevel 1 (
  echo Nao foi possivel montar o jar.
  pause
  exit /b 1
)
rmdir /s /q out\jar

echo.
echo Pronto: codigo\dist\JogoBalao.jar (precisa de Java !VERSAO! ou mais novo)
echo Abra com dois cliques ou com: java -jar dist\JogoBalao.jar
pause
