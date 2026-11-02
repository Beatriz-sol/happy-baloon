package br.mackenzie.screens;

import br.mackenzie.ReabilitacaoGame;
import br.mackenzie.audio.SoundEffects;
import br.mackenzie.metrics.LevelResult;
import br.mackenzie.metrics.MetricsTracker;
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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import java.util.Locale;

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
    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR"); // vírgula decimal no HUD
    private static final float COUNTDOWN_SECONDS = 3f;
    private static final float GO_MESSAGE_SECONDS = 0.8f;
    private static final float RESULT_DELAY = 1.5f; // tempo mostrando "Percurso concluído!"

    // Medidor de força na lateral direita
    private static final float GAUGE_X = W - 40f;
    private static final float GAUGE_WIDTH = 16f;
    private static final float GAUGE_BOTTOM = 70f;
    private static final float GAUGE_TOP = 380f;
    private static final Color GAUGE_IN_ZONE = new Color(0.3f, 0.75f, 0.35f, 1f);
    private static final Color GAUGE_OUT_OF_ZONE = new Color(0.95f, 0.6f, 0.2f, 1f);
    private static final Color GAIN_COLOR = new Color(1f, 0.9f, 0.3f, 1f);
    private static final Color LOSS_COLOR = new Color(1f, 0.35f, 0.3f, 1f);

    private final ReabilitacaoGame game;
    private final int levelIndex;
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
    private final MetricsTracker metrics = new MetricsTracker();

    private boolean paused;
    private boolean finished;
    private boolean leaving;
    private float countdown;
    private int countdownShown; // número da contagem na tela, para tocar o bipe só quando ele muda
    private float goMessageTimer;
    private float finishTimer;
    private float distance;
    private float animationTime;

    public GameScreen(ReabilitacaoGame game, int levelIndex) {
        this.game = game;
        this.levelIndex = levelIndex;
        viewport = new FitViewport(W, H);
        stage = new Stage(new FitViewport(W, H), game.batch);

        balloonTexture = new Texture("balao.png");
        balloonTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        balloon = new Balloon(balloonTexture, BALLOON_X, BALLOON_MIN_Y, BALLOON_MAX_Y);
        background = new ParallaxBackground(W, H);
        level = LevelConfig.forIndex(levelIndex);
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
        Table hud = new Table();
        hud.setFillParent(true);
        hud.top().left().pad(12f);
        hud.add(hudLabel).left();
        stage.addActor(hud);

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
        metrics.reset();
        distance = 0f;
        animationTime = 0f;
        finished = false;
        countdown = COUNTDOWN_SECONDS;
        countdownShown = 0;
        goMessageTimer = 0f;
        finishTimer = 0f;
        messageLabel.setText(String.valueOf((int) COUNTDOWN_SECONDS));
        updateHud();
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

        // Contagem inicial: o balão já responde ao aperto, mas a fase ainda não começou
        if (countdown > 0f) {
            int number = (int) Math.ceil(countdown);
            if (number != countdownShown) {
                countdownShown = number;
                messageLabel.setText(String.valueOf(number));
                game.sounds.play(SoundEffects.Effect.COUNTDOWN);
            }
            countdown -= delta;
            if (countdown > 0f) return;
            messageLabel.setText("Já!");
            game.sounds.play(SoundEffects.Effect.GO);
            goMessageTimer = GO_MESSAGE_SECONDS;
        }
        if (finished) {
            finishTimer += delta;
            if (finishTimer >= RESULT_DELAY) showResults();
            return;
        }
        if (goMessageTimer > 0f) {
            goMessageTimer -= delta;
            if (goMessageTimer <= 0f) messageLabel.setText("");
        }

        background.update(delta, level.speed);
        distance += level.speed * delta;
        animationTime += delta;

        int segment = level.segmentAt(distance);
        if (segment < 0) {
            finished = true;
            metrics.finish();
            messageLabel.setText("Percurso concluído!");
            game.sounds.play(SoundEffects.Effect.FINISH);
            updateHud();
            return;
        }
        metrics.update(delta, segment, level.segments[segment], force);
        checkCollisions();
        updateHud();
    }

    private void showResults() {
        if (leaving) return;
        leaving = true;
        int totalCoins = course.getCoinCount();
        LevelResult result = new LevelResult(levelIndex, level.name, metrics,
            MetricsTracker.maxScore(level, totalCoins), totalCoins);
        game.history.add(result);
        Gdx.app.postRunnable(() -> {
            game.setScreen(new ResultScreen(game, result));
            dispose();
        });
    }

    private void updateHud() {
        int progress = Math.min(100, Math.round(100f * distance / level.totalLength()));
        hudLabel.setText(String.format(PT_BR, "%s   Progresso: %d%%   Pontos: %d\n"
                + "Na faixa: %d%%   Resposta: %.1f s   Moedas: %d   Pássaros: %d",
            level.name, progress, metrics.getScore(),
            Math.round(100f * metrics.getConsistency()), metrics.getAverageResponseTime(),
            metrics.getCoins(), metrics.getBirdHits()));
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
                metrics.addCoin();
                game.sounds.play(SoundEffects.Effect.COIN);
                showFloatingPoints(MetricsTracker.POINTS_PER_COIN, x, item.y, GAIN_COLOR);
            } else if (!balloon.isHit()) {
                item.used = true;
                metrics.addBirdHit();
                balloon.hit();
                game.sounds.play(SoundEffects.Effect.BIRD);
                showFloatingPoints(MetricsTracker.POINTS_PER_BIRD_HIT, x, item.y, LOSS_COLOR);
            }
        }
    }

    // Os pontos ganhos ou perdidos sobem e somem no lugar da colisão
    private void showFloatingPoints(int points, float x, float y, Color color) {
        Label label = new Label((points > 0 ? "+" : "") + points, game.skin);
        label.setColor(color);
        label.setPosition(x - label.getPrefWidth() / 2f, y);
        label.addAction(Actions.sequence(
            Actions.parallel(Actions.moveBy(0f, 40f, 0.8f), Actions.fadeOut(0.8f)),
            Actions.removeActor()));
        stage.getRoot().addActorBefore(pauseTable, label); // fica atrás do menu de pausa
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

        drawForceGauge(shapes);
    }

    // Medidor vertical: mostra a força atual e a faixa-alvo do trecho, para o jogador corrigir o aperto
    private void drawForceGauge(ShapeRenderer shapes) {
        float force = game.grip.getForce();
        float height = GAUGE_TOP - GAUGE_BOTTOM;
        int index = level.segmentAt(distance);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0f, 0f, 0f, 0.35f);
        shapes.rect(GAUGE_X - 4f, GAUGE_BOTTOM - 4f, GAUGE_WIDTH + 8f, height + 8f);

        boolean inZone = false;
        if (index >= 0) {
            LevelConfig.Segment segment = level.segments[index];
            inZone = segment.contains(force);
            shapes.setColor(1f, 0.95f, 0.6f, 0.7f);
            shapes.rect(GAUGE_X - 4f, GAUGE_BOTTOM + segment.targetMin * height,
                GAUGE_WIDTH + 8f, (segment.targetMax - segment.targetMin) * height);
        }
        shapes.setColor(inZone ? GAUGE_IN_ZONE : GAUGE_OUT_OF_ZONE);
        shapes.rect(GAUGE_X, GAUGE_BOTTOM, GAUGE_WIDTH, force * height);
        shapes.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void drawItems(SpriteBatch batch) {
        TextureRegion birdFrame = birdAnimation.getKeyFrame(animationTime, true);
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
