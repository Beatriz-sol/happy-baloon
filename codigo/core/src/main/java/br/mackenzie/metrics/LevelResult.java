package br.mackenzie.metrics;

/**
 * Resultado de uma fase concluída. As estrelas vêm da fração da pontuação
 * máxima possível, então valem igual em qualquer fase.
 */
public class LevelResult {
    private static final float ONE_STAR = 0.4f;
    private static final float TWO_STARS = 0.6f;
    private static final float THREE_STARS = 0.8f;

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

    public LevelResult(int levelIndex, String levelName, MetricsTracker metrics, int maxScore, int totalCoins) {
        this.levelIndex = levelIndex;
        this.levelName = levelName;
        this.score = metrics.getScore();
        this.maxScore = maxScore;
        this.consistency = metrics.getConsistency();
        this.averageResponseTime = metrics.getAverageResponseTime();
        this.coins = metrics.getCoins();
        this.totalCoins = totalCoins;
        this.birdHits = metrics.getBirdHits();
        this.stars = starsFor(maxScore > 0 ? (float) score / maxScore : 0f);
    }

    private static int starsFor(float fraction) {
        if (fraction >= THREE_STARS) return 3;
        if (fraction >= TWO_STARS) return 2;
        if (fraction >= ONE_STAR) return 1;
        return 0;
    }
}
