package com.medicore.dialogs;

import com.medicore.theme.MediCoreTheme;
import com.medicore.theme.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Rapid Response Team (RRT) & Code Blue Emergency Dispatch Dialog.
 */
public class RapidResponseDialog extends JDialog {
    public RapidResponseDialog(Frame owner, String defaultLocation) {
        super(owner, "🚨 EMERGENCY RAPID RESPONSE DISPATCH (STAT)", true);
        setSize(500, 360);
        setLocationRelativeTo(owner);
        setResizable(false);

        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setBackground(new Color(0xFEF2F2));
        content.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel title = new JLabel("RAPID RESPONSE TEAM (RRT) DISPATCH");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(MediCoreTheme.ESI1_TEXT);
        content.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(4, 2, 8, 12));
        form.setOpaque(false);

        JLabel locLbl = new JLabel("Emergency Bed Location:");
        locLbl.setFont(MediCoreTheme.FONT_BODY_BOLD);
        JTextField locField = new JTextField(defaultLocation != null ? defaultLocation : "Ward 3A - Bed 303-A");

        JLabel typeLbl = new JLabel("Alert Type:");
        typeLbl.setFont(MediCoreTheme.FONT_BODY_BOLD);
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{
                "Rapid Response Team (RRT) - Acute Deterioration",
                "Code Blue - Cardiac / Respiratory Arrest",
                "Code Stroke - Acute Neurologic Deficit",
                "Code Sepsis - Severe Hypotension & Lactate Elevation"
        });

        JLabel triggerLbl = new JLabel("Primary Clinical Trigger:");
        triggerLbl.setFont(MediCoreTheme.FONT_BODY_BOLD);
        JComboBox<String> triggerCombo = new JComboBox<>(new String[]{
                "Telemetry Tachycardia > 115 bpm / Unstable BP",
                "Acute Hypoxemia SpO2 < 90% on O2",
                "Altered Mental Status / GCS Drop",
                "Cardiac Arrhythmia / Ventricular Ectopy",
                "Severe Respiratory Distress"
        });

        JLabel leadLbl = new JLabel("Calling Clinician:");
        leadLbl.setFont(MediCoreTheme.FONT_BODY_BOLD);
        JTextField leadField = new JTextField("Dr. Eleanor Vance, MD (Attending)");
        leadField.setEditable(false);

        form.add(locLbl); form.add(locField);
        form.add(typeLbl); form.add(typeCombo);
        form.add(triggerLbl); form.add(triggerCombo);
        form.add(leadLbl); form.add(leadField);

        content.add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        JButton cancelBtn = UIUtils.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        JButton dispatchBtn = UIUtils.createDangerButton("⚡ BROADCAST STAT DISPATCH");
        dispatchBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                    "RAPID RESPONSE TEAM ACTIVATED!\n" +
                            "Pagers & Vocera badges dispatched for: " + typeCombo.getSelectedItem() + "\n" +
                            "Location: " + locField.getText() + "\n" +
                            "ICU Fellow, Respiratory Therapist, and Code Cart en route (ETA < 90s).",
                    "RRT Dispatched", JOptionPane.WARNING_MESSAGE);
            dispose();
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(dispatchBtn);
        content.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(content);
    }
}
