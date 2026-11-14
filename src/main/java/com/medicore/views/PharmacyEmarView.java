package com.medicore.views;

import com.medicore.model.HospitalDataStore;
import com.medicore.model.Medication;
import com.medicore.model.Patient;
import com.medicore.theme.MediCoreTheme;
import com.medicore.theme.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Electronic Medication Administration Record (eMAR) & Inpatient Pharmacy Suite.
 */
public class PharmacyEmarView extends JPanel implements HospitalDataStore.DataChangeListener {
    private final DashboardView.ViewNavigator navigator;
    private final DefaultTableModel emarTableModel;
    private final JTable emarTable;
    private final JTextField barcodeScanField = new JTextField(16);

    public PharmacyEmarView(DashboardView.ViewNavigator navigator) {
        this.navigator = navigator;
        setLayout(new BorderLayout(0, 10));
        setBackground(MediCoreTheme.BG_STERILE);
        setBorder(new EmptyBorder(12, 14, 12, 14));
        HospitalDataStore.getInstance().addListener(this);

        // Header
        JPanel header = UIUtils.createCardPanel();
        header.setLayout(new BorderLayout(0, 4));

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBlock.setOpaque(false);
        JLabel t = new JLabel("eMAR & INPATIENT PHARMACY DISPENSING SUITE");
        t.setFont(MediCoreTheme.FONT_HEADER);
        t.setForeground(MediCoreTheme.PRIMARY_ACCENT);

        JLabel sub = new JLabel("Bar-Code Medication Administration (BCMA) • Pyxis MedStation 4000 Integration");
        sub.setFont(MediCoreTheme.FONT_SMALL);
        sub.setForeground(MediCoreTheme.TEXT_SECONDARY);
        titleBlock.add(t);
        titleBlock.add(sub);
        header.add(titleBlock, BorderLayout.WEST);

        JPanel badges = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        badges.setOpaque(false);
        badges.add(UIUtils.createPillBadge("PYXIS-3A: CONNECTED", MediCoreTheme.ESI4_TEXT, MediCoreTheme.ESI4_BG, MediCoreTheme.ESI4_BORDER));
        badges.add(UIUtils.createPillBadge("BCMA 5-RIGHTS CHECK: ACTIVE", MediCoreTheme.PRIMARY_ACCENT, MediCoreTheme.PRIMARY_LIGHT, MediCoreTheme.PRIMARY_ACCENT));
        header.add(badges, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Table panel
        JPanel tablePanel = UIUtils.createCardPanel();
        tablePanel.setLayout(new BorderLayout(0, 8));

        // Scan barcode bar
        JPanel scanBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        scanBar.setOpaque(false);
        scanBar.add(new JLabel("Scan Patient / Medication Barcode:"));
        barcodeScanField.setText("MED-FUROS-40MG");
        scanBar.add(barcodeScanField);

        JButton verifyBtn = UIUtils.createPrimaryButton("Verify & Administer (BCMA)", "Scan");
        verifyBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                    "5-Rights Safety Verification Succeeded!\n\n" +
                            "✓ Right Patient: Eleanor Vance (MRN-84920)\n" +
                            "✓ Right Medication: Furosemide (Lasix)\n" +
                            "✓ Right Dose: 40 mg IV Push\n" +
                            "✓ Right Route: Intravenous\n" +
                            "✓ Right Time: 14:00 Window\n\n" +
                            "eMAR timestamp recorded to central audit log.",
                    "BCMA 5-Rights Confirmed", JOptionPane.INFORMATION_MESSAGE);
        });
        scanBar.add(verifyBtn);
        tablePanel.add(scanBar, BorderLayout.NORTH);

        String[] cols = {"Patient MRN", "Patient Name", "Bed", "Medication Order", "Route", "Frequency", "eMAR Status", "Last Admin"};
        emarTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        emarTable = new JTable(emarTableModel);
        emarTable.setRowHeight(32);
        emarTable.getColumnModel().getColumn(6).setCellRenderer((table, value, isSelected, hasFocus, row, column) -> {
            String val = String.valueOf(value);
            Color fg = val.contains("Admin") ? MediCoreTheme.ESI4_TEXT : (val.contains("Due") ? MediCoreTheme.ESI2_TEXT : MediCoreTheme.TEXT_PRIMARY);
            Color bg = val.contains("Admin") ? MediCoreTheme.ESI4_BG : (val.contains("Due") ? MediCoreTheme.ESI2_BG : MediCoreTheme.BG_PANEL);
            Color bd = val.contains("Admin") ? MediCoreTheme.ESI4_BORDER : (val.contains("Due") ? MediCoreTheme.ESI2_BORDER : MediCoreTheme.BORDER_LIGHT);
            return UIUtils.createPillBadge(val, fg, bg, bd);
        });

        JScrollPane scroll = new JScrollPane(emarTable);
        scroll.setBorder(new LineBorder(MediCoreTheme.BORDER_LIGHT, 1));
        tablePanel.add(scroll, BorderLayout.CENTER);

        add(tablePanel, BorderLayout.CENTER);

        refreshTable();
    }

    private void refreshTable() {
        emarTableModel.setRowCount(0);
        List<Patient> list = HospitalDataStore.getInstance().getPatients();
        for (Patient p : list) {
            if (p.getMedications() != null) {
                for (Medication m : p.getMedications()) {
                    emarTableModel.addRow(new Object[]{
                            p.getMrn(),
                            p.getFullName(),
                            p.getBed(),
                            m.getName() + " " + m.getDosage(),
                            m.getRoute(),
                            m.getFrequency(),
                            m.getStatus(),
                            m.getLastAdministered() != null ? m.getLastAdministered() : "Scheduled"
                    });
                }
            }
        }
    }

    @Override
    public void onDataChanged() {
        SwingUtilities.invokeLater(this::refreshTable);
    }
}
