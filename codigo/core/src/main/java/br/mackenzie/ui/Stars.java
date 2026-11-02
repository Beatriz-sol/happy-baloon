package br.mackenzie.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

/**
 * Desenha estrelas com o ShapeRenderer (a fonte padrão não tem o caractere ★).
 */
public final class Stars {
    public static final Color GOLD = new Color(1f, 0.82f, 0.2f, 1f);
    public static final Color EMPTY = new Color(0.3f, 0.35f, 0.45f, 1f);

    private Stars() {
    }

    /** Estrela de cinco pontas. O ShapeRenderer precisa estar em modo Filled. */
    public static void draw(ShapeRenderer shapes, float centerX, float centerY, float radius, Color color) {
        float inner = radius * 0.45f;
        shapes.setColor(color);
        for (int i = 0; i < 5; i++) {
            float tip = 90f + i * 72f;
            float left = tip - 36f;
            float right = tip + 36f;
            float tipX = centerX + radius * MathUtils.cosDeg(tip);
            float tipY = centerY + radius * MathUtils.sinDeg(tip);
            float leftX = centerX + inner * MathUtils.cosDeg(left);
            float leftY = centerY + inner * MathUtils.sinDeg(left);
            float rightX = centerX + inner * MathUtils.cosDeg(right);
            float rightY = centerY + inner * MathUtils.sinDeg(right);
            shapes.triangle(tipX, tipY, leftX, leftY, rightX, rightY);
            shapes.triangle(centerX, centerY, leftX, leftY, rightX, rightY);
        }
    }
}
