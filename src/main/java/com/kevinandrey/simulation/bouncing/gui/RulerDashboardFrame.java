package com.kevinandrey.simulation.bouncing.gui;

import com.kevinandrey.simulation.bouncing.domain.BounceRule;
import com.kevinandrey.simulation.bouncing.domain.Direction;
import com.kevinandrey.simulation.bouncing.domain.InvalidConfigurationException;
import com.kevinandrey.simulation.bouncing.domain.RulerConfiguration;
import com.kevinandrey.simulation.bouncing.domain.RulerState;
import com.kevinandrey.simulation.bouncing.solver.AnalyticalSolver;
import com.kevinandrey.simulation.bouncing.solver.BouncingSolver;
import com.kevinandrey.simulation.bouncing.trajectory.Trajectory;
import com.kevinandrey.simulation.bouncing.trajectory.TrajectoryCsvExporter;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;

/**
 * Interactive desktop dashboard that animates the {@code O(M)} walk and checks every frame against the
 * {@code O(1)} closed-form prediction.
 */
public final class RulerDashboardFrame {

    static final long MAX_GUI_LENGTH = 1_000_000L;
    static final long MAX_GUI_STEPS = 1_000_000_000_000L;

    private final BouncingSolver analytical = new AnalyticalSolver();
    private final TrajectoryCsvExporter exporter = new TrajectoryCsvExporter();
    private final RulerCanvas canvas = new RulerCanvas();

    private final JFrame frame = new JFrame("Bounded Ruler Discrete Bouncing Simulator");
    private final JSpinner lengthSpinner;
    private final JSpinner startSpinner;
    private final JSpinner stepsSpinner;
    private final JComboBox<String> directionBox = new JComboBox<>(new String[] {"+1 forward", "-1 backward"});
    private final JSlider speedSlider = new JSlider(5, 600, 120);
    private final JButton playButton = new JButton("Play");
    private final JLabel stepLabel = new JLabel();
    private final JLabel simulatedLabel = new JLabel();
    private final JLabel predictedLabel = new JLabel();
    private final JLabel finalLabel = new JLabel();
    private final Timer timer;

    private RulerConfiguration configuration;
    private RulerState current;

    private RulerDashboardFrame(RulerConfiguration initial) {
        long initialLength = Math.min(initial.length(), MAX_GUI_LENGTH);
        lengthSpinner = new JSpinner(new SpinnerNumberModel(
                Long.valueOf(initialLength), Long.valueOf(1L), Long.valueOf(MAX_GUI_LENGTH), Long.valueOf(1L)));
        startSpinner = new JSpinner(new SpinnerNumberModel(
                Long.valueOf(Math.min(initial.start(), initialLength)), Long.valueOf(0L),
                Long.valueOf(MAX_GUI_LENGTH), Long.valueOf(1L)));
        stepsSpinner = new JSpinner(new SpinnerNumberModel(
                Long.valueOf(Math.min(initial.steps(), MAX_GUI_STEPS)), Long.valueOf(0L),
                Long.valueOf(MAX_GUI_STEPS), Long.valueOf(1L)));
        directionBox.setSelectedIndex(initial.direction() == Direction.FORWARD ? 0 : 1);
        timer = new Timer(speedSlider.getValue(), event -> advance());

        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout(8, 8));
        frame.add(buildControls(), BorderLayout.NORTH);
        frame.add(canvas, BorderLayout.CENTER);
        frame.add(buildStatus(), BorderLayout.SOUTH);
        frame.pack();
        frame.setLocationRelativeTo(null);

        applyConfiguration();
    }

    /**
     * Opens the dashboard on the Swing event dispatch thread.
     *
     * @param initial configuration loaded in the form
     */
    public static void launch(RulerConfiguration initial) {
        SwingUtilities.invokeLater(() -> new RulerDashboardFrame(initial).frame.setVisible(true));
    }

    private JPanel buildControls() {
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        form.setBorder(BorderFactory.createTitledBorder("Configuration"));
        form.add(new JLabel("N (length)"));
        form.add(lengthSpinner);
        form.add(new JLabel("P (start)"));
        form.add(startSpinner);
        form.add(new JLabel("d (direction)"));
        form.add(directionBox);
        form.add(new JLabel("M (steps)"));
        form.add(stepsSpinner);

        JButton applyButton = new JButton("Apply / Reset");
        applyButton.addActionListener(event -> applyConfiguration());
        JButton stepButton = new JButton("Step");
        stepButton.addActionListener(event -> advance());
        playButton.addActionListener(event -> togglePlayback());
        JButton jumpButton = new JButton("Jump to M (O(1))");
        jumpButton.addActionListener(event -> jumpToEnd());
        JButton exportButton = new JButton("Export CSV");
        exportButton.addActionListener(event -> exportTrajectory());
        speedSlider.setInverted(true);
        speedSlider.setToolTipText("animation delay per step in milliseconds");
        speedSlider.addChangeListener(event -> timer.setDelay(speedSlider.getValue()));

        form.add(applyButton);
        form.add(stepButton);
        form.add(playButton);
        form.add(jumpButton);
        form.add(exportButton);
        form.add(new JLabel("speed"));
        form.add(speedSlider);

        return form;
    }

    private JPanel buildStatus() {
        JPanel status = new JPanel(new GridLayout(2, 2, 12, 4));
        status.setBorder(BorderFactory.createTitledBorder("Step-by-step O(M) versus closed-form O(1)"));
        status.add(stepLabel);
        status.add(simulatedLabel);
        status.add(predictedLabel);
        status.add(finalLabel);

        return status;
    }

    private void applyConfiguration() {
        timer.stop();
        playButton.setText("Play");
        try {
            configuration = RulerConfiguration.of(
                    longValue(lengthSpinner), longValue(startSpinner),
                    directionBox.getSelectedIndex() == 0 ? Direction.FORWARD : Direction.BACKWARD,
                    longValue(stepsSpinner));
        } catch (InvalidConfigurationException e) {
            JOptionPane.showMessageDialog(frame, e.getMessage(), "Invalid configuration", JOptionPane.ERROR_MESSAGE);

            return;
        }

        current = configuration.initialState();
        canvas.reset(configuration.length(), current);
        RulerState finalState = analytical.solve(configuration);
        finalLabel.setText("Closed-form final state after M=" + configuration.steps() + ": position "
                + finalState.position() + ", direction " + finalState.direction().signedLabel());
        refreshStatus();
    }

    private void advance() {
        if (configuration == null || current.step() >= configuration.steps()) {
            timer.stop();
            playButton.setText("Play");

            return;
        }

        current = BounceRule.advance(current, configuration.length());
        canvas.show(configuration.length(), current);
        refreshStatus();
    }

    private void togglePlayback() {
        if (timer.isRunning()) {
            timer.stop();
            playButton.setText("Play");

            return;
        }

        timer.start();
        playButton.setText("Pause");
    }

    private void jumpToEnd() {
        if (configuration == null) {
            return;
        }

        timer.stop();
        playButton.setText("Play");
        current = analytical.solve(configuration);
        canvas.reset(configuration.length(), current);
        refreshStatus();
    }

    private void refreshStatus() {
        RulerState predicted = analytical.solve(configuration.withSteps(current.step()));
        boolean match = predicted.equals(current);
        stepLabel.setText("Step " + current.step() + " of " + configuration.steps()
                + "   (period 2N = " + configuration.period() + ")");
        simulatedLabel.setText("Walker: position " + current.position()
                + ", direction " + current.direction().signedLabel());
        predictedLabel.setText("Triangular wave prediction: position " + predicted.position()
                + ", direction " + predicted.direction().signedLabel() + (match ? "   [match]" : "   [mismatch]"));
    }

    private void exportTrajectory() {
        if (configuration == null) {
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("trajectory.csv"));
        if (chooser.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try {
            long rows = exporter.write(new Trajectory(configuration), chooser.getSelectedFile().toPath());
            JOptionPane.showMessageDialog(frame, "Exported " + rows + " states");
        } catch (IOException | IllegalArgumentException e) {
            JOptionPane.showMessageDialog(frame, e.getMessage(), "Export error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static long longValue(JSpinner spinner) {
        Object value = spinner.getValue();
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }

        throw new InvalidConfigurationException("unexpected spinner value " + value);
    }
}
