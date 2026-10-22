package br.mackenzie.lwjgl3;

import br.mackenzie.ReabilitacaoGame;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import java.util.Arrays;

/** Inicia a versão desktop. Use o argumento --simulador para jogar sem o handgrip. */
public class Lwjgl3Launcher {
    public static void main(String[] args) {
        boolean simulator = Arrays.asList(args).contains("--simulador");

        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Jogo Handgrip");
        config.setWindowedMode(800, 480);
        config.useVsync(true);
        config.setForegroundFPS(60);
        new Lwjgl3Application(new ReabilitacaoGame(simulator), config);
    }
}
