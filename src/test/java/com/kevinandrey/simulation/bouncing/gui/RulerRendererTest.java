package com.kevinandrey.simulation.bouncing.gui;

import com.kevinandrey.simulation.bouncing.domain.Direction;
import com.kevinandrey.simulation.bouncing.domain.RulerState;
import org.junit.jupiter.api.Test;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RulerRendererTest {

    private static final int WIDTH = 640;
    private static final int HEIGHT = 240;

    private final RulerRenderer renderer = new RulerRenderer();

    @Test
    void paintsTheWalkerAtItsProjectedPosition() {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        try {
            renderer.render(g, WIDTH, HEIGHT, 10, new RulerState(5, Direction.FORWARD, 3), Arrays.asList(3L, 4L, 5L));
        } finally {
            g.dispose();
        }

        int usable = WIDTH - 2 * RulerRenderer.MARGIN;
        int x = RulerRenderer.xFor(5, 10, usable);
        int y = RulerRenderer.walkerCenterY(RulerRenderer.baselineY(HEIGHT));

        assertEquals(RulerRenderer.WALKER.getRGB(), image.getRGB(x, y));
        assertEquals(RulerRenderer.BACKGROUND.getRGB(), image.getRGB(2, HEIGHT - 2));
    }

    @Test
    void projectsTheRulerEdgesOntoTheMargins() {
        int usable = WIDTH - 2 * RulerRenderer.MARGIN;

        assertEquals(RulerRenderer.MARGIN, RulerRenderer.xFor(0, 7, usable));
        assertEquals(WIDTH - RulerRenderer.MARGIN, RulerRenderer.xFor(7, 7, usable));
    }

    @Test
    void rendersHugeRulersWithoutTrail() {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        try {
            renderer.render(g, WIDTH, HEIGHT, 1_000_000, new RulerState(0, Direction.BACKWARD, 0),
                    Collections.<Long>emptyList());
        } finally {
            g.dispose();
        }

        int y = RulerRenderer.walkerCenterY(RulerRenderer.baselineY(HEIGHT));
        assertEquals(RulerRenderer.WALKER.getRGB(), image.getRGB(RulerRenderer.MARGIN, y));
    }

    @Test
    void tickStrideUsesNiceNumbers() {
        assertEquals(1, RulerRenderer.stride(10, 544, 8));
        assertEquals(2, RulerRenderer.stride(100, 544, 8));
        assertEquals(20000, RulerRenderer.stride(1_000_000, 544, 8));
        for (long length : new long[] {1, 7, 99, 12_345, 987_654_321}) {
            long stride = RulerRenderer.stride(length, 544, 44);
            assertTrue(length / stride <= 544 / 44 + 1, "length " + length + " stride " + stride);
        }
    }
}
