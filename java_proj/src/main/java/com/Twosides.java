package com;

import java.util.Objects;

/**
 * Represents a drug interaction record with details about drugs, conditions, and statistical measures
 * twosides is an interaction between two drugs for some condition
 */
public class Twosides {
    private int drug1RxnormId; 
    private String drug1ConceptName; 
    private int drug2RxnormId; 
    private String drug2ConceptName; 
    private int conditionMeddraId; 
    private String conditionName; 
    private int a; 
    private int b; 
    private int c; 
    private int d; 
    private double prr; 
    private double prrError; 
    private double meanReportingFrequency; 
    private double severity; 
    private String severityClass;

    /**
     * Default constructor for the Twosides class
     */
    public Twosides() {}

    /**
     * Constructs a Twosides object with all attributes
     *
     * @param drug1RxnormId RxNorm ID of the first drug
     * @param drug1ConceptName Concept name of the first drug
     * @param drug2RxnormId RxNorm ID of the second drug
     * @param drug2ConceptName Concept name of the second drug
     * @param conditionMeddraId MedDRA ID of the condition
     * @param conditionName Name of the condition
     * @param a Value 'a' representing number of reports for the pair of drugs that report the side effect
     * @param b Value 'b' representing number of reports for the pair of drugs that do not report the side effect
     * @param c Value 'c' representing number of reports for other PSM matched drugs (including perhaps the single versions of drug 1 or drug 2) that report the side effect
     * @param d Value 'd' representing number of reports for other PSM matched drugs and other side effects
     * @param prr Proportional reporting ratio
     * @param prrError Error in the proportional reporting ratio
     * @param meanReportingFrequency Mean frequency of reporting
     * @param severity Severity of the interaction
     * @param severityClass Classification of the severity
     */
    public Twosides(int drug1RxnormId, String drug1ConceptName, int drug2RxnormId, String drug2ConceptName,
                    int conditionMeddraId, String conditionName, int a, int b, int c, int d,
                    double prr, double prrError, double meanReportingFrequency, double severity, String severityClass) {
        this.drug1RxnormId = drug1RxnormId;
        this.drug1ConceptName = drug1ConceptName;
        this.drug2RxnormId = drug2RxnormId;
        this.drug2ConceptName = drug2ConceptName;
        this.conditionMeddraId = conditionMeddraId;
        this.conditionName = conditionName;
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.prr = prr;
        this.prrError = prrError;
        this.meanReportingFrequency = meanReportingFrequency;
        this.severity = severity;
        this.severityClass = severityClass;
    }

    //Getters and Setters
    public int getDrug1RxnormId() { return drug1RxnormId; } public void setDrug1RxnormId(int drug1RxnormId) { this.drug1RxnormId = drug1RxnormId; }
    public String getDrug1ConceptName() { return drug1ConceptName; } public void setDrug1ConceptName(String drug1ConceptName) { this.drug1ConceptName = drug1ConceptName; }
    public int getDrug2RxnormId() { return drug2RxnormId; } public void setDrug2RxnormId(int drug2RxnormId) { this.drug2RxnormId = drug2RxnormId; }
    public String getDrug2ConceptName() { return drug2ConceptName; } public void setDrug2ConceptName(String drug2ConceptName) { this.drug2ConceptName = drug2ConceptName; }
    public int getConditionMeddraId() { return conditionMeddraId; } public void setConditionMeddraId(int conditionMeddraId) { this.conditionMeddraId = conditionMeddraId; }
    public String getConditionName() { return conditionName; } public void setConditionName(String conditionName) { this.conditionName = conditionName; }
    public int getA() { return a; } public void setA(int a) { this.a = a; }
    public int getB() { return b; } public void setB(int b) { this.b = b; }
    public int getC() { return c; } public void setC(int c) { this.c = c; }
    public int getD() { return d; } public void setD(int d) { this.d = d; }
    public double getPrr() { return prr; } public void setPrr(double prr) { this.prr = prr; }
    public double getPrrError() { return prrError; } public void setPrrError(double prrError) { this.prrError = prrError; }
    public double getMeanReportingFrequency() { return meanReportingFrequency; } public void setMeanReportingFrequency(double meanReportingFrequency) { this.meanReportingFrequency = meanReportingFrequency; }
    public double getSeverity() { return severity; } public void setSeverity(double severity) { this.severity = severity; }
    public String getSeverityClass() { return severityClass; } public void setSeverityClass(String severityClass) { this.severityClass = severityClass; }

    /**
     * Returns a string representation of the Twosides object
     *
     * @return a string describing the Twosides object
     */
    @Override
    public String toString() {
        return "Twosides{" +
               "drug1RxnormId=" + drug1RxnormId +
               ", drug1ConceptName='" + drug1ConceptName + '\'' +
               ", drug2RxnormId=" + drug2RxnormId +
               ", drug2ConceptName='" + drug2ConceptName + '\'' +
               ", conditionMeddraId=" + conditionMeddraId +
               ", conditionName='" + conditionName + '\'' +
               ", a=" + a +
               ", b=" + b +
               ", c=" + c +
               ", d=" + d +
               ", prr=" + prr +
               ", prrError=" + prrError +
               ", meanReportingFrequency=" + meanReportingFrequency +
               ", severity=" + severity +
               ", severityClass='" + severityClass + '\'' +
               '}';
    }
    
    /**
     * Compares this object with another for equality based on all attributes
     *
     * @param o the object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Twosides twosides = (Twosides) o;
        return drug1RxnormId == twosides.drug1RxnormId && 
               drug2RxnormId == twosides.drug2RxnormId &&
               conditionMeddraId == twosides.conditionMeddraId &&
               a == twosides.a && b == twosides.b &&
               c == twosides.c && d == twosides.d &&
               Double.compare(twosides.prr, prr) == 0 &&
               Double.compare(twosides.prrError, prrError) == 0 &&
               Double.compare(twosides.meanReportingFrequency, meanReportingFrequency) == 0 &&
               Double.compare(twosides.severity, severity) == 0 &&
               Objects.equals(drug1ConceptName, twosides.drug1ConceptName) &&
               Objects.equals(drug2ConceptName, twosides.drug2ConceptName) &&
               Objects.equals(conditionName, twosides.conditionName) &&
               Objects.equals(severityClass, twosides.severityClass);
    }

    /**
     * Generates a hash code for the Twosides object based on all attributes
     *
     * @return the hash code of the object
     */
    @Override
    public int hashCode() {
        return Objects.hash(drug1RxnormId, drug1ConceptName, drug2RxnormId, drug2ConceptName, 
                            conditionMeddraId, conditionName, a, b, c, d, prr, prrError, 
                            meanReportingFrequency, severity, severityClass);
    }
}
