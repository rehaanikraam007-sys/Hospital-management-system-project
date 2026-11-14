package com.medicore.views;

import com.medicore.dialogs.WristbandDialog;
import com.medicore.model.Bed;
import com.medicore.model.HospitalDataStore;
import com.medicore.model.Patient;
import com.medicore.model.VitalSigns;
import com.medicore.theme.MediCoreTheme;
import com.medicore.theme.UIUtils;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

/**
 * Screen 3: Patient Registration & Inpatient Intake Form (HL7 ADT-A01 Registry Creation).
 */
public class PatientRegistrationView extends JPanel {
    private final DashboardView.ViewNavigator navigator;

    // Tab 1: Identity & Demographics
    private final JTextField nameField = new JTextField("Harrison Thorne");
    private final JTextField dobField = new JTextField("1976-08-14");
    private final JLabel ageLabel = new JLabel("Age: 50 years");
    private final JComboBox<String> sexCombo = new JComboBox<>(new String[]{"Male", "Female", "Other", "Unknown"});
    private final JTextField ssnField = new JTextField("***-**-8219");
    private final JTextField phoneField = new JTextField("(555) 749-1102");
    private final JTextField kinNameField = new JTextField("Laura Thorne");
    private final JTextField kinPhoneField = new JTextField("(555) 749-1103");
    private final JComboBox<String> kinRelationCombo = new JComboBox<>(new String[]{"Spouse", "Parent", "Sibling", "Child", "Guardian", "Other"});
    private final JComboBox<String> bloodGroupCombo = new JComboBox<>(new String[]{"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"});

    // Allergies & Safety
    private final JPanel allergyChipsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
    private final List<String> currentAllergies = new ArrayList<>();
    private final JTextField newAllergenField = new JTextField(12);

    // Tab 2: Triage & Vitals
    private final JTextArea chiefComplaintArea = new JTextArea(4, 30);
    private final JTextField bpSystolicField = new JTextField("134");
    private final JTextField bpDiastolicField = new JTextField("88");
    private final JTextField hrField = new JTextField("84");
    private final JTextField respField = new JTextField("18");
    private final JTextField spo2Field = new JTextField("97");
    private final JTextField tempField = new JTextField("37.2");
    private final JSlider painSlider = new JSlider(0, 10, 4);
    private final JComboBox<String> acuityCombo = new JComboBox<>(new String[]{
            "ESI-1: Resuscitation (STAT Red)",
            "ESI-2: Emergent / High Risk (Orange)",
            "ESI-3: Urgent / Caution (Yellow)",
            "ESI-4: Less Urgent / Stable (Green)",
            "ESI-5: Non-Urgent (Blue)"
    });

    // Tab 3: Insurance & Guarantor
    private final JTextField payerField = new JTextField("Blue Cross Blue Shield");
    private final JTextField policyField = new JTextField("BCBS-881920");
    private final JTextField groupField = new JTextField("GRP-4410");
    private final JComboBox<String> preauthCombo = new JComboBox<>(new String[]{"Pre-Authorized / Active", "Pending Verification", "Self-Pay / Emergency Waiver"});
    private final JTextField guarantorField = new JTextField("Harrison Thorne (Self)");

    // Tab 4: Bed & Attending Assignment
    private final JComboBox<String> departmentCombo = new JComboBox<>(new String[]{"Cardiology", "Internal Medicine", "Pulmonology", "Trauma / Surgery", "Neurology"});
    private final JComboBox<String> attendingCombo = new JComboBox<>(new String[]{
            "Dr. Eleanor Vance, MD (Cardiology)",
            "Dr. Robert Vance, MD (Cardiology)",
            "Dr. Raymond Holt, MD (Pulmonology)",
            "Dr. Sean Baker, MD (Critical Care)",
            "Dr. Karen Page, MD (Emergency)",
            "Dr. Michael Stone, MD (Internal Med)"
    });
    private final JComboBox<String> wardCombo = new JComboBox<>(new String[]{"Ward 3A", "ICU", "PACU", "Cardiology"});
    private final JComboBox<String> bedCombo = new JComboBox<>();
    private final JCheckBox fallRiskBox = new JCheckBox("Yellow Fall Risk Protocol", true);
    private final JCheckBox telemetryBox = new JCheckBox("Continuous Cardiac Telemetry Pack", true);
    private final JCheckBox isolationBox = new JCheckBox("Contact/Droplet Isolation", false);
    private final JCheckBox npoBox = new JCheckBox("NPO Strict (Surgical/Procedural)", false);

    private final JTabbedPane tabbedPane;

    public PatientRegistrationView(DashboardView.ViewNavigator navigator) {
        this.navigator = navigator;
        setLayout(new BorderLayout(0, 12));
        setBackground(MediCoreTheme.BG_STERILE);
        setBorder(new EmptyBorder(14, 16, 14, 16));

        // Header
        JPanel headerPanel = UIUtils.createCardPanel();
        headerPanel.setLayout(new BorderLayout());

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBlock.setOpaque(false);
        JLabel title = new JLabel("INPATIENT ADMISSION & REGISTRATION (HL7 ADT-A01)");
        title.setFont(MediCoreTheme.FONT_TITLE);
        title.setForeground(MediCoreTheme.PRIMARY_ACCENT);

        JLabel sub = new JLabel("Create Formal Inpatient Electronic Health Record, Assign Telemetry, and Generate Legal Medical Record Number");
        sub.setFont(MediCoreTheme.FONT_SMALL);
        sub.setForeground(MediCoreTheme.TEXT_SECONDARY);

        titleBlock.add(title);
        titleBlock.add(sub);
        headerPanel.add(titleBlock, BorderLayout.WEST);

        JLabel nodeBadge = UIUtils.createPillBadge("REGISTRY ENGINE ONLINE", MediCoreTheme.ESI4_TEXT, MediCoreTheme.ESI4_BG, MediCoreTheme.ESI4_BORDER);
        headerPanel.add(nodeBadge, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Center: Multi-Step Tabbed Pane
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(MediCoreTheme.FONT_SUBHEADER);

        tabbedPane.addTab("1. Demographics & Identity", createDemographicsTab());
        tabbedPane.addTab("2. Triage & Vitals Intake", createTriageTab());
        tabbedPane.addTab("3. Insurance & Guardian", createInsuranceTab());
        tabbedPane.addTab("4. Bed & Attending Assignment", createBedTab());

        add(tabbedPane, BorderLayout.CENTER);

        // Bottom Button Toolbar
        JPanel bottomToolbar = UIUtils.createCardPanel();
        bottomToolbar.setLayout(new BorderLayout());

        JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftActions.setOpaque(false);

        JButton clearBtn = UIUtils.createSecondaryButton("Clear Form");
        clearBtn.addActionListener(e -> clearForm());

        JButton cancelBtn = UIUtils.createSecondaryButton("Cancel (Esc)");
        cancelBtn.addActionListener(e -> {
            if (navigator != null) navigator.navigateTo("Dashboard", null);
        });

        leftActions.add(clearBtn);
        leftActions.add(cancelBtn);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightActions.setOpaque(false);

        JButton wristbandBtn = UIUtils.createSecondaryButton("🖨 Print Patient Wristband (Ctrl+P)");
        wristbandBtn.addActionListener(e -> showWristbandPreview());

        JButton saveBtn = UIUtils.createPrimaryButton("💾 Save & Generate MRN", "F5");
        saveBtn.setPreferredSize(new Dimension(220, 38));
        saveBtn.addActionListener(e -> executeSaveAndAdmit());

        rightActions.add(wristbandBtn);
        rightActions.add(saveBtn);

        bottomToolbar.add(leftActions, BorderLayout.WEST);
        bottomToolbar.add(rightActions, BorderLayout.EAST);

        add(bottomToolbar, BorderLayout.SOUTH);

        // Populate available beds
        populateAvailableBeds();
        wardCombo.addActionListener(e -> populateAvailableBeds());

        // Setup Allergies initial chips
        currentAllergies.add("Penicillin - Anaphylaxis");
        currentAllergies.add("Sulfa - Severe Rash");
        refreshAllergyChips();

        // DOB auto age calculation
        dobField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                updateAgeFromDob();
            }
        });
        updateAgeFromDob();
    }

    private JPanel createDemographicsTab() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setBackground(MediCoreTheme.BG_WHITE);
        p.setBorder(new EmptyBorder(16, 18, 16, 18));

        JPanel formGrid = new JPanel(new GridLayout(6, 2, 16, 10));
        formGrid.setOpaque(false);

        // Row 1: Full Name & DOB
        JPanel namePanel = new JPanel(new BorderLayout(4, 2));
        namePanel.setOpaque(false);
        JLabel nameLbl = new JLabel("Full Legal Name (Verified Gov ID ✓):");
        nameLbl.setFont(MediCoreTheme.FONT_BODY_BOLD);
        namePanel.add(nameLbl, BorderLayout.NORTH);
        namePanel.add(nameField, BorderLayout.CENTER);
        formGrid.add(namePanel);

        JPanel dobPanel = new JPanel(new BorderLayout(4, 2));
        dobPanel.setOpaque(false);
        JPanel dobHeader = new JPanel(new BorderLayout());
        dobHeader.setOpaque(false);
        JLabel dobLbl = new JLabel("Date of Birth (YYYY-MM-DD):");
        dobLbl.setFont(MediCoreTheme.FONT_BODY_BOLD);
        dobHeader.add(dobLbl, BorderLayout.WEST);
        ageLabel.setFont(MediCoreTheme.FONT_BODY_BOLD);
        ageLabel.setForeground(MediCoreTheme.PRIMARY_ACCENT);
        dobHeader.add(ageLabel, BorderLayout.EAST);
        dobPanel.add(dobHeader, BorderLayout.NORTH);
        dobPanel.add(dobField, BorderLayout.CENTER);
        formGrid.add(dobPanel);

        // Row 2: Biological Sex & SSN
        JPanel sexPanel = new JPanel(new BorderLayout(4, 2));
        sexPanel.setOpaque(false);
        JLabel sexLbl = new JLabel("Biological Sex:");
        sexLbl.setFont(MediCoreTheme.FONT_BODY_BOLD);
        sexPanel.add(sexLbl, BorderLayout.NORTH);
        sexPanel.add(sexCombo, BorderLayout.CENTER);
        formGrid.add(sexPanel);

        JPanel ssnPanel = new JPanel(new BorderLayout(4, 2));
        ssnPanel.setOpaque(false);
        JLabel ssnLbl = new JLabel("SSN / National Clinical Identifier (Masked):");
        ssnLbl.setFont(MediCoreTheme.FONT_BODY_BOLD);
        ssnPanel.add(ssnLbl, BorderLayout.NORTH);
        ssnPanel.add(ssnField, BorderLayout.CENTER);
        formGrid.add(ssnPanel);

        // Row 3: Primary Phone & Blood Group
        JPanel phonePanel = new JPanel(new BorderLayout(4, 2));
        phonePanel.setOpaque(false);
        JLabel phoneLbl = new JLabel("Primary Mobile Phone:");
        phoneLbl.setFont(MediCoreTheme.FONT_BODY_BOLD);
        phonePanel.add(phoneLbl, BorderLayout.NORTH);
        phonePanel.add(phoneField, BorderLayout.CENTER);
        formGrid.add(phonePanel);

        JPanel bloodPanel = new JPanel(new BorderLayout(4, 2));
        bloodPanel.setOpaque(false);
        JLabel bloodLbl = new JLabel("Blood Group (ABO/Rh):");
        bloodLbl.setFont(MediCoreTheme.FONT_BODY_BOLD);
        bloodPanel.add(bloodLbl, BorderLayout.NORTH);
        bloodPanel.add(bloodGroupCombo, BorderLayout.CENTER);
        formGrid.add(bloodPanel);

        // Row 4: Next of Kin Name & Phone
        JPanel kinNamePanel = new JPanel(new BorderLayout(4, 2));
        kinNamePanel.setOpaque(false);
        JLabel kinNameLbl = new JLabel("Next of Kin / Emergency Contact Name:");
        kinNameLbl.setFont(MediCoreTheme.FONT_BODY_BOLD);
        kinNamePanel.add(kinNameLbl, BorderLayout.NORTH);
        kinNamePanel.add(kinNameField, BorderLayout.CENTER);
        formGrid.add(kinNamePanel);

        JPanel kinPhonePanel = new JPanel(new BorderLayout(4, 2));
        kinPhonePanel.setOpaque(false);
        JLabel kinPhoneLbl = new JLabel("Emergency Contact Mobile:");
        kinPhoneLbl.setFont(MediCoreTheme.FONT_BODY_BOLD);
        kinPhonePanel.add(kinPhoneLbl, BorderLayout.NORTH);
        kinPhonePanel.add(kinPhoneField, BorderLayout.CENTER);
        formGrid.add(kinPhonePanel);

        // Row 5: Emergency Relation
        JPanel kinRelPanel = new JPanel(new BorderLayout(4, 2));
        kinRelPanel.setOpaque(false);
        JLabel kinRelLbl = new JLabel("Relationship to Patient:");
        kinRelLbl.setFont(MediCoreTheme.FONT_BODY_BOLD);
        kinRelPanel.add(kinRelLbl, BorderLayout.NORTH);
        kinRelPanel.add(kinRelationCombo, BorderLayout.CENTER);
        formGrid.add(kinRelPanel);

        JPanel certPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 16));
        certPanel.setOpaque(false);
        certPanel.add(UIUtils.createPillBadge("✓ Photo ID Verified", MediCoreTheme.ESI4_TEXT, MediCoreTheme.ESI4_BG, MediCoreTheme.ESI4_BORDER));
        certPanel.add(UIUtils.createPillBadge("✓ HIPAA Consent Signed", MediCoreTheme.PRIMARY_ACCENT, MediCoreTheme.PRIMARY_LIGHT, MediCoreTheme.PRIMARY_ACCENT));
        formGrid.add(certPanel);

        p.add(formGrid, BorderLayout.NORTH);

        // Section 2: Known Allergies & Adverse Reactions (Critical Safety Exclusion)
        JPanel allergySection = new JPanel(new BorderLayout(0, 6));
        allergySection.setOpaque(false);
        allergySection.setBorder(new CompoundBorder(
                new LineBorder(MediCoreTheme.ESI1_BORDER, 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));
        allergySection.setBackground(MediCoreTheme.ESI1_BG);

        JPanel allergyHeader = new JPanel(new BorderLayout());
        allergyHeader.setOpaque(false);
        JLabel allergyTitle = new JLabel("CRITICAL SAFETY EXCLUSION: KNOWN ALLERGIES & ADVERSE REACTIONS");
        allergyTitle.setFont(MediCoreTheme.FONT_BODY_BOLD);
        allergyTitle.setForeground(MediCoreTheme.ESI1_TEXT);
        allergyHeader.add(allergyTitle, BorderLayout.WEST);

        JPanel allergyInputRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        allergyInputRow.setOpaque(false);

        newAllergenField.setFont(MediCoreTheme.FONT_SMALL);
        JButton addAllergyBtn = new JButton("+ Add Allergen Tag");
        addAllergyBtn.setFont(MediCoreTheme.FONT_SMALL);
        addAllergyBtn.addActionListener(e -> {
            String text = newAllergenField.getText().trim();
            if (!text.isEmpty()) {
                currentAllergies.remove("NKDA (No Known Drug Allergies)");
                currentAllergies.add(text);
                newAllergenField.setText("");
                refreshAllergyChips();
            }
        });

        JButton nkdaBtn = new JButton("Mark NKDA");
        nkdaBtn.setFont(MediCoreTheme.FONT_SMALL);
        nkdaBtn.addActionListener(e -> {
            currentAllergies.clear();
            currentAllergies.add("NKDA (No Known Drug Allergies)");
            refreshAllergyChips();
        });

        allergyInputRow.add(new JLabel("New Allergen:"));
        allergyInputRow.add(newAllergenField);
        allergyInputRow.add(addAllergyBtn);
        allergyInputRow.add(nkdaBtn);
        allergyHeader.add(allergyInputRow, BorderLayout.EAST);

        allergySection.add(allergyHeader, BorderLayout.NORTH);

        allergyChipsPanel.setOpaque(false);
        allergySection.add(allergyChipsPanel, BorderLayout.CENTER);

        p.add(allergySection, BorderLayout.CENTER);

        return p;
    }

    private void refreshAllergyChips() {
        allergyChipsPanel.removeAll();
        for (String allergy : currentAllergies) {
            JPanel chip = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
            chip.setOpaque(false);

            boolean isNkda = allergy.contains("NKDA");
            JLabel tag = UIUtils.createPillBadge(allergy,
                    isNkda ? MediCoreTheme.ESI4_TEXT : MediCoreTheme.ESI1_TEXT,
                    isNkda ? MediCoreTheme.ESI4_BG : MediCoreTheme.ESI1_BG,
                    isNkda ? MediCoreTheme.ESI4_BORDER : MediCoreTheme.ESI1_BORDER);

            JButton removeBtn = new JButton("✕");
            removeBtn.setFont(new Font("Segoe UI", Font.BOLD, 9));
            removeBtn.setMargin(new Insets(0, 2, 0, 2));
            removeBtn.setContentAreaFilled(false);
            removeBtn.setBorderPainted(false);
            removeBtn.setForeground(isNkda ? MediCoreTheme.ESI4_TEXT : MediCoreTheme.ESI1_TEXT);
            removeBtn.addActionListener(e -> {
                currentAllergies.remove(allergy);
                refreshAllergyChips();
            });

            chip.add(tag);
            chip.add(removeBtn);
            allergyChipsPanel.add(chip);
        }
        allergyChipsPanel.revalidate();
        allergyChipsPanel.repaint();
    }

    private JPanel createTriageTab() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setBackground(MediCoreTheme.BG_WHITE);
        p.setBorder(new EmptyBorder(16, 18, 16, 18));

        // Chief Complaint Narrative
        JPanel narrativePanel = new JPanel(new BorderLayout(0, 4));
        narrativePanel.setOpaque(false);
        JLabel ccLbl = new JLabel("Initial Chief Complaint & Acute Presenting Symptoms (Nursing Narrative):");
        ccLbl.setFont(MediCoreTheme.FONT_BODY_BOLD);
        narrativePanel.add(ccLbl, BorderLayout.NORTH);

        chiefComplaintArea.setText("Patient presents with severe acute substernal chest tightness radiating to the left arm and back, accompanied by nausea and mild diaphoresis for approximately 2 hours.");
        chiefComplaintArea.setFont(MediCoreTheme.FONT_BODY);
        chiefComplaintArea.setLineWrap(true);
        chiefComplaintArea.setWrapStyleWord(true);
        JScrollPane ccScroll = new JScrollPane(chiefComplaintArea);
        ccScroll.setPreferredSize(new Dimension(0, 90));
        narrativePanel.add(ccScroll, BorderLayout.CENTER);

        p.add(narrativePanel, BorderLayout.NORTH);

        // Vitals Grid
        JPanel vitalsPanel = new JPanel(new GridLayout(4, 2, 16, 10));
        vitalsPanel.setOpaque(false);

        vitalsPanel.add(createLabeledField("Systolic Blood Pressure (mmHg):", bpSystolicField));
        vitalsPanel.add(createLabeledField("Diastolic Blood Pressure (mmHg):", bpDiastolicField));
        vitalsPanel.add(createLabeledField("Heart Rate (Pulse bpm):", hrField));
        vitalsPanel.add(createLabeledField("Respiratory Rate (Breaths/min):", respField));
        vitalsPanel.add(createLabeledField("Oxygen Saturation (SpO2 %):", spo2Field));
        vitalsPanel.add(createLabeledField("Body Temperature (°C):", tempField));

        // Pain scale slider
        JPanel painPanel = new JPanel(new BorderLayout(4, 2));
        painPanel.setOpaque(false);
        JLabel painLbl = new JLabel("Pain Assessment Scale (0 - 10 Numeric):");
        painLbl.setFont(MediCoreTheme.FONT_BODY_BOLD);
        painSlider.setMajorTickSpacing(2);
        painSlider.setMinorTickSpacing(1);
        painSlider.setPaintTicks(true);
        painSlider.setPaintLabels(true);
        painPanel.add(painLbl, BorderLayout.NORTH);
        painPanel.add(painSlider, BorderLayout.CENTER);
        vitalsPanel.add(painPanel);

        // Acuity Level
        JPanel acuityPanel = new JPanel(new BorderLayout(4, 2));
        acuityPanel.setOpaque(false);
        JLabel acuityLbl = new JLabel("Initial Triage Acuity Assessment:");
        acuityLbl.setFont(MediCoreTheme.FONT_BODY_BOLD);
        acuityCombo.setSelectedIndex(1); // default ESI-2
        acuityPanel.add(acuityLbl, BorderLayout.NORTH);
        acuityPanel.add(acuityCombo, BorderLayout.CENTER);
        vitalsPanel.add(acuityPanel);

        p.add(vitalsPanel, BorderLayout.CENTER);

        return p;
    }

    private JPanel createInsuranceTab() {
        JPanel p = new JPanel(new GridLayout(5, 2, 16, 14));
        p.setBackground(MediCoreTheme.BG_WHITE);
        p.setBorder(new EmptyBorder(18, 18, 18, 18));

        p.add(createLabeledField("Primary Health Insurance Carrier:", payerField));
        p.add(createLabeledField("Policy / Member ID #:", policyField));
        p.add(createLabeledField("Group Number:", groupField));
        p.add(createLabeledField("Prior-Authorization Verification Status:", preauthCombo));
        p.add(createLabeledField("Guarantor / Financial Responsible Party:", guarantorField));

        JPanel notice = new JPanel(new FlowLayout(FlowLayout.LEFT));
        notice.setOpaque(false);
        notice.add(UIUtils.createPillBadge("✓ Real-Time Eligibility (270/271 EDI Valid)", MediCoreTheme.ESI4_TEXT, MediCoreTheme.ESI4_BG, MediCoreTheme.ESI4_BORDER));
        p.add(notice);

        return p;
    }

    private JPanel createBedTab() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setBackground(MediCoreTheme.BG_WHITE);
        p.setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel assignGrid = new JPanel(new GridLayout(4, 2, 16, 12));
        assignGrid.setOpaque(false);

        assignGrid.add(createLabeledField("Admitting Department / Service:", departmentCombo));
        assignGrid.add(createLabeledField("Assigned Attending Physician:", attendingCombo));
        assignGrid.add(createLabeledField("Destination Ward / Unit:", wardCombo));
        assignGrid.add(createLabeledField("Target Bed Assignment (Clean / Ready):", bedCombo));

        p.add(assignGrid, BorderLayout.NORTH);

        // Safeguards and Precautions
        JPanel precPanel = new JPanel(new BorderLayout(0, 8));
        precPanel.setOpaque(false);
        JLabel precLbl = new JLabel("CLINICAL SAFETY PRECAUTIONS & TELEMETRY PROTOCOLS");
        precLbl.setFont(MediCoreTheme.FONT_BODY_BOLD);
        precLbl.setForeground(MediCoreTheme.TEXT_PRIMARY);
        precPanel.add(precLbl, BorderLayout.NORTH);

        JPanel checkGrid = new JPanel(new GridLayout(2, 2, 10, 8));
        checkGrid.setOpaque(false);
        checkGrid.add(fallRiskBox);
        checkGrid.add(telemetryBox);
        checkGrid.add(isolationBox);
        checkGrid.add(npoBox);
        precPanel.add(checkGrid, BorderLayout.CENTER);

        p.add(precPanel, BorderLayout.CENTER);

        return p;
    }

    private JPanel createLabeledField(String label, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(4, 2));
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(MediCoreTheme.FONT_BODY_BOLD);
        l.setForeground(MediCoreTheme.TEXT_PRIMARY);
        p.add(l, BorderLayout.NORTH);
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    private void populateAvailableBeds() {
        bedCombo.removeAllItems();
        String selectedWard = (String) wardCombo.getSelectedItem();
        List<Bed> beds = HospitalDataStore.getInstance().getBeds();

        boolean found = false;
        for (Bed b : beds) {
            if (b.getWard().equalsIgnoreCase(selectedWard) && b.getStatus() == Bed.Status.AVAILABLE) {
                bedCombo.addItem(b.getBedId());
                found = true;
            }
        }

        if (!found) {
            bedCombo.addItem("Bed " + selectedWard.substring(0, Math.min(3, selectedWard.length())) + "-99 (Overflow)");
        }
    }

    private void updateAgeFromDob() {
        try {
            LocalDate dob = LocalDate.parse(dobField.getText().trim());
            int age = Period.between(dob, LocalDate.now()).getYears();
            ageLabel.setText("Age: " + age + " years");
        } catch (Exception e) {
            ageLabel.setText("Age: -- years");
        }
    }

    private void clearForm() {
        nameField.setText("");
        dobField.setText("1980-01-01");
        ssnField.setText("***-**-0000");
        phoneField.setText("");
        kinNameField.setText("");
        kinPhoneField.setText("");
        chiefComplaintArea.setText("");
        currentAllergies.clear();
        currentAllergies.add("NKDA (No Known Drug Allergies)");
        refreshAllergyChips();
        updateAgeFromDob();
    }

    private void showWristbandPreview() {
        Patient temp = buildPatientFromForm("PREVIEW-00000");
        Frame topFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        new WristbandDialog(topFrame, temp).setVisible(true);
    }

    private Patient buildPatientFromForm(String mrn) {
        int age = 45;
        try {
            LocalDate dob = LocalDate.parse(dobField.getText().trim());
            age = Period.between(dob, LocalDate.now()).getYears();
        } catch (Exception ignored) {}

        int acuity = 3;
        String ac = (String) acuityCombo.getSelectedItem();
        if (ac != null) {
            if (ac.contains("ESI-1")) acuity = 1;
            else if (ac.contains("ESI-2")) acuity = 2;
            else if (ac.contains("ESI-3")) acuity = 3;
            else if (ac.contains("ESI-4")) acuity = 4;
            else acuity = 5;
        }

        Patient p = new Patient(
                mrn,
                nameField.getText().trim(),
                dobField.getText().trim(),
                age,
                (String) sexCombo.getSelectedItem(),
                ssnField.getText().trim(),
                phoneField.getText().trim(),
                kinNameField.getText().trim(),
                kinPhoneField.getText().trim(),
                (String) kinRelationCombo.getSelectedItem(),
                (String) bloodGroupCombo.getSelectedItem(),
                java.time.LocalDateTime.now(),
                (String) wardCombo.getSelectedItem(),
                (String) bedCombo.getSelectedItem(),
                (String) attendingCombo.getSelectedItem(),
                (String) departmentCombo.getSelectedItem(),
                acuity,
                chiefComplaintArea.getText().trim(),
                "R07.9 - Chest Pain, Unspecified",
                "Patient formally admitted via Inpatient Triage.",
                payerField.getText().trim(),
                policyField.getText().trim()
        );

        int sys = 120, dia = 80, hr = 75, resp = 16, spo2 = 98;
        double temp = 37.0;
        try { sys = Integer.parseInt(bpSystolicField.getText().trim()); } catch (Exception ignored) {}
        try { dia = Integer.parseInt(bpDiastolicField.getText().trim()); } catch (Exception ignored) {}
        try { hr = Integer.parseInt(hrField.getText().trim()); } catch (Exception ignored) {}
        try { resp = Integer.parseInt(respField.getText().trim()); } catch (Exception ignored) {}
        try { spo2 = Integer.parseInt(spo2Field.getText().trim()); } catch (Exception ignored) {}
        try { temp = Double.parseDouble(tempField.getText().trim()); } catch (Exception ignored) {}

        p.setVitals(new VitalSigns(sys, dia, hr, resp, spo2, temp, painSlider.getValue(), "Normal Sinus Rhythm"));
        p.getAllergies().addAll(currentAllergies);

        if (fallRiskBox.isSelected()) p.getPrecautions().add("Fall Risk");
        if (telemetryBox.isSelected()) p.getPrecautions().add("Continuous Cardiac Telemetry");
        if (isolationBox.isSelected()) p.getPrecautions().add("Strict Isolation");
        if (npoBox.isSelected()) p.getPrecautions().add("NPO After Midnight");

        p.addAuditEntry("Admitted via Inpatient ADT-A01 registry by Dr. Eleanor Vance, MD");
        return p;
    }

    private void executeSaveAndAdmit() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the Patient's Full Legal Name.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            tabbedPane.setSelectedIndex(0);
            nameField.requestFocus();
            return;
        }

        String mrn = HospitalDataStore.getInstance().generateNextMrn();
        Patient newPt = buildPatientFromForm(mrn);
        HospitalDataStore.getInstance().registerPatient(newPt);

        int choice = JOptionPane.showOptionDialog(this,
                "HL7 ADT-A01 Admission Message Transmitted Successfully!\n\n" +
                        "Generated MRN: " + mrn + "\n" +
                        "Patient: " + newPt.getFullName() + "\n" +
                        "Unit / Bed: " + newPt.getWard() + " - " + newPt.getBed() + "\n" +
                        "Acuity: ESI-" + newPt.getAcuityLevel() + "\n\n" +
                        "Would you like to print the patient wristband now?",
                "Patient Inpatient Admission Confirmed",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new String[]{"Print Wristband Now", "Go to Bedside Chart"},
                "Print Wristband Now"
        );

        if (choice == JOptionPane.YES_OPTION) {
            Frame topFrame = (Frame) SwingUtilities.getWindowAncestor(this);
            new WristbandDialog(topFrame, newPt).setVisible(true);
        }

        if (navigator != null) {
            navigator.navigateTo("Update Records", mrn);
        }
    }
}
