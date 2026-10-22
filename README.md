# Jogo Handgrip

Jogo sério para reabilitação da força de preensão da mão, feito com libGDX e controlado
por um handgrip adaptado ligado a um Arduino pela porta serial.

Autora: Beatriz Soares — disciplina de Jogos Digitais.

## Como executar

Requer Java 17 ou superior.

```bash
./gradlew lwjgl3:run                      # com o handgrip
cd # sem o dispositivo (segure ESPAÇO)
./gradlew lwjgl3:jar                      # gera lwjgl3/build/libs/JogoHandgrip.jar
```

No Windows use `gradlew.bat` no lugar de `./gradlew`. Para listar as portas seriais
disponíveis, execute a classe `br.mackenzie.input.SerialPortLister`.

## Configuração do handgrip

Edite `assets/controle.properties` (ou coloque uma cópia ao lado do `.jar`):

| Chave       | Significado                                                       |
|-------------|-------------------------------------------------------------------|
| `porta`     | Porta serial (`COM3`, `/dev/ttyUSB0`...) ou `auto`                |
| `baud`      | Velocidade da serial (padrão 9600)                                |
| `forcaMin`  | Valor enviado pelo Arduino com a mão solta                        |
| `forcaMax`  | Valor enviado com a força máxima desejada                         |
| `simulador` | `true` para usar o teclado no lugar do handgrip (só para testes)  |

O Arduino deve enviar uma linha por leitura contendo um número, por exemplo `512` ou
`Forca: 512`. Um esboço está em `firmware/handgrip/handgrip.ino`.

## Estrutura

- `core/.../ReabilitacaoGame.java` — classe principal e recursos compartilhados.
- `core/.../screens/` — menu principal e tela de jogo (com pausa e reinício).
- `core/.../input/` — leitura serial do handgrip, simulador e configuração.
- `lwjgl3/` — inicializador desktop.
