package br.mackenzie.screens;

import br.mackenzie.ReabilitacaoGame;
import br.mackenzie.audio.SoundEffects;
import br.mackenzie.metrics.LevelResult;
import br.mackenzie.ui.StarsActor;
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
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import java.util.Locale;

/**
 * Tela de resultados ao fim de uma fase: estrelas, métricas da fase
 * e a evolução do jogador entre as fases jogadas nesta sessão.
 */
public class ResultScreen extends ScreenAdapter {
    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final Color HIGHLIGHT = new Color(1f, 0.85f, 0.3f, 1f);
    private static final Color CAPTION = new Color(0.7f, 0.75f, 0.85f, 1f);

    private final ReabilitacaoGame game;
    private final LevelResult result;
    private final Stage stage;

    public ResultScreen(ReabilitacaoGame game, LevelResult result) {
        this.game = game;
        this.result = result;
        stage = new Stage(new FitViewport(ReabilitacaoGame.WORLD_WIDTH, ReabilitacaoGame.WORLD_HEIGHT), game.batch);

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        table.add(new Label(result.levelName + " concluída!", game.skin, "title")).padBottom(10).row();
        StarsActor stars = new StarsActor(game.shapes, result.stars, 28f, 80f, 0.4f);
        stars.setOnStarRevealed(() -> game.sounds.play(SoundEffects.Effect.STAR));
        table.add(stars).padBottom(10).row();
        table.add(new Label(messageFor(result.stars), game.skin)).padBottom(4).row();
        Label comparison = new Label(comparisonText(), game.skin, "small");
        comparison.setColor(CAPTION);
        table.add(comparison).padBottom(14).row();

        Table columns = new Table();
        columns.add(buildStats()).top().padRight(50);
        columns.add(buildEvolution()).top();
        table.add(columns).padBottom(25).row();
        table.add(buildButtons());
    }

    private Table buildStats() {
        Table stats = new Table();
        stats.add(new Label("Esta fase", game.skin)).colspan(2).left().padBottom(4).row();
        addStat(stats, "Pontos", String.valueOf(result.score));
        addStat(stats, "Na faixa-alvo", Math.round(100f * result.consistency) + "%");
        addStat(stats, "Resposta média", String.format(PT_BR, "%.1f s", result.averageResponseTime));
        addStat(stats, "Moedas", result.coins + " de " + result.totalCoins);
        addStat(stats, "Pássaros", String.valueOf(result.birdHits));
        return stats;
    }

    private void addStat(Table stats, String name, String value) {
        stats.add(new Label(name, game.skin, "small")).left().padRight(25);
        stats.add(new Label(value, game.skin, "small")).right().row();
    }

    // A partida atual já está no histórico; a anterior desta fase é a penúltima
    private String comparisonText() {
        Array<LevelResult> attempts = game.history.forLevel(result.levelIndex);
        if (attempts.size < 2) return "Primeira partida registrada nesta fase.";
        LevelResult previous = attempts.get(attempts.size - 2);
        return String.format(PT_BR, "Partida anterior: %d%% na faixa e %.1f s de resposta",
            Math.round(100f * previous.consistency), previous.averageResponseTime);
    }

    // Última partida de cada fase (do histórico salvo), para comparar consistência e tempo de resposta entre as fases
    private Table buildEvolution() {
        Table evolution = new Table();
        evolution.defaults().padRight(18);
        evolution.add(new Label("Evolução nas fases", game.skin)).colspan(5).left().padBottom(4).row();
        for (String header : new String[] {"", "Na faixa", "Resposta", "Pontos", ""}) {
            evolution.add(new Label(header, game.skin, "small")).left();
        }
        evolution.row();

        for (int index = 0; index < LevelConfig.COUNT; index++) {
            LevelResult last = game.history.lastFor(index);
            Color color = index == result.levelIndex ? HIGHLIGHT : Color.WHITE;
            evolution.add(smallLabel("Fase " + (index + 1), color)).left();
            if (last == null) {
                evolution.add(smallLabel("-", color)).left();
                evolution.add(smallLabel("-", color)).left();
                evolution.add(smallLabel("-", color)).left();
                evolution.add().row();
                continue;
            }
            evolution.add(smallLabel(Math.round(100f * last.consistency) + "%", color)).left();
            evolution.add(smallLabel(String.format(PT_BR, "%.1f s", last.averageResponseTime), color)).left();
            evolution.add(smallLabel(String.valueOf(last.score), color)).left();
            evolution.add(new StarsActor(game.shapes, last.stars, 8f, 20f, 0f)).left().row();
        }
        return evolution;
    }

    private Label smallLabel(String text, Color color) {
        Label label = new Label(text, game.skin, "small");
        label.setColor(color);
        return label;
    }

    private Table buildButtons() {
        Table buttons = new Table();
        buttons.defaults().width(220).height(50).pad(0, 8, 0, 8);
        addButton(buttons, "Jogar de novo", () -> new GameScreen(game, result.levelIndex));
        if (result.levelIndex + 1 < LevelConfig.COUNT) {
            addButton(buttons, "Próxima fase", () -> new GameScreen(game, result.levelIndex + 1));
        }
        addButton(buttons, "Menu principal", () -> new MenuScreen(game));
        return buttons;
    }

    private interface ScreenFactory {
        ScreenAdapter create();
    }

    private void addButton(Table buttons, String text, ScreenFactory next) {
        TextButton button = new TextButton(text, game.skin);
        buttons.add(button);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.postRunnable(() -> {
                    game.setScreen(next.create());
                    dispose();
                });
            }
        });
    }

    private static String messageFor(int stars) {
        switch (stars) {
            case 3: return "Excelente! Força firme e respostas rápidas.";
            case 2: return "Muito bem! Falta pouco para as três estrelas.";
            case 1: return "Bom trabalho! Tente ficar mais tempo na faixa.";
            default: return "Continue praticando, você vai melhorar!";
        }
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
