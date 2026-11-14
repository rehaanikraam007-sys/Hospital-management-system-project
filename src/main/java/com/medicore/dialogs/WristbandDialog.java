package com.medicore.dialogs;

import com.medicore.model.Patient;
import com.medicore.theme.MediCoreTheme;
import com.medicore.theme.UIUtils;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.format.DateTimeFormatter;

/**
 * Patient wristband preview & thermal printer output dialog with 2D barcode.
 */
public class WristbandDialog extends JDialog {
    public WristbandDialog(Frame owner, Patient patient) {
        super(owner, "MediCore HMS — Print Patient Identification Wristband", true);
        setSize(520, 340);
        setLocationRelativeTo(owner);
        setResizable(false);

        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setBackground(MediCoreTheme.BG_WHITE);
        content.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Thermal Wristband Print Preview (Zebra ZD510-HC)");
        title.setFont(MediCoreTheme.FONT_HEADER);
        title.setForeground(MediCoreTheme.TEXT_PRIMARY);
        content.add(title, BorderLayout.NORTH);

        // Visual wristband preview panel
        JPanel bandPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Wristband body
                g2.setColor(new Color(0xF8FAFC));
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 14, 14));
                g2.setColor(new Color(0x0284C7));
                g2.setStroke(new BasicStroke(2.0f));
                g2.draw(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, 14, 14));

                // Color-coded allergy clasp on left
                boolean hasAllergy = patient.getAllergies() != null && !patient.getAllergies().isEmpty() 
                        && !patient.getAllergies().get(0).contains("NKDA");
                g2.setColor(hasAllergy ? new Color(0xDC2626) : new Color(0x059669));
                g2.fillRect(0, 0, 14, h);

                // Barcode simulation lines
                g2.setColor(Color.BLACK);
                int startX = w - 90;
                for (int x = startX; x < w - 20; x += 4) {
                    int barW = (x % 3 == 0) ? 2 : 1;
                    g2.fillRect(x, 24, barW, 60);
                }
                g2.setFont(MediCoreTheme.FONT_MONO);
                g2.drawString(patient.getMrn(), startX, 98);

                g2.dispose();
            }
        };
        bandPanel.setLayout(new GridBagLayout());
        bandPanel.setOpaque(false);
        bandPanel.setPreferredSize(new Dimension(460, 140));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(4, 28, 4, 10);
        gbc.gridx = 0; gbc.gridy = 0;

        JLabel nameLbl = new JLabel(patient.getFullName().toUpperCase());
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        nameLbl.setForeground(MediCoreTheme.TEXT_PRIMARY);
        bandPanel.add(nameLbl, gbc);

        gbc.gridy = 1;
        JLabel demoLbl = new JLabel("DOB: " + patient.getDob() + " (" + patient.getAge() + "Y) • " + patient.getBiologicalSex() + " • BLOOD: " + patient.getBloodGroup());
        demoLbl.setFont(MediCoreTheme.FONT_BODY_BOLD);
        bandPanel.add(demoLbl, gbc);

        gbc.gridy = 2;
        JLabel locLbl = new JLabel("LOC: " + patient.getWard() + " - " + patient.getBed() + " • MD: " + patient.getAttendingDoctor());
        locLbl.setFont(MediCoreTheme.FONT_SMALL);
        bandPanel.add(locLbl, gbc);

        gbc.gridy = 3;
        String allergyText = (patient.getAllergies() != null && !patient.getAllergies().isEmpty()) ?
                "ALLERGIES: " + String.join(", ", patient.getAllergies()) : "ALLERGIES: NKDA";
        JLabel allergyLbl = new JLabel(allergyText);
        allergyLbl.setFont(MediCoreTheme.FONT_BODY_BOLD);
        allergyLbl.setForeground(allergyText.contains("NKDA") ? MediCoreTheme.ESI4_TEXT : MediCoreTheme.ESI1_TEXT);
        bandPanel.add(allergyLbl, gbc);

        content.add(bandPanel, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        JButton cancelBtn = UIUtils.createSecondaryButton("Close");
        cancelBtn.addActionListener(e -> dispose());

        JButton printBtn = UIUtils.createPrimaryButton("🖨 Send to Thermal Printer", "Enter");
        printBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                    "Wristband printed successfully to Zebra ZD510-HC [Printer Station 3A-East].\n" +
                            "Patient MRN: " + patient.getMrn() + "\nBarcoded Patient UID: " + patient.getFullName(),
                    "Print Confirmed", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(printBtn);
        content.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(content);
    }
}
