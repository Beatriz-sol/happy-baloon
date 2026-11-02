package br.mackenzie.metrics;

import br.mackenzie.world.LevelConfig;

/**
 * Mede o desempenho do jogador durante uma fase e calcula a pontuação.
 * Consistência: fração do tempo em que a força ficou dentro da faixa-alvo.
 * Tempo de resposta: quanto o jogador demora para entrar em cada faixa nova.
 */
public class MetricsTracker {
    public static final int POINTS_PER_SECOND_IN_ZONE = 100;
    public static final int POINTS_PER_COIN = 50;
    public static final int POINTS_PER_BIRD_HIT = -75;
    // Bônus por faixa alcançada: 300 se entrar na hora, 100 a menos por segundo de atraso
    public static final int MAX_RESPONSE_BONUS = 300;
    public static final int RESPONSE_PENALTY_PER_SECOND = 100;

    private float elapsedTime;
    private float timeInZone;
    private float deviationSum; // integral de |força - centro da faixa| no tempo
    private int coins;
    private int birdHits;

    private int currentSegment;
    private boolean waitingResponse;
    private float responseTimer;
    private float responseTimeSum;
    private int responses;
    private int responseBonus;

    public MetricsTracker() {
        reset();
    }

    public void reset() {
        elapsedTime = 0f;
        timeInZone = 0f;
        deviationSum = 0f;
        coins = 0;
        birdHits = 0;
        currentSegment = -1;
        waitingResponse = false;
        responseTimer = 0f;
        responseTimeSum = 0f;
        responses = 0;
        responseBonus = 0;
    }

    /** Chamado a cada frame com o trecho atual da fase e a força do aperto (0..1). */
    public void update(float delta, int segmentIndex, LevelConfig.Segment segment, float force) {
        boolean inZone = segment.contains(force);
        if (segmentIndex != currentSegment) {
            // Faixa anterior nunca alcançada: conta o tempo todo que o jogador teve
            if (waitingResponse) recordResponse(responseTimer);
            currentSegment = segmentIndex;
            responseTimer = 0f;
            waitingResponse = !inZone;
            if (inZone) recordResponse(0f);
        }

        elapsedTime += delta;
        if (inZone) timeInZone += delta;
        float center = (segment.targetMin + segment.targetMax) / 2f;
        deviationSum += Math.abs(force - center) * delta;

        if (waitingResponse) {
            responseTimer += delta;
            if (inZone) {
                recordResponse(responseTimer);
                waitingResponse = false;
            }
        }
    }

    private void recordResponse(float seconds) {
        responseTimeSum += seconds;
        responses++;
        responseBonus += Math.max(0, Math.round(MAX_RESPONSE_BONUS - RESPONSE_PENALTY_PER_SECOND * seconds));
    }

    /** Fecha a medição no fim da fase. */
    public void finish() {
        if (waitingResponse) {
            recordResponse(responseTimer);
            waitingResponse = false;
        }
    }

    public void addCoin() {
        coins++;
    }

    public void addBirdHit() {
        birdHits++;
    }

    /** Fração do tempo dentro da faixa-alvo, de 0 a 1. */
    public float getConsistency() {
        return elapsedTime > 0f ? timeInZone / elapsedTime : 0f;
    }

    /** Tempo médio, em segundos, para entrar em cada faixa nova. */
    public float getAverageResponseTime() {
        return responses > 0 ? responseTimeSum / responses : 0f;
    }

    /** Distância média da força até o centro da faixa, de 0 a 1. */
    public float getAverageDeviation() {
        return elapsedTime > 0f ? deviationSum / elapsedTime : 0f;
    }

    /** Pontuação de quem fica o tempo todo na faixa, entra em cada faixa na hora e pega todas as moedas. */
    public static int maxScore(LevelConfig level, int totalCoins) {
        return Math.round(level.totalDuration() * POINTS_PER_SECOND_IN_ZONE)
            + totalCoins * POINTS_PER_COIN
            + level.segments.length * MAX_RESPONSE_BONUS;
    }

    public int getScore() {
        int score = Math.round(timeInZone * POINTS_PER_SECOND_IN_ZONE)
            + coins * POINTS_PER_COIN
            + birdHits * POINTS_PER_BIRD_HIT
            + responseBonus;
        return Math.max(0, score);
    }

    public int getCoins() {
        return coins;
    }

    public int getBirdHits() {
        return birdHits;
    }

    public float getElapsedTime() {
        return elapsedTime;
    }
}
