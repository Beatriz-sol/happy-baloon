// Jogo do Balão - teste da conexão USB sem o sensor
// Não precisa ligar nada no Arduino: ele mesmo gera uma força que sobe e desce,
// no mesmo formato do handgrip (linhas "Forca: N" a 9600 baud).
// Serve para conferir se o jogo encontra o Arduino antes de montar o sensor.
const unsigned long PERIODO_MS = 6000;  // uma subida e uma descida a cada 6 segundos

void setup() {
  Serial.begin(9600);
}

void loop() {
  float fase = (millis() % PERIODO_MS) / (float) PERIODO_MS;  // de 0 a 1
  int forca = (int) (500 + 400 * sin(2 * PI * fase));         // entre 100 e 900
  Serial.print("Forca: ");
  Serial.println(forca);
  delay(36);  // cerca de 28 leituras por segundo, como o handgrip
}
