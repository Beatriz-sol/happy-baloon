package br.mackenzie.ui;

import br.mackenzie.metrics.LevelResult;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;

/**
 * Gráfico de linhas com o tempo na faixa-alvo de cada partida, uma linha por fase.
 * O eixo x é a sequência das partidas, da mais antiga (esquerda) para a mais recente.
 */
public class EvolutionChart extends Actor {
    public static final Color[] LEVEL_COLORS = {
        new Color(0.45f, 0.85f, 0.45f, 1f),
        new Color(0.4f, 0.7f, 1f, 1f),
        new Color(1f, 0.6f, 0.25f, 1f),
    };

    private static final float LABEL_WIDTH = 48f; // espaço para os rótulos do eixo y
    private static final float SIDE_PAD = 12f;
    private static final float LINE_WIDTH = 2.5f;
    private static final float POINT_RADIUS = 4.5f;

    private final ShapeRenderer shapes;
    private final BitmapFont font;
    private final Array<LevelResult> results = new Array<>();

    public EvolutionChart(ShapeRenderer shapes, BitmapFont font, Array<LevelResult> allResults, int maxPoints) {
        this.shapes = shapes;
        this.font = font;
        for (int i = Math.max(0, allResults.size - maxPoints); i < allResults.size; i++) {
            results.add(allResults.get(i));
        }
    }

    public static Color colorFor(int levelIndex) {
        return LEVEL_COLORS[Math.max(0, Math.min(LEVEL_COLORS.length - 1, levelIndex))];
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.end();
        shapes.setProjectionMatrix(batch.getProjectionMatrix());
        shapes.setTransformMatrix(batch.getTransformMatrix());
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        shapes.setColor(1f, 1f, 1f, 0.06f);
        shapes.rect(chartLeft(), getY(), chartWidth(), getHeight());
        shapes.setColor(1f, 1f, 1f, 0.2f);
        for (int percent = 0; percent <= 100; percent += 25) {
            shapes.rect(chartLeft(), yFor(percent / 100f), chartWidth(), 1f);
        }

        // Uma linha ligando as partidas de cada fase, para ver a tendência
        for (int level = 0; level < LEVEL_COLORS.length; level++) {
            shapes.setColor(LEVEL_COLORS[level]);
            int previous = -1;
            for (int i = 0; i < results.size; i++) {
                if (results.get(i).levelIndex != level) continue;
                if (previous >= 0) {
                    shapes.rectLine(xFor(previous), yFor(results.get(previous).consistency),
                        xFor(i), yFor(results.get(i).consistency), LINE_WIDTH);
                }
                previous = i;
            }
        }
        for (int i = 0; i < results.size; i++) {
            shapes.setColor(colorFor(results.get(i).levelIndex));
            shapes.circle(xFor(i), yFor(results.get(i).consistency), POINT_RADIUS, 16);
        }

        shapes.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
        batch.begin();

        font.setColor(Color.LIGHT_GRAY);
        for (int percent = 0; percent <= 100; percent += 50) {
            font.draw(batch, percent + "%", getX(), yFor(percent / 100f) + font.getCapHeight() / 2f);
        }
        font.setColor(Color.WHITE);
    }

    private float chartLeft() {
        return getX() + LABEL_WIDTH;
    }

    private float chartWidth() {
        return getWidth() - LABEL_WIDTH;
    }

    private float xFor(int index) {
        if (results.size == 1) return chartLeft() + chartWidth() / 2f;
        return chartLeft() + SIDE_PAD + index * (chartWidth() - 2f * SIDE_PAD) / (results.size - 1);
    }

    private float yFor(float fraction) {
        return getY() + fraction * getHeight();
    }
}
