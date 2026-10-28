package br.mackenzie.input;

/**
 * Fonte da força de aperto usada para controlar o jogo.
 */
public interface GripInput {
    // Chamado a cada frame pelo jogo
    void update(float delta);

    /** Força normalizada entre 0 (solto) e 1 (força máxima). */
    float getForce();
}
