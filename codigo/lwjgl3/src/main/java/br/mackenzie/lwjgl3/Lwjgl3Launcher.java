package br.mackenzie.lwjgl3;

import br.mackenzie.ReabilitacaoGame;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

/** Inicia a versão desktop. */
public class Lwjgl3Launcher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Jogo do Balão");
        config.setWindowedMode(800, 480);
        config.useVsync(true);
        config.setForegroundFPS(60);
        config.setWindowIcon(Files.FileType.Internal,
            "icones/icone128.png", "icones/icone64.png", "icones/icone32.png", "icones/icone16.png");
        new Lwjgl3Application(new ReabilitacaoGame(), config);
    }
}
