package br.mackenzie.world;

/**
 * Configuração de uma fase: velocidade do balão e a sequência de faixas-alvo.
 * Cada trecho pede que a força fique entre um mínimo e um máximo por alguns segundos.
 */
public class LevelConfig {
    public static class Segment {
        public final float duration;   // segundos
        public final float targetMin;  // força mínima (0..1)
        public final float targetMax;  // força máxima (0..1)

        public Segment(float duration, float targetMin, float targetMax) {
            this.duration = duration;
            this.targetMin = targetMin;
            this.targetMax = targetMax;
        }

        public boolean contains(float force) {
            return force >= targetMin && force <= targetMax;
        }
    }

    public final String name;
    public final float speed;  // unidades por segundo
    public final Segment[] segments;

    public LevelConfig(String name, float speed, Segment... segments) {
        this.name = name;
        this.speed = speed;
        this.segments = segments;
    }

    /** Comprimento de um trecho no cenário. */
    public float lengthOf(int index) {
        return segments[index].duration * speed;
    }

    public float totalLength() {
        float total = 0f;
        for (int i = 0; i < segments.length; i++) total += lengthOf(i);
        return total;
    }

    /** Índice do trecho na distância percorrida, ou -1 depois do fim. */
    public int segmentAt(float distance) {
        float start = 0f;
        for (int i = 0; i < segments.length; i++) {
            start += lengthOf(i);
            if (distance < start) return i;
        }
        return -1;
    }

    // Fase 1: faixas largas e trocas lentas, para o jogador aprender o controle
    public static LevelConfig level1() {
        return new LevelConfig("Fase 1", 120f,
            new Segment(6f, 0.20f, 0.45f),
            new Segment(6f, 0.40f, 0.65f),
            new Segment(5f, 0.25f, 0.50f),
            new Segment(6f, 0.50f, 0.75f),
            new Segment(5f, 0.35f, 0.60f),
            new Segment(6f, 0.20f, 0.45f));
    }
}
