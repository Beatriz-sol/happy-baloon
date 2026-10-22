// Esboço do firmware do handgrip adaptado.
// Lê o sensor de força (ex.: célula de carga/FSR/potenciômetro) no pino A0
// e envia uma linha por leitura no formato "Forca: <valor>" a 9600 baud.
// Ajuste forcaMin/forcaMax no controle.properties do jogo conforme os valores lidos.

const int PINO_SENSOR = A0;

void setup() {
  Serial.begin(9600);
}

void loop() {
  int valor = analogRead(PINO_SENSOR); // 0..1023
  Serial.print("Forca: ");
  Serial.println(valor);
  delay(20); // ~50 leituras por segundo
}
