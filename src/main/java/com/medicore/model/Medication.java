package com.medicore.model;

import java.io.Serializable;

/**
 * Medication prescription and eMAR administration entry.
 */
public class Medication implements Serializable {
    private String name;
    private String dosage;
    private String route;       // PO, IV, SubQ, IM, Inhaled, Topical
    private String frequency;   // QD, BID, TID, Q4H PRN, STAT
    private String indication;
    private String status;      // Given, Due, Held, Discontinued, Verifying
    private String lastAdministered;

    public Medication() {}

    public Medication(String name, String dosage, String route, String frequency, String indication, String status, String lastAdministered) {
        this.name = name;
        this.dosage = dosage;
        this.route = route;
        this.frequency = frequency;
        this.indication = indication;
        this.status = status;
        this.lastAdministered = lastAdministered;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public String getRoute() { return route; }
    public void setRoute(String route) { this.route = route; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public String getIndication() { return indication; }
    public void setIndication(String indication) { this.indication = indication; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getLastAdministered() { return lastAdministered; }
    public void setLastAdministered(String lastAdministered) { this.lastAdministered = lastAdministered; }

    public String getFullSig() {
        return name + " " + dosage + " " + route + " " + frequency;
    }
}
