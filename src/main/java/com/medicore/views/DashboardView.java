package com.medicore.views;

import com.medicore.components.BedMatrixCard;
import com.medicore.components.StatCard;
import com.medicore.dialogs.RapidResponseDialog;
import com.medicore.model.Bed;
import com.medicore.model.HospitalDataStore;
import com.medicore.model.Patient;
import com.medicore.theme.MediCoreTheme;
import com.medicore.theme.UIUtils;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Screen 2: Clinical Dashboard Workstation & Charge Nurse / Attending Command Center.
 */
public class DashboardView extends JPanel implements HospitalDataStore.DataChangeListener {
    public interface ViewNavigator {
        void navigateTo(String viewName, String patientMrn);
    }

    private final ViewNavigator navigator;
    private final StatCard censusCard;
    private final StatCard edFlowCard;
    private final StatCard throughputCard;
    private final StatCard labsCard;

    private final JPanel bedGridPanel;
    private final JTable queueTable;
    private final DefaultTableModel tableModel;
    private final JComboBox<String> wardSelector;

    public DashboardView(ViewNavigator navigator) {
        this.navigator = navigator;
        setLayout(new BorderLayout(0, 10));
        setBackground(MediCoreTheme.BG_STERILE);
        setBorder(new EmptyBorder(12, 14, 12, 14));

        HospitalDataStore.getInstance().addListener(this);

        // 1. Executive Census Ribbon (Top)
        JPanel ribbonPanel = new JPanel(new GridLayout(1, 4, 12, 0));
        ribbonPanel.setOpaque(false);
        ribbonPanel.setPreferredSize(new Dimension(0, 96));

        censusCard = new StatCard("TOTAL HOSPITAL CENSUS", "142 / 165 Beds", "86% Total Occupancy • 23 Available", MediCoreTheme.PRIMARY_ACCENT, true, 86);
        edFlowCard = new StatCard("ED CLINICAL FLOW", "14 Active In-Queue", "Avg Triage Wait: 8m • 4 Bedded", MediCoreTheme.ESI2_TEXT, false, 0);
        throughputCard = new StatCard("DAILY THROUGHPUT", "18 Admits / 12 Discharges", "Net Census Delta: +6 Patients", MediCoreTheme.ESI4_TEXT, false, 0);
        labsCard = new StatCard("CRITICAL STAT LABS", "7 Pending Verification", "4 Troponin • 2 ABG • 1 Lactate", MediCoreTheme.ESI1_TEXT, false, 0);

        ribbonPanel.add(censusCard);
        ribbonPanel.add(edFlowCard);
        ribbonPanel.add(throughputCard);
        ribbonPanel.add(labsCard);

        add(ribbonPanel, BorderLayout.NORTH);

        // 2. Main Center Workspace: Split between Bed Matrix / Queue (Left 2 cols) and STAT Action Sidebar (Right col)
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplit.setResizeWeight(0.74);
        mainSplit.setBorder(null);
        mainSplit.setOpaque(false);

        // Left 2-Column Content: Bed Matrix + Queue
        JPanel leftWorkarea = new JPanel(new BorderLayout(0, 12));
        leftWorkarea.setOpaque(false);

        // Upper section: Ward & Bed Occupancy Matrix
        JPanel bedMatrixSection = UIUtils.createCardPanel();
        bedMatrixSection.setLayout(new BorderLayout(0, 8));

        JPanel bedHeader = new JPanel(new BorderLayout());
        bedHeader.setOpaque(false);

        JPanel headerTitles = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        headerTitles.setOpaque(false);
        JLabel bedTitle = new JLabel("WARD & BED OCCUPANCY MATRIX");
        bedTitle.setFont(MediCoreTheme.FONT_HEADER);
        bedTitle.setForeground(MediCoreTheme.TEXT_PRIMARY);
        headerTitles.add(bedTitle);

        wardSelector = new JComboBox<>(new String[]{"All Units", "Ward 3A", "ICU", "PACU", "Cardiology"});
        wardSelector.setFont(MediCoreTheme.FONT_SMALL);
        wardSelector.addActionListener(e -> refreshBeds());
        headerTitles.add(wardSelector);

        bedHeader.add(headerTitles, BorderLayout.WEST);

        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        legendPanel.setOpaque(false);
        legendPanel.add(UIUtils.createPillBadge("Available", MediCoreTheme.ESI4_TEXT, MediCoreTheme.ESI4_BG, MediCoreTheme.ESI4_BORDER));
        legendPanel.add(UIUtils.createPillBadge("Occupied", MediCoreTheme.PRIMARY_ACCENT, MediCoreTheme.PRIMARY_LIGHT, MediCoreTheme.PRIMARY_ACCENT));
        legendPanel.add(UIUtils.createPillBadge("Disinfection", MediCoreTheme.ESI3_TEXT, MediCoreTheme.ESI3_BG, MediCoreTheme.ESI3_BORDER));
        legendPanel.add(UIUtils.createPillBadge("Dirty (EVS)", MediCoreTheme.ESI1_TEXT, MediCoreTheme.ESI1_BG, MediCoreTheme.ESI1_BORDER));
        bedHeader.add(legendPanel, BorderLayout.EAST);

        bedMatrixSection.add(bedHeader, BorderLayout.NORTH);

        bedGridPanel = new JPanel(new GridLayout(0, 4, 8, 8));
        bedGridPanel.setOpaque(false);
        JScrollPane bedScroll = new JScrollPane(bedGridPanel);
        bedScroll.setBorder(null);
        bedScroll.setOpaque(false);
        bedScroll.getViewport().setOpaque(false);
        bedScroll.setPreferredSize(new Dimension(0, 240));
        bedMatrixSection.add(bedScroll, BorderLayout.CENTER);

        // Lower section: Admissions & Emergency Queue Worklist (JTable)
        JPanel queueSection = UIUtils.createCardPanel();
        queueSection.setLayout(new BorderLayout(0, 8));

        JPanel queueHeader = new JPanel(new BorderLayout());
        queueHeader.setOpaque(false);
        JLabel queueTitle = new JLabel("ADMISSIONS & EMERGENCY WORKLIST QUEUE");
        queueTitle.setFont(MediCoreTheme.FONT_HEADER);
        queueTitle.setForeground(MediCoreTheme.TEXT_PRIMARY);
        queueHeader.add(queueTitle, BorderLayout.WEST);

        JLabel queueSub = new JLabel("Real-time HL7 ADT Inpatient Roster • 10 Active Charts");
        queueSub.setFont(MediCoreTheme.FONT_SMALL);
        queueSub.setForeground(MediCoreTheme.TEXT_MUTED);
        queueHeader.add(queueSub, BorderLayout.EAST);

        queueSection.add(queueHeader, BorderLayout.NORTH);

        String[] cols = {"MRN", "Patient Demographics", "Acuity ESI", "Ward / Bed", "Attending MD", "Latest Telemetry Vitals", "Quick Action"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // only actions clickable
            }
        };
        queueTable = new JTable(tableModel);
        queueTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        queueTable.getColumnModel().getColumn(0).setPreferredWidth(90);
        queueTable.getColumnModel().getColumn(1).setPreferredWidth(160);
        queueTable.getColumnModel().getColumn(2).setPreferredWidth(110);
        queueTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        queueTable.getColumnModel().getColumn(4).setPreferredWidth(140);
        queueTable.getColumnModel().getColumn(5).setPreferredWidth(160);
        queueTable.getColumnModel().getColumn(6).setPreferredWidth(140);

        // Custom Renderer for ESI badge column
        queueTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                if (value instanceof Integer) {
                    return UIUtils.createAcuityBadge((Integer) value);
                }
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            }
        });

        // Custom Action Button Renderer & Editor
        queueTable.getColumnModel().getColumn(6).setCellRenderer((table, value, isSelected, hasFocus, row, column) -> {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 2));
            p.setOpaque(false);
            JButton chartBtn = new JButton("📊 Chart");
            chartBtn.setFont(MediCoreTheme.FONT_SMALL);
            chartBtn.setBackground(MediCoreTheme.PRIMARY_ACCENT);
            chartBtn.setForeground(Color.WHITE);
            p.add(chartBtn);
            return p;
        });

        queueTable.getColumnModel().getColumn(6).setCellEditor(new DefaultCellEditor(new JCheckBox()) {
            private String selectedMrn;
            private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 2));
            private final JButton chartBtn = new JButton("📊 Chart");

            {
                panel.setOpaque(false);
                chartBtn.setFont(MediCoreTheme.FONT_SMALL);
                chartBtn.setBackground(MediCoreTheme.PRIMARY_ACCENT);
                chartBtn.setForeground(Color.WHITE);
                chartBtn.addActionListener(e -> {
                    fireEditingStopped();
                    if (navigator != null && selectedMrn != null) {
                        navigator.navigateTo("Update Records", selectedMrn);
                    }
                });
                panel.add(chartBtn);
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

        JScrollPane tableScroll = new JScrollPane(queueTable);
        tableScroll.setBorder(new LineBorder(MediCoreTheme.BORDER_LIGHT, 1));
        queueSection.add(tableScroll, BorderLayout.CENTER);

        JSplitPane leftSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, bedMatrixSection, queueSection);
        leftSplit.setResizeWeight(0.5);
        leftSplit.setBorder(null);
        leftSplit.setOpaque(false);
        leftWorkarea.add(leftSplit, BorderLayout.CENTER);

        mainSplit.setLeftComponent(leftWorkarea);

        // 3. Right Action Sidebar
        JPanel rightSidebar = createRightSidebar();
        mainSplit.setRightComponent(rightSidebar);

        add(mainSplit, BorderLayout.CENTER);

        // Initial populate
        refreshAll();
    }

    private JPanel createRightSidebar() {
        JPanel sidebar = UIUtils.createCardPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(320, 0));

        // High Priority STAT Alert Widget
        JPanel statWidget = new JPanel(new BorderLayout(0, 8));
        statWidget.setOpaque(false);
        statWidget.setBorder(new CompoundBorder(
                new LineBorder(MediCoreTheme.ESI1_BORDER, 1, true),
                new EmptyBorder(10, 10, 10, 10)
        ));
        statWidget.setBackground(MediCoreTheme.ESI1_BG);

        JPanel alertHeader = new JPanel(new BorderLayout());
        alertHeader.setOpaque(false);
        JLabel alertTitle = new JLabel("🚨 STAT CLINICAL ALERT");
        alertTitle.setFont(MediCoreTheme.FONT_HEADER);
        alertTitle.setForeground(MediCoreTheme.ESI1_TEXT);
        alertHeader.add(alertTitle, BorderLayout.WEST);
        statWidget.add(alertHeader, BorderLayout.NORTH);

        JLabel alertBody = new JLabel("<html><b>Bed 303-A (David Cho)</b><br/>Sinus Tachycardia 118 bpm • BP 92/58<br/>ABG PaO2 62 • Transfer to MICU Pending</html>");
        alertBody.setFont(MediCoreTheme.FONT_BODY);
        alertBody.setForeground(MediCoreTheme.TEXT_PRIMARY);
        statWidget.add(alertBody, BorderLayout.CENTER);

        JButton rrtBtn = UIUtils.createDangerButton("⚡ Dispatch RRT (Rapid Response)");
        rrtBtn.addActionListener(e -> {
            Frame topFrame = (Frame) SwingUtilities.getWindowAncestor(this);
            new RapidResponseDialog(topFrame, "Ward 3A - Bed 303-A (David Cho)").setVisible(true);
        });
        statWidget.add(rrtBtn, BorderLayout.SOUTH);

        sidebar.add(statWidget);
        sidebar.add(Box.createVerticalStrut(14));

        // Rapid Action Launcher Shortcuts
        JLabel launcherTitle = new JLabel("RAPID ACTION LAUNCHER");
        launcherTitle.setFont(MediCoreTheme.FONT_SUBHEADER);
        launcherTitle.setForeground(MediCoreTheme.TEXT_PRIMARY);
        sidebar.add(launcherTitle);
        sidebar.add(Box.createVerticalStrut(8));

        String[] quickActions = {
                "➕ Code Blue Activation",
                "🩸 Massive Transfusion Protocol",
                "🧠 Stroke Protocol (NIHSS)",
                "🫁 Sepsis Resus Bundle Order",
                "📋 Fast-Track Bed Transfer",
                "🖨 Print Shift Census Roster"
        };

        for (String act : quickActions) {
            JButton btn = UIUtils.createSecondaryButton(act);
            btn.setMaximumSize(new Dimension(Short.MAX_VALUE, 34));
            btn.setAlignmentX(Component.LEFT_ALIGNMENT);
            btn.addActionListener(e -> {
                if (act.contains("Fast-Track Bed Transfer")) {
                    navigator.navigateTo("Ward & Bed Changes", "MRN-71932");
                } else if (act.contains("Print Shift Census")) {
                    JOptionPane.showMessageDialog(this, "Daily Inpatient Census Roster generated & sent to print queue.", "Report Printed", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    Frame topFrame = (Frame) SwingUtilities.getWindowAncestor(this);
                    new RapidResponseDialog(topFrame, act).setVisible(true);
                }
            });
            sidebar.add(btn);
            sidebar.add(Box.createVerticalStrut(6));
        }

        sidebar.add(Box.createVerticalStrut(12));

        // Shift Rounding Schedule
        JLabel roundTitle = new JLabel("ATTENDING ROUNDING SCHEDULE");
        roundTitle.setFont(MediCoreTheme.FONT_SUBHEADER);
        roundTitle.setForeground(MediCoreTheme.TEXT_PRIMARY);
        sidebar.add(roundTitle);
        sidebar.add(Box.createVerticalStrut(6));

        JPanel schedulePanel = new JPanel(new GridLayout(3, 1, 0, 4));
        schedulePanel.setOpaque(false);

        schedulePanel.add(createScheduleRow("14:30", "Cardiology MD Huddle (Ward 3A)", "In 28 mins"));
        schedulePanel.add(createScheduleRow("15:00", "MICU Bed Capacity Clearance", "Pending"));
        schedulePanel.add(createScheduleRow("15:30", "Shift Changeover SBAR Handoff", "Scheduled"));

        sidebar.add(schedulePanel);
        sidebar.add(Box.createVerticalGlue());

        return sidebar;
    }

    private JPanel createScheduleRow(String time, String desc, String eta) {
        JPanel r = new JPanel(new BorderLayout(6, 0));
        r.setOpaque(false);
        JLabel t = new JLabel(time);
        t.setFont(MediCoreTheme.FONT_MONO_BOLD);
        t.setForeground(MediCoreTheme.PRIMARY_ACCENT);

        JLabel d = new JLabel(desc);
        d.setFont(MediCoreTheme.FONT_SMALL);

        JLabel e = new JLabel(eta);
        e.setFont(MediCoreTheme.FONT_SMALL);
        e.setForeground(MediCoreTheme.TEXT_MUTED);

        r.add(t, BorderLayout.WEST);
        r.add(d, BorderLayout.CENTER);
        r.add(e, BorderLayout.EAST);
        return r;
    }

    private void refreshBeds() {
        bedGridPanel.removeAll();
        String selectedWard = (String) wardSelector.getSelectedItem();
        List<Bed> beds = HospitalDataStore.getInstance().getBeds();

        for (Bed b : beds) {
            if ("All Units".equals(selectedWard) || b.getWard().equalsIgnoreCase(selectedWard) || (selectedWard.contains("Ward 3A") && b.getWard().contains("3A"))) {
                BedMatrixCard card = new BedMatrixCard(b, () -> {
                    if (b.getPatientMrn() != null && navigator != null) {
                        navigator.navigateTo("Update Records", b.getPatientMrn());
                    } else if (b.getStatus() == Bed.Status.AVAILABLE && navigator != null) {
                        navigator.navigateTo("Register Patient", null);
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "Bed " + b.getBedId() + " (" + b.getWard() + ")\nStatus: " + b.getStatus() + "\n" +
                                        (b.getTelemetryVitals() != null ? b.getTelemetryVitals() : ""),
                                "Bed Details", JOptionPane.INFORMATION_MESSAGE);
                    }
                });
                bedGridPanel.add(card);
            }
        }
        bedGridPanel.revalidate();
        bedGridPanel.repaint();
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        List<Patient> patients = HospitalDataStore.getInstance().getPatients();
        for (Patient p : patients) {
            tableModel.addRow(new Object[]{
                    p.getMrn(),
                    p.getFullName() + " (" + p.getAge() + p.getBiologicalSex().substring(0, 1) + ")",
                    p.getAcuityLevel(),
                    p.getWard() + " - " + p.getBed(),
                    p.getAttendingDoctor(),
                    p.getVitals().getSummary(),
                    "Chart"
            });
        }
    }

    public void refreshAll() {
        refreshBeds();
        refreshTable();
    }

    @Override
    public void onDataChanged() {
        SwingUtilities.invokeLater(this::refreshAll);
    }
}
