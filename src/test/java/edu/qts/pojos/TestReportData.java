package edu.qts.pojos;

import java.util.Date;
import java.util.List;

public class TestReportData {
    private String suiteName;
    private Date suiteStartTime;
    private Date suiteEndTime;
    private int totalTests;
    private int passed;
    private int failed;
    private int skipped;
    private long totalDuration;
    private double passRate;
    private List<TestDetailDTO> tests;
    
    // Getters and Setters
    public String getSuiteName() { return suiteName; }
    public void setSuiteName(String suiteName) { this.suiteName = suiteName; }
    
    public Date getSuiteStartTime() { return suiteStartTime; }
    public void setSuiteStartTime(Date suiteStartTime) { this.suiteStartTime = suiteStartTime; }
    
    public Date getSuiteEndTime() { return suiteEndTime; }
    public void setSuiteEndTime(Date suiteEndTime) { this.suiteEndTime = suiteEndTime; }
    
    public int getTotalTests() { return totalTests; }
    public void setTotalTests(int totalTests) { this.totalTests = totalTests; }
    
    public int getPassed() { return passed; }
    public void setPassed(int passed) { this.passed = passed; }
    
    public int getFailed() { return failed; }
    public void setFailed(int failed) { this.failed = failed; }
    
    public int getSkipped() { return skipped; }
    public void setSkipped(int skipped) { this.skipped = skipped; }
    
    public long getTotalDuration() { return totalDuration; }
    public void setTotalDuration(long totalDuration) { this.totalDuration = totalDuration; }
    
    public double getPassRate() { return passRate; }
    public void setPassRate(double passRate) { this.passRate = passRate; }
    
    public List<TestDetailDTO> getTests() { return tests; }
    public void setTests(List<TestDetailDTO> tests) { this.tests = tests; }
}