package br.mackenzie.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

/**
 * Simula o aperto pelo teclado: segurar ESPAÇO aumenta a força e soltar diminui.
 */
public class KeyboardGripInput implements GripInput {
    private static final float SQUEEZE_RATE = 1.2f; // força por segundo ao apertar
    private static final float RELEASE_RATE = 1.6f; // força por segundo ao soltar

    private float force;

    @Override
    public void update(float delta) {
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            force = Math.min(1f, force + SQUEEZE_RATE * delta);
        } else {
            force = Math.max(0f, force - RELEASE_RATE * delta);
        }
    }

    @Override
    public float getForce() {
        return force;
    }
}
