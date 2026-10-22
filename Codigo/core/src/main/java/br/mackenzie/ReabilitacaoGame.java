package br.mackenzie;

import br.mackenzie.input.GripConfig;
import br.mackenzie.input.GripInput;
import br.mackenzie.input.SerialGripInput;
import br.mackenzie.input.SimulatedGripInput;
import br.mackenzie.screens.MenuScreen;
import br.mackenzie.ui.UiSkin;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

/**
 * Classe principal: guarda os recursos compartilhados entre as telas.
 */
public class ReabilitacaoGame extends Game {
    public static final float WORLD_WIDTH = 800f;
    public static final float WORLD_HEIGHT = 480f;

    private final boolean forceSimulator;

    public SpriteBatch batch;
    public ShapeRenderer shapes;
    public Skin skin;
    public GripInput grip;

    public ReabilitacaoGame(boolean forceSimulator) {
        this.forceSimulator = forceSimulator;
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapes = new ShapeRenderer();
        skin = UiSkin.create();

        GripConfig config = GripConfig.load();
        grip = (forceSimulator || config.simulator) ? new SimulatedGripInput() : new SerialGripInput(config);
        grip.start();

        setScreen(new MenuScreen(this));
    }

    @Override
    public void render() {
        grip.update(Gdx.graphics.getDeltaTime());
        super.render();
    }

    @Override
    public void dispose() {
        if (getScreen() != null) getScreen().dispose();
        grip.dispose();
        batch.dispose();
        shapes.dispose();
        skin.dispose();
    }
}
