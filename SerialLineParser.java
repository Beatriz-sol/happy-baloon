package br.mackenzie.input;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class SerialLineParserTest {
    @Test
    public void parsesNumbersInDifferentFormats() {
        assertEquals(512f, SerialLineParser.parse("512"), 0f);
        assertEquals(300f, SerialLineParser.parse("Forca: 300"), 0f);
        assertEquals(-1f, SerialLineParser.parse("Sentido: -1"), 0f);
        assertEquals(2.5f, SerialLineParser.parse("kg: 2,5"), 0f);
        assertNull(SerialLineParser.parse("Nenhum dado"));
    }

    @Test
    public void normalizesToZeroOne() {
        assertEquals(0.5f, SerialLineParser.normalize(50, 0, 100), 0.0001f);
        assertEquals(0f, SerialLineParser.normalize(-10, 0, 100), 0f);
        assertEquals(1f, SerialLineParser.normalize(200, 0, 100), 0f);
    }
}
