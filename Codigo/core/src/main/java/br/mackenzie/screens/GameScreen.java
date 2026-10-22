package br.mackenzie.screens;

import br.mackenzie.ReabilitacaoGame;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

/**
 * Tela de jogo: a altura do personagem segue a força do handgrip.
 * Nesta primeira versão há uma única faixa-alvo fixa; as fases vêm nos próximos commits.
 */
public class GameScreen extends ScreenAdapter {
    private static final float W = ReabilitacaoGame.WORLD_WIDTH;
    private static final float H = ReabilitacaoGame.WORLD_HEIGHT;

    private static final float GROUND_Y = 60f;
    private static final float CEILING_Y = 420f;
    private static final float PLAYER_X = 200f;
    private static final float PLAYER_RADIUS = 22f;
    private static final float FOLLOW_SPEED = 8f; // suavização do movimento
    private static final float SCROLL_SPEED = 120f;

    // Faixa de força-alvo (0..1)
    private static final float TARGET_MIN = 0.4f;
    private static final float TARGET_MAX = 0.6f;

    private final ReabilitacaoGame game;
    private final FitViewport viewport;
    private final Stage stage;
    private final Label hudLabel;
    private final Label warningLabel;
    private final Table pauseTable;

    private boolean paused;
    private float playerY;
    private float scrollX;
    private float elapsedTime;
    private float timeInZone;

    public GameScreen(ReabilitacaoGame game) {
        this.game = game;
        viewport = new FitViewport(W, H);
        stage = new Stage(new FitViewport(W, H), game.batch);

        hudLabel = new Label("", game.skin);
        hudLabel.setPosition(15, H - 35);
        stage.addActor(hudLabel);

        warningLabel = new Label("", game.skin);
        warningLabel.setColor(Color.ORANGE);
        warningLabel.setPosition(15, H - 65);
        stage.addActor(warningLabel);

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
        playerY = GROUND_Y;
        scrollX = 0f;
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
        float targetY = MathUtils.lerp(GROUND_Y, CEILING_Y, force);
        playerY += (targetY - playerY) * Math.min(1f, FOLLOW_SPEED * delta);
        scrollX += SCROLL_SPEED * delta;

        elapsedTime += delta;
        if (force >= TARGET_MIN && force <= TARGET_MAX) timeInZone += delta;

        int zonePercent = elapsedTime > 0 ? Math.round(100f * timeInZone / elapsedTime) : 0;
        hudLabel.setText(String.format("Força: %d%%   Na faixa-alvo: %d%%   Tempo: %ds",
            Math.round(force * 100), zonePercent, (int) elapsedTime));
        warningLabel.setText(game.grip.isConnected() ? "" : "Handgrip: " + game.grip.getStatus());
    }

    private void draw() {
        ScreenUtils.clear(0.53f, 0.78f, 0.95f, 1f);
        viewport.apply();
        ShapeRenderer shapes = game.shapes;
        shapes.setProjectionMatrix(viewport.getCamera().combined);

        // Faixa-alvo semitransparente
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(1f, 1f, 1f, 0.35f);
        float zoneBottom = MathUtils.lerp(GROUND_Y, CEILING_Y, TARGET_MIN);
        float zoneTop = MathUtils.lerp(GROUND_Y, CEILING_Y, TARGET_MAX);
        shapes.rect(0, zoneBottom - PLAYER_RADIUS, W, zoneTop - zoneBottom + 2 * PLAYER_RADIUS);
        shapes.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        // Chão com faixas rolando para dar sensação de movimento
        shapes.setColor(0.35f, 0.65f, 0.3f, 1f);
        shapes.rect(0, 0, W, GROUND_Y - PLAYER_RADIUS);
        shapes.setColor(0.3f, 0.55f, 0.25f, 1f);
        float stripeOffset = scrollX % 80f;
        for (float x = -stripeOffset; x < W; x += 80f) {
            shapes.rect(x, 0, 40f, GROUND_Y - PLAYER_RADIUS);
        }

        // Personagem provisório (será trocado pelo sprite do tema escolhido)
        shapes.setColor(Color.FIREBRICK);
        shapes.circle(PLAYER_X, playerY, PLAYER_RADIUS);
        shapes.setColor(Color.BROWN);
        shapes.rect(PLAYER_X - 8, playerY - PLAYER_RADIUS - 14, 16, 12);
        shapes.end();
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
    }
}
