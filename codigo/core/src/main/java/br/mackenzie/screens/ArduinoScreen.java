package br.mackenzie.screens;

import br.mackenzie.ReabilitacaoGame;
import br.mackenzie.input.GripManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.fazecast.jSerialComm.SerialPort;

/**
 * Conexão e calibração do handgrip com Arduino: mostra a porta, a leitura ao vivo
 * e calibra a mão solta (0%) e o aperto máximo (100%) do jogador.
 */
public class ArduinoScreen extends ScreenAdapter {
    private enum Step { NONE, RELAX, SQUEEZE }

    private static final float RELAX_SECONDS = 2f;
    private static final float SQUEEZE_SECONDS = 3f;
    private static final int MAX_PORT_BUTTONS = 5;
    private static final float BAR_WIDTH = 360f;
    private static final float BAR_HEIGHT = 20f;
    private static final Color CAPTION = new Color(0.7f, 0.75f, 0.85f, 1f);

    private final ReabilitacaoGame game;
    private final GripManager grip;
    private final Stage stage;
    private final Label statusLabel;
    private final Label readingLabel;
    private final Label calibrationLabel;
    private final Label instructionLabel;
    private final Table portButtons = new Table();
    private final Actor barArea = new Actor();
    private final Vector2 barPosition = new Vector2();

    private Step step = Step.NONE;
    private float stepTime;
    private float relaxedSum;
    private int relaxedSamples;
    private float relaxedValue;
    private int peak;

    public ArduinoScreen(ReabilitacaoGame game) {
        this.game = game;
        this.grip = game.grip;
        stage = new Stage(new FitViewport(ReabilitacaoGame.WORLD_WIDTH, ReabilitacaoGame.WORLD_HEIGHT), game.batch);

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        statusLabel = new Label("", game.skin);
        readingLabel = new Label("", game.skin, "small");
        calibrationLabel = new Label("", game.skin, "small");
        calibrationLabel.setColor(CAPTION);
        instructionLabel = new Label("", game.skin);
        instructionLabel.setColor(1f, 0.85f, 0.3f, 1f);
        instructionLabel.setAlignment(Align.center);

        table.add(new Label("Handgrip com Arduino", game.skin, "title")).padBottom(12).row();
        table.add(statusLabel).padBottom(10).row();
        table.add(readingLabel).padBottom(6).row();
        table.add(barArea).size(BAR_WIDTH, BAR_HEIGHT).padBottom(10).row();
        table.add(calibrationLabel).padBottom(8).row();
        table.add(instructionLabel).height(30).padBottom(8).row();
        Label portsCaption = new Label("Portas USB encontradas (clique para usar uma delas):", game.skin, "small");
        portsCaption.setColor(CAPTION);
        table.add(portsCaption).padBottom(4).row();
        table.add(portButtons).height(36).padBottom(16).row();

        TextButton searchButton = new TextButton("Procurar de novo", game.skin);
        TextButton calibrateButton = new TextButton("Calibrar", game.skin);
        TextButton backButton = new TextButton("Voltar", game.skin);
        Table buttons = new Table();
        buttons.defaults().height(50).pad(0, 6, 0, 6);
        buttons.add(searchButton).width(240);
        buttons.add(calibrateButton).width(170);
        buttons.add(backButton).width(170);
        table.add(buttons);

        searchButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                step = Step.NONE;
                grip.search(null);
                buildPortButtons();
            }
        });
        calibrateButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                startCalibration();
            }
        });
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.postRunnable(() -> {
                    game.setScreen(new MenuScreen(game));
                    dispose();
                });
            }
        });
        buildPortButtons();
    }

    private void buildPortButtons() {
        portButtons.clearChildren();
        SerialPort[] ports = GripManager.listPorts();
        if (ports.length == 0) {
            portButtons.add(new Label("nenhuma porta", game.skin, "small"));
            return;
        }
        for (int i = 0; i < ports.length && i < MAX_PORT_BUTTONS; i++) {
            SerialPort port = ports[i];
            TextButton button = new TextButton(port.getSystemPortName(), game.skin);
            portButtons.add(button).width(130).height(36).pad(0, 4, 0, 4);
            button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    step = Step.NONE;
                    grip.search(port);
                }
            });
        }
    }

    private void startCalibration() {
        if (!grip.isUsingArduino()) {
            instructionLabel.setText("Conecte o Arduino antes de calibrar.");
            return;
        }
        step = Step.RELAX;
        stepTime = 0f;
        relaxedSum = 0f;
        relaxedSamples = 0;
    }

    // Primeiro mede a mão solta (média) e depois o aperto máximo (maior valor)
    private void updateCalibration(float delta) {
        if (step == Step.NONE) return;
        int raw = grip.getRaw();
        if (raw < 0) {
            step = Step.NONE;
            instructionLabel.setText("O Arduino desconectou durante a calibração.");
            return;
        }
        stepTime += delta;
        if (step == Step.RELAX) {
            relaxedSum += raw;
            relaxedSamples++;
            instructionLabel.setText("Deixe a mão solta... " + (int) Math.ceil(RELAX_SECONDS - stepTime));
            if (stepTime >= RELAX_SECONDS) {
                relaxedValue = relaxedSum / relaxedSamples;
                peak = raw;
                step = Step.SQUEEZE;
                stepTime = 0f;
            }
        } else {
            peak = Math.max(peak, raw);
            instructionLabel.setText("Aperte o máximo que conseguir... " + (int) Math.ceil(SQUEEZE_SECONDS - stepTime));
            if (stepTime >= SQUEEZE_SECONDS) {
                step = Step.NONE;
                instructionLabel.setText(grip.setCalibration(relaxedValue, peak)
                    ? "Calibração salva!"
                    : "O aperto mudou pouco a leitura. Confira o sensor e tente de novo.");
            }
        }
    }

    private void updateLabels() {
        switch (grip.getState()) {
            case SEARCHING:
                statusLabel.setText("Procurando o Arduino...");
                break;
            case CONNECTED:
                statusLabel.setText("Conectado em " + grip.getPortName());
                break;
            default:
                statusLabel.setText("Arduino não encontrado. Confira o cabo e feche o Monitor Serial.");
        }
        int raw = grip.getRaw();
        readingLabel.setText(raw >= 0
            ? "Leitura do sensor: " + raw + "   Força: " + Math.round(100f * grip.getForce()) + "%"
            : "Sem Arduino: a barra mostra a força da tecla ESPAÇO");
        calibrationLabel.setText("Calibração: mão solta = " + Math.round(grip.getRelaxedRaw())
            + ", aperto máximo = " + Math.round(grip.getMaxRaw()));
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        updateCalibration(delta);
        updateLabels();
        ScreenUtils.clear(0.08f, 0.12f, 0.22f, 1f);
        stage.act(delta);
        stage.draw();
        drawForceBar();
    }

    private void drawForceBar() {
        barArea.localToStageCoordinates(barPosition.set(0f, 0f));
        ShapeRenderer shapes = game.shapes;
        shapes.setProjectionMatrix(stage.getCamera().combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(Color.DARK_GRAY);
        shapes.rect(barPosition.x, barPosition.y, BAR_WIDTH, BAR_HEIGHT);
        shapes.setColor(Color.LIME);
        shapes.rect(barPosition.x, barPosition.y, BAR_WIDTH * game.grip.getForce(), BAR_HEIGHT);
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
