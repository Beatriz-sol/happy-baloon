package br.mackenzie.world;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

/**
 * Moedas e pássaros de uma fase, gerados a partir das faixas-alvo.
 * As moedas ficam no centro da faixa (recompensa por manter a força certa)
 * e os pássaros ficam fora dela (punição por sair da faixa).
 */
public class Course {
    public static final float COIN_RADIUS = 12f;
    public static final float BIRD_RADIUS = 14f;

    private static final float COIN_SPACING = 180f;
    private static final float BIRD_FORCE_OFFSET = 0.25f; // distância do pássaro até a faixa, em força

    private final Array<CourseItem> items = new Array<>();
    private final float minY;
    private final float maxY;
    private final float centerOffset;

    /**
     * @param minY         altura do balão com força 0
     * @param maxY         altura do balão com força 1
     * @param centerOffset distância da base do balão até o centro do envelope
     */
    public Course(LevelConfig level, float minY, float maxY, float centerOffset) {
        this.minY = minY;
        this.maxY = maxY;
        this.centerOffset = centerOffset;

        float start = 0f;
        for (int i = 0; i < level.segments.length; i++) {
            LevelConfig.Segment segment = level.segments[i];
            float length = level.lengthOf(i);
            float middle = (segment.targetMin + segment.targetMax) / 2f;

            for (float offset = COIN_SPACING / 2f; offset < length - COIN_SPACING / 3f; offset += COIN_SPACING) {
                items.add(new CourseItem(CourseItem.Type.COIN, start + offset, centerYFor(middle), COIN_RADIUS));
            }

            // Primeiro trecho sem pássaros, para o jogador se acostumar
            if (i > 0) {
                float birdForce = middle < 0.5f
                    ? Math.min(1f, segment.targetMax + BIRD_FORCE_OFFSET)
                    : Math.max(0f, segment.targetMin - BIRD_FORCE_OFFSET);
                items.add(new CourseItem(CourseItem.Type.BIRD, start + length / 2f, centerYFor(birdForce), BIRD_RADIUS));
            }
            start += length;
        }
    }

    /** Altura do centro do envelope do balão para uma força. */
    public float centerYFor(float force) {
        return MathUtils.lerp(minY, maxY, force) + centerOffset;
    }

    public Array<CourseItem> getItems() {
        return items;
    }

    public void reset() {
        for (CourseItem item : items) item.used = false;
    }
}
