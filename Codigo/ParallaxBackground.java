package br.mackenzie.world;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

/**
 * Cenário em camadas: céu, morros, nuvens e grama.
 * Cada camada rola numa velocidade diferente para dar profundidade (paralaxe).
 */
public class ParallaxBackground {
    public static final float GROUND_HEIGHT = 48f;

    private static final float HILLS_FACTOR = 0.2f;
    private static final float GROUND_FACTOR = 1f;
    private static final int CLOUD_COUNT = 5;

    private static final Color SKY_TOP = new Color(0.36f, 0.62f, 0.90f, 1f);
    private static final Color SKY_BOTTOM = new Color(0.72f, 0.88f, 0.98f, 1f);

    private final float width;
    private final float height;
    private final Texture hills;
    private final Texture cloud;
    private final Texture grass;

    private final float[] cloudX = new float[CLOUD_COUNT];
    private final float[] cloudY = new float[CLOUD_COUNT];
    private final float[] cloudScale = new float[CLOUD_COUNT];

    private float hillsOffset;
    private float groundOffset;

    public ParallaxBackground(float width, float height) {
        this.width = width;
        this.height = height;
        hills = new Texture("morros.png");
        cloud = new Texture("nuvem.png");
        grass = new Texture("grama.png");
        hills.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        cloud.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        reset();
    }

    public void reset() {
        hillsOffset = 0f;
        groundOffset = 0f;
        for (int i = 0; i < CLOUD_COUNT; i++) {
            placeCloud(i, i * (width + cloud.getWidth()) / CLOUD_COUNT);
        }
    }

    private void placeCloud(int i, float x) {
        cloudX[i] = x;
        cloudY[i] = MathUtils.random(height * 0.45f, height - 70f);
        cloudScale[i] = MathUtils.random(0.5f, 1.1f);
    }

    /** Avança o cenário; speed é a velocidade do chão em unidades por segundo. */
    public void update(float delta, float speed) {
        hillsOffset = (hillsOffset + speed * HILLS_FACTOR * delta) % hills.getWidth();
        groundOffset = (groundOffset + speed * GROUND_FACTOR * delta) % grass.getWidth();

        for (int i = 0; i < CLOUD_COUNT; i++) {
            // Nuvens menores parecem mais distantes, então andam mais devagar
            cloudX[i] -= speed * 0.5f * cloudScale[i] * delta;
            if (cloudX[i] < -cloud.getWidth() * cloudScale[i]) {
                placeCloud(i, width + MathUtils.random(0f, 120f));
            }
        }
    }

    /** Céu em degradê; desenhar antes de tudo, com o ShapeRenderer. */
    public void drawSky(ShapeRenderer shapes) {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.rect(0, 0, width, height, SKY_BOTTOM, SKY_BOTTOM, SKY_TOP, SKY_TOP);
        shapes.end();
    }

    /** Morros e nuvens, atrás do personagem. */
    public void drawFar(SpriteBatch batch) {
        float hillsY = GROUND_HEIGHT - 8f;
        for (float x = -hillsOffset; x < width; x += hills.getWidth()) {
            batch.draw(hills, x, hillsY);
        }
        for (int i = 0; i < CLOUD_COUNT; i++) {
            batch.draw(cloud, cloudX[i], cloudY[i],
                cloud.getWidth() * cloudScale[i], cloud.getHeight() * cloudScale[i]);
        }
    }

    /** Faixa de grama na frente, a camada mais rápida. */
    public void drawGround(SpriteBatch batch) {
        for (float x = -groundOffset; x < width; x += grass.getWidth()) {
            batch.draw(grass, x, 0);
        }
    }

    public void dispose() {
        hills.dispose();
        cloud.dispose();
        grass.dispose();
    }
}
