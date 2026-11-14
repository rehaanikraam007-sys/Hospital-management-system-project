package com.medicore.dialogs;

import com.medicore.model.Patient;
import com.medicore.theme.MediCoreTheme;
import com.medicore.theme.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Inpatient ADT Bed Transfer Manifest & Authorization Slip.
 */
public class TransferSlipDialog extends JDialog {
    public TransferSlipDialog(Frame owner, Patient patient, String originBed, String destBed, String reason, String escort, String porter) {
        super(owner, "MediCore HMS — Inpatient Bed Transfer Manifest (HL7 ADT-A02)", true);
        setSize(540, 420);
        setLocationRelativeTo(owner);
        setResizable(false);

        JPanel content = new JPanel(new BorderLayout(0, 14));
        content.setBackground(MediCoreTheme.BG_WHITE);
        content.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("INPATIENT BED TRANSFER SLIP (ADT-A02)");
        title.setFont(MediCoreTheme.FONT_HEADER);
        title.setForeground(MediCoreTheme.PRIMARY_ACCENT);

        JLabel timestamp = new JLabel(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        timestamp.setFont(MediCoreTheme.FONT_SMALL);
        timestamp.setForeground(MediCoreTheme.TEXT_MUTED);

        header.add(title, BorderLayout.WEST);
        header.add(timestamp, BorderLayout.EAST);
        header.add(new JSeparator(), BorderLayout.SOUTH);
        content.add(header, BorderLayout.NORTH);

        // Body details
        JPanel body = new JPanel(new GridLayout(8, 2, 8, 6));
        body.setOpaque(false);

        addPair(body, "Patient Name / MRN:", patient.getFullName() + " (" + patient.getMrn() + ")");
        addPair(body, "Origin Unit & Bed:", originBed);
        addPair(body, "Destination Unit & Bed:", destBed);
        addPair(body, "Clinical Transfer Reason:", reason);
        addPair(body, "Escorting Registered Nurse:", escort);
        addPair(body, "Transport Porter Dispatch:", porter);
        addPair(body, "Telemetry Safeguard:", "Continuous Telemetry Pack Armed");
        addPair(body, "EVS Sanitization Dispatch:", "Auto-triggered for " + originBed);

        content.add(body, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        JButton closeBtn = UIUtils.createSecondaryButton("Close");
        closeBtn.addActionListener(e -> dispose());

        JButton printBtn = UIUtils.createPrimaryButton("🖨 Print Transfer Slip", "Enter");
        printBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Transfer Manifest sent to Charge Desk Laser Printer.", "Printed", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        });

        btnPanel.add(closeBtn);
        btnPanel.add(printBtn);
        content.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(content);
    }

    private void addPair(JPanel panel, String label, String val) {
        JLabel l = new JLabel(label);
        l.setFont(MediCoreTheme.FONT_BODY_BOLD);
        l.setForeground(MediCoreTheme.TEXT_SECONDARY);

        JLabel v = new JLabel(val);
        v.setFont(MediCoreTheme.FONT_BODY);
        v.setForeground(MediCoreTheme.TEXT_PRIMARY);

        panel.add(l);
        panel.add(v);
    }
}
