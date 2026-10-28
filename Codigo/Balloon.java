package br.mackenzie.world;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

/**
 * Personagem principal: o balão de ar quente.
 * A altura acompanha a força do aperto, com suavização, leve inclinação e balanço.
 */
public class Balloon {
    public static final float WIDTH = 60f;
    public static final float HEIGHT = 80f;

    private static final float FOLLOW_SPEED = 8f;   // suavização do movimento vertical
    private static final float MAX_TILT = 8f;       // graus
    private static final float BOB_AMPLITUDE = 3f;  // balanço quando parado

    private final Texture texture;
    private final float x;
    private final float minY;
    private final float maxY;

    private float y;
    private float rotation;
    private float time;

    public Balloon(Texture texture, float x, float minY, float maxY) {
        this.texture = texture;
        this.x = x;
        this.minY = minY;
        this.maxY = maxY;
        reset();
    }

    public void reset() {
        y = minY;
        rotation = 0f;
        time = 0f;
    }

    public void update(float force, float delta) {
        time += delta;
        float previousY = y;
        y += (heightFor(force) - y) * Math.min(1f, FOLLOW_SPEED * delta);

        // Inclina para trás ao subir e para frente ao descer
        float velocityY = delta > 0 ? (y - previousY) / delta : 0f;
        float targetRotation = MathUtils.clamp(velocityY * 0.04f, -MAX_TILT, MAX_TILT);
        rotation += (targetRotation - rotation) * Math.min(1f, 5f * delta);
    }

    /** Altura (base do cesto) correspondente a uma força entre 0 e 1. */
    public float heightFor(float force) {
        return MathUtils.lerp(minY, maxY, force);
    }

    public void draw(SpriteBatch batch) {
        float bob = MathUtils.sin(time * 2f) * BOB_AMPLITUDE;
        batch.draw(texture, x, y + bob, WIDTH / 2f, HEIGHT / 2f, WIDTH, HEIGHT,
            1f, 1f, rotation, 0, 0, texture.getWidth(), texture.getHeight(), false, false);
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}
