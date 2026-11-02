package br.mackenzie.metrics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import java.util.Locale;

/**
 * Histórico de todas as partidas, salvo em um arquivo CSV na pasta do usuário
 * (no Windows, C:\Users\nome\JogoDoBalao\historico.csv). O arquivo abre no Excel
 * e permite acompanhar a evolução do jogador de uma sessão de reabilitação para outra.
 */
public class History {
    private static final String PATH = "JogoDoBalao/historico.csv";
    private static final String SEPARATOR = ";";
    private static final String HEADER =
        "data;fase;pontos;pontos_maximos;na_faixa_pct;resposta_s;moedas;moedas_total;passaros;estrelas";
    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR"); // vírgula decimal, como o Excel em português espera

    private final FileHandle file;
    private final Array<LevelResult> results = new Array<>();

    public History() {
        file = Gdx.files.external(PATH);
        load();
    }

    private void load() {
        if (!file.exists()) return;
        try {
            String[] lines = file.readString("UTF-8").split("\\r?\\n");
            for (int i = 1; i < lines.length; i++) {
                LevelResult result = parse(lines[i]);
                if (result != null) results.add(result);
            }
        } catch (GdxRuntimeException e) {
            Gdx.app.error("Historico", "Não foi possível ler " + getFilePath(), e);
        }
    }

    // Linhas com erro (arquivo editado à mão, por exemplo) são ignoradas
    private static LevelResult parse(String line) {
        String[] fields = line.split(SEPARATOR);
        if (fields.length < 9) return null;
        try {
            int levelIndex = Integer.parseInt(fields[1].trim()) - 1;
            return new LevelResult(fields[0].trim(), levelIndex, "Fase " + (levelIndex + 1),
                Integer.parseInt(fields[2].trim()), Integer.parseInt(fields[3].trim()),
                Integer.parseInt(fields[4].trim()) / 100f, Float.parseFloat(fields[5].trim().replace(',', '.')),
                Integer.parseInt(fields[6].trim()), Integer.parseInt(fields[7].trim()),
                Integer.parseInt(fields[8].trim()));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Guarda o resultado na memória e acrescenta uma linha no arquivo. */
    public void add(LevelResult result) {
        results.add(result);
        String line = String.join(SEPARATOR,
            result.playedAt,
            String.valueOf(result.levelIndex + 1),
            String.valueOf(result.score),
            String.valueOf(result.maxScore),
            String.valueOf(Math.round(100f * result.consistency)),
            String.format(PT_BR, "%.2f", result.averageResponseTime),
            String.valueOf(result.coins),
            String.valueOf(result.totalCoins),
            String.valueOf(result.birdHits),
            String.valueOf(result.stars));
        try {
            if (!file.exists()) file.writeString(HEADER + "\n", false, "UTF-8");
            file.writeString(line + "\n", true, "UTF-8");
        } catch (GdxRuntimeException e) {
            // Sem permissão de escrita o jogo continua; só o histórico deixa de ser salvo
            Gdx.app.error("Historico", "Não foi possível salvar em " + getFilePath(), e);
        }
    }

    /** Todas as partidas, da mais antiga para a mais recente. */
    public Array<LevelResult> getResults() {
        return results;
    }

    /** Partidas de uma fase, da mais antiga para a mais recente. */
    public Array<LevelResult> forLevel(int levelIndex) {
        Array<LevelResult> level = new Array<>();
        for (LevelResult result : results) {
            if (result.levelIndex == levelIndex) level.add(result);
        }
        return level;
    }

    /** Última partida de uma fase, ou null se ela nunca foi jogada. */
    public LevelResult lastFor(int levelIndex) {
        Array<LevelResult> level = forLevel(levelIndex);
        return level.size > 0 ? level.peek() : null;
    }

    public String getFilePath() {
        return file.file().getAbsolutePath();
    }
}
