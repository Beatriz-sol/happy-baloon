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
    // Círculo usado na colisão: só o envelope, sem o cesto
    public static final float ENVELOPE_OFFSET_Y = 52f;
    public static final float ENVELOPE_RADIUS = 26f;
    public static final float HIT_DURATION = 1f; // segundos piscando depois de bater

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
    private float hitTimer;

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
        hitTimer = 0f;
    }

    public void update(float force, float delta) {
        time += delta;
        hitTimer = Math.max(0f, hitTimer - delta);
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

    /** Marca a batida; enquanto pisca, o balão não leva outra. */
    public void hit() {
        hitTimer = HIT_DURATION;
    }

    public boolean isHit() {
        return hitTimer > 0f;
    }

    public float getEnvelopeCenterY() {
        return y + ENVELOPE_OFFSET_Y;
    }

    public void draw(SpriteBatch batch) {
        // Pisca depois de bater em um pássaro
        if (isHit() && (int) (hitTimer * 10f) % 2 == 0) return;
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
