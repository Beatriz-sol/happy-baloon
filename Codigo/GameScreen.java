package br.mackenzie.screens;

import br.mackenzie.ReabilitacaoGame;
import br.mackenzie.world.Balloon;
import br.mackenzie.world.ParallaxBackground;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

/**
 * Tela de jogo: a altura do balão segue a força do aperto.
 * Nesta primeira versão há uma única faixa-alvo fixa; as fases vêm nos próximos commits.
 */
public class GameScreen extends ScreenAdapter {
    private static final float W = ReabilitacaoGame.WORLD_WIDTH;
    private static final float H = ReabilitacaoGame.WORLD_HEIGHT;

    private static final float BALLOON_X = 170f;
    private static final float BALLOON_MIN_Y = ParallaxBackground.GROUND_HEIGHT - 4f;
    private static final float BALLOON_MAX_Y = H - Balloon.HEIGHT - 50f;
    private static final float SCROLL_SPEED = 120f;

    // Faixa de força-alvo (0..1)
    private static final float TARGET_MIN = 0.4f;
    private static final float TARGET_MAX = 0.6f;

    private final ReabilitacaoGame game;
    private final FitViewport viewport;
    private final Stage stage;
    private final Label hudLabel;
    private final Table pauseTable;
    private final Texture balloonTexture;
    private final Balloon balloon;
    private final ParallaxBackground background;

    private boolean paused;
    private float elapsedTime;
    private float timeInZone;

    public GameScreen(ReabilitacaoGame game) {
        this.game = game;
        viewport = new FitViewport(W, H);
        stage = new Stage(new FitViewport(W, H), game.batch);

        balloonTexture = new Texture("balao.png");
        balloonTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        balloon = new Balloon(balloonTexture, BALLOON_X, BALLOON_MIN_Y, BALLOON_MAX_Y);
        background = new ParallaxBackground(W, H);

        hudLabel = new Label("", game.skin);
        hudLabel.setPosition(15, H - 35);
        stage.addActor(hudLabel);

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
        background.update(delta, SCROLL_SPEED);

        elapsedTime += delta;
        if (force >= TARGET_MIN && force <= TARGET_MAX) timeInZone += delta;

        int zonePercent = elapsedTime > 0 ? Math.round(100f * timeInZone / elapsedTime) : 0;
        hudLabel.setText(String.format("Força: %d%%   Na faixa-alvo: %d%%   Tempo: %ds",
            Math.round(force * 100), zonePercent, (int) elapsedTime));
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

        // Faixa-alvo semitransparente: alturas em que o balão fica com a força certa
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(1f, 1f, 1f, 0.3f);
        float zoneBottom = balloon.heightFor(TARGET_MIN);
        float zoneTop = balloon.heightFor(TARGET_MAX) + Balloon.HEIGHT;
        shapes.rect(0, zoneBottom, W, zoneTop - zoneBottom);
        shapes.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.begin();
        background.drawGround(batch);
        balloon.draw(batch);
        batch.end();
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
    }
}
