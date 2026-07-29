package com.gestiontaches.service.dto;

import java.io.Serializable;

public class VelocityReportDTO implements Serializable {

    private int tachesPrevues;
    private int tachesTerminees;
    private int pourcentage;
    private int tachesReportees;

    public int getTachesPrevues() {
        return tachesPrevues;
    }

    public void setTachesPrevues(int tachesPrevues) {
        this.tachesPrevues = tachesPrevues;
    }

    public int getTachesTerminees() {
        return tachesTerminees;
    }

    public void setTachesTerminees(int tachesTerminees) {
        this.tachesTerminees = tachesTerminees;
    }

    public int getPourcentage() {
        return pourcentage;
    }

    public void setPourcentage(int pourcentage) {
        this.pourcentage = pourcentage;
    }

    public int getTachesReportees() {
        return tachesReportees;
    }

    public void setTachesReportees(int tachesReportees) {
        this.tachesReportees = tachesReportees;
    }

    @Override
    public String toString() {
        return (
            "VelocityReportDTO{" +
            "tachesPrevues=" +
            tachesPrevues +
            ", tachesTerminees=" +
            tachesTerminees +
            ", pourcentage=" +
            pourcentage +
            ", tachesReportees=" +
            tachesReportees +
            "}"
        );
    }
}
