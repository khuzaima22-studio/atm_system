package org.example;

public class ATMTechnician {
    private String technicianId;
    private String technicianPin;

    public ATMTechnician(String technicianId, String technicianPin) {
        this.technicianId = technicianId;
        this.technicianPin = technicianPin;
    }

    public String getTechnicianPin() {
        return technicianPin;
    }
}