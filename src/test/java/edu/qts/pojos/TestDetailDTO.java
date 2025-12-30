package edu.qts.pojos;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TestDetailDTO {
    private String testName;
    private String className;
    private String status;
    private long duration;
    private Date startTime;
    private Date endTime;
    private String description;
    private List<String> groups;
    private List<String> authors;
    private List<String> devices;
    private List<String> categories;
    private int priority;
    private String errorMessage;
    private String stackTrace;
    private List<String> parameters;
    
    // Getters and Setters
    public String getTestName() { return testName; }
    public void setTestName(String testName) { this.testName = testName; }
    
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public long getDuration() { return duration; }
    public void setDuration(long duration) { this.duration = duration; }
    
    public Date getStartTime() { return startTime; }
    public void setStartTime(Date startTime) { this.startTime = startTime; }
    
    public Date getEndTime() { return endTime; }
    public void setEndTime(Date endTime) { this.endTime = endTime; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public List<String> getGroups() { return groups; }
    public void setGroups(List<String> groups) { this.groups = groups; }
    
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    
    public String getStackTrace() { return stackTrace; }
    public void setStackTrace(String stackTrace) { this.stackTrace = stackTrace; }
    
    public List<String> getParameters() { return parameters; }
    public void setParameters(List<String> parameters) { this.parameters = parameters; }
    
    public void setAuthors(List<String> authors) { this.authors = authors; }
    public List<String> getAuthors(){
    	return authors;
    }
    
    public void setDevices(List<String> devices) { this.devices = devices; }
    public List<String> getDevices(){ return devices;}
    
    public void setCategories(List<String> categories) { this.categories = categories; }
    public List<String> getCategories(){
    	return categories;
    }

	
}