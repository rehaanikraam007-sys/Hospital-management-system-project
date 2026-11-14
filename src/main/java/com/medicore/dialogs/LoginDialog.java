package com.medicore.dialogs;

import com.medicore.model.HospitalDataStore;
import com.medicore.model.StaffUser;
import com.medicore.theme.MediCoreTheme;
import com.medicore.theme.UIUtils;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Screen 1: Secure Biometric & Badge Credentialing Login Dialog.
 */
public class LoginDialog extends JDialog {
    private final JTextField badgeField = new JTextField("MD-84920");
    private final JPasswordField passwordField = new JPasswordField("ClinicalAuth123!");
    private final JComboBox<String> unitCombo;
    private final JComboBox<String> shiftCombo;
    private boolean authenticated = false;
    private boolean showPassword = false;

    public LoginDialog(Frame owner) {
        super(owner, "MediCore HMS — Terminal Security Authentication", true);
        setSize(600, 520);
        setResizable(false);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(MediCoreTheme.BG_WHITE);
        mainPanel.setBorder(new EmptyBorder(24, 28, 20, 28));

        // 1. Header with Hospital Brand, TLS status & Node ID
        JPanel headerPanel = new JPanel(new BorderLayout(0, 4));
        headerPanel.setOpaque(false);

        JPanel brandRow = new JPanel(new BorderLayout());
        brandRow.setOpaque(false);

        JLabel logoLabel = new JLabel("✚ MediCore HMS");
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        logoLabel.setForeground(MediCoreTheme.PRIMARY_ACCENT);

        JPanel badgeChips = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        badgeChips.setOpaque(false);
        badgeChips.add(UIUtils.createPillBadge("TLS 1.3 SECURE", MediCoreTheme.ESI4_TEXT, MediCoreTheme.ESI4_BG, MediCoreTheme.ESI4_BORDER));
        badgeChips.add(UIUtils.createPillBadge("NODE-PHX-09A", MediCoreTheme.TEXT_SECONDARY, MediCoreTheme.BG_PANEL, MediCoreTheme.BORDER_DARK));

        brandRow.add(logoLabel, BorderLayout.WEST);
        brandRow.add(badgeChips, BorderLayout.EAST);

        JLabel subtitleLabel = new JLabel("Enterprise Clinical Workstation • Inpatient Care & Telemetry Gateway");
        subtitleLabel.setFont(MediCoreTheme.FONT_SMALL);
        subtitleLabel.setForeground(MediCoreTheme.TEXT_SECONDARY);

        headerPanel.add(brandRow, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.CENTER);
        headerPanel.add(new JSeparator(), BorderLayout.SOUTH);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // 2. Center Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(16, 0, 12, 0));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(6, 4, 6, 4);

        // Staff ID / Badge Field
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        JLabel badgeLbl = new JLabel("Medical Staff ID / Clinician Badge Number:");
        badgeLbl.setFont(MediCoreTheme.FONT_BODY_BOLD);
        badgeLbl.setForeground(MediCoreTheme.TEXT_PRIMARY);
        formPanel.add(badgeLbl, c);

        c.gridy = 1; c.gridwidth = 1; c.weightx = 0.75;
        badgeField.setFont(MediCoreTheme.FONT_BODY);
        badgeField.setPreferredSize(new Dimension(200, 36));
        formPanel.add(badgeField, c);

        c.gridx = 1; c.weightx = 0.25;
        JButton nfcBtn = new JButton("📶 Scan NFC Badge");
        nfcBtn.setFont(MediCoreTheme.FONT_SMALL);
        nfcBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        nfcBtn.setPreferredSize(new Dimension(140, 36));
        nfcBtn.addActionListener(e -> {
            badgeField.setText("MD-84920");
            passwordField.setText("ClinicalAuth123!");
            JOptionPane.showMessageDialog(this,
                    "PIV/NFC Badge Authenticated:\nClinician: Dr. Eleanor Vance, MD\nDepartment: Cardiology / Attending\nCertificate: SHA-256 Valid",
                    "NFC Badge Scanner", JOptionPane.INFORMATION_MESSAGE);
        });
        formPanel.add(nfcBtn, c);

        // Masked Password Field
        c.gridx = 0; c.gridy = 2; c.gridwidth = 2; c.weightx = 1.0;
        JLabel passLbl = new JLabel("Masked Security Passphrase:");
        passLbl.setFont(MediCoreTheme.FONT_BODY_BOLD);
        formPanel.add(passLbl, c);

        c.gridy = 3; c.gridwidth = 1; c.weightx = 0.85;
        passwordField.setFont(MediCoreTheme.FONT_BODY);
        passwordField.setPreferredSize(new Dimension(200, 36));
        formPanel.add(passwordField, c);

        c.gridx = 1; c.weightx = 0.15;
        JButton eyeBtn = new JButton("👁 Show");
        eyeBtn.setFont(MediCoreTheme.FONT_SMALL);
        eyeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        eyeBtn.setPreferredSize(new Dimension(80, 36));
        eyeBtn.addActionListener(e -> {
            showPassword = !showPassword;
            passwordField.setEchoChar(showPassword ? (char) 0 : '•');
            eyeBtn.setText(showPassword ? "Hide" : "👁 Show");
        });
        formPanel.add(eyeBtn, c);

        // Dual Dropdown Row: Unit & Shift
        c.gridx = 0; c.gridy = 4; c.gridwidth = 1; c.weightx = 0.5;
        JLabel unitLbl = new JLabel("Assigned Clinical Unit:");
        unitLbl.setFont(MediCoreTheme.FONT_BODY_BOLD);
        formPanel.add(unitLbl, c);

        c.gridx = 1; c.weightx = 0.5;
        JLabel shiftLbl = new JLabel("Active Duty Shift:");
        shiftLbl.setFont(MediCoreTheme.FONT_BODY_BOLD);
        formPanel.add(shiftLbl, c);

        c.gridx = 0; c.gridy = 5;
        unitCombo = new JComboBox<>(new String[]{"Cardiology / Ward 3A", "ICU / Critical Care", "Emergency Department", "Surgery & PACU", "General Inpatient"});
        unitCombo.setPreferredSize(new Dimension(160, 34));
        formPanel.add(unitCombo, c);

        c.gridx = 1;
        shiftCombo = new JComboBox<>(new String[]{"Morning 07:00–15:30", "Evening 15:00–23:30", "Night 23:00–07:30"});
        shiftCombo.setPreferredSize(new Dimension(160, 34));
        formPanel.add(shiftCombo, c);

        // Smart Card Status Chip
        c.gridx = 0; c.gridy = 6; c.gridwidth = 2;
        JPanel smartCardRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 8));
        smartCardRow.setOpaque(false);
        JLabel chip = UIUtils.createPillBadge("💳 PIV/CAC Slot #0: Token Verified (NIST FIPS 140-3)", MediCoreTheme.ESI4_TEXT, MediCoreTheme.ESI4_BG, MediCoreTheme.ESI4_BORDER);
        smartCardRow.add(chip);
        formPanel.add(smartCardRow, c);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // 3. Bottom Action Buttons & Compliance Footer
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setOpaque(false);

        // Buttons row
        JPanel btnRow = new JPanel(new GridLayout(1, 2, 12, 0));
        btnRow.setOpaque(false);

        JButton bypassBtn = new JButton("⚠ Emergency Bypass: STAT Mode");
        bypassBtn.setFont(MediCoreTheme.FONT_BODY_BOLD);
        bypassBtn.setBackground(new Color(0xFEF2F2));
        bypassBtn.setForeground(MediCoreTheme.ESI1_TEXT);
        bypassBtn.setFocusPainted(false);
        bypassBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        bypassBtn.setBorder(new CompoundBorder(new LineBorder(MediCoreTheme.ESI1_BORDER, 1, true), new EmptyBorder(10, 10, 10, 10)));
        bypassBtn.addActionListener(e -> executeBypassLogin());

        JButton loginBtn = UIUtils.createPrimaryButton("Log In to Workstation", "Ctrl+Enter");
        loginBtn.setPreferredSize(new Dimension(180, 42));
        loginBtn.addActionListener(e -> executeStandardLogin());

        btnRow.add(bypassBtn);
        btnRow.add(loginBtn);
        bottomPanel.add(btnRow);

        bottomPanel.add(Box.createVerticalStrut(12));

        // Footer notice
        JPanel footerInfo = new JPanel(new BorderLayout());
        footerInfo.setOpaque(false);
        JLabel complianceLbl = new JLabel("HIPAA / HITECH Compliance Enforced • Biometric Audit Logged");
        complianceLbl.setFont(MediCoreTheme.FONT_SMALL);
        complianceLbl.setForeground(MediCoreTheme.TEXT_MUTED);

        JLabel pingLbl = new JLabel("HL7 v2.5 / FHIR R4 Engine: ACTIVE (12ms)");
        pingLbl.setFont(MediCoreTheme.FONT_SMALL);
        pingLbl.setForeground(MediCoreTheme.ESI4_TEXT);

        footerInfo.add(complianceLbl, BorderLayout.WEST);
        footerInfo.add(pingLbl, BorderLayout.EAST);
        bottomPanel.add(footerInfo);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Keyboard accelerator: Ctrl+Enter or Enter in fields
        KeyAdapter enterKey = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    executeStandardLogin();
                }
            }
        };
        badgeField.addKeyListener(enterKey);
        passwordField.addKeyListener(enterKey);

        setContentPane(mainPanel);
    }

    private void executeStandardLogin() {
        String badge = badgeField.getText().trim();
        if (badge.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your Clinician Staff ID or scan NFC badge.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        StaffUser staff = new StaffUser(badge, "Dr. Eleanor Vance, MD", "Attending Physician", "Cardiology",
                (String) unitCombo.getSelectedItem(), (String) shiftCombo.getSelectedItem());
        HospitalDataStore.getInstance().setCurrentStaff(staff);
        authenticated = true;
        dispose();
    }

    private void executeBypassLogin() {
        StaffUser bypassStaff = new StaffUser("STAT-EMERGENCY", "Code Triage Attending (STAT)", "Trauma / Code Lead", "Emergency",
                "Emergency / Resus", "Continuous STAT");
        HospitalDataStore.getInstance().setCurrentStaff(bypassStaff);
        authenticated = true;
        dispose();
    }

    public boolean isAuthenticated() {
        return authenticated;
    }
}
