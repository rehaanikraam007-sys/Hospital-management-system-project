# MediCore HMS v3.4 — Clinical Desktop Client & Hospital Management Suite

MediCore HMS is a mission-critical, enterprise hospital management suite and clinical desktop client built for attending physicians, charge nurses, and healthcare staff. Built with **Java Swing** and styled using **FlatLaf (Flat Light Modern Look & Feel)**, it delivers a dense, high-contrast, responsive clinical workstation conforming to HL7 v2.5 / FHIR R4 clinical workflows.

---

## Key Features & Screen Specifications

### 1. Login Authentication Dialog (`LoginDialog`)
* **Biometric & Badge Credentialing**: 600px desktop modal dialog with shadow elevation.
* **Credentials**: Clinician Badge ID field with one-click **Scan NFC Badge** simulation.
* **Security**: Masked passphrase field with toggle visibility icon (`👁 Show`).
* **Unit & Shift**: Dual dropdown row for assigned unit (ICU, Emergency, Cardiology, Surgery) and active duty shift.
* **Smart Card**: Hardware smart card chip status (*PIV/CAC Slot #0: Token Verified*).
* **Emergency Bypass**: High-contrast STAT emergency bypass button for immediate triage access.
* **Compliance**: HIPAA / HITECH security notice and live HL7/FHIR socket ping indicator.

### 2. Clinical Dashboard Workstation (`DashboardView` & `MainFrame`)
* **Global Window Chrome**:
  - Native desktop window bar with title: `MediCore HMS v3.4 – Clinical Desktop Client`.
  - Desktop menu bar (`JMenuBar`): File, Patients, Admissions, Records, Pharmacy, Tools, Help.
  - Live status header: Pulsing green HL7/FHIR socket heartbeat indicator (`HL7/FHIR LINK: 99.98% OK`), active clinician badge (`Dr. Eleanor Vance, MD`), and synchronized hospital clock (`SYNC: HH:mm:ss EST`).
  - Left navigation sidebar for one-click access across all clinical modules.
* **3-Column Modular Command Center**:
  - **Executive Census Ribbon**: 4 KPI cards (Total Hospital Census 142/165 with progress meter, ED Flow 14 active, Throughput 18 admits / 12 discharges, Critical STAT Labs pending).
  - **Ward & Bed Occupancy Matrix**: Visual bed grid across Ward 3A, ICU, PACU, and Cardiology showing bed numbers, occupancy status, live telemetry vitals (`HR 118! | BP 92/58`), and EVS disinfection timers.
  - **Admissions & Emergency Queue**: Sortable clinical worklist with MRN, Demographics, ESI Acuity pill, Ward/Bed, Attending MD, Latest Vitals, and quick action buttons (`Chart`).
  - **STAT Action Sidebar**: High-Priority STAT Alert widget, Rapid Response dispatch CTA (`⚡ Dispatch RRT`), Rapid Action Launcher shortcuts, and Shift Rounding schedule.

### 3. Patient Registration & Intake Form (`PatientRegistrationView`)
* **Multi-Step Tabbed Workflow (`JTabbedPane`)**:
  - **Tab 1: Demographics & Identity**: Full legal name, date of birth with real-time age auto-calculation, biological sex, masked SSN/clinical ID, primary phone, next of kin contact, and blood group (`ABO/Rh`).
  - **Tab 2: Triage & Vitals Intake**: Clinical chief complaint narrative `JTextArea`, vital signs (BP, HR, Resp, SpO2, Temp), pain scale slider (0–10), and initial ESI Acuity level.
  - **Tab 3: Insurance & Guardian**: Payer name, policy/group ID, real-time eligibility verification, and guarantor details.
  - **Tab 4: Bed & Attending Assignment**: Admitting department, attending doctor, target ward/clean bed assignment, and clinical precautions.
* **Critical Safety Allergen Chips**: Tag list for severe allergies (Penicillin, Sulfa, Latex) with removal chips, new allergen input, and **Mark NKDA** button.
* **Action Toolbar**: Clear Form, Cancel (Esc), Print Patient Wristband (Ctrl+P) with 2D barcode, and **Save & Generate MRN (F5)**.

### 4. Patient Search & Directory (`PatientDirectoryView`)
* **Live Query Filter Bar**: Universal search field (MRN, Name, SSN) with instant filtering.
* **Filters**: Ward/Unit, Admission Status, and Acuity Level (ESI-1 to ESI-5).
* **Quick View Filter Pills**: Clickable count pills: *All Inpatients (248)*, *Code Alerts (3)*, *Stat Labs Pending (7)*, *My Assigned (12)*.
* **Dense Clinical Data Table (`JTable`)**:
  - Demographics avatar initials with age, sex, DOB.
  - Admission date and Length of Stay calculation (`4d 9h`).
  - Assigned ward & bed with telemetry status tags.
  - Attending physician and department.
  - ESI Acuity badges (ESI-1 RESUS to ESI-5 ROUTINE).
  - Alert flags (`Allergy: PCN`, `Stat Lab`, `NPO`).
  - Instant navigation to bedside chart (`Open Chart ➔`).
* **Export Actions**: Export to CSV and print report controls.

### 5. Update Patient Records Console (`UpdateRecordsView`)
* **Left Snapshot Pane (380px Fixed)**:
  - Patient switcher dropdown for fast bedside navigation.
  - Patient card banner (Eleanor Vance, MRN #84920, Bed 301-A).
  - **Live Animated ECG Rhythm Strip**: Real-time physiological Lead II cardiac monitor with sweeping phosphor trace, P-Q-R-S-T wave complexes, and synchronized BPM pulse indicator.
  - Current telemetry readouts (HR 78 BPM, BP 138/86, SpO2 98%, Temp 37.1°C).
  - Active clinical precaution chips and clinical audit log.
* **Right Scrollable Clinical Editing Forms**:
  - **Section A**: Clinical Diagnosis & ICD-10 Coding dropdown, ESI acuity radio group, secondary chronic comorbidities builder, and attending progress narrative.
  - **Section B**: Vitals update with admission baseline comparison and **Sync IntelliVue Monitor** button.
  - **Section C**: Active Medication & eMAR adjustments with compound names, dosages, routes, frequencies, and eMAR status.
  - **Section D**: Attending care team assignment (Attending, Fellow, Primary RN, Clinical Pharmacist).
  - **Bottom Approval Bar**: Audit justification dropdown, mandatory Digital MD PIN input, Revert button, and primary **Save Changes to Chart (Ctrl+S)**.

### 6. Bed & Shift Change Management Workspace (`BedShiftManagementView`)
* **Left Panel: Bed Transfer & Reassignment**:
  - Origin bed occupant summary and telemetry values.
  - Destination bed picker with live clean/dirty status indicators (`MICU-04-B [CLEAN]`, `Cardiology-201-B [DIRTY]`).
  - Clinical transfer reason, escort nurse, and transport porter dispatch selectors.
  - Clinical safety checklist (Continuous Telemetry, Auto-Dispatch EVS Sanitation, O2 Tank, Fall Risk Band).
  - HL7 Transfer Status 4-step progress flow.
* **Right Panel: Shift Handoff & Roster Change**:
  - Hospital capacity meter (142 / 165 Beds, 86%).
  - Structured SBAR clinical handoff notes (Situation, Background, Assessment, Recommendation).
  - Dual-Nurse Electronic Co-Sign panel (Outgoing Charge RN sign-off timestamp + Incoming Charge RN digital PIN sign-in).
  - Ward shift nurse-to-patient staffing ratio and acuity workload table.
* **Action Toolbar**: Send Alert to EVS, Print Transfer Slip, Execute Shift Handoff (Ctrl+H), and Authorize Bed Transfer (Enter).

### 7. Auxiliary Screens & Dialogs
* **eMAR & Inpatient Pharmacy Suite (`PharmacyEmarView`)**: Barcode Medication Administration (BCMA) 5-rights verification simulator and dispensing log.
* **JVM & System Diagnostics (`DiagnosticsView`)**: Real-time heap memory usage, thread monitoring, garbage collection trigger, and HL7 v2.5 / FHIR R4 socket telemetry.
* **Print Wristband Dialog (`WristbandDialog`)**: Thermal wristband preview with 2D barcode, color-coded allergy clasp, and patient demographics.
* **Bed Transfer Slip Dialog (`TransferSlipDialog`)**: Inpatient ADT-A02 bed transfer manifest preview.
* **Emergency Dispatch Dialog (`RapidResponseDialog`)**: Rapid Response Team (RRT) / Code Blue activation modal.

---

## Architecture & Technology Stack

| Layer | Technology | Details |
|---|---|---|
| **Platform** | Java 17+ (OpenJDK 25) | High-performance desktop client |
| **GUI Framework** | Java Swing | Native desktop window chrome, menus, split panes, tables |
| **Theme Engine** | FlatLaf 3.5.4 | Modern Flat Light medical styling |
| **Build Tool** | Apache Maven 3.9.16 | Bundled runnable uber JAR with all dependencies |
| **Data Engine** | Thread-safe in-memory store | Pre-seeded with 10 detailed clinical inpatient profiles |
| **Testing** | JUnit 4.13.2 | Automated unit tests and headless GUI smoke tests |

---

## Design Language & Color Palette

* **Primary Accent**: Deep Medical Sky Blue (`#0284C7` / `#0369A1`)
* **Background & Chrome**: Clean Hospital White (`#FFFFFF`) and Cool Sterile Greys (`#F8FAFC`, `#F1F5F9`)
* **Triage Acuity Indicators**:
  - `ESI-1 RESUS / Code Red`: `#DC2626` / `#FEF2F2`
  - `ESI-2 EMERGENT / High Risk`: `#EA580C` / `#FFF7ED`
  - `ESI-3 URGENT / Caution`: `#D97706` / `#FEF3C7`
  - `ESI-4 STABLE / Clean`: `#059669` / `#ECFDF5`
  - `ESI-5 ROUTINE / Info`: `#2563EB` / `#EFF6FF`

---

## How to Run

### Option 1: One-Click Desktop Launcher
Double-click `run.bat` or execute in terminal:
```cmd
run.bat
```

### Option 2: Command Line
```cmd
java --enable-native-access=ALL-UNNAMED -jar target\medicore-hms-3.4.0-jar-with-dependencies.jar
```

To bypass the login dialog directly during development or testing:
```cmd
java --enable-native-access=ALL-UNNAMED -jar target\medicore-hms-3.4.0-jar-with-dependencies.jar --bypass-login
```

### Option 3: Compile from Source
```cmd
mvn clean package
```