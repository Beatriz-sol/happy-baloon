package br.mackenzie.screens;

import br.mackenzie.ReabilitacaoGame;
import br.mackenzie.world.LevelConfig;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
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
 * Menu principal: iniciar o jogo, escolher a fase, ver o histórico, ligar o som,
 * configurar o Arduino e testar o aperto.
 */
public class MenuScreen extends ScreenAdapter {
    private final ReabilitacaoGame game;
    private final Stage stage;
    private final Label statusLabel;

    public MenuScreen(ReabilitacaoGame game) {
        this.game = game;
        stage = new Stage(new FitViewport(ReabilitacaoGame.WORLD_WIDTH, ReabilitacaoGame.WORLD_HEIGHT), game.batch);

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        TextButton startButton = new TextButton("Iniciar", game.skin);
        TextButton historyButton = new TextButton("Histórico", game.skin);
        TextButton exitButton = new TextButton("Sair", game.skin);
        statusLabel = new Label(game.grip.getStatusText(), game.skin, "small");

        Table levelButtons = new Table();
        for (int i = 0; i < LevelConfig.COUNT; i++) {
            TextButton levelButton = new TextButton("Fase " + (i + 1), game.skin);
            levelButtons.add(levelButton).width(120).height(45).pad(0, 5, 0, 5);
            startLevelOnClick(levelButton, i);
        }

        table.add(new Label("Jogo do Balão", game.skin, "title")).padBottom(30).row();
        table.add(startButton).width(260).height(55).padBottom(12).row();
        table.add(new Label("ou escolha a fase:", game.skin, "small")).padBottom(6).row();
        table.add(levelButtons).padBottom(12).row();
        Table bottomButtons = new Table();
        bottomButtons.add(historyButton).width(170).height(50).padRight(10);
        bottomButtons.add(exitButton).width(170).height(50);
        table.add(bottomButtons).padBottom(25).row();

        Label credits = new Label("Beatriz Soares - Jogos Digitais", game.skin, "small");
        credits.setColor(0.6f, 0.65f, 0.75f, 1f);
        credits.setPosition(12, 10);
        stage.addActor(credits);

        TextButton arduinoButton = new TextButton("Arduino", game.skin);
        arduinoButton.setBounds(15, ReabilitacaoGame.WORLD_HEIGHT - 50, 140, 40);
        stage.addActor(arduinoButton);
        arduinoButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.postRunnable(() -> {
                    game.setScreen(new ArduinoScreen(game));
                    dispose();
                });
            }
        });

        TextButton soundButton = new TextButton(soundText(), game.skin);
        soundButton.setBounds(ReabilitacaoGame.WORLD_WIDTH - 185, ReabilitacaoGame.WORLD_HEIGHT - 50, 170, 40);
        stage.addActor(soundButton);
        soundButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.sounds.setEnabled(!game.sounds.isEnabled());
                soundButton.setText(soundText());
            }
        });
        table.add(statusLabel).padBottom(40).row();

        startLevelOnClick(startButton, 0);
        historyButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.postRunnable(() -> {
                    game.setScreen(new HistoryScreen(game));
                    dispose();
                });
            }
        });
        exitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit();
            }
        });
    }

    private String soundText() {
        return game.sounds.isEnabled() ? "Som: ligado" : "Som: desligado";
    }

    private void startLevelOnClick(TextButton button, int levelIndex) {
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Troca de tela fora do tratamento do clique, para não descartar o Stage em uso
                Gdx.app.postRunnable(() -> {
                    game.setScreen(new GameScreen(game, levelIndex));
                    dispose();
                });
            }
        });
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.08f, 0.12f, 0.22f, 1f);
        statusLabel.setText(game.grip.getStatusText());
        stage.act(delta);
        stage.draw();
        drawForceBar();
    }

    // Barra na parte de baixo para testar o aperto antes de jogar
    private void drawForceBar() {
        ShapeRenderer shapes = game.shapes;
        shapes.setProjectionMatrix(stage.getCamera().combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(Color.DARK_GRAY);
        shapes.rect(250, 40, 300, 20);
        shapes.setColor(Color.LIME);
        shapes.rect(250, 40, 300 * game.grip.getForce(), 20);
        shapes.end();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
