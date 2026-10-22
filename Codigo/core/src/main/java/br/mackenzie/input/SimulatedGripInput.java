package br.mackenzie.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

/**
 * Simula o handgrip pelo teclado (segurar ESPAÇO = apertar).
 * Serve só para desenvolver sem o dispositivo; a versão entregue usa o handgrip.
 */
public class SimulatedGripInput implements GripInput {
    private static final float SQUEEZE_RATE = 1.2f; // força por segundo ao apertar
    private static final float RELEASE_RATE = 1.6f; // força por segundo ao soltar

    private float force;

    @Override
    public void start() {
    }

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

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public String getStatus() {
        return "Simulador (segure ESPAÇO)";
    }

    @Override
    public void dispose() {
    }
}
