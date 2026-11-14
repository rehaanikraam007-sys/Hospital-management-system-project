package com.medicore;

import com.medicore.model.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * Automated unit test suite verifying clinical models, data store transactions, and bed transfers.
 */
public class MediCoreHmsTest {

    private HospitalDataStore dataStore;

    @Before
    public void setUp() {
        dataStore = HospitalDataStore.getInstance();
        dataStore.resetForTesting();
    }

    @Test
    public void testInitialPatientCensusLoaded() {
        List<Patient> list = dataStore.getPatients();
        Assert.assertNotNull("Patients list should not be null", list);
        Assert.assertTrue("Should have initial pre-seeded patients", list.size() >= 10);

        Patient eleanor = dataStore.getPatientByMrn("MRN-84920");
        Assert.assertNotNull("Eleanor Vance should exist", eleanor);
        Assert.assertEquals("Eleanor Vance", eleanor.getFullName());
        Assert.assertEquals("Ward 3A", eleanor.getWard());
        Assert.assertEquals("Bed 301-A", eleanor.getBed());
        Assert.assertEquals("I50.9 - Heart Failure, Unspecified", eleanor.getDiagnosisIcd10());
        Assert.assertTrue("Should have allergies listed", eleanor.getAllergies().size() >= 2);
    }

    @Test
    public void testVitalSignsCalculationsAndAcuity() {
        VitalSigns normalVitals = new VitalSigns(120, 80, 72, 16, 98, 37.0, 1, "Normal Sinus Rhythm");
        Assert.assertFalse("Normal vitals should not be critical", normalVitals.isCritical());
        Assert.assertEquals("120/80 mmHg", normalVitals.getBpFormatted());

        VitalSigns criticalVitals = new VitalSigns(85, 50, 130, 26, 89, 39.1, 7, "Sinus Tachycardia");
        Assert.assertTrue("Tachycardia 130 and SpO2 89 should be flagged critical", criticalVitals.isCritical());
    }

    @Test
    public void testPatientRegistrationAndMrnGeneration() {
        int initialCount = dataStore.getPatients().size();
        String nextMrn = dataStore.generateNextMrn();
        Assert.assertNotNull(nextMrn);
        Assert.assertTrue(nextMrn.startsWith("MRN-"));

        Patient newPatient = new Patient(
                nextMrn, "Alexander Pierce", "1988-03-21", 38, "Male",
                "***-**-9012", "(555) 332-9901", "Julia Pierce", "(555) 332-9902", "Spouse",
                "O+", java.time.LocalDateTime.now(), "Ward 3A", "Bed 301-B",
                "Dr. Eleanor Vance, MD", "Cardiology", 3,
                "Chest pain on exertion", "I20.9 - Angina Pectoris",
                "Admitted for observation", "Aetna", "AET-11029"
        );

        dataStore.registerPatient(newPatient);

        Assert.assertEquals(initialCount + 1, dataStore.getPatients().size());
        Patient retrieved = dataStore.getPatientByMrn(nextMrn);
        Assert.assertNotNull(retrieved);
        Assert.assertEquals("Alexander Pierce", retrieved.getFullName());

        Bed assignedBed = dataStore.getBedById("Bed 301-B");
        Assert.assertNotNull(assignedBed);
        Assert.assertEquals(Bed.Status.OCCUPIED, assignedBed.getStatus());
        Assert.assertEquals(nextMrn, assignedBed.getPatientMrn());
    }

    @Test
    public void testBedTransferWorkflow() {
        Patient davidCho = dataStore.getPatientByMrn("MRN-71932");
        Assert.assertNotNull(davidCho);
        String originBedId = davidCho.getBed();

        String targetBedId = "MICU-04-B";
        boolean success = dataStore.transferPatientBed("MRN-71932", targetBedId, "Acuity Escalation", "RN Sarah Jenkins", "Porter Marcus Cruz");
        Assert.assertTrue("Bed transfer should succeed", success);

        Assert.assertEquals(targetBedId, davidCho.getBed());
        Assert.assertEquals("ICU", davidCho.getWard());

        Bed originBed = dataStore.getBedById(originBedId);
        Assert.assertNotNull(originBed);
        Assert.assertEquals(Bed.Status.DIRTY, originBed.getStatus());
        Assert.assertNull(originBed.getPatientMrn());

        Bed destBed = dataStore.getBedById(targetBedId);
        Assert.assertNotNull(destBed);
        Assert.assertEquals(Bed.Status.OCCUPIED, destBed.getStatus());
        Assert.assertEquals("MRN-71932", destBed.getPatientMrn());
    }

    @Test
    public void testChartUpdateAndAuditTrail() {
        Patient eleanor = dataStore.getPatientByMrn("MRN-84920");
        Assert.assertNotNull(eleanor);

        eleanor.setDiagnosisIcd10("I50.22 - Chronic Systolic Heart Failure");
        eleanor.addAuditEntry("Bedside ultrasound conducted: EF 35%");
        dataStore.updatePatient(eleanor);

        Patient reloaded = dataStore.getPatientByMrn("MRN-84920");
        Assert.assertEquals("I50.22 - Chronic Systolic Heart Failure", reloaded.getDiagnosisIcd10());
        Assert.assertTrue("Audit trail should have new entry", reloaded.getAuditLog().get(0).contains("Bedside ultrasound conducted"));
    }

    @Test
    public void testShiftHandoffModel() {
        ShiftHandoff handoff = dataStore.getCurrentShiftHandoff();
        Assert.assertNotNull(handoff);
        Assert.assertFalse(handoff.isFullyCoSigned());

        handoff.setIncomingSignedTimestamp("15:30:00");
        handoff.setFullyCoSigned(true);

        dataStore.setCurrentShiftHandoff(handoff);
        Assert.assertTrue(dataStore.getCurrentShiftHandoff().isFullyCoSigned());
    }
}
