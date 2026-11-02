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

    public static final int COUNT = 3;

    public final String name;
    public final float speed;  // unidades por segundo
    public final int birdsPerSegment;
    public final Segment[] segments;

    public LevelConfig(String name, float speed, int birdsPerSegment, Segment... segments) {
        this.name = name;
        this.speed = speed;
        this.birdsPerSegment = birdsPerSegment;
        this.segments = segments;
    }

    /** Comprimento de um trecho no cenário. */
    public float lengthOf(int index) {
        return segments[index].duration * speed;
    }

    /** Duração da fase em segundos. */
    public float totalDuration() {
        float total = 0f;
        for (Segment segment : segments) total += segment.duration;
        return total;
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

    /** Fase pelo índice: 0, 1 ou 2. */
    public static LevelConfig forIndex(int index) {
        switch (index) {
            case 0: return level1();
            case 1: return level2();
            case 2: return level3();
            default: throw new IllegalArgumentException("Fase inexistente: " + index);
        }
    }

    // Fase 1: faixas largas (0,25) e trocas lentas, para o jogador aprender o controle
    public static LevelConfig level1() {
        return new LevelConfig("Fase 1", 120f, 1,
            new Segment(6f, 0.20f, 0.45f),
            new Segment(6f, 0.40f, 0.65f),
            new Segment(5f, 0.25f, 0.50f),
            new Segment(6f, 0.50f, 0.75f),
            new Segment(5f, 0.35f, 0.60f),
            new Segment(6f, 0.20f, 0.45f));
    }

    // Fase 2: faixas mais estreitas (0,18), trocas mais rápidas e balão mais veloz
    public static LevelConfig level2() {
        return new LevelConfig("Fase 2", 140f, 1,
            new Segment(5f, 0.20f, 0.38f),
            new Segment(5f, 0.45f, 0.63f),
            new Segment(4f, 0.30f, 0.48f),
            new Segment(5f, 0.58f, 0.76f),
            new Segment(4f, 0.25f, 0.43f),
            new Segment(5f, 0.50f, 0.68f),
            new Segment(4f, 0.35f, 0.53f),
            new Segment(5f, 0.20f, 0.38f));
    }

    // Fase 3: faixas finas (0,13) alternando entre alto e baixo, com pássaros acima e abaixo da faixa
    public static LevelConfig level3() {
        return new LevelConfig("Fase 3", 160f, 2,
            new Segment(4f, 0.20f, 0.33f),
            new Segment(4f, 0.62f, 0.75f),
            new Segment(3f, 0.30f, 0.43f),
            new Segment(4f, 0.70f, 0.83f),
            new Segment(3f, 0.25f, 0.38f),
            new Segment(4f, 0.55f, 0.68f),
            new Segment(3f, 0.18f, 0.31f),
            new Segment(5f, 0.65f, 0.78f),
            new Segment(3f, 0.40f, 0.53f),
            new Segment(4f, 0.20f, 0.33f));
    }
}
