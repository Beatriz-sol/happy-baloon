package br.mackenzie.screens;

import br.mackenzie.ReabilitacaoGame;
import br.mackenzie.world.Balloon;
import br.mackenzie.world.Course;
import br.mackenzie.world.CourseItem;
import br.mackenzie.world.LevelConfig;
import br.mackenzie.world.ParallaxBackground;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

/**
 * Tela de jogo: a altura do balão segue a força do aperto e o jogador
 * tenta manter o balão dentro das faixas-alvo da fase, pegando moedas
 * e desviando dos pássaros.
 */
public class GameScreen extends ScreenAdapter {
    private static final float W = ReabilitacaoGame.WORLD_WIDTH;
    private static final float H = ReabilitacaoGame.WORLD_HEIGHT;

    private static final float BALLOON_X = 170f;
    private static final float BALLOON_MIN_Y = ParallaxBackground.GROUND_HEIGHT - 4f;
    private static final float BALLOON_MAX_Y = H - Balloon.HEIGHT - 50f;
    private static final float BALLOON_CENTER_X = BALLOON_X + Balloon.WIDTH / 2f;
    private static final float BAND_GAP = 6f; // espaço entre faixas vizinhas

    private final ReabilitacaoGame game;
    private final FitViewport viewport;
    private final Stage stage;
    private final Label hudLabel;
    private final Label messageLabel;
    private final Table pauseTable;
    private final Texture balloonTexture;
    private final Balloon balloon;
    private final ParallaxBackground background;
    private final LevelConfig level;
    private final Course course;
    private final Texture coinTexture;
    private final Texture[] birdTextures;
    private final Animation<TextureRegion> birdAnimation;

    private boolean paused;
    private boolean finished;
    private float distance;
    private float elapsedTime;
    private float timeInZone;
    private int coins;
    private int birdHits;

    public GameScreen(ReabilitacaoGame game) {
        this.game = game;
        viewport = new FitViewport(W, H);
        stage = new Stage(new FitViewport(W, H), game.batch);

        balloonTexture = new Texture("balao.png");
        balloonTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        balloon = new Balloon(balloonTexture, BALLOON_X, BALLOON_MIN_Y, BALLOON_MAX_Y);
        background = new ParallaxBackground(W, H);
        level = LevelConfig.level1();
        course = new Course(level, BALLOON_MIN_Y, BALLOON_MAX_Y, Balloon.ENVELOPE_OFFSET_Y);

        coinTexture = new Texture("moeda.png");
        coinTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        birdTextures = new Texture[] {new Texture("passaro1.png"), new Texture("passaro2.png")};
        TextureRegion[] birdFrames = new TextureRegion[birdTextures.length];
        for (int i = 0; i < birdTextures.length; i++) {
            birdTextures[i].setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            birdFrames[i] = new TextureRegion(birdTextures[i]);
        }
        birdAnimation = new Animation<>(0.15f, birdFrames);

        hudLabel = new Label("", game.skin);
        hudLabel.setPosition(15, H - 35);
        stage.addActor(hudLabel);

        messageLabel = new Label("", game.skin, "title");
        messageLabel.setAlignment(Align.center);
        messageLabel.setBounds(0, H / 2f, W, 60);
        stage.addActor(messageLabel);

        TextButton pauseButton = new TextButton("Pausar", game.skin);
        pauseButton.setBounds(W - 135, H - 50, 120, 40);
        stage.addActor(pauseButton);
        pauseButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                setPaused(true);
            }
        });

        pauseTable = buildPauseMenu();
        stage.addActor(pauseTable);

        restart();
    }

    private Table buildPauseMenu() {
        Table table = new Table();
        table.setFillParent(true);
        table.setBackground(game.skin.getDrawable("panel"));

        TextButton resumeButton = new TextButton("Continuar", game.skin);
        TextButton restartButton = new TextButton("Reiniciar", game.skin);
        TextButton menuButton = new TextButton("Menu principal", game.skin);

        table.add(new Label("Pausado", game.skin, "title")).padBottom(30).row();
        table.add(resumeButton).width(260).height(55).padBottom(15).row();
        table.add(restartButton).width(260).height(55).padBottom(15).row();
        table.add(menuButton).width(260).height(55).row();

        resumeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                setPaused(false);
            }
        });
        restartButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                restart();
            }
        });
        menuButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.postRunnable(() -> {
                    game.setScreen(new MenuScreen(game));
                    dispose();
                });
            }
        });
        return table;
    }

    private void restart() {
        balloon.reset();
        background.reset();
        course.reset();
        coins = 0;
        birdHits = 0;
        distance = 0f;
        finished = false;
        messageLabel.setText("");
        elapsedTime = 0f;
        timeInZone = 0f;
        setPaused(false);
    }

    private void setPaused(boolean value) {
        paused = value;
        pauseTable.setVisible(value);
    }

    @Override
    public void show() {
        // ESC também pausa/continua (atalho de teclado só para o menu, não para o controle do jogo)
        InputAdapter keys = new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    setPaused(!paused);
                    return true;
                }
                return false;
            }
        };
        Gdx.input.setInputProcessor(new InputMultiplexer(stage, keys));
    }

    @Override
    public void render(float delta) {
        if (!paused) update(delta);
        draw();
        stage.act(delta);
        stage.draw();
    }

    private void update(float delta) {
        float force = game.grip.getForce();
        balloon.update(force, delta);
        if (finished) return;

        background.update(delta, level.speed);
        distance += level.speed * delta;
        elapsedTime += delta;

        int segment = level.segmentAt(distance);
        if (segment < 0) {
            finished = true;
            messageLabel.setText("Percurso concluído!");
            return;
        }
        if (level.segments[segment].contains(force)) timeInZone += delta;
        checkCollisions();

        int zonePercent = elapsedTime > 0 ? Math.round(100f * timeInZone / elapsedTime) : 0;
        int progress = Math.round(100f * distance / level.totalLength());
        hudLabel.setText(String.format("%s  %d%%   Na faixa-alvo: %d%%   Moedas: %d   Pássaros: %d",
            level.name, progress, zonePercent, coins, birdHits));
    }

    private void checkCollisions() {
        float balloonY = balloon.getEnvelopeCenterY();
        for (CourseItem item : course.getItems()) {
            if (item.used) continue;
            float x = screenX(item.distance);
            if (Math.abs(x - BALLOON_CENTER_X) > Balloon.ENVELOPE_RADIUS + item.radius) continue;

            float dx = x - BALLOON_CENTER_X;
            float dy = item.y - balloonY;
            float reach = Balloon.ENVELOPE_RADIUS + item.radius;
            if (dx * dx + dy * dy > reach * reach) continue;

            if (item.type == CourseItem.Type.COIN) {
                item.used = true;
                coins++;
            } else if (!balloon.isHit()) {
                item.used = true;
                birdHits++;
                balloon.hit();
            }
        }
    }

    /** Converte a distância de um item no percurso para a posição x na tela. */
    private float screenX(float itemDistance) {
        return BALLOON_CENTER_X + itemDistance - distance;
    }

    private void draw() {
        ScreenUtils.clear(0f, 0f, 0f, 1f);
        viewport.apply();
        ShapeRenderer shapes = game.shapes;
        SpriteBatch batch = game.batch;
        shapes.setProjectionMatrix(viewport.getCamera().combined);
        batch.setProjectionMatrix(viewport.getCamera().combined);

        background.drawSky(shapes);

        batch.begin();
        background.drawFar(batch);
        batch.end();

        drawTargetBands(shapes);

        batch.begin();
        background.drawGround(batch);
        drawItems(batch);
        balloon.draw(batch);
        batch.end();
    }

    private void drawItems(SpriteBatch batch) {
        TextureRegion birdFrame = birdAnimation.getKeyFrame(elapsedTime, true);
        for (CourseItem item : course.getItems()) {
            if (item.used) continue;
            float x = screenX(item.distance);
            if (x < -50f || x > W + 50f) continue;

            if (item.type == CourseItem.Type.COIN) {
                float size = item.radius * 2f;
                batch.draw(coinTexture, x - item.radius, item.y - item.radius, size, size);
            } else {
                batch.draw(birdFrame, x - 24f, item.y - 16f, 48f, 32f);
            }
        }
    }

    // Faixas-alvo semitransparentes: alturas em que o balão fica com a força certa.
    // Elas andam junto com o cenário, então o jogador vê a próxima faixa chegando.
    private void drawTargetBands(ShapeRenderer shapes) {
        int current = level.segmentAt(distance);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        float start = BALLOON_CENTER_X - distance;
        for (int i = 0; i < level.segments.length; i++) {
            float length = level.lengthOf(i);
            if (start < W && start + length > 0) {
                LevelConfig.Segment segment = level.segments[i];
                float bottom = balloon.heightFor(segment.targetMin);
                float top = balloon.heightFor(segment.targetMax) + Balloon.HEIGHT;
                shapes.setColor(1f, 1f, 1f, i == current ? 0.4f : 0.2f);
                shapes.rect(start + BAND_GAP / 2f, bottom, length - BAND_GAP, top - bottom);
            }
            start += length;
        }
        shapes.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
        // Janela minimizada ou sem foco: pausa o jogo
        setPaused(true);
    }

    @Override
    public void dispose() {
        stage.dispose();
        balloonTexture.dispose();
        background.dispose();
        coinTexture.dispose();
        for (Texture texture : birdTextures) texture.dispose();
    }
}
