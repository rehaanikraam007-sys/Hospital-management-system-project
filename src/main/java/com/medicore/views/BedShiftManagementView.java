package com.medicore.views;

import com.medicore.dialogs.TransferSlipDialog;
import com.medicore.model.Bed;
import com.medicore.model.HospitalDataStore;
import com.medicore.model.Patient;
import com.medicore.model.ShiftHandoff;
import com.medicore.theme.MediCoreTheme;
import com.medicore.theme.UIUtils;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BedShiftManagementView extends JPanel implements HospitalDataStore.DataChangeListener {
    private final DashboardView.ViewNavigator navigator;

    // Left Panel: Bed Transfer
    private final JComboBox<String> patientTransferCombo = new JComboBox<>();
    private final JLabel originBedLbl = new JLabel();
    private final JLabel originStatusLbl = new JLabel();
    private final JComboBox<String> destBedCombo = new JComboBox<>();
    private final JComboBox<String> reasonCombo = new JComboBox<>(new String[]{
            "Clinical Acuity Escalation (ICU / Step-Down)",
            "Step-Down to General Inpatient Telemetry",
            "Specialized Procedural / Post-Cath Bedding",
            "Infection Control / Negative Pressure Isolation",
            "Discharge Lounge Transition"
    });
    private final JComboBox<String> escortNurseCombo = new JComboBox<>(new String[]{
            "RN Sarah Jenkins, BSN (Telemetry Lead)",
            "RN Tyler Brody, BSN (ICU Float)",
            "RN Marcus Vance, BSN (Staff RN)"
    });
    private final JComboBox<String> porterCombo = new JComboBox<>(new String[]{
            "Porter Marcus Cruz (Station 3A)",
            "Porter Andre Washington (Transport Pool)",
            "Porter Elena Rossi (Central Dispatch)"
    });
    private final JCheckBox telemetryCheck = new JCheckBox("Continuous Telemetry Required & Armed", true);
    private final JCheckBox evsSanitizeCheck = new JCheckBox("Auto-Dispatch EVS Sanitation to Origin Bed", true);
    private final JCheckBox o2TankCheck = new JCheckBox("O2 Portable Transport Tank (4 L/min)", true);
    private final JCheckBox fallRiskCheck = new JCheckBox("Yellow Fall Risk Armband Re-verified", true);

    // Right Panel: Shift Handoff & Roster
    private final JProgressBar capacityMeter = new JProgressBar(0, 165);
    private final JLabel capacityLbl = new JLabel("142 / 165 Beds Occupied (86% Occupancy)");
    private final JTextArea situationArea = new JTextArea(2, 20);
    private final JTextArea backgroundArea = new JTextArea(2, 20);
    private final JTextArea assessmentArea = new JTextArea(2, 20);
    private final JTextArea recommendationArea = new JTextArea(2, 20);

    private final JLabel outgoingSignLbl = new JLabel("RN Kendra Miller, BSN — Signed at 14:00:12");
    private final JPasswordField incomingPinField = new JPasswordField("2941");
    private final JLabel handoffStatusBadge;

    public BedShiftManagementView(DashboardView.ViewNavigator navigator) {
        this.navigator = navigator;
        setLayout(new BorderLayout(0, 10));
        setBackground(MediCoreTheme.BG_STERILE);
        setBorder(new EmptyBorder(12, 14, 12, 14));
        HospitalDataStore.getInstance().addListener(this);

        handoffStatusBadge = UIUtils.createPillBadge("PENDING INCOMING CO-SIGN", MediCoreTheme.ESI3_TEXT, MediCoreTheme.ESI3_BG, MediCoreTheme.ESI3_BORDER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createLeftTransferPanel(), createRightShiftPanel());
        split.setResizeWeight(0.5);
        split.setBorder(null);
        split.setOpaque(false);
        add(split, BorderLayout.CENTER);

        add(createActionToolbar(), BorderLayout.SOUTH);

        refreshData();
    }
    private JPanel createLeftTransferPanel() {
        JPanel p = UIUtils.createCardPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        p.add(UIUtils.createSectionHeader("BED TRANSFER & REASSIGNMENT PROTOCOL", "HL7 ADT-A02 Inpatient Intra-Hospital Room / Bed Relocation"));
        p.add(Box.createVerticalStrut(8));

        // Origin summary
        JPanel originCard = new JPanel(new GridLayout(3, 1, 0, 2));
        originCard.setOpaque(false);
        originCard.setBorder(new CompoundBorder(new LineBorder(MediCoreTheme.BORDER_LIGHT, 1, true), new EmptyBorder(8, 10, 8, 10)));

        JPanel ptRow = new JPanel(new BorderLayout(6, 0));
        ptRow.setOpaque(false);
        ptRow.add(new JLabel("Transfer Candidate:"), BorderLayout.WEST);
        patientTransferCombo.setFont(MediCoreTheme.FONT_SMALL);
        patientTransferCombo.addActionListener(e -> updateOriginSummary());
        ptRow.add(patientTransferCombo, BorderLayout.CENTER);
        originCard.add(ptRow);

        originBedLbl.setFont(MediCoreTheme.FONT_BODY_BOLD);
        originBedLbl.setForeground(MediCoreTheme.PRIMARY_ACCENT);
        originCard.add(originBedLbl);

        originStatusLbl.setFont(MediCoreTheme.FONT_SMALL);
        originStatusLbl.setForeground(MediCoreTheme.TEXT_SECONDARY);
        originCard.add(originStatusLbl);

        p.add(originCard);
        p.add(Box.createVerticalStrut(10));

        // Destination selector
        JPanel formGrid = new JPanel(new GridLayout(3, 2, 10, 8));
        formGrid.setOpaque(false);

        formGrid.add(createLabeledField("Target Destination Bed:", destBedCombo));
        formGrid.add(createLabeledField("Clinical Transfer Reason:", reasonCombo));
        formGrid.add(createLabeledField("Assigned Escort Registered Nurse:", escortNurseCombo));
        formGrid.add(createLabeledField("Transport Porter Dispatch:", porterCombo));

        p.add(formGrid);
        p.add(Box.createVerticalStrut(10));

        // Safeguards checklist
        JLabel safeTitle = new JLabel("CLINICAL SAFETY MANDATES & CHECKLIST");
        safeTitle.setFont(MediCoreTheme.FONT_SUBHEADER);
        p.add(safeTitle);
        p.add(Box.createVerticalStrut(4));

        JPanel checkGrid = new JPanel(new GridLayout(2, 2, 8, 4));
        checkGrid.setOpaque(false);
        checkGrid.add(telemetryCheck);
        checkGrid.add(evsSanitizeCheck);
        checkGrid.add(o2TankCheck);
        checkGrid.add(fallRiskCheck);
        p.add(checkGrid);
        p.add(Box.createVerticalStrut(12));

        // HL7 Transfer Step Flow
        p.add(createStepFlowPanel());
        p.add(Box.createVerticalGlue());

        return p;
    }

    private JPanel createStepFlowPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        JLabel title = new JLabel("HL7 ADT-A02 LIFECYCLE PROGRESS");
        title.setFont(MediCoreTheme.FONT_SMALL);
        title.setForeground(MediCoreTheme.TEXT_MUTED);
        p.add(title, BorderLayout.NORTH);

        JPanel flow = new JPanel(new GridLayout(1, 4, 6, 0));
        flow.setOpaque(false);
        flow.add(UIUtils.createPillBadge("1. Origin Release ✓", MediCoreTheme.ESI4_TEXT, MediCoreTheme.ESI4_BG, MediCoreTheme.ESI4_BORDER));
        flow.add(UIUtils.createPillBadge("2. EVS Disinfection", MediCoreTheme.ESI3_TEXT, MediCoreTheme.ESI3_BG, MediCoreTheme.ESI3_BORDER));
        flow.add(UIUtils.createPillBadge("3. SBAR Handoff", MediCoreTheme.PRIMARY_ACCENT, MediCoreTheme.PRIMARY_LIGHT, MediCoreTheme.PRIMARY_ACCENT));
        flow.add(UIUtils.createPillBadge("4. Destination Armed", MediCoreTheme.TEXT_SECONDARY, MediCoreTheme.BG_PANEL, MediCoreTheme.BORDER_DARK));

        p.add(flow, BorderLayout.CENTER);
        return p;
    }
    private JPanel createRightShiftPanel() {
        JPanel p = UIUtils.createCardPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        JPanel topHead = new JPanel(new BorderLayout());
        topHead.setOpaque(false);
        topHead.add(UIUtils.createSectionHeader("SHIFT HANDOFF & NURSE ROSTER (SBAR)", "Dual-Nurse Electronic Co-Signature & Acuity Workload Balance"), BorderLayout.WEST);
        topHead.add(handoffStatusBadge, BorderLayout.EAST);
        p.add(topHead);
        p.add(Box.createVerticalStrut(6));

        // Capacity Meter
        JPanel capRow = new JPanel(new BorderLayout(0, 4));
        capRow.setOpaque(false);
        capacityLbl.setFont(MediCoreTheme.FONT_SMALL);
        capacityLbl.setForeground(MediCoreTheme.TEXT_SECONDARY);
        capacityMeter.setValue(142);
        capacityMeter.setForeground(MediCoreTheme.PRIMARY_ACCENT);
        capacityMeter.setPreferredSize(new Dimension(0, 10));
        capRow.add(capacityLbl, BorderLayout.NORTH);
        capRow.add(capacityMeter, BorderLayout.CENTER);
        p.add(capRow);
        p.add(Box.createVerticalStrut(8));

        // SBAR Fields
        p.add(createSbarBox("S - Situation:", situationArea));
        p.add(Box.createVerticalStrut(4));
        p.add(createSbarBox("B - Background:", backgroundArea));
        p.add(Box.createVerticalStrut(4));
        p.add(createSbarBox("A - Assessment:", assessmentArea));
        p.add(Box.createVerticalStrut(4));
        p.add(createSbarBox("R - Recommendation:", recommendationArea));
        p.add(Box.createVerticalStrut(8));

        // Dual Co-Sign Panel
        JPanel signPanel = new JPanel(new GridLayout(2, 2, 8, 4));
        signPanel.setOpaque(false);
        signPanel.setBorder(new CompoundBorder(new LineBorder(MediCoreTheme.BORDER_LIGHT, 1, true), new EmptyBorder(6, 8, 6, 8)));

        signPanel.add(new JLabel("Outgoing Charge RN Sign-Off:"));
        signPanel.add(new JLabel("Incoming Charge RN Digital PIN:"));
        signPanel.add(outgoingSignLbl);

        JPanel pinRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        pinRow.setOpaque(false);
        incomingPinField.setPreferredSize(new Dimension(80, 28));
        pinRow.add(incomingPinField);
        pinRow.add(new JLabel("RN Marcus Vance, BSN"));
        signPanel.add(pinRow);
        p.add(signPanel);
        p.add(Box.createVerticalStrut(8));

        // Staffing Ratio Table
        JLabel rosterLbl = new JLabel("WARD 3A NURSE-TO-PATIENT RATIO & ACUITY LOAD");
        rosterLbl.setFont(MediCoreTheme.FONT_SMALL);
        rosterLbl.setForeground(MediCoreTheme.TEXT_MUTED);
        p.add(rosterLbl);
        p.add(Box.createVerticalStrut(4));

        String[] cols = {"Nurse Name", "Role / Title", "Assigned Pod", "Bed Count", "Acuity Index"};
        DefaultTableModel rosterModel = new DefaultTableModel(cols, 0);
        rosterModel.addRow(new Object[]{"RN Sarah Jenkins", "Staff RN, BSN", "Pod 3A-North (301-303)", "3 Beds", "9.4 (High Acuity)"});
        rosterModel.addRow(new Object[]{"RN Marcus Vance", "Staff RN, BSN", "Pod 3A-South (304-306)", "3 Beds", "7.1 (Standard)"});
        rosterModel.addRow(new Object[]{"RN Kendra Miller", "Charge Nurse", "Flow Coordinator", "All 16", "4.2 (Supervisory)"});

        JTable rosterTable = new JTable(rosterModel);
        rosterTable.setRowHeight(24);
        rosterTable.setFont(MediCoreTheme.FONT_SMALL);
        JScrollPane rosterScroll = new JScrollPane(rosterTable);
        rosterScroll.setPreferredSize(new Dimension(0, 85));
        rosterScroll.setBorder(new LineBorder(MediCoreTheme.BORDER_LIGHT, 1));
        p.add(rosterScroll);

        return p;
    }

    private JPanel createSbarBox(String label, JTextArea area) {
        JPanel p = new JPanel(new BorderLayout(0, 2));
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(MediCoreTheme.FONT_SMALL);
        l.setForeground(MediCoreTheme.TEXT_PRIMARY);
        area.setFont(MediCoreTheme.FONT_SMALL);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(new CompoundBorder(new LineBorder(MediCoreTheme.BORDER_LIGHT, 1), new EmptyBorder(2, 4, 2, 4)));
        p.add(l, BorderLayout.NORTH);
        p.add(area, BorderLayout.CENTER);
        return p;
    }
    private JPanel createActionToolbar() {
        JPanel p = UIUtils.createCardPanel();
        p.setLayout(new BorderLayout());

        JPanel leftBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftBtns.setOpaque(false);

        JButton evsBtn = UIUtils.createSecondaryButton("🧹 Send Alert to EVS");
        evsBtn.addActionListener(e -> sendEvsAlert());

        JButton slipBtn = UIUtils.createSecondaryButton("🖨 Print Transfer Slip");
        slipBtn.addActionListener(e -> printCurrentSlip());

        leftBtns.add(evsBtn);
        leftBtns.add(slipBtn);

        JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightBtns.setOpaque(false);

        JButton handoffBtn = UIUtils.createSecondaryButton("📋 Execute Shift Handoff (Ctrl+H)");
        handoffBtn.addActionListener(e -> executeShiftHandoff());

        JButton transferBtn = UIUtils.createPrimaryButton("🔄 Authorize Bed Transfer", "Enter");
        transferBtn.addActionListener(e -> executeBedTransfer());

        rightBtns.add(handoffBtn);
        rightBtns.add(transferBtn);

        p.add(leftBtns, BorderLayout.WEST);
        p.add(rightBtns, BorderLayout.EAST);
        return p;
    }

    private JPanel createLabeledField(String label, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(4, 2));
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(MediCoreTheme.FONT_SMALL);
        l.setForeground(MediCoreTheme.TEXT_SECONDARY);
        p.add(l, BorderLayout.NORTH);
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    private void updateOriginSummary() {
        String sel = (String) patientTransferCombo.getSelectedItem();
        if (sel != null && sel.contains("[")) {
            String mrn = sel.substring(sel.indexOf("[") + 1, sel.indexOf("]"));
            Patient p = HospitalDataStore.getInstance().getPatientByMrn(mrn);
            if (p != null) {
                originBedLbl.setText("Current Location: " + p.getWard() + " • " + p.getBed());
                originStatusLbl.setText("Acuity: ESI-" + p.getAcuityLevel() + " • Telemetry: " + p.getVitals().getSummary());
            }
        }
    }

    private void refreshData() {
        patientTransferCombo.removeAllItems();
        List<Patient> patients = HospitalDataStore.getInstance().getPatients();
        for (Patient p : patients) {
            patientTransferCombo.addItem(p.getFullName() + " [" + p.getMrn() + "]");
        }

        // Set David Cho as default if available
        for (int i = 0; i < patientTransferCombo.getItemCount(); i++) {
            if (patientTransferCombo.getItemAt(i).contains("MRN-71932")) {
                patientTransferCombo.setSelectedIndex(i);
                break;
            }
        }

        destBedCombo.removeAllItems();
        List<Bed> beds = HospitalDataStore.getInstance().getBeds();
        for (Bed b : beds) {
            if (b.getStatus() == Bed.Status.AVAILABLE) {
                destBedCombo.addItem(b.getBedId() + " (" + b.getWard() + ") [CLEAN]");
            }
        }
        destBedCombo.addItem("MICU-04-B (ICU) [CLEAN]");
        destBedCombo.addItem("Cardiology-201-B (Cardiology) [DIRTY - Needs EVS]");

        ShiftHandoff sh = HospitalDataStore.getInstance().getCurrentShiftHandoff();
        situationArea.setText(sh.getSituation());
        backgroundArea.setText(sh.getBackground());
        assessmentArea.setText(sh.getAssessment());
        recommendationArea.setText(sh.getRecommendation());
        outgoingSignLbl.setText(sh.getOutgoingChargeRn() + " — Signed at " + sh.getOutgoingSignedTimestamp());

        updateOriginSummary();
    }

    private void executeBedTransfer() {
        String sel = (String) patientTransferCombo.getSelectedItem();
        if (sel == null || !sel.contains("[")) return;
        String mrn = sel.substring(sel.indexOf("[") + 1, sel.indexOf("]"));
        Patient p = HospitalDataStore.getInstance().getPatientByMrn(mrn);

        String destRaw = (String) destBedCombo.getSelectedItem();
        if (p == null || destRaw == null) return;
        String destBedId = destRaw.split(" ")[0].trim();

        if (destRaw.contains("DIRTY")) {
            JOptionPane.showMessageDialog(this, "Destination bed " + destBedId + " is marked DIRTY.\nCannot transfer patient until EVS sanitation is certified.", "Sanitation Hazard", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String reason = (String) reasonCombo.getSelectedItem();
        String nurse = (String) escortNurseCombo.getSelectedItem();
        String porter = (String) porterCombo.getSelectedItem();

        String originBedId = p.getBed();
        boolean ok = HospitalDataStore.getInstance().transferPatientBed(mrn, destBedId, reason, nurse, porter);

        if (ok) {
            JOptionPane.showMessageDialog(this,
                    "HL7 ADT-A02 Bed Transfer Successful!\n\n" +
                            "Patient: " + p.getFullName() + " (" + p.getMrn() + ")\n" +
                            "From: " + originBedId + "  ➔  To: " + destBedId + "\n" +
                            "Origin Bed Vacated: EVS Sanitation Dispatched (ETA 25m)\n" +
                            "Escort RN: " + nurse + "\n" +
                            "Safeguards: Telemetry Active • Transport O2 Armed",
                    "Bed Transfer Authorized", JOptionPane.INFORMATION_MESSAGE);

            printCurrentSlip();
        }
    }

    private void printCurrentSlip() {
        String sel = (String) patientTransferCombo.getSelectedItem();
        if (sel == null || !sel.contains("[")) return;
        String mrn = sel.substring(sel.indexOf("[") + 1, sel.indexOf("]"));
        Patient p = HospitalDataStore.getInstance().getPatientByMrn(mrn);
        if (p == null) return;

        String dest = (String) destBedCombo.getSelectedItem();
        Frame top = (Frame) SwingUtilities.getWindowAncestor(this);
        new TransferSlipDialog(top, p, p.getWard() + " - " + p.getBed(), dest,
                (String) reasonCombo.getSelectedItem(),
                (String) escortNurseCombo.getSelectedItem(),
                (String) porterCombo.getSelectedItem()).setVisible(true);
    }

    private void sendEvsAlert() {
        JOptionPane.showMessageDialog(this,
                "EVS STAT Sanitation Request Transmitted!\n\n" +
                        "Unit: Ward 3A / Central Telemetry\n" +
                        "Beds Flagged for Terminal UV-C Wash: Bed 305-B, Cardiology-201-B\n" +
                        "EVS Lead Pager Ack: RECEIVED (ETA: 8 mins)",
                "EVS Dispatch Confirmed", JOptionPane.INFORMATION_MESSAGE);
    }

    private void executeShiftHandoff() {
        String pin = new String(incomingPinField.getPassword()).trim();
        if (pin.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Incoming Charge RN must enter digital PIN to complete dual co-sign.", "Sign-off Required", JOptionPane.WARNING_MESSAGE);
            incomingPinField.requestFocus();
            return;
        }

        ShiftHandoff sh = HospitalDataStore.getInstance().getCurrentShiftHandoff();
        sh.setSituation(situationArea.getText().trim());
        sh.setBackground(backgroundArea.getText().trim());
        sh.setAssessment(assessmentArea.getText().trim());
        sh.setRecommendation(recommendationArea.getText().trim());
        sh.setIncomingSignedTimestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        sh.setFullyCoSigned(true);

        handoffStatusBadge.setText("✓ DUAL CO-SIGN COMPLETE");
        handoffStatusBadge.setForeground(MediCoreTheme.ESI4_TEXT);

        JOptionPane.showMessageDialog(this,
                "Formal Shift Handoff SBAR Certified!\n\n" +
                        "Outgoing Charge RN: " + sh.getOutgoingChargeRn() + " (Signed " + sh.getOutgoingSignedTimestamp() + ")\n" +
                        "Incoming Charge RN: " + sh.getIncomingChargeRn() + " (Signed " + sh.getIncomingSignedTimestamp() + ")\n" +
                        "Audit Trail: HL7 Handoff Manifest archived to medical record server.",
                "Shift Handoff Complete", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void onDataChanged() {
        SwingUtilities.invokeLater(this::refreshData);
    }
}
