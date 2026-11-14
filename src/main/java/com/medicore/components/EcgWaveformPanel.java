package com.medicore.components;

import com.medicore.theme.MediCoreTheme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;

/**
 * Animated real-time physiological ECG rhythm strip simulating hospital cardiac telemetry monitor.
 */
public class EcgWaveformPanel extends JPanel {
    private final Timer timer;
    private int sweepX = 0;
    private int heartRate = 78;
    private String rhythmName = "Lead II • Normal Sinus Rhythm";
    private final float[] waveBuffer = new float[1200];
    private boolean pulseBlink = false;
    private int sampleCounter = 0;

    public EcgWaveformPanel() {
        setPreferredSize(new Dimension(360, 110));
        setMinimumSize(new Dimension(280, 90));
        setBackground(new Color(0x0A0F1D)); // Deep dark telemetry monitor background

        for (int i = 0; i < waveBuffer.length; i++) {
            waveBuffer[i] = 0.0f;
        }

        // 30 FPS telemetry sweep animation
        timer = new Timer(33, e -> {
            advanceWaveform();
            repaint();
        });
        timer.start();
    }

    public void setHeartRate(int hr) {
        this.heartRate = hr;
    }

    public void setRhythmName(String rhythm) {
        this.rhythmName = rhythm;
    }

    private void advanceWaveform() {
        int speed = 3; // pixels advanced per tick
        int width = Math.max(1, getWidth());
        if (waveBuffer.length < width) return;

        // Calculate heart cycle period in samples based on heart rate
        int period = Math.max(20, (int) (1800.0 / Math.max(40, heartRate)));

        for (int s = 0; s < speed; s++) {
            sweepX = (sweepX + 1) % width;
            sampleCounter = (sampleCounter + 1) % period;

            float yOffset = 0.0f;
            // Synthesize P-Q-R-S-T wave
            if (sampleCounter >= 2 && sampleCounter <= 6) {
                // P wave
                yOffset = 0.18f * (float) Math.sin((sampleCounter - 2) * Math.PI / 4.0);
            } else if (sampleCounter == 9) {
                // Q wave
                yOffset = -0.15f;
            } else if (sampleCounter == 11) {
                // R spike
                yOffset = 1.0f;
                pulseBlink = true;
            } else if (sampleCounter == 13) {
                // S wave
                yOffset = -0.35f;
            } else if (sampleCounter >= 18 && sampleCounter <= 26) {
                // T wave
                yOffset = 0.28f * (float) Math.sin((sampleCounter - 18) * Math.PI / 8.0);
                pulseBlink = false;
            } else {
                pulseBlink = false;
            }

            waveBuffer[sweepX] = yOffset;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // 1. Draw ECG Grid paper (subtle dark green/slate telemetry grid)
        g2.setColor(new Color(0x132338));
        for (int x = 0; x < w; x += 15) {
            g2.drawLine(x, 0, x, h);
        }
        for (int y = 0; y < h; y += 15) {
            g2.drawLine(0, y, w, y);
        }

        // Accent major grid lines
        g2.setColor(new Color(0x1B334F));
        for (int x = 0; x < w; x += 75) {
            g2.drawLine(x, 0, x, h);
        }
        for (int y = 0; y < h; y += 75) {
            g2.drawLine(0, y, w, y);
        }

        // 2. Draw telemetry header info
        g2.setFont(MediCoreTheme.FONT_SMALL);
        g2.setColor(new Color(0x38BDF8)); // Cyan telemetry accent
        g2.drawString(rhythmName, 10, 16);

        // Heart pulse indicator
        g2.setColor(pulseBlink ? Color.RED : new Color(0x10B981));
        g2.fillOval(w - 75, 7, 10, 10);

        g2.setColor(Color.WHITE);
        g2.setFont(MediCoreTheme.FONT_MONO_BOLD);
        g2.drawString(heartRate + " BPM", w - 60, 16);

        // 3. Draw physiological green ECG waveform
        int midY = h / 2 + 8;
        float amp = h * 0.38f;

        Path2D.Float path = new Path2D.Float();
        boolean first = true;
        int blankGap = 16;

        for (int x = 0; x < w; x++) {
            // Leave a blank gap around sweep line to simulate oscilloscope phosphor sweep
            if (x >= sweepX && x < (sweepX + blankGap) % w) {
                first = true;
                continue;
            }

            float yVal = midY - (waveBuffer[x] * amp);
            if (first) {
                path.moveTo(x, yVal);
                first = false;
            } else {
                path.lineTo(x, yVal);
            }
        }

        // Phosphor glow effect
        g2.setColor(new Color(0x10B981, false)); // Emerald green
        g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(path);

        // Draw sweep head line
        g2.setColor(new Color(0x6EE7B7));
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawLine(sweepX, 22, sweepX, h - 4);

        g2.dispose();
    }

    public void stopAnimation() {
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }
    }
}
