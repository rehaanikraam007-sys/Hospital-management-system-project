package com.medicore.theme;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;

/**
 * MediCore clinical design language, color palette, typography and FlatLaf configuration.
 */
public class MediCoreTheme {

    // Primary Accents
    public static final Color PRIMARY_ACCENT = new Color(0x0284C7); // Deep Medical Sky Blue
    public static final Color PRIMARY_DARK = new Color(0x0369A1);
    public static final Color PRIMARY_LIGHT = new Color(0xE0F2FE);

    // Chrome & Backgrounds
    public static final Color BG_WHITE = new Color(0xFFFFFF);
    public static final Color BG_STERILE = new Color(0xF8FAFC); // Cool Sterile Grey
    public static final Color BG_PANEL = new Color(0xF1F5F9);
    public static final Color BORDER_LIGHT = new Color(0xE2E8F0);
    public static final Color BORDER_DARK = new Color(0xCBD5E1);

    // Text hierarchy
    public static final Color TEXT_PRIMARY = new Color(0x0F172A);
    public static final Color TEXT_SECONDARY = new Color(0x475569);
    public static final Color TEXT_MUTED = new Color(0x94A3B8);

    // Triage & Acuity Indicators
    // ESI-1 Resus / Code Red
    public static final Color ESI1_TEXT = new Color(0xDC2626);
    public static final Color ESI1_BG = new Color(0xFEF2F2);
    public static final Color ESI1_BORDER = new Color(0xFCA5A5);

    // ESI-2 Emergent / High Risk
    public static final Color ESI2_TEXT = new Color(0xEA580C);
    public static final Color ESI2_BG = new Color(0xFFF7ED);
    public static final Color ESI2_BORDER = new Color(0xFDBA74);

    // ESI-3 Urgent / Caution
    public static final Color ESI3_TEXT = new Color(0xD97706);
    public static final Color ESI3_BG = new Color(0xFEF3C7);
    public static final Color ESI3_BORDER = new Color(0xFCD34D);

    // ESI-4 / Stable / Clean
    public static final Color ESI4_TEXT = new Color(0x059669);
    public static final Color ESI4_BG = new Color(0xECFDF5);
    public static final Color ESI4_BORDER = new Color(0x6EE7B7);

    // ESI-5 / Routine / Info
    public static final Color ESI5_TEXT = new Color(0x2563EB);
    public static final Color ESI5_BG = new Color(0xEFF6FF);
    public static final Color ESI5_BORDER = new Color(0x93C5FD);

    // Fonts
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_SUBHEADER = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO = new Font("Consolas", Font.PLAIN, 12);
    public static final Font FONT_MONO_BOLD = new Font("Consolas", Font.BOLD, 12);

    /**
     * Initializes the FlatLaf theme with custom clinical UI defaults.
     */
    public static void setup() {
        try {
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("ProgressBar.arc", 6);
            UIManager.put("TextComponent.arc", 6);
            UIManager.put("ScrollBar.thumbArc", 6);
            UIManager.put("ScrollBar.width", 10);
            UIManager.put("Table.rowHeight", 34);
            UIManager.put("Table.showHorizontalLines", true);
            UIManager.put("Table.showVerticalLines", false);
            UIManager.put("Table.gridColor", BORDER_LIGHT);
            UIManager.put("TableHeader.background", BG_STERILE);
            UIManager.put("TableHeader.font", FONT_SUBHEADER);
            UIManager.put("TabbedPane.selectedBackground", BG_WHITE);
            UIManager.put("TabbedPane.tabInsets", new Insets(8, 16, 8, 16));
            UIManager.put("Component.focusWidth", 1);
            UIManager.put("Component.innerFocusWidth", 0);

            FlatLightLaf.setup();
        } catch (Exception ex) {
            System.err.println("Could not initialize FlatLaf look and feel: " + ex.getMessage());
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
        }
    }
}
