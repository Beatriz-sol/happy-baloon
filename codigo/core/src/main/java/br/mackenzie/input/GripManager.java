package br.mackenzie.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.fazecast.jSerialComm.SerialPort;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Escolhe de onde vem a força do aperto: do handgrip com Arduino, quando ele está
 * conectado e mandando dados, ou da barra de ESPAÇO, para testar sem o dispositivo.
 * A leitura do Arduino é convertida para 0..1 pela calibração (mão solta e aperto máximo).
 */
public class GripManager implements GripInput, Disposable {
    public enum State { SEARCHING, CONNECTED, NOT_FOUND }

    private static final long WAIT_FOR_DATA_MILLIS = 3000;
    private static final long MAX_SILENCE_MILLIS = 2000; // sem dados por mais tempo = Arduino desconectado
    private static final float SMOOTHING = 15f;
    private static final float MIN_CALIBRATION_RANGE = 20f;
    // Nomes comuns das portas USB do Arduino e dos adaptadores USB-serial
    private static final String[] ARDUINO_HINTS = {"arduino", "ch340", "ch341", "wch", "usb", "acm", "ftdi", "cp210"};

    private final KeyboardGripInput keyboard = new KeyboardGripInput();
    private final Preferences preferences;
    private final AtomicInteger searchId = new AtomicInteger(); // uma procura nova cancela a anterior
    private volatile SerialReader arduino;
    private volatile SerialReader testing; // porta aberta pela procura, esperando o primeiro dado
    private volatile State state = State.NOT_FOUND;
    private float relaxedRaw;
    private float maxRaw;
    private float arduinoForce;

    public GripManager() {
        preferences = Gdx.app.getPreferences("jogo-do-balao");
        relaxedRaw = preferences.getFloat("arduino-solto", 0f);
        maxRaw = preferences.getFloat("arduino-maximo", 1023f);
        search(null);
    }

    /** Procura o Arduino em segundo plano. Com port diferente de null, tenta só essa porta. */
    public void search(SerialPort port) {
        int id = searchId.incrementAndGet();
        SerialReader previous = testing;
        if (previous != null) previous.dispose(); // libera a porta para a procura nova
        disconnect();
        state = State.SEARCHING;
        Thread thread = new Thread(() -> {
            SerialPort[] candidates = port != null ? new SerialPort[] {port} : likelyPorts();
            for (SerialPort candidate : candidates) {
                if (searchId.get() != id) return;
                SerialReader reader = SerialReader.open(candidate);
                if (reader == null) continue;
                testing = reader;
                boolean receiving = reader.waitForData(WAIT_FOR_DATA_MILLIS);
                testing = null;
                if (searchId.get() != id) {
                    reader.dispose();
                    return;
                }
                if (receiving) {
                    arduino = reader;
                    state = State.CONNECTED;
                    return;
                }
                reader.dispose();
            }
            if (searchId.get() == id) state = State.NOT_FOUND;
        }, "procura-arduino");
        thread.setDaemon(true);
        thread.start();
    }

    /** Portas com cara de Arduino primeiro; as outras só se nenhuma parecer um Arduino. */
    private static SerialPort[] likelyPorts() {
        Array<SerialPort> likely = new Array<>(SerialPort.class);
        SerialPort[] all = listPorts();
        for (SerialPort port : all) {
            String name = (port.getSystemPortName() + " " + port.getDescriptivePortName() + " "
                + port.getPortDescription()).toLowerCase(Locale.ROOT);
            for (String hint : ARDUINO_HINTS) {
                if (name.contains(hint)) {
                    likely.add(port);
                    break;
                }
            }
        }
        return likely.size > 0 ? likely.toArray() : all;
    }

    /** Todas as portas seriais do computador (COM3, COM4... no Windows). */
    public static SerialPort[] listPorts() {
        try {
            return SerialPort.getCommPorts();
        } catch (RuntimeException | UnsatisfiedLinkError e) {
            Gdx.app.error("Arduino", "Não foi possível listar as portas seriais", e);
            return new SerialPort[0];
        }
    }

    private void disconnect() {
        SerialReader reader = arduino;
        arduino = null;
        if (reader != null) reader.dispose();
        if (state == State.CONNECTED) state = State.NOT_FOUND;
    }

    @Override
    public void update(float delta) {
        keyboard.update(delta);
        SerialReader reader = arduino;
        if (reader == null) return;
        if (!reader.isReceiving(MAX_SILENCE_MILLIS)) {
            disconnect(); // cabo solto: volta para o teclado até procurar de novo
            return;
        }
        float target = Math.max(0f, Math.min(1f, (reader.getRaw() - relaxedRaw) / (maxRaw - relaxedRaw)));
        arduinoForce += (target - arduinoForce) * Math.min(1f, delta * SMOOTHING);
    }

    @Override
    public float getForce() {
        return isUsingArduino() ? arduinoForce : keyboard.getForce();
    }

    public boolean isUsingArduino() {
        return arduino != null;
    }

    public State getState() {
        return state;
    }

    /** Leitura bruta do Arduino, ou -1 sem Arduino conectado. */
    public int getRaw() {
        SerialReader reader = arduino;
        return reader != null ? reader.getRaw() : -1;
    }

    public String getPortName() {
        SerialReader reader = arduino;
        return reader != null ? reader.getPortName() : "";
    }

    public float getRelaxedRaw() {
        return relaxedRaw;
    }

    public float getMaxRaw() {
        return maxRaw;
    }

    /** Salva a calibração. Devolve false se a diferença entre solto e apertado for pequena demais. */
    public boolean setCalibration(float relaxed, float max) {
        if (max - relaxed < MIN_CALIBRATION_RANGE) return false;
        relaxedRaw = relaxed;
        maxRaw = max;
        preferences.putFloat("arduino-solto", relaxed);
        preferences.putFloat("arduino-maximo", max);
        preferences.flush();
        return true;
    }

    /** Texto curto para o menu dizendo qual controle está valendo. */
    public String getStatusText() {
        switch (state) {
            case SEARCHING: return "Procurando o Arduino... (ESPAÇO funciona enquanto isso)";
            case CONNECTED: return "Arduino conectado em " + getPortName() + ": aperte o handgrip";
            default: return "Arduino não encontrado: segure ESPAÇO para apertar";
        }
    }

    @Override
    public void dispose() {
        disconnect();
    }
}
