package br.mackenzie.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;

/**
 * Fileira de três estrelas para usar dentro de tabelas do Scene2D.
 * Com revealDelay maior que zero, as estrelas ganhas acendem uma de cada vez.
 */
public class StarsActor extends Actor {
    private static final int TOTAL = 3;

    private final ShapeRenderer shapes;
    private final int stars;
    private final float radius;
    private final float revealDelay;
    private float time;

    public StarsActor(ShapeRenderer shapes, int stars, float radius, float spacing, float revealDelay) {
        this.shapes = shapes;
        this.stars = stars;
        this.radius = radius;
        this.revealDelay = revealDelay;
        setSize(spacing * TOTAL, radius * 2f);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        time += delta;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        // O ShapeRenderer não pode desenhar com o SpriteBatch aberto
        batch.end();
        shapes.setProjectionMatrix(batch.getProjectionMatrix());
        shapes.setTransformMatrix(batch.getTransformMatrix());
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        float spacing = getWidth() / TOTAL;
        for (int i = 0; i < TOTAL; i++) {
            boolean earned = i < stars && time >= revealDelay * (i + 1);
            Stars.draw(shapes, getX() + spacing * (i + 0.5f), getY() + getHeight() / 2f, radius,
                earned ? Stars.GOLD : Stars.EMPTY);
        }
        shapes.end();
        batch.begin();
    }
}
