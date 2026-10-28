package br.mackenzie;

import br.mackenzie.input.GripInput;
import br.mackenzie.input.KeyboardGripInput;
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

    public SpriteBatch batch;
    public ShapeRenderer shapes;
    public Skin skin;
    public GripInput grip;

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapes = new ShapeRenderer();
        skin = UiSkin.create();

        grip = new KeyboardGripInput();

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
        batch.dispose();
        shapes.dispose();
        skin.dispose();
    }
}
