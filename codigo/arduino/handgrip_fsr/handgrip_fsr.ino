// Jogo do Balão - handgrip com sensor de força FSR (opção A)
// Ligação: 5V -> FSR -> A0 -> resistor de 10 kOhm -> GND
// Envia a força do aperto pela serial (USB), a 9600 baud, em linhas "Forca: N".
const int PINO_SENSOR = A0;
const int AMOSTRAS = 8;  // média de várias leituras para suavizar

void setup() {
  Serial.begin(9600);
}

void loop() {
  long soma = 0;
  for (int i = 0; i < AMOSTRAS; i++) {
    soma += analogRead(PINO_SENSOR);
    delay(2);
  }
  int forca = soma / AMOSTRAS;  // 0 = solto, até 1023 = aperto máximo
  Serial.print("Forca: ");
  Serial.println(forca);
  delay(20);
}
