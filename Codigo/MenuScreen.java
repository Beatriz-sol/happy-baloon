package br.mackenzie.screens;

import br.mackenzie.ReabilitacaoGame;
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
 * Menu principal: iniciar o jogo, sair e testar o aperto.
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
        TextButton exitButton = new TextButton("Sair", game.skin);
        statusLabel = new Label("Segure ESPAÇO para apertar", game.skin);

        table.add(new Label("Jogo do Balão", game.skin, "title")).padBottom(40).row();
        table.add(startButton).width(260).height(55).padBottom(15).row();
        table.add(exitButton).width(260).height(55).padBottom(40).row();
        table.add(statusLabel).row();

        startButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Troca de tela fora do tratamento do clique, para não descartar o Stage em uso
                Gdx.app.postRunnable(() -> {
                    game.setScreen(new GameScreen(game));
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

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.08f, 0.12f, 0.22f, 1f);
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
