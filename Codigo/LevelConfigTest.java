package br.mackenzie.world;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class LevelConfigTest {
    private final LevelConfig level = new LevelConfig("Teste", 100f,
        new LevelConfig.Segment(2f, 0.2f, 0.4f),
        new LevelConfig.Segment(3f, 0.5f, 0.7f));

    @Test
    public void findsSegmentByDistance() {
        assertEquals(0, level.segmentAt(0f));
        assertEquals(0, level.segmentAt(199f));
        assertEquals(1, level.segmentAt(200f));
        assertEquals(1, level.segmentAt(499f));
        assertEquals(-1, level.segmentAt(500f));
        assertEquals(500f, level.totalLength(), 0.001f);
    }

    @Test
    public void checksForceInsideTarget() {
        assertTrue(level.segments[0].contains(0.3f));
        assertFalse(level.segments[0].contains(0.5f));
    }
}
