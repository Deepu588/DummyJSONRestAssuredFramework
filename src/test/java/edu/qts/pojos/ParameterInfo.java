package edu.qts.pojos;
public  class ParameterInfo {
    private String name;
    private String type;
    private boolean required;
    private String description;
    private Object defaultValue;
    private Object example;
    
    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public boolean isRequired() { return required; }
    public void setRequired(boolean required) { this.required = required; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Object getDefaultValue() { return defaultValue; }
    public void setDefaultValue(Object defaultValue) { this.defaultValue = defaultValue; }
    
    public Object getExample() { return example; }
    public void setExample(Object example) { this.example = example; }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(" (").append(type).append(")");
        if (required) sb.append(" [REQUIRED]");
        if (description != null) sb.append(" - ").append(description);
        if (example != null) sb.append(" Example: ").append(example);
        return sb.toString();
    }
}
