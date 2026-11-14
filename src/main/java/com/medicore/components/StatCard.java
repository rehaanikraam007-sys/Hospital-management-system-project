package com.medicore.components;

import com.medicore.theme.MediCoreTheme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * KPI metric card for the Executive Census Ribbon.
 */
public class StatCard extends JPanel {
    private final JLabel titleLabel;
    private final JLabel valueLabel;
    private final JLabel subLabel;
    private final JProgressBar progressBar;

    public StatCard(String title, String value, String subtext, Color accentColor, boolean showProgress, int progressValue) {
        setLayout(new BorderLayout(8, 6));
        setOpaque(false);
        setBorder(new EmptyBorder(10, 14, 10, 14));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        titleLabel = new JLabel(title);
        titleLabel.setFont(MediCoreTheme.FONT_SMALL);
        titleLabel.setForeground(MediCoreTheme.TEXT_SECONDARY);

        valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valueLabel.setForeground(accentColor != null ? accentColor : MediCoreTheme.TEXT_PRIMARY);

        subLabel = new JLabel(subtext);
        subLabel.setFont(MediCoreTheme.FONT_SMALL);
        subLabel.setForeground(MediCoreTheme.TEXT_MUTED);

        content.add(titleLabel);
        content.add(Box.createVerticalStrut(4));
        content.add(valueLabel);
        content.add(Box.createVerticalStrut(2));
        content.add(subLabel);

        if (showProgress) {
            progressBar = new JProgressBar(0, 100);
            progressBar.setValue(progressValue);
            progressBar.setForeground(accentColor != null ? accentColor : MediCoreTheme.PRIMARY_ACCENT);
            progressBar.setBackground(MediCoreTheme.BORDER_LIGHT);
            progressBar.setPreferredSize(new Dimension(100, 6));
            progressBar.setMaximumSize(new Dimension(Short.MAX_VALUE, 6));
            content.add(Box.createVerticalStrut(6));
            content.add(progressBar);
        } else {
            progressBar = null;
        }

        add(content, BorderLayout.CENTER);
    }

    public void updateValues(String value, String subtext, int progressValue) {
        valueLabel.setText(value);
        subLabel.setText(subtext);
        if (progressBar != null) {
            progressBar.setValue(progressValue);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(MediCoreTheme.BG_WHITE);
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
        g2.setColor(MediCoreTheme.BORDER_LIGHT);
        g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 10, 10));
        g2.dispose();
        super.paintComponent(g);
    }
}
