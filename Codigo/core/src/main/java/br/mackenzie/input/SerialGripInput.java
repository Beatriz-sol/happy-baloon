package br.mackenzie.input;

import com.fazecast.jSerialComm.SerialPort;

import java.io.InputStream;

/**
 * Lê o handgrip pela porta serial em uma thread separada.
 * Baseado no ArduinoReaderThread disponibilizado pelo professor.
 */
public class SerialGripInput implements GripInput {
    private final GripConfig config;
    private Thread readerThread;

    // 'volatile' garante que a thread do jogo veja os valores escritos pela thread serial
    private volatile float lastRaw;
    private volatile boolean connected;
    private volatile String status = "Desconectado";

    public SerialGripInput(GripConfig config) {
        this.config = config;
        this.lastRaw = config.rawMin;
    }

    @Override
    public void start() {
        readerThread = new Thread(this::readLoop, "handgrip-serial");
        readerThread.setDaemon(true);
        readerThread.start();
    }

    private void readLoop() {
        SerialPort port = findPort();
        if (port == null) {
            status = "Nenhuma porta serial encontrada";
            return;
        }
        if (!port.openPort()) {
            status = "Erro ao abrir " + port.getSystemPortName();
            return;
        }
        port.setBaudRate(config.baudRate);
        connected = true;
        status = "Conectado em " + port.getSystemPortName();

        InputStream in = port.getInputStream();
        StringBuilder currentLine = new StringBuilder();
        try {
            while (!Thread.currentThread().isInterrupted()) {
                if (in.available() > 0) {
                    char character = (char) in.read();
                    if (character == '\n') {
                        Float value = SerialLineParser.parse(currentLine.toString());
                        if (value != null) lastRaw = value;
                        currentLine.setLength(0);
                    } else {
                        currentLine.append(character);
                    }
                }
                Thread.sleep(1);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            status = "Erro na leitura serial: " + e.getMessage();
        } finally {
            connected = false;
            if (port.isOpen()) port.closePort();
        }
    }

    private SerialPort findPort() {
        if (!"auto".equalsIgnoreCase(config.port)) return SerialPort.getCommPort(config.port);
        SerialPort[] ports = SerialPort.getCommPorts();
        return ports.length > 0 ? ports[0] : null;
    }

    @Override
    public void update(float delta) {
    }

    @Override
    public float getForce() {
        return SerialLineParser.normalize(lastRaw, config.rawMin, config.rawMax);
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public void dispose() {
        if (readerThread == null) return;
        // Garante que a porta serial seja liberada quando o jogo fechar
        readerThread.interrupt();
        try {
            readerThread.join(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
