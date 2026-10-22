package br.mackenzie.input;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extrai o valor numérico de uma linha enviada pelo Arduino.
 * Aceita linhas como "512", "Forca: 512" ou "Sentido: -1" (exemplo do professor).
 */
public final class SerialLineParser {
    private static final Pattern NUMBER = Pattern.compile("-?\\d+(?:[.,]\\d+)?");

    private SerialLineParser() {
    }

    /** Retorna o último número da linha, ou null se não houver nenhum. */
    public static Float parse(String line) {
        if (line == null) return null;
        Matcher matcher = NUMBER.matcher(line);
        String last = null;
        while (matcher.find()) last = matcher.group();
        if (last == null) return null;
        return Float.parseFloat(last.replace(',', '.'));
    }

    /** Converte o valor bruto para a faixa 0..1. */
    public static float normalize(float raw, float rawMin, float rawMax) {
        if (rawMax == rawMin) return 0f;
        float value = (raw - rawMin) / (rawMax - rawMin);
        return Math.max(0f, Math.min(1f, value));
    }
}
