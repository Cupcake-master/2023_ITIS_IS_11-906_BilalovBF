package ru.itis;


public class TFIDF {
    private String term;
    private double tfValue;
    private double idfValue;
    private double value;

    public TFIDF(String term, double tfValue, double idfValue, double value) {
        this.term = term;
        this.tfValue = tfValue;
        this.idfValue = idfValue;
        this.value = value;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public double getTfValue() {
        return tfValue;
    }

    public void setTfValue(double tfValue) {
        this.tfValue = tfValue;
    }

    public double getIdfValue() {
        return idfValue;
    }

    public void setIdfValue(double idfValue) {
        this.idfValue = idfValue;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
