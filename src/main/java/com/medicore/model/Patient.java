package com.medicore.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Core clinical Inpatient & EHR model representing an admitted or emergency patient.
 */
public class Patient implements Serializable {
    private String mrn;
    private String fullName;
    private String dob; // YYYY-MM-DD
    private int age;
    private String biologicalSex; // Male, Female, Other
    private String ssnMasked;
    private String phone;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String emergencyContactRelation;
    private String bloodGroup; // A+, A-, B+, B-, AB+, AB-, O+, O-
    private LocalDateTime admissionDateTime;
    private String ward;
    private String bed;
    private String attendingDoctor;
    private String department;
    private int acuityLevel; // 1 (ESI-1) to 5 (ESI-5)
    private String chiefComplaint;
    private String diagnosisIcd10;
    private List<String> secondaryComorbidities = new ArrayList<>();
    private String clinicalProgressNotes;
    private List<String> allergies = new ArrayList<>();
    private List<String> precautions = new ArrayList<>();
    private List<String> alerts = new ArrayList<>();
    private VitalSigns vitals = new VitalSigns();
    private List<Medication> medications = new ArrayList<>();
    private boolean telemetryConnected;
    private List<String> auditLog = new ArrayList<>();
    private String payerName;
    private String policyNumber;
    private String preAuthStatus;
    private String admissionStatus; // Inpatient, ED Queue, Observation, Discharged

    public Patient() {
        this.admissionDateTime = LocalDateTime.now();
        this.admissionStatus = "Inpatient";
        this.telemetryConnected = true;
    }

    public Patient(String mrn, String fullName, String dob, int age, String biologicalSex,
                   String ssnMasked, String phone, String emergencyContactName, String emergencyContactPhone,
                   String emergencyContactRelation, String bloodGroup, LocalDateTime admissionDateTime,
                   String ward, String bed, String attendingDoctor, String department, int acuityLevel,
                   String chiefComplaint, String diagnosisIcd10, String clinicalProgressNotes,
                   String payerName, String policyNumber) {
        this.mrn = mrn;
        this.fullName = fullName;
        this.dob = dob;
        this.age = age;
        this.biologicalSex = biologicalSex;
        this.ssnMasked = ssnMasked;
        this.phone = phone;
        this.emergencyContactName = emergencyContactName;
        this.emergencyContactPhone = emergencyContactPhone;
        this.emergencyContactRelation = emergencyContactRelation;
        this.bloodGroup = bloodGroup;
        this.admissionDateTime = admissionDateTime != null ? admissionDateTime : LocalDateTime.now();
        this.ward = ward;
        this.bed = bed;
        this.attendingDoctor = attendingDoctor;
        this.department = department;
        this.acuityLevel = acuityLevel;
        this.chiefComplaint = chiefComplaint;
        this.diagnosisIcd10 = diagnosisIcd10;
        this.clinicalProgressNotes = clinicalProgressNotes;
        this.payerName = payerName;
        this.policyNumber = policyNumber;
        this.preAuthStatus = "Verified & Active";
        this.admissionStatus = "Inpatient";
        this.telemetryConnected = true;
    }

    public String getMrn() { return mrn; }
    public void setMrn(String mrn) { this.mrn = mrn; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getBiologicalSex() { return biologicalSex; }
    public void setBiologicalSex(String biologicalSex) { this.biologicalSex = biologicalSex; }

    public String getSsnMasked() { return ssnMasked; }
    public void setSsnMasked(String ssnMasked) { this.ssnMasked = ssnMasked; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmergencyContactName() { return emergencyContactName; }
    public void setEmergencyContactName(String emergencyContactName) { this.emergencyContactName = emergencyContactName; }

    public String getEmergencyContactPhone() { return emergencyContactPhone; }
    public void setEmergencyContactPhone(String emergencyContactPhone) { this.emergencyContactPhone = emergencyContactPhone; }

    public String getEmergencyContactRelation() { return emergencyContactRelation; }
    public void setEmergencyContactRelation(String emergencyContactRelation) { this.emergencyContactRelation = emergencyContactRelation; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public LocalDateTime getAdmissionDateTime() { return admissionDateTime; }
    public void setAdmissionDateTime(LocalDateTime admissionDateTime) { this.admissionDateTime = admissionDateTime; }

    public String getWard() { return ward; }
    public void setWard(String ward) { this.ward = ward; }

    public String getBed() { return bed; }
    public void setBed(String bed) { this.bed = bed; }

    public String getAttendingDoctor() { return attendingDoctor; }
    public void setAttendingDoctor(String attendingDoctor) { this.attendingDoctor = attendingDoctor; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public int getAcuityLevel() { return acuityLevel; }
    public void setAcuityLevel(int acuityLevel) { this.acuityLevel = acuityLevel; }

    public String getChiefComplaint() { return chiefComplaint; }
    public void setChiefComplaint(String chiefComplaint) { this.chiefComplaint = chiefComplaint; }

    public String getDiagnosisIcd10() { return diagnosisIcd10; }
    public void setDiagnosisIcd10(String diagnosisIcd10) { this.diagnosisIcd10 = diagnosisIcd10; }

    public List<String> getSecondaryComorbidities() { return secondaryComorbidities; }
    public void setSecondaryComorbidities(List<String> secondaryComorbidities) { this.secondaryComorbidities = secondaryComorbidities; }

    public String getClinicalProgressNotes() { return clinicalProgressNotes; }
    public void setClinicalProgressNotes(String clinicalProgressNotes) { this.clinicalProgressNotes = clinicalProgressNotes; }

    public List<String> getAllergies() { return allergies; }
    public void setAllergies(List<String> allergies) { this.allergies = allergies; }

    public List<String> getPrecautions() { return precautions; }
    public void setPrecautions(List<String> precautions) { this.precautions = precautions; }

    public List<String> getAlerts() { return alerts; }
    public void setAlerts(List<String> alerts) { this.alerts = alerts; }

    public VitalSigns getVitals() { return vitals; }
    public void setVitals(VitalSigns vitals) { this.vitals = vitals; }

    public List<Medication> getMedications() { return medications; }
    public void setMedications(List<Medication> medications) { this.medications = medications; }

    public boolean isTelemetryConnected() { return telemetryConnected; }
    public void setTelemetryConnected(boolean telemetryConnected) { this.telemetryConnected = telemetryConnected; }

    public List<String> getAuditLog() { return auditLog; }
    public void setAuditLog(List<String> auditLog) { this.auditLog = auditLog; }

    public String getPayerName() { return payerName; }
    public void setPayerName(String payerName) { this.payerName = payerName; }

    public String getPolicyNumber() { return policyNumber; }
    public void setPolicyNumber(String policyNumber) { this.policyNumber = policyNumber; }

    public String getPreAuthStatus() { return preAuthStatus; }
    public void setPreAuthStatus(String preAuthStatus) { this.preAuthStatus = preAuthStatus; }

    public String getAdmissionStatus() { return admissionStatus; }
    public void setAdmissionStatus(String admissionStatus) { this.admissionStatus = admissionStatus; }

    public String getLengthOfStay() {
        if (admissionDateTime == null) return "0d 0h";
        long totalHours = ChronoUnit.HOURS.between(admissionDateTime, LocalDateTime.now());
        if (totalHours < 0) totalHours = 0;
        long days = totalHours / 24;
        long hours = totalHours % 24;
        return days + "d " + hours + "h";
    }

    public String getInitials() {
        if (fullName == null || fullName.trim().isEmpty()) return "PT";
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
    }

    public void addAuditEntry(String entry) {
        if (this.auditLog == null) this.auditLog = new ArrayList<>();
        this.auditLog.add(0, entry);
    }
}
