package br.mackenzie.screens;

import br.mackenzie.ReabilitacaoGame;
import br.mackenzie.metrics.LevelResult;
import br.mackenzie.ui.EvolutionChart;
import br.mackenzie.world.LevelConfig;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import java.util.Locale;

/**
 * Evolução do jogador em todas as partidas salvas: gráfico do tempo na faixa-alvo
 * e um resumo por fase comparando a primeira e a última partida.
 */
public class HistoryScreen extends ScreenAdapter {
    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final int MAX_POINTS = 20;
    private static final Color CAPTION = new Color(0.7f, 0.75f, 0.85f, 1f);

    private final ReabilitacaoGame game;
    private final Stage stage;

    public HistoryScreen(ReabilitacaoGame game) {
        this.game = game;
        stage = new Stage(new FitViewport(ReabilitacaoGame.WORLD_WIDTH, ReabilitacaoGame.WORLD_HEIGHT), game.batch);

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        table.add(new Label("Sua evolução", game.skin, "title")).padBottom(8).row();

        Array<LevelResult> results = game.history.getResults();
        if (results.size == 0) {
            Label empty = new Label("Nenhuma partida salva ainda.\nJogue uma fase para começar o histórico.", game.skin);
            empty.setAlignment(Align.center);
            table.add(empty).pad(40).row();
        } else {
            table.add(caption("Tempo na faixa-alvo em cada partida (últimas " + MAX_POINTS + ")")).padBottom(6).row();
            table.add(new EvolutionChart(game.shapes, game.skin.getFont("small"), results, MAX_POINTS))
                .size(620, 140).padBottom(12).row();
            table.add(buildSummary()).padBottom(4).row();
            table.add(caption("Na faixa e Resposta: primeira partida / última partida")).padBottom(8).row();
        }
        table.add(caption("Arquivo: " + game.history.getFilePath())).padBottom(10).row();

        TextButton backButton = new TextButton("Voltar", game.skin);
        table.add(backButton).width(200).height(45);
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.postRunnable(() -> {
                    game.setScreen(new MenuScreen(game));
                    dispose();
                });
            }
        });
    }

    private Table buildSummary() {
        Table summary = new Table();
        summary.defaults().padRight(28).left();
        for (String header : new String[] {"", "Partidas", "Na faixa", "Resposta", "Recorde"}) {
            summary.add(new Label(header, game.skin, "small"));
        }
        summary.row();

        for (int index = 0; index < LevelConfig.COUNT; index++) {
            Array<LevelResult> attempts = game.history.forLevel(index);
            Label name = new Label("Fase " + (index + 1), game.skin, "small");
            name.setColor(EvolutionChart.colorFor(index));
            summary.add(name);
            summary.add(new Label(String.valueOf(attempts.size), game.skin, "small"));
            if (attempts.size == 0) {
                summary.add(new Label("-", game.skin, "small"));
                summary.add(new Label("-", game.skin, "small"));
                summary.add(new Label("-", game.skin, "small")).row();
                continue;
            }
            LevelResult first = attempts.first();
            LevelResult last = attempts.peek();
            int best = 0;
            for (LevelResult attempt : attempts) best = Math.max(best, attempt.score);
            summary.add(new Label(Math.round(100f * first.consistency) + "% / "
                + Math.round(100f * last.consistency) + "%", game.skin, "small"));
            summary.add(new Label(String.format(PT_BR, "%.1f s / %.1f s",
                first.averageResponseTime, last.averageResponseTime), game.skin, "small"));
            summary.add(new Label(String.valueOf(best), game.skin, "small")).row();
        }
        return summary;
    }

    private Label caption(String text) {
        Label label = new Label(text, game.skin, "small");
        label.setColor(CAPTION);
        return label;
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
