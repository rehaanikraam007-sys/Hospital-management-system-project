package com.medicore.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Structured SBAR Shift Handoff Record with Dual-Nurse verification.
 */
public class ShiftHandoff implements Serializable {
    private String id;
    private String unit;
    private String outgoingChargeRn;
    private String incomingChargeRn;
    private String outgoingSignedTimestamp;
    private String incomingSignedTimestamp;
    private boolean fullyCoSigned;
    private String situation;
    private String background;
    private String assessment;
    private String recommendation;

    public ShiftHandoff() {
        this.outgoingSignedTimestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        this.situation = "Ward 3A census at 14/16 beds. 2 ICU transfers pending (Bed 303-A David Cho, Bed 305-B).";
        this.background = "Day shift admissions: 4 post-op, 2 step-down transfers. EVS turnaround avg 22 mins.";
        this.assessment = "Bed 303-A telemetry unstable (tachycardia 118bpm, troponin elevated). Bed 301-A Eleanor Vance stable on IV Lasix.";
        this.recommendation = "Expedite MICU transfer for Bed 303-A once bed 04-B is sanitized. Review PM labs for Eleanor Vance.";
        this.outgoingChargeRn = "RN Kendra Miller, BSN";
        this.incomingChargeRn = "RN Marcus Vance, BSN";
        this.unit = "Ward 3A Medical/Telemetry";
        this.fullyCoSigned = false;
    }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getOutgoingChargeRn() { return outgoingChargeRn; }
    public void setOutgoingChargeRn(String outgoingChargeRn) { this.outgoingChargeRn = outgoingChargeRn; }

    public String getIncomingChargeRn() { return incomingChargeRn; }
    public void setIncomingChargeRn(String incomingChargeRn) { this.incomingChargeRn = incomingChargeRn; }

    public String getOutgoingSignedTimestamp() { return outgoingSignedTimestamp; }
    public void setOutgoingSignedTimestamp(String outgoingSignedTimestamp) { this.outgoingSignedTimestamp = outgoingSignedTimestamp; }

    public String getIncomingSignedTimestamp() { return incomingSignedTimestamp; }
    public void setIncomingSignedTimestamp(String incomingSignedTimestamp) { this.incomingSignedTimestamp = incomingSignedTimestamp; }

    public boolean isFullyCoSigned() { return fullyCoSigned; }
    public void setFullyCoSigned(boolean fullyCoSigned) { this.fullyCoSigned = fullyCoSigned; }

    public String getSituation() { return situation; }
    public void setSituation(String situation) { this.situation = situation; }

    public String getBackground() { return background; }
    public void setBackground(String background) { this.background = background; }

    public String getAssessment() { return assessment; }
    public void setAssessment(String assessment) { this.assessment = assessment; }

    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }
}
