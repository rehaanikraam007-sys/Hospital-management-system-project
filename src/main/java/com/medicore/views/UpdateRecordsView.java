package com.medicore.views;

import com.medicore.components.EcgWaveformPanel;
import com.medicore.model.HospitalDataStore;
import com.medicore.model.Medication;
import com.medicore.model.Patient;
import com.medicore.model.VitalSigns;
import com.medicore.theme.MediCoreTheme;
import com.medicore.theme.UIUtils;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class UpdateRecordsView extends JPanel implements HospitalDataStore.DataChangeListener {
    private final DashboardView.ViewNavigator navigator;
    private Patient currentPatient;

    private final JComboBox<String> patientSelectorCombo = new JComboBox<>();
    private final JLabel ptNameLabel = new JLabel();
    private final JLabel ptMrnLabel = new JLabel();
    private final JLabel ptLocationLabel = new JLabel();
    private final JLabel ptDemogLabel = new JLabel();
    private final EcgWaveformPanel ecgPanel = new EcgWaveformPanel();
    private final JLabel hrValueLbl = new JLabel();
    private final JLabel bpValueLbl = new JLabel();
    private final JLabel spo2ValueLbl = new JLabel();
    private final JLabel tempValueLbl = new JLabel();
    private final JPanel precautionsListPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
    private final JPanel auditHistoryPanel = new JPanel();

    private final JComboBox<String> icd10Combo = new JComboBox<>(new String[]{
            "I50.9 - Heart Failure, Unspecified",
            "I21.4 - Non-ST Elevation Myocardial Infarction (NSTEMI)",
            "I21.0 - ST Elevation Myocardial Infarction (STEMI Anterior Wall)",
            "J44.1 - Chronic Obstructive Pulmonary Disease with Acute Exacerbation",
            "J18.9 - Pneumonia, Unspecified Organism",
            "A41.9 - Sepsis, Unspecified Organism",
            "K35.80 - Unspecified Acute Appendicitis",
            "S36.115A - Unspecified Liver Injury, Major",
            "E11.69 - Type 2 Diabetes with Complications"
    });
    private final JRadioButton esi1Radio = new JRadioButton("ESI-1 Resus");
    private final JRadioButton esi2Radio = new JRadioButton("ESI-2 High Risk");
    private final JRadioButton esi3Radio = new JRadioButton("ESI-3 Urgent");
    private final JRadioButton esi4Radio = new JRadioButton("ESI-4 Stable");
    private final JRadioButton esi5Radio = new JRadioButton("ESI-5 Routine");
    private final ButtonGroup esiGroup = new ButtonGroup();

    private final JPanel comorbiditiesTagPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
    private final List<String> currentComorbidities = new ArrayList<>();
    private final JTextField newComorbidityField = new JTextField(14);
    private final JTextArea progressNotesArea = new JTextArea(4, 20);

    private final JTextField editSystolic = new JTextField(4);
    private final JTextField editDiastolic = new JTextField(4);
    private final JTextField editHr = new JTextField(4);
    private final JTextField editResp = new JTextField(4);
    private final JTextField editSpo2 = new JTextField(4);
    private final JTextField editTemp = new JTextField(4);
    private final JLabel baselineVitalsComparison = new JLabel();

    private final JPanel medsContainerPanel = new JPanel();
    private final JTextField attendingField = new JTextField();
    private final JTextField residentField = new JTextField("Dr. Marcus Thorne, MD (Fellow)");
    private final JTextField primaryRnField = new JTextField("RN Sarah Jenkins, BSN (Bedside Primary)");
    private final JTextField pharmacistField = new JTextField("PharmD Alexis Zhao, BCPS");

    private final JComboBox<String> auditReasonCombo = new JComboBox<>(new String[]{
            "Routine Attending Bedside Rounds",
            "Clinical Acuity Deterioration / SBAR Update",
            "Post-Intervention / Diagnostic Re-Evaluation",
            "IntelliVue Monitor Telemetry Synchronization",
            "Discharge Preparation & Order Reconciliation"
    });
    private final JPasswordField mdPinField = new JPasswordField("8492");

    public UpdateRecordsView(DashboardView.ViewNavigator navigator) {
        this.navigator = navigator;
        setLayout(new BorderLayout(0, 10));
        setBackground(MediCoreTheme.BG_STERILE);
        setBorder(new EmptyBorder(12, 14, 12, 14));
        HospitalDataStore.getInstance().addListener(this);

        esiGroup.add(esi1Radio); esiGroup.add(esi2Radio); esiGroup.add(esi3Radio);
        esiGroup.add(esi4Radio); esiGroup.add(esi5Radio);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.32);
        splitPane.setDividerLocation(380);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);

        splitPane.setLeftComponent(createLeftSnapshotPane());
        splitPane.setRightComponent(createRightClinicalForms());
        add(splitPane, BorderLayout.CENTER);
        add(createBottomApprovalBar(), BorderLayout.SOUTH);

        refreshPatientCombo();
        loadPatientData("MRN-84920");
    }
    private JPanel createLeftSnapshotPane() {
        JPanel p = UIUtils.createCardPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setPreferredSize(new Dimension(380, 0));

        JPanel selRow = new JPanel(new BorderLayout(6, 0));
        selRow.setOpaque(false);
        JLabel selLbl = new JLabel("Chart Patient:");
        selLbl.setFont(MediCoreTheme.FONT_BODY_BOLD);
        patientSelectorCombo.setFont(MediCoreTheme.FONT_SMALL);
        patientSelectorCombo.addActionListener(e -> {
            String sel = (String) patientSelectorCombo.getSelectedItem();
            if (sel != null && sel.contains("[")) {
                String mrn = sel.substring(sel.indexOf("[") + 1, sel.indexOf("]"));
                loadPatientData(mrn);
            }
        });
        selRow.add(selLbl, BorderLayout.WEST);
        selRow.add(patientSelectorCombo, BorderLayout.CENTER);
        p.add(selRow);
        p.add(Box.createVerticalStrut(8));

        JPanel ptHeader = new JPanel(new GridLayout(4, 1, 0, 2));
        ptHeader.setOpaque(false);
        ptNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        ptMrnLabel.setFont(MediCoreTheme.FONT_SMALL);
        ptMrnLabel.setForeground(MediCoreTheme.TEXT_MUTED);
        ptLocationLabel.setFont(MediCoreTheme.FONT_SUBHEADER);
        ptLocationLabel.setForeground(MediCoreTheme.PRIMARY_ACCENT);
        ptDemogLabel.setFont(MediCoreTheme.FONT_BODY);
        ptDemogLabel.setForeground(MediCoreTheme.TEXT_SECONDARY);

        ptHeader.add(ptNameLabel);
        ptHeader.add(ptMrnLabel);
        ptHeader.add(ptLocationLabel);
        ptHeader.add(ptDemogLabel);
        p.add(ptHeader);
        p.add(Box.createVerticalStrut(10));

        JLabel ecgHeader = new JLabel("LIVE CONTINUOUS CARDIAC TELEMETRY");
        ecgHeader.setFont(MediCoreTheme.FONT_SUBHEADER);
        p.add(ecgHeader);
        p.add(Box.createVerticalStrut(4));
        ecgPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 110));
        p.add(ecgPanel);
        p.add(Box.createVerticalStrut(10));

        JPanel vitalsReadout = new JPanel(new GridLayout(2, 2, 8, 6));
        vitalsReadout.setOpaque(false);
        vitalsReadout.setBorder(new CompoundBorder(new LineBorder(MediCoreTheme.BORDER_LIGHT, 1, true), new EmptyBorder(8, 10, 8, 10)));
        vitalsReadout.add(createVitalChip("HEART RATE", hrValueLbl, MediCoreTheme.ESI4_TEXT));
        vitalsReadout.add(createVitalChip("BLOOD PRESSURE", bpValueLbl, MediCoreTheme.PRIMARY_ACCENT));
        vitalsReadout.add(createVitalChip("PULSE OX (SpO2)", spo2ValueLbl, MediCoreTheme.ESI4_TEXT));
        vitalsReadout.add(createVitalChip("TEMPERATURE", tempValueLbl, MediCoreTheme.TEXT_PRIMARY));
        p.add(vitalsReadout);
        p.add(Box.createVerticalStrut(10));

        JLabel precTitle = new JLabel("ACTIVE CLINICAL PRECAUTIONS");
        precTitle.setFont(MediCoreTheme.FONT_SUBHEADER);
        p.add(precTitle);
        p.add(Box.createVerticalStrut(4));
        precautionsListPanel.setOpaque(false);
        p.add(precautionsListPanel);
        p.add(Box.createVerticalStrut(10));

        JLabel auditTitle = new JLabel("CHART AUDIT LOG & CLINICAL EVENTS");
        auditTitle.setFont(MediCoreTheme.FONT_SUBHEADER);
        p.add(auditTitle);
        p.add(Box.createVerticalStrut(4));

        auditHistoryPanel.setLayout(new BoxLayout(auditHistoryPanel, BoxLayout.Y_AXIS));
        auditHistoryPanel.setOpaque(false);
        JScrollPane auditScroll = new JScrollPane(auditHistoryPanel);
        auditScroll.setBorder(new LineBorder(MediCoreTheme.BORDER_LIGHT, 1));
        auditScroll.setPreferredSize(new Dimension(0, 95));
        p.add(auditScroll);

        return p;
    }

    private JPanel createVitalChip(String label, JLabel valLbl, Color fg) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        l.setForeground(MediCoreTheme.TEXT_MUTED);
        valLbl.setFont(MediCoreTheme.FONT_MONO_BOLD);
        valLbl.setForeground(fg);
        p.add(l, BorderLayout.NORTH);
        p.add(valLbl, BorderLayout.CENTER);
        return p;
    }
    private JPanel createRightClinicalForms() {
        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setOpaque(false);

        // Section A: ICD-10
        JPanel secA = UIUtils.createCardPanel();
        secA.setLayout(new BorderLayout(0, 8));
        secA.add(UIUtils.createSectionHeader("SECTION A: CLINICAL DIAGNOSIS & ICD-10 CODING", "Update primary ICD-10 diagnosis, ESI acuity score, and attending progress notes"), BorderLayout.NORTH);

        JPanel secAContent = new JPanel();
        secAContent.setLayout(new BoxLayout(secAContent, BoxLayout.Y_AXIS));
        secAContent.setOpaque(false);

        JPanel icdRow = new JPanel(new BorderLayout(8, 2));
        icdRow.setOpaque(false);
        JLabel icdLbl = new JLabel("Primary Clinical Diagnosis (ICD-10-CM):");
        icdLbl.setFont(MediCoreTheme.FONT_BODY_BOLD);
        icdRow.add(icdLbl, BorderLayout.NORTH);
        icdRow.add(icd10Combo, BorderLayout.CENTER);
        secAContent.add(icdRow);
        secAContent.add(Box.createVerticalStrut(8));

        JPanel acuityRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        acuityRow.setOpaque(false);
        acuityRow.add(new JLabel("Acuity Level: "));
        acuityRow.add(esi1Radio); acuityRow.add(esi2Radio); acuityRow.add(esi3Radio);
        acuityRow.add(esi4Radio); acuityRow.add(esi5Radio);
        secAContent.add(acuityRow);
        secAContent.add(Box.createVerticalStrut(8));

        JPanel comorbRow = new JPanel(new BorderLayout(6, 4));
        comorbRow.setOpaque(false);
        JPanel comorbHeader = new JPanel(new BorderLayout());
        comorbHeader.setOpaque(false);
        JLabel comorbLbl = new JLabel("Secondary Chronic Comorbidities:");
        comorbLbl.setFont(MediCoreTheme.FONT_BODY_BOLD);
        comorbHeader.add(comorbLbl, BorderLayout.WEST);

        JPanel addComorbBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        addComorbBar.setOpaque(false);
        newComorbidityField.setFont(MediCoreTheme.FONT_SMALL);
        JButton addComorbBtn = new JButton("+ Add Comorbidity");
        addComorbBtn.setFont(MediCoreTheme.FONT_SMALL);
        addComorbBtn.addActionListener(e -> {
            String text = newComorbidityField.getText().trim();
            if (!text.isEmpty()) {
                currentComorbidities.add(text);
                newComorbidityField.setText("");
                refreshComorbiditiesChips();
            }
        });
        addComorbBar.add(newComorbidityField);
        addComorbBar.add(addComorbBtn);
        comorbHeader.add(addComorbBar, BorderLayout.EAST);
        comorbRow.add(comorbHeader, BorderLayout.NORTH);

        comorbiditiesTagPanel.setOpaque(false);
        comorbRow.add(comorbiditiesTagPanel, BorderLayout.CENTER);
        secAContent.add(comorbRow);
        secAContent.add(Box.createVerticalStrut(8));

        JPanel progRow = new JPanel(new BorderLayout(0, 4));
        progRow.setOpaque(false);
        JLabel progLbl = new JLabel("Attending Physician Bedside Progress Narrative:");
        progLbl.setFont(MediCoreTheme.FONT_BODY_BOLD);
        progRow.add(progLbl, BorderLayout.NORTH);
        progressNotesArea.setFont(MediCoreTheme.FONT_BODY);
        progressNotesArea.setLineWrap(true);
        progressNotesArea.setWrapStyleWord(true);
        JScrollPane progScroll = new JScrollPane(progressNotesArea);
        progScroll.setPreferredSize(new Dimension(0, 75));
        progRow.add(progScroll, BorderLayout.CENTER);
        secAContent.add(progRow);

        secA.add(secAContent, BorderLayout.CENTER);
        main.add(secA);
        main.add(Box.createVerticalStrut(10));

        // Section B: Vitals
        JPanel secB = UIUtils.createCardPanel();
        secB.setLayout(new BorderLayout(0, 8));

        JPanel secBHeader = new JPanel(new BorderLayout());
        secBHeader.setOpaque(false);
        secBHeader.add(UIUtils.createSectionHeader("SECTION B: VITALS & NURSING OBSERVATIONS", "Bedside physiological parameters with telemetry baseline comparison"), BorderLayout.WEST);

        JButton syncMonitorBtn = new JButton("📡 Sync IntelliVue Monitor");
        syncMonitorBtn.setFont(MediCoreTheme.FONT_SMALL);
        syncMonitorBtn.setBackground(new Color(0xECFDF5));
        syncMonitorBtn.setForeground(MediCoreTheme.ESI4_TEXT);
        syncMonitorBtn.setBorder(new CompoundBorder(new LineBorder(MediCoreTheme.ESI4_BORDER, 1, true), new EmptyBorder(6, 10, 6, 10)));
        syncMonitorBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                    "Philips IntelliVue MX800 Gateway Synced!\nTelemetry Stream ID: PHIL-MX-301A\nCaptured 60-second real-time physiological snapshot.",
                    "IntelliVue Sync", JOptionPane.INFORMATION_MESSAGE);
        });
        secBHeader.add(syncMonitorBtn, BorderLayout.EAST);
        secB.add(secBHeader, BorderLayout.NORTH);

        JPanel vitalsInputs = new JPanel(new GridLayout(2, 6, 8, 6));
        vitalsInputs.setOpaque(false);
        vitalsInputs.add(new JLabel("Systolic BP:")); vitalsInputs.add(new JLabel("Diastolic BP:"));
        vitalsInputs.add(new JLabel("Pulse (HR):")); vitalsInputs.add(new JLabel("Resp Rate:"));
        vitalsInputs.add(new JLabel("SpO2 (%):")); vitalsInputs.add(new JLabel("Temp (°C):"));

        vitalsInputs.add(editSystolic); vitalsInputs.add(editDiastolic);
        vitalsInputs.add(editHr); vitalsInputs.add(editResp);
        vitalsInputs.add(editSpo2); vitalsInputs.add(editTemp);

        JPanel secBContent = new JPanel(new BorderLayout(0, 6));
        secBContent.setOpaque(false);
        secBContent.add(vitalsInputs, BorderLayout.CENTER);

        baselineVitalsComparison.setFont(MediCoreTheme.FONT_SMALL);
        baselineVitalsComparison.setForeground(MediCoreTheme.TEXT_MUTED);
        secBContent.add(baselineVitalsComparison, BorderLayout.SOUTH);
        secB.add(secBContent, BorderLayout.CENTER);
        main.add(secB);
        main.add(Box.createVerticalStrut(10));

        // Section C: Medications
        JPanel secC = UIUtils.createCardPanel();
        secC.setLayout(new BorderLayout(0, 8));

        JPanel secCHeader = new JPanel(new BorderLayout());
        secCHeader.setOpaque(false);
        secCHeader.add(UIUtils.createSectionHeader("SECTION C: ACTIVE MEDICATION & eMAR ADJUSTMENTS", "Pharmacologic dosing, routes, frequencies, and eMAR administration status"), BorderLayout.WEST);

        JButton addMedBtn = new JButton("+ Prescribe Medication");
        addMedBtn.setFont(MediCoreTheme.FONT_SMALL);
        addMedBtn.addActionListener(e -> addNewMedicationDialog());
        secCHeader.add(addMedBtn, BorderLayout.EAST);
        secC.add(secCHeader, BorderLayout.NORTH);

        medsContainerPanel.setLayout(new BoxLayout(medsContainerPanel, BoxLayout.Y_AXIS));
        medsContainerPanel.setOpaque(false);
        secC.add(medsContainerPanel, BorderLayout.CENTER);
        main.add(secC);
        main.add(Box.createVerticalStrut(10));

        // Section D: Care Team
        JPanel secD = UIUtils.createCardPanel();
        secD.setLayout(new BorderLayout(0, 8));
        secD.add(UIUtils.createSectionHeader("SECTION D: ATTENDING CARE TEAM ASSIGNMENT", "Multi-disciplinary rounding roster and responsible providers"), BorderLayout.NORTH);

        JPanel careGrid = new JPanel(new GridLayout(2, 2, 12, 8));
        careGrid.setOpaque(false);
        careGrid.add(createLabeledField("Primary Attending Physician:", attendingField));
        careGrid.add(createLabeledField("Fellow / Resident Physician:", residentField));
        careGrid.add(createLabeledField("Primary Assigned Registered Nurse:", primaryRnField));
        careGrid.add(createLabeledField("Clinical Pharmacist (eMAR Review):", pharmacistField));
        secD.add(careGrid, BorderLayout.CENTER);
        main.add(secD);

        JScrollPane scrollPane = new JScrollPane(main);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(scrollPane, BorderLayout.CENTER);
        return wrapper;
    }
    private JPanel createBottomApprovalBar() {
        JPanel p = UIUtils.createCardPanel();
        p.setLayout(new BorderLayout(12, 0));

        JPanel leftAudit = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftAudit.setOpaque(false);
        JLabel reasonLbl = new JLabel("Clinical Audit Justification:");
        reasonLbl.setFont(MediCoreTheme.FONT_BODY_BOLD);
        auditReasonCombo.setPreferredSize(new Dimension(280, 34));
        leftAudit.add(reasonLbl);
        leftAudit.add(auditReasonCombo);
        p.add(leftAudit, BorderLayout.WEST);

        JPanel rightPinAction = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPinAction.setOpaque(false);
        JLabel pinLbl = new JLabel("Digital MD PIN:");
        pinLbl.setFont(MediCoreTheme.FONT_BODY_BOLD);
        mdPinField.setPreferredSize(new Dimension(80, 34));

        JButton revertBtn = UIUtils.createSecondaryButton("Revert Changes");
        revertBtn.addActionListener(e -> {
            if (currentPatient != null) loadPatientData(currentPatient.getMrn());
        });

        JButton saveBtn = UIUtils.createPrimaryButton("💾 Save Changes to Chart", "Ctrl+S");
        saveBtn.setPreferredSize(new Dimension(220, 38));
        saveBtn.addActionListener(e -> executeSaveChart());

        rightPinAction.add(pinLbl);
        rightPinAction.add(mdPinField);
        rightPinAction.add(revertBtn);
        rightPinAction.add(saveBtn);
        p.add(rightPinAction, BorderLayout.EAST);
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

    private void refreshPatientCombo() {
        patientSelectorCombo.removeAllItems();
        List<Patient> list = HospitalDataStore.getInstance().getPatients();
        for (Patient p : list) {
            patientSelectorCombo.addItem(p.getFullName() + " [" + p.getMrn() + "] - " + p.getBed());
        }
    }

    public void selectPatient(String mrn) {
        loadPatientData(mrn);
    }

    private void loadPatientData(String mrn) {
        Patient pt = HospitalDataStore.getInstance().getPatientByMrn(mrn);
        if (pt == null) {
            List<Patient> list = HospitalDataStore.getInstance().getPatients();
            if (!list.isEmpty()) pt = list.get(0);
        }
        if (pt == null) return;

        this.currentPatient = pt;
        ptNameLabel.setText(pt.getFullName());
        ptMrnLabel.setText(pt.getMrn() + " • ADMITTED: " + pt.getLengthOfStay() + " AGO");
        ptLocationLabel.setText(pt.getWard() + " • " + pt.getBed());
        ptDemogLabel.setText(pt.getAge() + " yrs • " + pt.getBiologicalSex() + " • Blood: " + pt.getBloodGroup() + " • SSN: " + pt.getSsnMasked());

        VitalSigns v = pt.getVitals();
        ecgPanel.setHeartRate(v.getHeartRate());
        ecgPanel.setRhythmName(v.getEcgRhythm());
        hrValueLbl.setText(v.getHeartRate() + " BPM");
        bpValueLbl.setText(v.getBpFormatted());
        spo2ValueLbl.setText(v.getSpo2() + "%");
        tempValueLbl.setText(v.getTempFormatted());

        precautionsListPanel.removeAll();
        if (pt.getPrecautions() != null && !pt.getPrecautions().isEmpty()) {
            for (String prec : pt.getPrecautions()) {
                precautionsListPanel.add(UIUtils.createPillBadge(prec, MediCoreTheme.ESI2_TEXT, MediCoreTheme.ESI2_BG, MediCoreTheme.ESI2_BORDER));
            }
        } else {
            precautionsListPanel.add(UIUtils.createPillBadge("Standard Precautions", MediCoreTheme.ESI4_TEXT, MediCoreTheme.ESI4_BG, MediCoreTheme.ESI4_BORDER));
        }
        precautionsListPanel.revalidate();
        precautionsListPanel.repaint();

        auditHistoryPanel.removeAll();
        if (pt.getAuditLog() != null) {
            for (String a : pt.getAuditLog()) {
                JLabel logItem = new JLabel("• " + a);
                logItem.setFont(MediCoreTheme.FONT_SMALL);
                logItem.setForeground(MediCoreTheme.TEXT_SECONDARY);
                auditHistoryPanel.add(logItem);
                auditHistoryPanel.add(Box.createVerticalStrut(3));
            }
        }
        auditHistoryPanel.revalidate();
        auditHistoryPanel.repaint();

        boolean icdFound = false;
        for (int i = 0; i < icd10Combo.getItemCount(); i++) {
            if (icd10Combo.getItemAt(i).equalsIgnoreCase(pt.getDiagnosisIcd10())) {
                icd10Combo.setSelectedIndex(i);
                icdFound = true;
                break;
            }
        }
        if (!icdFound && pt.getDiagnosisIcd10() != null) {
            icd10Combo.addItem(pt.getDiagnosisIcd10());
            icd10Combo.setSelectedItem(pt.getDiagnosisIcd10());
        }

        switch (pt.getAcuityLevel()) {
            case 1: esi1Radio.setSelected(true); break;
            case 2: esi2Radio.setSelected(true); break;
            case 3: esi3Radio.setSelected(true); break;
            case 4: esi4Radio.setSelected(true); break;
            default: esi5Radio.setSelected(true); break;
        }

        currentComorbidities.clear();
        if (pt.getSecondaryComorbidities() != null) {
            currentComorbidities.addAll(pt.getSecondaryComorbidities());
        }
        refreshComorbiditiesChips();

        progressNotesArea.setText(pt.getClinicalProgressNotes() != null ? pt.getClinicalProgressNotes() : "");

        editSystolic.setText(String.valueOf(v.getSystolicBp()));
        editDiastolic.setText(String.valueOf(v.getDiastolicBp()));
        editHr.setText(String.valueOf(v.getHeartRate()));
        editResp.setText(String.valueOf(v.getRespiratoryRate()));
        editSpo2.setText(String.valueOf(v.getSpo2()));
        editTemp.setText(String.valueOf(v.getTemperatureC()));

        baselineVitalsComparison.setText("Admission Baseline: HR " + (v.getHeartRate() - 4) + " bpm | BP 140/90 mmHg | SpO2 96% | Philips IntelliVue Calibrated");
        refreshMedicationList(pt.getMedications());
        attendingField.setText(pt.getAttendingDoctor() != null ? pt.getAttendingDoctor() : "Dr. Robert Vance, MD");
    }

    private void refreshComorbiditiesChips() {
        comorbiditiesTagPanel.removeAll();
        for (String c : currentComorbidities) {
            JPanel chip = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
            chip.setOpaque(false);
            JLabel lbl = UIUtils.createPillBadge(c, MediCoreTheme.TEXT_PRIMARY, MediCoreTheme.BG_PANEL, MediCoreTheme.BORDER_DARK);
            JButton del = new JButton("✕");
            del.setFont(new Font("Segoe UI", Font.BOLD, 9));
            del.setContentAreaFilled(false);
            del.setBorderPainted(false);
            del.addActionListener(e -> {
                currentComorbidities.remove(c);
                refreshComorbiditiesChips();
            });
            chip.add(lbl);
            chip.add(del);
            comorbiditiesTagPanel.add(chip);
        }
        comorbiditiesTagPanel.revalidate();
        comorbiditiesTagPanel.repaint();
    }

    private void refreshMedicationList(List<Medication> meds) {
        medsContainerPanel.removeAll();
        if (meds != null) {
            for (Medication m : meds) {
                JPanel row = new JPanel(new BorderLayout(8, 0));
                row.setOpaque(false);
                row.setBorder(new CompoundBorder(new LineBorder(MediCoreTheme.BORDER_LIGHT, 1, true), new EmptyBorder(6, 10, 6, 10)));
                JLabel title = new JLabel(m.getName() + " " + m.getDosage() + " • " + m.getRoute() + " • " + m.getFrequency());
                title.setFont(MediCoreTheme.FONT_BODY_BOLD);
                JLabel status = UIUtils.createPillBadge(m.getStatus(),
                        m.getStatus().equalsIgnoreCase("Administered") ? MediCoreTheme.ESI4_TEXT : MediCoreTheme.ESI3_TEXT,
                        m.getStatus().equalsIgnoreCase("Administered") ? MediCoreTheme.ESI4_BG : MediCoreTheme.ESI3_BG,
                        m.getStatus().equalsIgnoreCase("Administered") ? MediCoreTheme.ESI4_BORDER : MediCoreTheme.ESI3_BORDER);
                row.add(title, BorderLayout.WEST);
                row.add(status, BorderLayout.EAST);
                medsContainerPanel.add(row);
                medsContainerPanel.add(Box.createVerticalStrut(4));
            }
        }
        medsContainerPanel.revalidate();
        medsContainerPanel.repaint();
    }

    private void addNewMedicationDialog() {
        JTextField nameF = new JTextField();
        JTextField doseF = new JTextField();
        JComboBox<String> routeC = new JComboBox<>(new String[]{"PO", "IV", "SubQ", "IM", "Inhalation"});
        JComboBox<String> freqC = new JComboBox<>(new String[]{"QD", "BID", "TID", "Q4H PRN", "Q6H", "STAT"});

        JPanel p = new JPanel(new GridLayout(4, 2, 8, 8));
        p.add(new JLabel("Medication Name:")); p.add(nameF);
        p.add(new JLabel("Dosage (mg/mL):")); p.add(doseF);
        p.add(new JLabel("Route:")); p.add(routeC);
        p.add(new JLabel("Frequency:")); p.add(freqC);

        int res = JOptionPane.showConfirmDialog(this, p, "Prescribe New Medication", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION && !nameF.getText().trim().isEmpty()) {
            Medication newMed = new Medication(nameF.getText().trim(), doseF.getText().trim(), (String) routeC.getSelectedItem(), (String) freqC.getSelectedItem(), "Prescribed Order", "Due", "Scheduled");
            if (currentPatient != null) {
                currentPatient.getMedications().add(newMed);
                refreshMedicationList(currentPatient.getMedications());
            }
        }
    }

    private void executeSaveChart() {
        if (currentPatient == null) return;
        String pin = new String(mdPinField.getPassword()).trim();
        if (pin.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Mandatory Digital MD PIN required to authorize chart modification.", "Security Signature Required", JOptionPane.WARNING_MESSAGE);
            mdPinField.requestFocus();
            return;
        }

        currentPatient.setDiagnosisIcd10((String) icd10Combo.getSelectedItem());
        int acuity = 3;
        if (esi1Radio.isSelected()) acuity = 1;
        else if (esi2Radio.isSelected()) acuity = 2;
        else if (esi3Radio.isSelected()) acuity = 3;
        else if (esi4Radio.isSelected()) acuity = 4;
        else if (esi5Radio.isSelected()) acuity = 5;
        currentPatient.setAcuityLevel(acuity);

        currentPatient.setSecondaryComorbidities(new ArrayList<>(currentComorbidities));
        currentPatient.setClinicalProgressNotes(progressNotesArea.getText().trim());

        try {
            int sys = Integer.parseInt(editSystolic.getText().trim());
            int dia = Integer.parseInt(editDiastolic.getText().trim());
            int hr = Integer.parseInt(editHr.getText().trim());
            int resp = Integer.parseInt(editResp.getText().trim());
            int spo2 = Integer.parseInt(editSpo2.getText().trim());
            double temp = Double.parseDouble(editTemp.getText().trim());

            VitalSigns v = currentPatient.getVitals();
            v.setSystolicBp(sys); v.setDiastolicBp(dia);
            v.setHeartRate(hr); v.setRespiratoryRate(resp);
            v.setSpo2(spo2); v.setTemperatureC(temp);
            ecgPanel.setHeartRate(hr);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid vital sign numeric values entered.", "Format Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        currentPatient.setAttendingDoctor(attendingField.getText().trim());
        String justification = (String) auditReasonCombo.getSelectedItem();
        currentPatient.addAuditEntry("Chart modified (" + justification + ") signed with MD PIN ***" + pin.substring(Math.max(0, pin.length() - 1)));

        HospitalDataStore.getInstance().updatePatient(currentPatient);

        JOptionPane.showMessageDialog(this,
                "Patient Chart Updated & Digitally Co-Signed!\n\n" +
                        "Patient: " + currentPatient.getFullName() + " (" + currentPatient.getMrn() + ")\n" +
                        "Diagnosis: " + currentPatient.getDiagnosisIcd10() + "\n" +
                        "Telemetry Vitals: " + currentPatient.getVitals().getSummary() + "\n" +
                        "Audit Justification: " + justification,
                "Chart Modification Confirmed", JOptionPane.INFORMATION_MESSAGE);

        loadPatientData(currentPatient.getMrn());
    }

    @Override
    public void onDataChanged() {
        SwingUtilities.invokeLater(() -> {
            refreshPatientCombo();
            if (currentPatient != null) {
                loadPatientData(currentPatient.getMrn());
            }
        });
    }
}
