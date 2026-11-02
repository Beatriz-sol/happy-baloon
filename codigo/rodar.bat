@echo off
setlocal enabledelayedexpansion
rem Compila e roda o Jogo do Balao. Precisa apenas do JDK 17 ou mais novo.
cd /d "%~dp0"

rem Usa o java.exe da mesma pasta do javac (o JDK novo), e nao um Java 8 antigo que esteja no PATH
set "JDK_BIN="
for /f "delims=" %%j in ('where javac 2^>nul') do if not defined JDK_BIN set "JDK_BIN=%%~dpj"
if not defined JDK_BIN (
  echo O comando javac nao foi encontrado. Instale o JDK 21 ou 25 em https://adoptium.net
  pause
  exit /b 1
)
echo Usando o Java de !JDK_BIN!

if exist out\rodar rmdir /s /q out\rodar
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
"!JDK_BIN!javac.exe" -encoding UTF-8 -cp "libs\*" -d out\rodar @fontes.txt
set erro=!errorlevel!
del fontes.txt
if not "!erro!"=="0" (
  echo.
  echo Nao foi possivel compilar. Veja as mensagens acima.
  pause
  exit /b 1
)

echo Abrindo o jogo...
cd assets
"!JDK_BIN!java.exe" -cp "..\out\rodar;..\libs\*" br.mackenzie.lwjgl3.Lwjgl3Launcher > ..\log-do-jogo.txt 2>&1
if errorlevel 1 (
  echo.
  echo O jogo fechou com erro. Mensagens salvas em codigo\log-do-jogo.txt:
  echo.
  type ..\log-do-jogo.txt
  pause
  exit /b 1
)
