package br.mackenzie.world;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CourseTest {
    private static final float MIN_Y = 0f;
    private static final float MAX_Y = 100f;

    private final LevelConfig level = new LevelConfig("Teste", 100f,
        new LevelConfig.Segment(4f, 0.2f, 0.4f),
        new LevelConfig.Segment(4f, 0.6f, 0.8f));
    private final Course course = new Course(level, MIN_Y, MAX_Y, 0f);

    @Test
    public void coinsStayInsideTheTargetBand() {
        for (CourseItem item : course.getItems()) {
            if (item.type != CourseItem.Type.COIN) continue;
            LevelConfig.Segment segment = level.segments[level.segmentAt(item.distance)];
            float force = item.y / MAX_Y;
            assertTrue(segment.contains(force));
        }
    }

    @Test
    public void birdsStayOutsideTheTargetBandAndSkipFirstSegment() {
        int birds = 0;
        for (CourseItem item : course.getItems()) {
            if (item.type != CourseItem.Type.BIRD) continue;
            birds++;
            int index = level.segmentAt(item.distance);
            assertTrue(index > 0);
            assertFalse(level.segments[index].contains(item.y / MAX_Y));
        }
        assertEquals(1, birds);
    }
}
