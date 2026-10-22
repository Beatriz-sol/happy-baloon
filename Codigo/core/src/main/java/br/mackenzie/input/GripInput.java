package br.mackenzie.input;

/**
 * Fonte da força de preensão (handgrip) usada para controlar o jogo.
 */
public interface GripInput {
    void start();

    // Chamado a cada frame pelo jogo; usado pelo simulador.
    void update(float delta);

    /** Força normalizada entre 0 (mão solta) e 1 (força máxima configurada). */
    float getForce();

    boolean isConnected();

    /** Texto curto para mostrar ao jogador (porta, erro, simulador...). */
    String getStatus();

    void dispose();
}
