package com.kevinandrey.simulation.bouncing.gui;

import com.kevinandrey.simulation.bouncing.domain.Direction;
import com.kevinandrey.simulation.bouncing.domain.RulerState;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.util.List;

/**
 * Stateless painter of the ruler, its reflecting walls, the recent trail and the walker.
 *
 * <p>It only depends on {@link Graphics2D}, so it renders identically on screen and on off-screen images in
 * headless environments.</p>
 */
public final class RulerRenderer {

    static final Color BACKGROUND = new Color(18, 24, 38);
    static final Color RULER = new Color(200, 210, 225);
    static final Color WALL = new Color(255, 107, 107);
    static final Color WALKER = new Color(0, 217, 255);
    static final Color TRAIL = new Color(0, 217, 255);
    static final Color TEXT = new Color(230, 236, 245);

    static final int MARGIN = 48;
    static final int WALKER_RADIUS = 12;
    private static final int MIN_TICK_SPACING = 8;
    private static final int MIN_LABEL_SPACING = 44;

    /**
     * Paints a full frame.
     *
     * @param g      target graphics, not disposed by this method
     * @param width  drawable width in pixels
     * @param height drawable height in pixels
     * @param length highest mark {@code N}
     * @param state  walker state to highlight
     * @param trail  recently visited positions, oldest first
     */
    public void render(Graphics2D g, int width, int height, long length, RulerState state, List<Long> trail) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(BACKGROUND);
        g.fillRect(0, 0, width, height);

        int baseline = baselineY(height);
        int usable = Math.max(1, width - 2 * MARGIN);
        drawRuler(g, width, baseline, usable, length);
        drawTrail(g, baseline, usable, length, trail);
        drawWalker(g, baseline, usable, length, state);
        drawCaption(g, state);
    }

    static int baselineY(int height) {
        return (int) Math.round(height * 0.62);
    }

    static int xFor(long position, long length, int usable) {
        return MARGIN + (int) Math.round((double) position / (double) length * usable);
    }

    static int walkerCenterY(int baseline) {
        return baseline - WALKER_RADIUS - 10;
    }

    private static void drawRuler(Graphics2D g, int width, int baseline, int usable, long length) {
        g.setColor(RULER);
        g.setStroke(new BasicStroke(3f));
        g.drawLine(MARGIN, baseline, width - MARGIN, baseline);

        long tickStride = stride(length, usable, MIN_TICK_SPACING);
        long labelStride = stride(length, usable, MIN_LABEL_SPACING);
        g.setStroke(new BasicStroke(1.5f));
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        FontMetrics metrics = g.getFontMetrics();
        for (long mark = 0; mark <= length; mark += tickStride) {
            int x = xFor(mark, length, usable);
            boolean labeled = mark % labelStride == 0;
            g.drawLine(x, baseline, x, baseline + (labeled ? 12 : 6));
            if (labeled && length - mark >= labelStride / 2) {
                drawCentered(g, metrics, Long.toString(mark), x, baseline + 26);
            }
        }
        drawCentered(g, metrics, Long.toString(length), xFor(length, length, usable), baseline + 26);

        g.setColor(WALL);
        g.setStroke(new BasicStroke(5f));
        g.drawLine(MARGIN, baseline - 40, MARGIN, baseline + 4);
        g.drawLine(width - MARGIN, baseline - 40, width - MARGIN, baseline + 4);
    }

    private static void drawTrail(Graphics2D g, int baseline, int usable, long length, List<Long> trail) {
        if (trail == null || trail.isEmpty()) {
            return;
        }

        int size = trail.size();
        for (int i = 0; i < size; i++) {
            int alpha = 30 + (int) (150.0 * (i + 1) / size);
            g.setColor(new Color(TRAIL.getRed(), TRAIL.getGreen(), TRAIL.getBlue(), Math.min(alpha, 255)));
            int x = xFor(trail.get(i), length, usable);
            g.fillOval(x - 4, baseline - 4, 8, 8);
        }
    }

    private static void drawWalker(Graphics2D g, int baseline, int usable, long length, RulerState state) {
        int x = xFor(state.position(), length, usable);
        int y = walkerCenterY(baseline);
        g.setColor(WALKER);
        g.fillOval(x - WALKER_RADIUS, y - WALKER_RADIUS, 2 * WALKER_RADIUS, 2 * WALKER_RADIUS);

        int sign = state.direction() == Direction.FORWARD ? 1 : -1;
        int tip = x + sign * (WALKER_RADIUS + 16);
        int base = x + sign * (WALKER_RADIUS + 4);
        Polygon arrow = new Polygon(new int[] {base, tip, base}, new int[] {y - 7, y, y + 7}, 3);
        g.fillPolygon(arrow);
    }

    private static void drawCaption(Graphics2D g, RulerState state) {
        g.setColor(TEXT);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        g.drawString("step " + state.step() + "   position " + state.position()
                + "   direction " + state.direction().signedLabel(), MARGIN, 28);
    }

    private static void drawCentered(Graphics2D g, FontMetrics metrics, String text, int x, int y) {
        g.drawString(text, x - metrics.stringWidth(text) / 2, y);
    }

    static long stride(long length, int usable, int minSpacing) {
        long maxMarks = Math.max(1, usable / minSpacing);
        long raw = Math.max(1, (length + maxMarks - 1) / maxMarks);
        long magnitude = 1;
        while (magnitude <= raw / 10) {
            magnitude *= 10;
        }
        for (long factor : new long[] {1, 2, 5, 10}) {
            if (magnitude * factor >= raw) {
                return magnitude * factor;
            }
        }

        return magnitude * 10;
    }
}
