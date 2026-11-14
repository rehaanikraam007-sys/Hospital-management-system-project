package com.medicore.model;

import java.io.Serializable;

/**
 * Logged-in clinician / nursing session identity.
 */
public class StaffUser implements Serializable {
    private String staffId;
    private String fullName;
    private String role;
    private String department;
    private String assignedUnit;
    private String activeShift;
    private boolean smartCardVerified;

    public StaffUser(String staffId, String fullName, String role, String department, String assignedUnit, String activeShift) {
        this.staffId = staffId;
        this.fullName = fullName;
        this.role = role;
        this.department = department;
        this.assignedUnit = assignedUnit;
        this.activeShift = activeShift;
        this.smartCardVerified = true;
    }

    public String getStaffId() { return staffId; }
    public String getFullName() { return fullName; }
    public String getRole() { return role; }
    public String getDepartment() { return department; }
    public String getAssignedUnit() { return assignedUnit; }
    public String getActiveShift() { return activeShift; }
    public boolean isSmartCardVerified() { return smartCardVerified; }

    public String getBadgeDisplay() {
        return fullName + " (" + department + " / " + role + ")";
    }
}
