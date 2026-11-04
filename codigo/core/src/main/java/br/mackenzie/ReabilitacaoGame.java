package br.mackenzie;

import br.mackenzie.audio.SoundEffects;
import br.mackenzie.input.GripManager;
import br.mackenzie.metrics.History;
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
    /** Força do aperto: handgrip com Arduino quando conectado, senão a barra de ESPAÇO. */
    public GripManager grip;
    public SoundEffects sounds;
    /** Todas as partidas já jogadas, salvas em arquivo para mostrar a evolução. */
    public History history;

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapes = new ShapeRenderer();
        skin = UiSkin.create();

        grip = new GripManager();
        sounds = new SoundEffects();
        history = new History();

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
        sounds.dispose();
        grip.dispose();
    }
}
