package com.medicore;

import com.medicore.dialogs.LoginDialog;
import com.medicore.theme.MediCoreTheme;
import com.medicore.views.MainFrame;

import javax.swing.*;

/**
 * MediCore HMS v3.4 Application Entry Point.
 */
public class Main {
    public static void main(String[] args) {
        // Configure Windows high-DPI scaling
        System.setProperty("sun.java2d.uiScale", "1.0");
        System.setProperty("flatlaf.useWindowDecorations", "false");

        SwingUtilities.invokeLater(() -> {
            // Apply MediCore FlatLaf clinical design theme
            MediCoreTheme.setup();

            boolean autoLogin = false;
            for (String arg : args) {
                if ("--auto-login".equalsIgnoreCase(arg) || "--bypass-login".equalsIgnoreCase(arg)) {
                    autoLogin = true;
                    break;
                }
            }

            if (autoLogin) {
                MainFrame frame = new MainFrame();
                frame.setVisible(true);
            } else {
                LoginDialog loginDialog = new LoginDialog(null);
                loginDialog.setVisible(true);

                if (loginDialog.isAuthenticated()) {
                    MainFrame frame = new MainFrame();
                    frame.setVisible(true);
                } else {
                    System.exit(0);
                }
            }
        });
    }
}
