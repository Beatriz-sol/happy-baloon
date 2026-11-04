package br.mackenzie.input;

import com.badlogic.gdx.utils.Disposable;
import com.fazecast.jSerialComm.SerialPort;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lê as linhas "Forca: N" que o Arduino do handgrip manda pela porta serial (USB).
 * A leitura roda numa thread separada para o jogo não travar esperando dados.
 */
public class SerialReader implements Disposable {
    private static final int BAUD_RATE = 9600;
    private static final int READ_TIMEOUT_MILLIS = 200;
    private static final int MAX_LINE_LENGTH = 80;
    private static final Pattern NUMBER = Pattern.compile("-?\\d{1,9}");

    private final SerialPort port;
    private final Thread thread;
    private volatile boolean running = true;
    private volatile int raw;
    private volatile long lastDataMillis; // 0 enquanto nada foi recebido

    private SerialReader(SerialPort port) {
        this.port = port;
        thread = new Thread(this::readLoop, "leitura-arduino");
        thread.setDaemon(true);
        thread.start();
    }

    /** Abre a porta a 9600 baud. Devolve null se ela não existir ou estiver em uso (por exemplo, pelo Monitor Serial). */
    public static SerialReader open(SerialPort port) {
        port.setComPortParameters(BAUD_RATE, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
        port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, READ_TIMEOUT_MILLIS, 0);
        if (!port.openPort()) return null;
        port.setDTR(); // algumas placas só enviam dados com o DTR ligado
        return new SerialReader(port);
    }

    private void readLoop() {
        byte[] buffer = new byte[256];
        StringBuilder line = new StringBuilder();
        while (running) {
            int count = port.readBytes(buffer, buffer.length);
            if (count < 0) break; // cabo desconectado ou porta fechada
            for (int i = 0; i < count; i++) {
                char c = (char) buffer[i];
                if (c == '\n') {
                    parse(line);
                    line.setLength(0);
                } else if (c != '\r' && line.length() < MAX_LINE_LENGTH) {
                    line.append(c);
                }
            }
        }
    }

    // Aceita "Forca: 512" ou só "512"; usa o último número da linha
    private void parse(CharSequence line) {
        Matcher matcher = NUMBER.matcher(line);
        boolean found = false;
        int value = 0;
        while (matcher.find()) {
            value = Integer.parseInt(matcher.group());
            found = true;
        }
        if (found) {
            raw = value;
            lastDataMillis = System.currentTimeMillis();
        }
    }

    /** Espera a primeira leitura chegar. O Arduino Uno reinicia ao abrir a porta e demora uns 2 segundos. */
    public boolean waitForData(long timeoutMillis) {
        long end = System.currentTimeMillis() + timeoutMillis;
        while (running && System.currentTimeMillis() < end) {
            if (lastDataMillis != 0) return true;
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return running && lastDataMillis != 0;
    }

    /** true se chegou alguma leitura nos últimos maxSilenceMillis. */
    public boolean isReceiving(long maxSilenceMillis) {
        long last = lastDataMillis;
        return last != 0 && System.currentTimeMillis() - last <= maxSilenceMillis;
    }

    /** Último valor enviado pelo Arduino (0 a 1023 no sensor FSR). */
    public int getRaw() {
        return raw;
    }

    public String getPortName() {
        return port.getSystemPortName();
    }

    @Override
    public void dispose() {
        running = false;
        port.closePort();
    }
}
