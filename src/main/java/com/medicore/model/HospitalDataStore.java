package com.medicore.model;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe clinical data repository holding active hospital census, beds, and telemetry.
 */
public class HospitalDataStore {
    private static final HospitalDataStore INSTANCE = new HospitalDataStore();

    public static HospitalDataStore getInstance() {
        return INSTANCE;
    }

    public interface DataChangeListener {
        void onDataChanged();
    }

    private final List<Patient> patients = new CopyOnWriteArrayList<>();
    private final List<Bed> beds = new CopyOnWriteArrayList<>();
    private final List<DataChangeListener> listeners = new CopyOnWriteArrayList<>();
    private final AtomicInteger mrnCounter = new AtomicInteger(95420);
    private ShiftHandoff currentShiftHandoff = new ShiftHandoff();
    private StaffUser currentStaff = new StaffUser("MD-84920", "Dr. Eleanor Vance, MD", "Attending Physician", "Cardiology", "Cardiology / Ward 3A", "Morning 07:00–15:30");

    private HospitalDataStore() {
        seedInitialData();
    }

    public void addListener(DataChangeListener listener) {
        if (!listeners.contains(listener)) listeners.add(listener);
    }

    public void removeListener(DataChangeListener listener) {
        listeners.remove(listener);
    }

    public void notifyListeners() {
        for (DataChangeListener listener : listeners) {
            try {
                listener.onDataChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public StaffUser getCurrentStaff() { return currentStaff; }
    public void setCurrentStaff(StaffUser staff) { this.currentStaff = staff; }

    public ShiftHandoff getCurrentShiftHandoff() { return currentShiftHandoff; }
    public void setCurrentShiftHandoff(ShiftHandoff handoff) { 
        this.currentShiftHandoff = handoff; 
        notifyListeners();
    }

    public List<Patient> getPatients() {
        return Collections.unmodifiableList(patients);
    }

    public List<Bed> getBeds() {
        return Collections.unmodifiableList(beds);
    }

    public Patient getPatientByMrn(String mrn) {
        if (mrn == null) return null;
        for (Patient p : patients) {
            if (p.getMrn().equalsIgnoreCase(mrn.trim())) {
                return p;
            }
        }
        return null;
    }

    public Bed getBedById(String bedId) {
        if (bedId == null) return null;
        for (Bed b : beds) {
            if (b.getBedId().equalsIgnoreCase(bedId.trim())) {
                return b;
            }
        }
        return null;
    }

    public synchronized String generateNextMrn() {
        return "MRN-" + mrnCounter.incrementAndGet();
    }

    public synchronized void registerPatient(Patient patient) {
        patients.add(0, patient);
        // If assigned bed, update bed status
        if (patient.getBed() != null && !patient.getBed().isEmpty() && !patient.getBed().equalsIgnoreCase("Unassigned")) {
            Bed bed = getBedById(patient.getBed());
            if (bed != null) {
                bed.setStatus(Bed.Status.OCCUPIED);
                bed.setPatientMrn(patient.getMrn());
                bed.setPatientName(patient.getFullName());
                bed.setAcuityLevel(patient.getAcuityLevel());
                bed.setTelemetryVitals(patient.getVitals().getSummary());
            }
        }
        notifyListeners();
    }

    public synchronized void updatePatient(Patient updated) {
        for (int i = 0; i < patients.size(); i++) {
            if (patients.get(i).getMrn().equalsIgnoreCase(updated.getMrn())) {
                patients.set(i, updated);
                // Update bed telemetry if applicable
                if (updated.getBed() != null) {
                    Bed b = getBedById(updated.getBed());
                    if (b != null) {
                        b.setTelemetryVitals(updated.getVitals().getSummary());
                        b.setAcuityLevel(updated.getAcuityLevel());
                    }
                }
                notifyListeners();
                return;
            }
        }
    }

    public synchronized boolean transferPatientBed(String patientMrn, String destBedId, String transferReason, String escortNurse, String porter) {
        Patient p = getPatientByMrn(patientMrn);
        Bed destBed = getBedById(destBedId);
        if (p == null || destBed == null) return false;

        String originBedId = p.getBed();
        Bed originBed = getBedById(originBedId);

        // Free origin bed and mark dirty / EVS
        if (originBed != null) {
            originBed.setStatus(Bed.Status.DIRTY);
            originBed.setPatientMrn(null);
            originBed.setPatientName(null);
            originBed.setTelemetryVitals("Bed Vacated - Pending EVS");
            originBed.setEvsTimerRemainingMinutes(25);
        }

        // Occupy destination bed
        destBed.setStatus(Bed.Status.OCCUPIED);
        destBed.setPatientMrn(p.getMrn());
        destBed.setPatientName(p.getFullName());
        destBed.setAcuityLevel(p.getAcuityLevel());
        destBed.setTelemetryVitals(p.getVitals().getSummary());

        p.setBed(destBed.getBedId());
        p.setWard(destBed.getWard());
        p.addAuditEntry("Transferred from " + originBedId + " to " + destBedId + " (" + transferReason + ") by " + escortNurse + " / " + porter);

        notifyListeners();
        return true;
    }

    public synchronized void setBedStatus(String bedId, Bed.Status status, int evsMinutes) {
        Bed b = getBedById(bedId);
        if (b != null) {
            b.setStatus(status);
            b.setEvsTimerRemainingMinutes(evsMinutes);
            if (status != Bed.Status.OCCUPIED) {
                b.setPatientMrn(null);
                b.setPatientName(null);
            }
            notifyListeners();
        }
    }

    // Pre-seed realistic clinical hospital census
    private void seedInitialData() {
        // Beds setup
        // Ward 3A
        beds.add(new Bed("Bed 301-A", "Ward 3A", Bed.Status.OCCUPIED, "MRN-84920", "Eleanor Vance", 0, true, "HR 78 | BP 138/86", 3));
        beds.add(new Bed("Bed 301-B", "Ward 3A", Bed.Status.AVAILABLE, null, null, 0, false, "Ready for Admission", 5));
        beds.add(new Bed("Bed 302-A", "Ward 3A", Bed.Status.AVAILABLE, null, null, 0, false, "Ready for Admission", 5));
        beds.add(new Bed("Bed 302-B", "Ward 3A", Bed.Status.OCCUPIED, "MRN-55829", "James Henderson", 0, true, "HR 72 | BP 124/76", 3));
        beds.add(new Bed("Bed 303-A", "Ward 3A", Bed.Status.OCCUPIED, "MRN-71932", "David Cho", 0, true, "HR 118! | BP 92/58", 2));
        beds.add(new Bed("Bed 303-B", "Ward 3A", Bed.Status.DISINFECTION, null, null, 14, false, "EVS Sanitizing (14m)", 5));
        beds.add(new Bed("Bed 304-A", "Ward 3A", Bed.Status.AVAILABLE, null, null, 0, false, "Ready for Admission", 5));
        beds.add(new Bed("Bed 304-B", "Ward 3A", Bed.Status.OCCUPIED, "MRN-64810", "Liam Gallagher", 0, true, "HR 82 | BP 120/80", 4));
        beds.add(new Bed("Bed 305-A", "Ward 3A", Bed.Status.OCCUPIED, "MRN-78104", "Arthur Pendelton", 0, true, "HR 86 | BP 110/68", 3));
        beds.add(new Bed("Bed 305-B", "Ward 3A", Bed.Status.DIRTY, null, null, 0, false, "Dirty - EVS Alert Sent", 5));

        // ICU
        beds.add(new Bed("ICU-01", "ICU", Bed.Status.OCCUPIED, "MRN-90214", "Marcus Bell", 0, true, "HR 132! | BP 82/48!", 1));
        beds.add(new Bed("ICU-02", "ICU", Bed.Status.AVAILABLE, null, null, 0, true, "Ready (Vent Standby)", 5));
        beds.add(new Bed("ICU-03", "ICU", Bed.Status.OCCUPIED, "MRN-33291", "Chloe Chen", 0, true, "HR 126! | BP 86/52!", 1));
        beds.add(new Bed("MICU-04-B", "ICU", Bed.Status.AVAILABLE, null, null, 0, true, "Clean / Telemetry Armed", 5));

        // PACU
        beds.add(new Bed("PACU-01", "PACU", Bed.Status.AVAILABLE, null, null, 0, true, "Post-Op Recovery Ready", 5));
        beds.add(new Bed("PACU-02", "PACU", Bed.Status.OCCUPIED, "MRN-18293", "Elena Rostova", 0, true, "HR 68 | BP 118/72", 3));
        beds.add(new Bed("PACU-03", "PACU", Bed.Status.AVAILABLE, null, null, 0, true, "Post-Op Recovery Ready", 5));

        // Cardiology
        beds.add(new Bed("Cardiology-201-A", "Cardiology", Bed.Status.OCCUPIED, "MRN-29401", "Evelyn Wright", 0, true, "HR 94 | BP 142/90", 2));
        beds.add(new Bed("Cardiology-201-B", "Cardiology", Bed.Status.DIRTY, null, null, 0, false, "Dirty - Needs Terminal Wash", 5));

        // Emergency
        beds.add(new Bed("ED-Bay-04", "Emergency", Bed.Status.OCCUPIED, "MRN-43912", "Sofia Ramirez", 0, true, "HR 104 | BP 112/74", 2));

        // Patients setup
        // 1. Eleanor Vance (Primary patient for bedside chart)
        Patient p1 = new Patient(
                "MRN-84920", "Eleanor Vance", "1964-04-12", 62, "Female",
                "***-**-6789", "(555) 234-8901", "David Vance", "(555) 234-8902", "Spouse",
                "O+", LocalDateTime.now().minusDays(4).minusHours(9),
                "Ward 3A", "Bed 301-A", "Dr. Robert Vance, MD", "Cardiology", 3,
                "Exertional dyspnea, 3-pillow orthopnea, bilateral lower-extremity pitting edema +3, fatigue.",
                "I50.9 - Heart Failure, Unspecified",
                "Patient resting in semi-Fowler position. Net fluid balance -1,450 mL over 24h following IV bumetanide/furosemide protocol. Lung bases demonstrate resolving crackles. Serial BNP trending down (980 -> 610 pg/mL). Continuous telemetry active.",
                "Blue Cross Blue Shield", "BCBS-9021948"
        );
        p1.setVitals(new VitalSigns(138, 86, 78, 16, 98, 37.1, 2, "Normal Sinus Rhythm"));
        p1.getAllergies().add("Penicillin - Anaphylaxis");
        p1.getAllergies().add("Sulfa - Severe Rash");
        p1.getPrecautions().add("Fall Risk (Yellow)");
        p1.getPrecautions().add("Fluid Restriction 1.5L/day");
        p1.getAlerts().add("Allergy: PCN");
        p1.getAlerts().add("Stat Lab: BNP Pending");
        p1.getAlerts().add("Telemetry Lead Check Due");
        p1.getSecondaryComorbidities().add("Essential Hypertension (I10)");
        p1.getSecondaryComorbidities().add("Coronary Artery Disease (I25.10)");
        p1.getSecondaryComorbidities().add("Type 2 Diabetes Mellitus (E11.9)");
        p1.getMedications().add(new Medication("Furosemide", "40 mg", "IV Push", "Daily Morning", "Volume Overload", "Administered", "08:00"));
        p1.getMedications().add(new Medication("Metoprolol Succinate", "25 mg", "PO", "BID", "Rate Control / CHF", "Administered", "08:30"));
        p1.getMedications().add(new Medication("Sacubitril / Valsartan", "49/51 mg", "PO", "BID", "ARNI Heart Failure", "Due (15:00)", "Pending"));
        p1.getMedications().add(new Medication("Enoxaparin Sodium", "40 mg", "SubQ", "QD", "VTE Prophylaxis", "Due (20:00)", "Pending"));
        p1.addAuditEntry("Bedside rounding completed by Dr. Robert Vance, MD (22m ago)");
        p1.addAuditEntry("Telemetry monitor IntelliVue synchronized (1h ago)");
        p1.addAuditEntry("Admitted via Emergency Department (4d 9h ago)");
        patients.add(p1);

        // 2. David Cho (High risk ESI-2 transfer candidate)
        Patient p2 = new Patient(
                "MRN-71932", "David Cho", "1972-11-03", 54, "Male",
                "***-**-4192", "(555) 782-1094", "Grace Cho", "(555) 782-1095", "Spouse",
                "A+", LocalDateTime.now().minusDays(1).minusHours(14),
                "Ward 3A", "Bed 303-A", "Dr. Raymond Holt, MD", "Pulmonology", 2,
                "Severe COPD exacerbation, wheezing, diaphoresis, resting tachycardia.",
                "J44.1 - COPD with (Acute) Exacerbation",
                "Patient presenting tachypneic at 26 bpm. Telemetry alarming for sinus tachycardia 118 bpm. Arterial blood gas shows PaCO2 56, PaO2 62 on 4L nasal cannula. Escalate care: transfer to MICU Bed 04-B recommended.",
                "Aetna Premier", "AET-8830192"
        );
        p2.setVitals(new VitalSigns(92, 58, 118, 26, 91, 38.3, 5, "Sinus Tachycardia"));
        p2.getAllergies().add("Latex - Contact Dermatitis");
        p2.getPrecautions().add("Continuous O2 (4L NC)");
        p2.getPrecautions().add("Aspiration Risk");
        p2.getAlerts().add("STAT ABG Pending");
        p2.getAlerts().add("Transfer to ICU Requested");
        p2.getAlerts().add("Tachycardia Alert (118 bpm)");
        p2.getSecondaryComorbidities().add("Tobacco Use Disorder (F17.210)");
        p2.getSecondaryComorbidities().add("Pulmonary Hypertension (I27.20)");
        p2.getMedications().add(new Medication("Albuterol/Ipratropium", "3 mL", "Inhalation", "Q4H", "Bronchodilation", "Administered", "12:00"));
        p2.getMedications().add(new Medication("Methylprednisolone", "125 mg", "IV", "Q6H", "Anti-inflammatory", "Administered", "10:30"));
        p2.getMedications().add(new Medication("Ceftriaxone", "1 g", "IV Piggyback", "QD", "Infection Prophylaxis", "Due (16:00)", "Pending"));
        p2.addAuditEntry("Rapid Response consult placed by RN Miller (15m ago)");
        patients.add(p2);

        // 3. Marcus Bell (ESI-1 Resus)
        Patient p3 = new Patient(
                "MRN-90214", "Marcus Bell", "1985-06-22", 41, "Male",
                "***-**-1104", "(555) 301-4491", "Sarah Bell", "(555) 301-4492", "Sister",
                "O-", LocalDateTime.now().minusHours(6),
                "ICU", "ICU-01", "Dr. Sean Baker, MD", "Trauma Surgery", 1,
                "Motor vehicle collision, flail chest, hepatic laceration grade III, hypotensive shock.",
                "S36.115A - Unspecified Injury of Liver, Initial",
                "Intubated, SIMV mode. Massive transfusion protocol initiated. On Norepinephrine at 0.18 mcg/kg/min. Arterial line in right radial artery. ICP monitor reading 12 mmHg.",
                "Kaiser Permanente", "KP-449102"
        );
        p3.setVitals(new VitalSigns(82, 48, 132, 22, 94, 36.4, 8, "Sinus Tachycardia with PVCs"));
        p3.getAllergies().add("NKDA (No Known Drug Allergies)");
        p3.getPrecautions().add("Strict C-Spine");
        p3.getPrecautions().add("Invasive Vent Support");
        p3.getAlerts().add("Code Triage / Resus");
        p3.getAlerts().add("Massive Transfusion Protocol");
        p3.getMedications().add(new Medication("Norepinephrine", "8 mg / 250mL", "IV Infusion", "Continuous Titrate", "Vasopressor MAP > 65", "Administered", "Active Infusion"));
        p3.getMedications().add(new Medication("Fentanyl", "100 mcg", "IV Push", "Q1H PRN", "Analgesia Vent", "Administered", "13:30"));
        p3.addAuditEntry("Level 1 Trauma activation (6h ago)");
        patients.add(p3);

        // 4. Sofia Ramirez (ESI-2 ED Admission)
        Patient p4 = new Patient(
                "MRN-43912", "Sofia Ramirez", "1997-02-18", 29, "Female",
                "***-**-9912", "(555) 604-1823", "Carlos Ramirez", "(555) 604-1824", "Father",
                "B+", LocalDateTime.now().minusHours(3),
                "Emergency", "ED-Bay-04", "Dr. Karen Page, MD", "Emergency", 2,
                "Right lower quadrant pain, positive McBurney point tenderness, anorexia, high fevers.",
                "K35.80 - Unspecified Acute Appendicitis",
                "Surgical consult completed. Scheduled for laparoscopic appendectomy at 16:30. IV hydration running at 125 mL/hr. NPO status maintained.",
                "UnitedHealthcare", "UHC-99210"
        );
        p4.setVitals(new VitalSigns(112, 74, 104, 18, 99, 38.9, 7, "Sinus Tachycardia"));
        p4.getAllergies().add("Penicillin - Hives");
        p4.getPrecautions().add("NPO After Midnight");
        p4.getPrecautions().add("Surgical Pre-Op");
        p4.getAlerts().add("NPO Strict");
        p4.getAlerts().add("OR Scheduled 16:30");
        p4.getMedications().add(new Medication("Cefoxitin", "2 g", "IV Piggyback", "Pre-Op STAT", "Surgical Prophylaxis", "Due (15:30)", "Pending"));
        p4.getMedications().add(new Medication("Morphine Sulfate", "4 mg", "IV Push", "Q3H PRN", "Acute Pain", "Administered", "12:45"));
        p4.addAuditEntry("ED Triage triage completed (3h ago)");
        patients.add(p4);

        // 5. James Henderson (Ward 3A, Bed 302-B)
        Patient p5 = new Patient(
                "MRN-55829", "James Henderson", "1953-09-08", 73, "Male",
                "***-**-3341", "(555) 912-4029", "Martha Henderson", "(555) 912-4030", "Wife",
                "A-", LocalDateTime.now().minusDays(3).minusHours(4),
                "Ward 3A", "Bed 302-B", "Dr. Eleanor Vance, MD", "Cardiology", 3,
                "Post-CABG recovery, sternal incision intact, ambulatory with physical therapy.",
                "Z95.1 - Presence of Aortocoronary Bypass Graft",
                "Post-op Day 3. Pacing wires removed. Chest tubes discontinued yesterday. Tolerating regular cardiac diet.",
                "Medicare Part A/B", "MED-3341098"
        );
        p5.setVitals(new VitalSigns(124, 76, 72, 16, 97, 36.9, 3, "Normal Sinus Rhythm"));
        p5.getAllergies().add("Codeine - Nausea/Vomiting");
        p5.getPrecautions().add("Sternal Precautions");
        p5.getAlerts().add("PT Clearance Required");
        p5.getMedications().add(new Medication("Aspirin", "81 mg", "PO", "Daily", "Antiplatelet", "Administered", "09:00"));
        p5.getMedications().add(new Medication("Atorvastatin", "80 mg", "PO", "QHS", "Lipid Lowering", "Due (21:00)", "Pending"));
        p5.addAuditEntry("Physical therapy ambulated 150ft (1h ago)");
        patients.add(p5);

        // 6. Elena Rostova (PACU-02)
        Patient p6 = new Patient(
                "MRN-18293", "Elena Rostova", "1978-08-14", 48, "Female",
                "***-**-5561", "(555) 888-2101", "Dmitri Rostov", "(555) 888-2102", "Brother",
                "AB+", LocalDateTime.now().minusHours(2),
                "PACU", "PACU-02", "Dr. Gregory House, MD", "Anesthesiology", 3,
                "Immediate post-op status following elective laparoscopic cholecystectomy.",
                "K80.20 - Calculus of Gallbladder without Cholecystitis",
                "Extubated in OR. Aldrete score 9/10. Ready for transfer to Day Surgery Discharge or short-stay observation.",
                "Cigna Healthcare", "CIG-182930"
        );
        p6.setVitals(new VitalSigns(118, 72, 68, 14, 99, 36.7, 3, "Sinus Rhythm"));
        p6.getAllergies().add("NKDA");
        p6.getPrecautions().add("Post-Anesthesia Monitoring");
        p6.getAlerts().add("PACU Discharge Clearance Pending");
        patients.add(p6);

        // 7. Liam Gallagher (Ward 3A, Bed 304-B)
        Patient p7 = new Patient(
                "MRN-64810", "Liam Gallagher", "1991-03-30", 35, "Male",
                "***-**-7019", "(555) 441-9988", "Noel Gallagher", "(555) 441-9989", "Brother",
                "O+", LocalDateTime.now().minusDays(2).minusHours(6),
                "Ward 3A", "Bed 304-B", "Dr. Michael Stone, MD", "Internal Medicine", 4,
                "Productive cough, pleuritic chest pain, chills, right lower lobe opacity on CXR.",
                "J18.9 - Pneumonia, Unspecified Organism",
                "Afebrile for past 24 hours on IV Rocephin. Oxygen saturation stable on room air. Plan step-down to oral Augmentin tomorrow.",
                "Humana Health", "HUM-648102"
        );
        p7.setVitals(new VitalSigns(120, 80, 82, 16, 96, 37.5, 2, "Sinus Rhythm"));
        p7.getAllergies().add("Ciprofloxacin - Tendonitis");
        p7.getAlerts().add("Repeat CXR Scheduled 08:00 AM");
        patients.add(p7);

        // 8. Chloe Chen (ICU-03)
        Patient p8 = new Patient(
                "MRN-33291", "Chloe Chen", "1968-12-05", 58, "Female",
                "***-**-8822", "(555) 772-3341", "Victor Chen", "(555) 772-3342", "Spouse",
                "B-", LocalDateTime.now().minusDays(1).minusHours(18),
                "ICU", "ICU-03", "Dr. Sean Baker, MD", "Critical Care", 1,
                "Urosepsis, altered mental status, refractory hypotension, lactic acidosis.",
                "A41.9 - Sepsis, Unspecified Organism",
                "Central venous line placed. Vasopressin and Norepinephrine infusions weaning slowly. Lactic acid improved from 4.8 to 2.1 mmol/L.",
                "Blue Cross Blue Shield", "BCBS-332910"
        );
        p8.setVitals(new VitalSigns(86, 52, 126, 24, 93, 39.2, 0, "Sinus Tachycardia"));
        p8.getAllergies().add("Sulfa - Stevens-Johnson Warning");
        p8.getPrecautions().add("Strict Contact Isolation");
        p8.getPrecautions().add("CVC Central Line Care");
        p8.getAlerts().add("Severe Sepsis Alert");
        p8.getAlerts().add("STAT Lactate Protocol");
        patients.add(p8);

        // 9. Arthur Pendelton (Ward 3A, Bed 305-A)
        Patient p9 = new Patient(
                "MRN-78104", "Arthur Pendelton", "1945-01-15", 81, "Male",
                "***-**-6643", "(555) 229-8800", "Penelope Pendelton", "(555) 229-8801", "Daughter",
                "A+", LocalDateTime.now().minusDays(5).minusHours(11),
                "Ward 3A", "Bed 305-A", "Dr. Robert Vance, MD", "Geriatrics", 3,
                "Hepatic encephalopathy grade 1, ascites, lower extremity edema.",
                "K74.60 - Unspecified Cirrhosis of Liver",
                "Alert, oriented x2. Ammonia levels down to 45 umol/L. Ambulates with 2-person assist.",
                "Medicare Advantage", "MED-781049"
        );
        p9.setVitals(new VitalSigns(110, 68, 86, 18, 96, 36.8, 1, "Sinus Rhythm"));
        p9.getAllergies().add("Morphine - Severe Bronchospasm");
        p9.getPrecautions().add("Fall Risk (High)");
        p9.getAlerts().add("Ammonia Level Routine");
        patients.add(p9);

        // 10. Evelyn Wright (Cardiology-201-A)
        Patient p10 = new Patient(
                "MRN-29401", "Evelyn Wright", "1959-05-20", 67, "Female",
                "***-**-5028", "(555) 431-7711", "Thomas Wright", "(555) 431-7712", "Spouse",
                "O+", LocalDateTime.now().minusDays(1).minusHours(8),
                "Cardiology", "Cardiology-201-A", "Dr. Robert Vance, MD", "Cardiology", 2,
                "Substernal chest tightness, radiation to left jaw, troponin elevation (0.48 ng/mL).",
                "I21.4 - Non-ST Elevation Myocardial Infarction",
                "Coronary angiography scheduled tomorrow morning. Currently pain-free on IV nitroglycerin drip and Heparin. Continuous telemetry.",
                "Aetna Senior Health", "AET-294018"
        );
        p10.setVitals(new VitalSigns(142, 90, 94, 18, 97, 37.0, 4, "Sinus Rhythm with T-wave inversion"));
        p10.getAllergies().add("Aspirin - Wheezing/Asthma Exacerbation");
        p10.getPrecautions().add("Continuous Cardiac Telemetry");
        p10.getPrecautions().add("Bed Rest Strict");
        p10.getAlerts().add("Cath Lab Scheduled 09:00");
        p10.getAlerts().add("Stat Troponin Q3H");
        patients.add(p10);
    }

    public synchronized void resetForTesting() {
        patients.clear();
        beds.clear();
        seedInitialData();
        currentShiftHandoff = new ShiftHandoff();
    }
}
