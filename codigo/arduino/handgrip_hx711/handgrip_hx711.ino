// Jogo do Balão - handgrip com célula de carga e módulo HX711 (opção B)
// Ligação: HX711 VCC -> 5V, GND -> GND, DT -> D3, SCK -> D2
// Precisa da biblioteca HX711 (de Bogdan Necula), instalada pela Arduino IDE.
// Envia a força em gramas pela serial (USB), a 9600 baud, em linhas "Forca: N".
#include "HX711.h"

const int PINO_DT = 3;
const int PINO_SCK = 2;
// Descubra este valor na calibração: pendure um peso conhecido e ajuste
// até o Monitor Serial mostrar o peso certo em gramas
const float FATOR_CALIBRACAO = 1.0;

HX711 balanca;

void setup() {
  Serial.begin(9600);
  balanca.begin(PINO_DT, PINO_SCK);
  balanca.set_scale(FATOR_CALIBRACAO);
  balanca.tare();  // zera com a mão solta
}

void loop() {
  float gramas = balanca.get_units(3);
  if (gramas < 0) gramas = 0;
  Serial.print("Forca: ");
  Serial.println((int) gramas);
  delay(20);
}
