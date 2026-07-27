package com.gestiontaches.service.dto;

import java.io.Serializable;
import java.util.List;

public class BurndownData implements Serializable {

    private List<String> dates;
    private List<Long> ideal;
    private List<Long> actual;

    public BurndownData() {}

    public BurndownData(List<String> dates, List<Long> ideal, List<Long> actual) {
        this.dates = dates;
        this.ideal = ideal;
        this.actual = actual;
    }

    public List<String> getDates() {
        return dates;
    }

    public void setDates(List<String> dates) {
        this.dates = dates;
    }

    public List<Long> getIdeal() {
        return ideal;
    }

    public void setIdeal(List<Long> ideal) {
        this.ideal = ideal;
    }

    public List<Long> getActual() {
        return actual;
    }

    public void setActual(List<Long> actual) {
        this.actual = actual;
    }
}
