package com.medicore.model;

import java.io.Serializable;

/**
 * Bed occupancy and environmental services (EVS) status model.
 */
public class Bed implements Serializable {
    public enum Status {
        OCCUPIED,
        AVAILABLE,
        DIRTY,
        DISINFECTION,
        TRANSFER_PENDING
    }

    private String bedId;
    private String ward;
    private Status status;
    private String patientMrn;
    private String patientName;
    private int evsTimerRemainingMinutes;
    private boolean telemetryConnected;
    private String telemetryVitals;
    private int acuityLevel; // 1 (ESI-1) to 5 (ESI-5)

    public Bed() {
        this.status = Status.AVAILABLE;
    }

    public Bed(String bedId, String ward, Status status, String patientMrn, String patientName, 
               int evsTimerRemainingMinutes, boolean telemetryConnected, String telemetryVitals, int acuityLevel) {
        this.bedId = bedId;
        this.ward = ward;
        this.status = status;
        this.patientMrn = patientMrn;
        this.patientName = patientName;
        this.evsTimerRemainingMinutes = evsTimerRemainingMinutes;
        this.telemetryConnected = telemetryConnected;
        this.telemetryVitals = telemetryVitals;
        this.acuityLevel = acuityLevel;
    }

    public String getBedId() { return bedId; }
    public void setBedId(String bedId) { this.bedId = bedId; }

    public String getWard() { return ward; }
    public void setWard(String ward) { this.ward = ward; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public String getPatientMrn() { return patientMrn; }
    public void setPatientMrn(String patientMrn) { this.patientMrn = patientMrn; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public int getEvsTimerRemainingMinutes() { return evsTimerRemainingMinutes; }
    public void setEvsTimerRemainingMinutes(int evsTimerRemainingMinutes) { this.evsTimerRemainingMinutes = evsTimerRemainingMinutes; }

    public boolean isTelemetryConnected() { return telemetryConnected; }
    public void setTelemetryConnected(boolean telemetryConnected) { this.telemetryConnected = telemetryConnected; }

    public String getTelemetryVitals() { return telemetryVitals; }
    public void setTelemetryVitals(String telemetryVitals) { this.telemetryVitals = telemetryVitals; }

    public int getAcuityLevel() { return acuityLevel; }
    public void setAcuityLevel(int acuityLevel) { this.acuityLevel = acuityLevel; }

    public boolean isClean() {
        return status == Status.AVAILABLE;
    }

    public boolean isOccupied() {
        return status == Status.OCCUPIED;
    }
}
