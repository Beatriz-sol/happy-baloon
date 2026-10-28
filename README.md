# Jogo do Balão

Jogo sério para reabilitação da força de preensão da mão, feito com libGDX.
O jogador segura a barra de ESPAÇO para "apertar": quanto mais tempo apertando, mais o balão sobe.

Autora: Beatriz Soares — disciplina de Jogos Digitais.

## Requisitos

- JDK 17 ou superior (testado com 17, 21 e 25).
- VS Code com as extensões **Extension Pack for Java** e **Gradle for Java**
  (o VS Code sugere as duas ao abrir a pasta).

## Como executar no VS Code

1. Abra a pasta do projeto (a que contém `settings.gradle`) com **Arquivo > Abrir Pasta**.
2. Aguarde a importação do Gradle terminar (barra de status embaixo). Na primeira vez ele baixa
   as dependências, então é preciso internet.
3. Pressione **F5** e escolha **Rodar o jogo**, ou use **Terminal > Executar Tarefa de Build**
   (Ctrl+Shift+B), que roda `gradlew lwjgl3:run`.

Se algo der errado depois de trocar de JDK, use **Ctrl+Shift+P > Java: Clean Java Language Server Workspace**.

## Pelo terminal

```bash
./gradlew lwjgl3:run   # roda o jogo (no Windows: gradlew.bat lwjgl3:run)
./gradlew lwjgl3:jar   # gera lwjgl3/build/libs/JogoBalao.jar
```

## Controles

- **ESPAÇO**: apertar (o balão sobe); soltar faz o balão descer.
- **ESC** ou botão **Pausar**: pausa, com opções Continuar, Reiniciar e Menu principal.

## Estrutura

- `core/.../ReabilitacaoGame.java` — classe principal e recursos compartilhados.
- `core/.../screens/` — menu principal e tela de jogo.
- `core/.../input/` — leitura do aperto (barra de espaço).
- `lwjgl3/` — inicializador desktop.
