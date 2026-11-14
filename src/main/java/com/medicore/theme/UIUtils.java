package com.medicore.theme;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Modern clinical UI utility helper for badges, cards, buttons, and visual styling.
 */
public class UIUtils {

    public static JPanel createCardPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(MediCoreTheme.BG_WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.setColor(MediCoreTheme.BORDER_LIGHT);
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 10, 10));
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));
        return panel;
    }

    public static JButton createPrimaryButton(String text, String shortcutHint) {
        String label = shortcutHint != null && !shortcutHint.isEmpty() ? text + "  [" + shortcutHint + "]" : text;
        JButton btn = new JButton(label);
        btn.setFont(MediCoreTheme.FONT_BODY_BOLD);
        btn.setBackground(MediCoreTheme.PRIMARY_ACCENT);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new CompoundBorder(new LineBorder(MediCoreTheme.PRIMARY_DARK, 1, true), new EmptyBorder(8, 16, 8, 16)));
        return btn;
    }

    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(MediCoreTheme.FONT_BODY);
        btn.setBackground(MediCoreTheme.BG_WHITE);
        btn.setForeground(MediCoreTheme.TEXT_PRIMARY);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new CompoundBorder(new LineBorder(MediCoreTheme.BORDER_DARK, 1, true), new EmptyBorder(7, 14, 7, 14)));
        return btn;
    }

    public static JButton createDangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(MediCoreTheme.FONT_BODY_BOLD);
        btn.setBackground(MediCoreTheme.ESI1_TEXT);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new CompoundBorder(new LineBorder(MediCoreTheme.ESI1_TEXT.darker(), 1, true), new EmptyBorder(8, 16, 8, 16)));
        return btn;
    }

    public static JLabel createAcuityBadge(int acuityLevel) {
        String label;
        Color fg, bg, border;

        switch (acuityLevel) {
            case 1:
                label = "ESI-1 RESUS";
                fg = MediCoreTheme.ESI1_TEXT;
                bg = MediCoreTheme.ESI1_BG;
                border = MediCoreTheme.ESI1_BORDER;
                break;
            case 2:
                label = "ESI-2 EMERGENT";
                fg = MediCoreTheme.ESI2_TEXT;
                bg = MediCoreTheme.ESI2_BG;
                border = MediCoreTheme.ESI2_BORDER;
                break;
            case 3:
                label = "ESI-3 URGENT";
                fg = MediCoreTheme.ESI3_TEXT;
                bg = MediCoreTheme.ESI3_BG;
                border = MediCoreTheme.ESI3_BORDER;
                break;
            case 4:
                label = "ESI-4 STABLE";
                fg = MediCoreTheme.ESI4_TEXT;
                bg = MediCoreTheme.ESI4_BG;
                border = MediCoreTheme.ESI4_BORDER;
                break;
            default:
                label = "ESI-5 ROUTINE";
                fg = MediCoreTheme.ESI5_TEXT;
                bg = MediCoreTheme.ESI5_BG;
                border = MediCoreTheme.ESI5_BORDER;
                break;
        }

        return createPillBadge(label, fg, bg, border);
    }

    public static JLabel createPillBadge(String text, Color fg, Color bg, Color border) {
        JLabel badge = new JLabel(text, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.setColor(border);
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 12, 12));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setOpaque(false);
        badge.setForeground(fg);
        badge.setFont(MediCoreTheme.FONT_SMALL);
        badge.setBorder(new EmptyBorder(3, 8, 3, 8));
        return badge;
    }

    public static JPanel createSectionHeader(String title, String subtitle) {
        JPanel p = new JPanel(new BorderLayout(0, 2));
        p.setOpaque(false);
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(MediCoreTheme.FONT_HEADER);
        titleLbl.setForeground(MediCoreTheme.TEXT_PRIMARY);
        p.add(titleLbl, BorderLayout.NORTH);

        if (subtitle != null && !subtitle.isEmpty()) {
            JLabel subLbl = new JLabel(subtitle);
            subLbl.setFont(MediCoreTheme.FONT_SMALL);
            subLbl.setForeground(MediCoreTheme.TEXT_SECONDARY);
            p.add(subLbl, BorderLayout.CENTER);
        }
        p.setBorder(new EmptyBorder(0, 0, 8, 0));
        return p;
    }

    public static Icon createAvatarIcon(String initials, Color bgColor) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgColor);
                g2.fillOval(x, y, 28, 28);
                g2.setColor(Color.WHITE);
                g2.setFont(MediCoreTheme.FONT_SUBHEADER);
                FontMetrics fm = g2.getFontMetrics();
                int tx = x + (28 - fm.stringWidth(initials)) / 2;
                int ty = y + ((28 - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(initials, tx, ty);
                g2.dispose();
            }

            @Override
            public int getIconWidth() { return 28; }
            @Override
            public int getIconHeight() { return 28; }
        };
    }
}
