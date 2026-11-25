package com.kevinandrey.simulation.bouncing.gui;

import com.kevinandrey.simulation.bouncing.domain.RulerState;

import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

/**
 * Swing component that delegates painting to {@link RulerRenderer} and keeps a short trail of positions.
 */
final class RulerCanvas extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final int TRAIL_LENGTH = 24;

    private final RulerRenderer renderer = new RulerRenderer();
    private final Deque<Long> trail = new ArrayDeque<>();
    private long length = 1;
    private RulerState state;

    RulerCanvas() {
        setPreferredSize(new Dimension(920, 260));
        setBackground(RulerRenderer.BACKGROUND);
    }

    void reset(long newLength, RulerState newState) {
        trail.clear();
        show(newLength, newState);
    }

    void show(long newLength, RulerState newState) {
        this.length = newLength;
        this.state = newState;
        trail.addLast(newState.position());
        while (trail.size() > TRAIL_LENGTH) {
            trail.removeFirst();
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        if (state == null) {
            return;
        }

        Graphics2D g = (Graphics2D) graphics.create();
        try {
            renderer.render(g, getWidth(), getHeight(), length, state, new ArrayList<>(trail));
        } finally {
            g.dispose();
        }
    }
}
