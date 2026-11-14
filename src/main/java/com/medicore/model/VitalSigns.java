package com.medicore.model;

import java.io.Serializable;

/**
 * Encapsulates clinical vital signs and telemetry values.
 */
public class VitalSigns implements Serializable {
    private int systolicBp;
    private int diastolicBp;
    private int heartRate;
    private int respiratoryRate;
    private int spo2;
    private double temperatureC;
    private int painScale; // 0-10
    private String ecgRhythm;

    public VitalSigns() {
        this.systolicBp = 120;
        this.diastolicBp = 80;
        this.heartRate = 75;
        this.respiratoryRate = 16;
        this.spo2 = 98;
        this.temperatureC = 37.0;
        this.painScale = 0;
        this.ecgRhythm = "Normal Sinus Rhythm";
    }

    public VitalSigns(int systolicBp, int diastolicBp, int heartRate, int respiratoryRate, int spo2, double temperatureC, int painScale, String ecgRhythm) {
        this.systolicBp = systolicBp;
        this.diastolicBp = diastolicBp;
        this.heartRate = heartRate;
        this.respiratoryRate = respiratoryRate;
        this.spo2 = spo2;
        this.temperatureC = temperatureC;
        this.painScale = painScale;
        this.ecgRhythm = ecgRhythm;
    }

    public int getSystolicBp() { return systolicBp; }
    public void setSystolicBp(int systolicBp) { this.systolicBp = systolicBp; }

    public int getDiastolicBp() { return diastolicBp; }
    public void setDiastolicBp(int diastolicBp) { this.diastolicBp = diastolicBp; }

    public int getHeartRate() { return heartRate; }
    public void setHeartRate(int heartRate) { this.heartRate = heartRate; }

    public int getRespiratoryRate() { return respiratoryRate; }
    public void setRespiratoryRate(int respiratoryRate) { this.respiratoryRate = respiratoryRate; }

    public int getSpo2() { return spo2; }
    public void setSpo2(int spo2) { this.spo2 = spo2; }

    public double getTemperatureC() { return temperatureC; }
    public void setTemperatureC(double temperatureC) { this.temperatureC = temperatureC; }

    public int getPainScale() { return painScale; }
    public void setPainScale(int painScale) { this.painScale = painScale; }

    public String getEcgRhythm() { return ecgRhythm; }
    public void setEcgRhythm(String ecgRhythm) { this.ecgRhythm = ecgRhythm; }

    public String getBpFormatted() {
        return systolicBp + "/" + diastolicBp + " mmHg";
    }

    public String getTempFormatted() {
        return String.format("%.1f °C", temperatureC);
    }

    public String getSummary() {
        return "HR " + heartRate + " | BP " + systolicBp + "/" + diastolicBp + " | SpO2 " + spo2 + "%";
    }

    public boolean isCritical() {
        return heartRate > 115 || heartRate < 45 || systolicBp < 90 || systolicBp > 180 || spo2 < 92 || temperatureC > 38.8;
    }
}
