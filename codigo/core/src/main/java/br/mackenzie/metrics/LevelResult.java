package br.mackenzie.metrics;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Resultado de uma fase concluída. As estrelas vêm da fração da pontuação
 * máxima possível, então valem igual em qualquer fase.
 */
public class LevelResult {
    private static final float ONE_STAR = 0.4f;
    private static final float TWO_STARS = 0.6f;
    private static final float THREE_STARS = 0.8f;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public final String playedAt;  // data e hora da partida, "aaaa-mm-dd hh:mm"
    public final int levelIndex;
    public final String levelName;
    public final int score;
    public final int maxScore;
    public final float consistency;
    public final float averageResponseTime;
    public final int coins;
    public final int totalCoins;
    public final int birdHits;
    public final int stars;

    /** Resultado da partida que acabou de terminar. */
    public LevelResult(int levelIndex, String levelName, MetricsTracker metrics, int maxScore, int totalCoins) {
        this(LocalDateTime.now().format(DATE_FORMAT), levelIndex, levelName, metrics.getScore(), maxScore,
            metrics.getConsistency(), metrics.getAverageResponseTime(), metrics.getCoins(), totalCoins,
            metrics.getBirdHits());
    }

    /** Resultado lido do histórico salvo. */
    public LevelResult(String playedAt, int levelIndex, String levelName, int score, int maxScore,
                       float consistency, float averageResponseTime, int coins, int totalCoins, int birdHits) {
        this.playedAt = playedAt;
        this.levelIndex = levelIndex;
        this.levelName = levelName;
        this.score = score;
        this.maxScore = maxScore;
        this.consistency = consistency;
        this.averageResponseTime = averageResponseTime;
        this.coins = coins;
        this.totalCoins = totalCoins;
        this.birdHits = birdHits;
        this.stars = starsFor(maxScore > 0 ? (float) score / maxScore : 0f);
    }

    private static int starsFor(float fraction) {
        if (fraction >= THREE_STARS) return 3;
        if (fraction >= TWO_STARS) return 2;
        if (fraction >= ONE_STAR) return 1;
        return 0;
    }
}
