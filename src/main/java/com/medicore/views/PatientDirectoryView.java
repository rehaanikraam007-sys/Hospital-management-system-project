package com.medicore.views;

import com.medicore.model.HospitalDataStore;
import com.medicore.model.Patient;
import com.medicore.theme.MediCoreTheme;
import com.medicore.theme.UIUtils;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Screen 4: Comprehensive HL7 Patient Directory, Census Query, and Medical Records Ledger.
 */
public class PatientDirectoryView extends JPanel implements HospitalDataStore.DataChangeListener {
    private final DashboardView.ViewNavigator navigator;
    private final JTextField searchField = new JTextField(18);
    private final JComboBox<String> wardFilter = new JComboBox<>(new String[]{"All Units", "Ward 3A", "ICU", "PACU", "Cardiology", "Emergency"});
    private final JComboBox<String> statusFilter = new JComboBox<>(new String[]{"All Statuses", "Inpatient", "ED Queue", "Observation", "Discharged"});
    private final JComboBox<String> acuityFilter = new JComboBox<>(new String[]{"All Acuity Levels", "ESI-1 (Resus)", "ESI-2 (Emergent)", "ESI-3 (Urgent)", "ESI-4 (Stable)", "ESI-5 (Routine)"});

    private final JButton pillAll;
    private final JButton pillAlerts;
    private final JButton pillLabs;
    private final JButton pillAssigned;

    private String activeQuickFilter = "ALL";

    private final JTable dataTable;
    private final DefaultTableModel tableModel;
    private final JLabel recordCountLbl = new JLabel();

    private List<Patient> displayedPatients = new ArrayList<>();

    public PatientDirectoryView(DashboardView.ViewNavigator navigator) {
        this.navigator = navigator;
        setLayout(new BorderLayout(0, 10));
        setBackground(MediCoreTheme.BG_STERILE);
        setBorder(new EmptyBorder(12, 14, 12, 14));

        HospitalDataStore.getInstance().addListener(this);

        // Top Filter Bar Panel
        JPanel topContainer = UIUtils.createCardPanel();
        topContainer.setLayout(new BorderLayout(0, 8));

        // Upper row: Search & dropdown filters
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        filterRow.setOpaque(false);

        JLabel searchLbl = new JLabel("🔍 Search Patient / MRN / SSN:");
        searchLbl.setFont(MediCoreTheme.FONT_BODY_BOLD);
        searchField.setPreferredSize(new Dimension(200, 32));

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilters(); }
            public void removeUpdate(DocumentEvent e) { applyFilters(); }
            public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });

        filterRow.add(searchLbl);
        filterRow.add(searchField);
        filterRow.add(Box.createHorizontalStrut(8));

        filterRow.add(new JLabel("Unit:"));
        wardFilter.setPreferredSize(new Dimension(110, 32));
        wardFilter.addActionListener(e -> applyFilters());
        filterRow.add(wardFilter);

        filterRow.add(new JLabel("Status:"));
        statusFilter.setPreferredSize(new Dimension(110, 32));
        statusFilter.addActionListener(e -> applyFilters());
        filterRow.add(statusFilter);

        filterRow.add(new JLabel("Acuity:"));
        acuityFilter.setPreferredSize(new Dimension(140, 32));
        acuityFilter.addActionListener(e -> applyFilters());
        filterRow.add(acuityFilter);

        topContainer.add(filterRow, BorderLayout.NORTH);

        // Lower row: Quick View Pills
        JPanel pillRow = new JPanel(new BorderLayout());
        pillRow.setOpaque(false);

        JPanel pillButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pillButtons.setOpaque(false);

        pillAll = createFilterPill("All Inpatients (248)", "ALL", true);
        pillAlerts = createFilterPill("Code Alerts (3)", "ALERTS", false);
        pillLabs = createFilterPill("Stat Labs Pending (7)", "LABS", false);
        pillAssigned = createFilterPill("My Assigned (12)", "ASSIGNED", false);

        pillButtons.add(pillAll);
        pillButtons.add(pillAlerts);
        pillButtons.add(pillLabs);
        pillButtons.add(pillAssigned);
        pillRow.add(pillButtons, BorderLayout.WEST);

        // Export Actions on the right
        JPanel exportGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        exportGroup.setOpaque(false);

        JButton csvBtn = UIUtils.createSecondaryButton("📥 Export CSV");
        csvBtn.addActionListener(e -> exportCsv());

        JButton printBtn = UIUtils.createSecondaryButton("🖨 Print Census Grid");
        printBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "HL7 Inpatient Census Ledger rendered to Print Spooler.", "Print Spooled", JOptionPane.INFORMATION_MESSAGE));

        exportGroup.add(csvBtn);
        exportGroup.add(printBtn);
        pillRow.add(exportGroup, BorderLayout.EAST);

        topContainer.add(pillRow, BorderLayout.SOUTH);
        add(topContainer, BorderLayout.NORTH);

        // Center: Dense Table with Custom Renderers
        JPanel tableContainer = UIUtils.createCardPanel();
        tableContainer.setLayout(new BorderLayout(0, 8));

        String[] cols = {
                "MRN #", "Patient Demographics", "LOS / Adm Date", "Ward & Bed",
                "Attending MD", "Acuity ESI", "Alert Flags", "Chart Action"
        };

        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 7;
            }
        };

        dataTable = new JTable(tableModel);
        dataTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        dataTable.getColumnModel().getColumn(0).setPreferredWidth(95);
        dataTable.getColumnModel().getColumn(1).setPreferredWidth(175);
        dataTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        dataTable.getColumnModel().getColumn(3).setPreferredWidth(125);
        dataTable.getColumnModel().getColumn(4).setPreferredWidth(145);
        dataTable.getColumnModel().getColumn(5).setPreferredWidth(110);
        dataTable.getColumnModel().getColumn(6).setPreferredWidth(165);
        dataTable.getColumnModel().getColumn(7).setPreferredWidth(110);

        // Custom Demographics Renderer with Avatar
        dataTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                if (row < displayedPatients.size()) {
                    Patient p = displayedPatients.get(row);
                    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
                    panel.setOpaque(isSelected);
                    if (isSelected) panel.setBackground(table.getSelectionBackground());

                    Color avColor = p.getAcuityLevel() <= 2 ? MediCoreTheme.ESI1_TEXT : MediCoreTheme.PRIMARY_ACCENT;
                    JLabel av = new JLabel(UIUtils.createAvatarIcon(p.getInitials(), avColor));
                    panel.add(av);

                    JPanel textP = new JPanel(new GridLayout(2, 1, 0, 0));
                    textP.setOpaque(false);
                    JLabel nameLbl = new JLabel(p.getFullName());
                    nameLbl.setFont(MediCoreTheme.FONT_BODY_BOLD);
                    JLabel subLbl = new JLabel(p.getAge() + "y • " + p.getBiologicalSex() + " • DOB " + p.getDob());
                    subLbl.setFont(MediCoreTheme.FONT_SMALL);
                    subLbl.setForeground(MediCoreTheme.TEXT_MUTED);

                    textP.add(nameLbl);
                    textP.add(subLbl);
                    panel.add(textP);
                    return panel;
                }
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            }
        });

        // ESI Renderer
        dataTable.getColumnModel().getColumn(5).setCellRenderer((table, value, isSelected, hasFocus, row, column) -> {
            if (value instanceof Integer) {
                return UIUtils.createAcuityBadge((Integer) value);
            }
            return new JLabel(String.valueOf(value));
        });

        // Alert Flags Renderer
        dataTable.getColumnModel().getColumn(6).setCellRenderer((table, value, isSelected, hasFocus, row, column) -> {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
            p.setOpaque(isSelected);
            if (isSelected) p.setBackground(table.getSelectionBackground());

            if (row < displayedPatients.size()) {
                Patient pt = displayedPatients.get(row);
                if (pt.getAlerts() != null && !pt.getAlerts().isEmpty()) {
                    for (int i = 0; i < Math.min(2, pt.getAlerts().size()); i++) {
                        String a = pt.getAlerts().get(i);
                        Color c = a.contains("Allergy") ? MediCoreTheme.ESI1_TEXT : (a.contains("STAT") || a.contains("Code") ? MediCoreTheme.ESI2_TEXT : MediCoreTheme.TEXT_PRIMARY);
                        Color bg = a.contains("Allergy") ? MediCoreTheme.ESI1_BG : MediCoreTheme.BG_PANEL;
                        Color bd = a.contains("Allergy") ? MediCoreTheme.ESI1_BORDER : MediCoreTheme.BORDER_LIGHT;
                        p.add(UIUtils.createPillBadge(a, c, bg, bd));
                    }
                } else {
                    p.add(UIUtils.createPillBadge("No Alerts", MediCoreTheme.ESI4_TEXT, MediCoreTheme.ESI4_BG, MediCoreTheme.ESI4_BORDER));
                }
            }
            return p;
        });

        // Action Renderer & Editor
        dataTable.getColumnModel().getColumn(7).setCellRenderer((table, value, isSelected, hasFocus, row, column) -> {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 2));
            p.setOpaque(isSelected);
            if (isSelected) p.setBackground(table.getSelectionBackground());

            JButton btn = new JButton("Open Chart ➔");
            btn.setFont(MediCoreTheme.FONT_SMALL);
            btn.setBackground(MediCoreTheme.PRIMARY_ACCENT);
            btn.setForeground(Color.WHITE);
            p.add(btn);
            return p;
        });

        dataTable.getColumnModel().getColumn(7).setCellEditor(new DefaultCellEditor(new JCheckBox()) {
            private String selectedMrn;
            private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 2));
            private final JButton btn = new JButton("Open Chart ➔");

            {
                panel.setOpaque(false);
                btn.setFont(MediCoreTheme.FONT_SMALL);
                btn.setBackground(MediCoreTheme.PRIMARY_ACCENT);
                btn.setForeground(Color.WHITE);
                btn.addActionListener(e -> {
                    fireEditingStopped();
                    if (navigator != null && selectedMrn != null) {
                        navigator.navigateTo("Update Records", selectedMrn);
                    }
                });
                panel.add(btn);
            }

            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                this.selectedMrn = (String) table.getValueAt(row, 0);
                return panel;
            }

            @Override
            public Object getCellEditorValue() {
                return selectedMrn;
            }
        });

        JScrollPane scrollPane = new JScrollPane(dataTable);
        scrollPane.setBorder(new LineBorder(MediCoreTheme.BORDER_LIGHT, 1));
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        // Pagination & Footer status bar
        JPanel paginationBar = new JPanel(new BorderLayout());
        paginationBar.setOpaque(false);

        recordCountLbl.setFont(MediCoreTheme.FONT_SMALL);
        recordCountLbl.setForeground(MediCoreTheme.TEXT_SECONDARY);
        paginationBar.add(recordCountLbl, BorderLayout.WEST);

        JPanel pageControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pageControls.setOpaque(false);

        JButton prevBtn = UIUtils.createSecondaryButton("« Prev");
        JLabel pageLbl = new JLabel("Page 1 of 1 (100 Rows/Page)");
        pageLbl.setFont(MediCoreTheme.FONT_SMALL);
        JButton nextBtn = UIUtils.createSecondaryButton("Next »");

        pageControls.add(prevBtn);
        pageControls.add(pageLbl);
        pageControls.add(nextBtn);

        paginationBar.add(pageControls, BorderLayout.EAST);
        tableContainer.add(paginationBar, BorderLayout.SOUTH);

        add(tableContainer, BorderLayout.CENTER);

        applyFilters();
    }

    private JButton createFilterPill(String title, String key, boolean selected) {
        JButton btn = new JButton(title);
        btn.setFont(MediCoreTheme.FONT_SMALL);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.putClientProperty("filterKey", key);

        if (selected) {
            btn.setBackground(MediCoreTheme.PRIMARY_ACCENT);
            btn.setForeground(Color.WHITE);
            btn.setBorder(new CompoundBorder(new LineBorder(MediCoreTheme.PRIMARY_DARK, 1, true), new EmptyBorder(5, 12, 5, 12)));
        } else {
            btn.setBackground(MediCoreTheme.BG_WHITE);
            btn.setForeground(MediCoreTheme.TEXT_PRIMARY);
            btn.setBorder(new CompoundBorder(new LineBorder(MediCoreTheme.BORDER_LIGHT, 1, true), new EmptyBorder(5, 12, 5, 12)));
        }

        btn.addActionListener(e -> {
            activeQuickFilter = key;
            updatePillStyles();
            applyFilters();
        });

        return btn;
    }

    private void updatePillStyles() {
        JButton[] pills = {pillAll, pillAlerts, pillLabs, pillAssigned};
        for (JButton p : pills) {
            String k = (String) p.getClientProperty("filterKey");
            boolean active = k.equals(activeQuickFilter);
            if (active) {
                p.setBackground(MediCoreTheme.PRIMARY_ACCENT);
                p.setForeground(Color.WHITE);
                p.setBorder(new CompoundBorder(new LineBorder(MediCoreTheme.PRIMARY_DARK, 1, true), new EmptyBorder(5, 12, 5, 12)));
            } else {
                p.setBackground(MediCoreTheme.BG_WHITE);
                p.setForeground(MediCoreTheme.TEXT_PRIMARY);
                p.setBorder(new CompoundBorder(new LineBorder(MediCoreTheme.BORDER_LIGHT, 1, true), new EmptyBorder(5, 12, 5, 12)));
            }
        }
    }

    private void applyFilters() {
        String query = searchField.getText().trim().toLowerCase();
        String selectedWard = (String) wardFilter.getSelectedItem();
        String selectedStatus = (String) statusFilter.getSelectedItem();
        String selectedAcuity = (String) acuityFilter.getSelectedItem();

        List<Patient> all = HospitalDataStore.getInstance().getPatients();

        displayedPatients = all.stream().filter(p -> {
            // Text search
            if (!query.isEmpty()) {
                boolean match = p.getMrn().toLowerCase().contains(query) ||
                                p.getFullName().toLowerCase().contains(query) ||
                                (p.getSsnMasked() != null && p.getSsnMasked().toLowerCase().contains(query));
                if (!match) return false;
            }

            // Ward filter
            if (!"All Units".equals(selectedWard)) {
                if (!p.getWard().equalsIgnoreCase(selectedWard) && !p.getWard().contains(selectedWard)) return false;
            }

            // Status filter
            if (!"All Statuses".equals(selectedStatus)) {
                if (!p.getAdmissionStatus().equalsIgnoreCase(selectedStatus)) return false;
            }

            // Acuity filter
            if (!"All Acuity Levels".equals(selectedAcuity)) {
                int targetAcuity = Integer.parseInt(selectedAcuity.substring(4, 5));
                if (p.getAcuityLevel() != targetAcuity) return false;
            }

            // Quick view pills
            if ("ALERTS".equals(activeQuickFilter)) {
                return p.getAcuityLevel() <= 2 || (p.getAlerts() != null && !p.getAlerts().isEmpty());
            } else if ("LABS".equals(activeQuickFilter)) {
                return p.getAlerts() != null && p.getAlerts().stream().anyMatch(a -> a.toLowerCase().contains("lab") || a.toLowerCase().contains("troponin") || a.toLowerCase().contains("abg"));
            } else if ("ASSIGNED".equals(activeQuickFilter)) {
                return p.getAttendingDoctor().contains("Vance");
            }

            return true;
        }).collect(Collectors.toList());

        // Refresh table rows
        tableModel.setRowCount(0);
        for (Patient p : displayedPatients) {
            tableModel.addRow(new Object[]{
                    p.getMrn(),
                    p.getFullName(),
                    p.getLengthOfStay() + " (" + p.getAdmissionDateTime().format(DateTimeFormatter.ofPattern("MM/dd HH:mm")) + ")",
                    p.getWard() + " - " + p.getBed(),
                    p.getAttendingDoctor(),
                    p.getAcuityLevel(),
                    p.getAlerts(),
                    "Chart"
            });
        }

        recordCountLbl.setText("Showing " + displayedPatients.size() + " of " + all.size() + " Active Inpatients • HL7 Directory Synchronized");
    }

    private void exportCsv() {
        try {
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new File("medicore_census_export.csv"));
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File f = chooser.getSelectedFile();
                try (FileWriter w = new FileWriter(f)) {
                    w.write("MRN,Name,Age,Sex,Ward,Bed,Attending,Acuity,AdmissionDate,LOS,Allergies,Vitals\n");
                    for (Patient p : displayedPatients) {
                        w.write(String.format("\"%s\",\"%s\",%d,\"%s\",\"%s\",\"%s\",\"%s\",%d,\"%s\",\"%s\",\"%s\",\"%s\"\n",
                                p.getMrn(), p.getFullName(), p.getAge(), p.getBiologicalSex(),
                                p.getWard(), p.getBed(), p.getAttendingDoctor(), p.getAcuityLevel(),
                                p.getAdmissionDateTime().toString(), p.getLengthOfStay(),
                                String.join("; ", p.getAllergies()), p.getVitals().getSummary()));
                    }
                }
                JOptionPane.showMessageDialog(this, "Exported " + displayedPatients.size() + " records to " + f.getName(), "CSV Export Complete", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void onDataChanged() {
        SwingUtilities.invokeLater(this::applyFilters);
    }
}
