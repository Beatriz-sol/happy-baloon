package br.mackenzie.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.io.IOException;
import java.io.Reader;
import java.util.Properties;

/**
 * Configuração do handgrip lida do arquivo controle.properties.
 * O arquivo é procurado primeiro ao lado do executável e depois dentro dos assets.
 */
public class GripConfig {
    public static final String FILE_NAME = "controle.properties";

    // "auto" usa a primeira porta serial encontrada
    public String port = "auto";
    public int baudRate = 9600;
    // Valores brutos enviados pelo Arduino para mão solta e força máxima
    public float rawMin = 0f;
    public float rawMax = 1023f;
    // Simulador pelo teclado: apenas para desenvolvimento sem o dispositivo
    public boolean simulator = false;

    public static GripConfig load() {
        GripConfig config = new GripConfig();
        FileHandle file = Gdx.files.local(FILE_NAME);
        if (!file.exists()) file = Gdx.files.internal(FILE_NAME);
        if (!file.exists()) return config;

        Properties props = new Properties();
        try (Reader reader = file.reader("UTF-8")) {
            props.load(reader);
        } catch (IOException e) {
            Gdx.app.error("GripConfig", "Erro ao ler " + FILE_NAME + ": " + e.getMessage());
            return config;
        }
        config.port = props.getProperty("porta", config.port).trim();
        config.baudRate = Integer.parseInt(props.getProperty("baud", String.valueOf(config.baudRate)).trim());
        config.rawMin = Float.parseFloat(props.getProperty("forcaMin", String.valueOf(config.rawMin)).trim());
        config.rawMax = Float.parseFloat(props.getProperty("forcaMax", String.valueOf(config.rawMax)).trim());
        config.simulator = Boolean.parseBoolean(props.getProperty("simulador", String.valueOf(config.simulator)).trim());
        return config;
    }
}
