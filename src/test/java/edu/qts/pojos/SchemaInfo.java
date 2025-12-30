package edu.qts.pojos;

import edu.qts.pojos.ParameterInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public  class SchemaInfo {
    private String testMethodName;
    private String endpoint;
    private String httpMethod;
    private String requestSchema;
    private Map<String, String> responseSchemas;
    private List<String> expectedStatusCodes;
    private Map<String, List<ParameterInfo>> parameters;
    public SchemaInfo() {
        this.responseSchemas = new HashMap<>();
        this.expectedStatusCodes = new ArrayList<>();
        this.parameters = new HashMap<>();
    }
    
    // Getters and Setters
    public String getTestMethodName() { return testMethodName; }
    public void setTestMethodName(String testMethodName) { this.testMethodName = testMethodName; }
    
    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    
    public String getHttpMethod() { return httpMethod; }
    public void setHttpMethod(String httpMethod) { this.httpMethod = httpMethod; }
    
    public String getRequestSchema() { return requestSchema; }
    public void setRequestSchema(String requestSchema) { this.requestSchema = requestSchema; }
    
    public Map<String, String> getResponseSchemas() { return responseSchemas; }
    public void setResponseSchemas(Map<String, String> responseSchemas) { this.responseSchemas = responseSchemas; }
    
    public List<String> getExpectedStatusCodes() { return expectedStatusCodes; }
    public void setExpectedStatusCodes(List<String> expectedStatusCodes) { this.expectedStatusCodes = expectedStatusCodes; }
    
    public Map<String, List<ParameterInfo>> getParameters() { return parameters; }
    public void setParameters(Map<String, List<ParameterInfo>> parameters) { this.parameters = parameters; }
    
    public List<ParameterInfo> getHeaderParameters() { 
        return parameters.getOrDefault("header", new ArrayList<>()); 
    }
    
    public List<ParameterInfo> getQueryParameters() { 
        return parameters.getOrDefault("query", new ArrayList<>()); 
    }
    
    public List<ParameterInfo> getPathParameters() { 
        return parameters.getOrDefault("path", new ArrayList<>()); 
    }
    
    public String getResponseSchemaForStatus(String statusCode) {
        return responseSchemas != null ? responseSchemas.get(statusCode) : null;
    }
    
    public String getFormattedParameters() {
        StringBuilder sb = new StringBuilder();
        
        List<ParameterInfo> headers = getHeaderParameters();
        if (!headers.isEmpty()) {
            sb.append("Headers:\n");
            for (ParameterInfo param : headers) {
                sb.append("  ").append(param.toString()).append("\n");
            }
        }
        
        List<ParameterInfo> queryParams = getQueryParameters();
        if (!queryParams.isEmpty()) {
            sb.append("Query Parameters:\n");
            for (ParameterInfo param : queryParams) {
                sb.append("  ").append(param.toString()).append("\n");
            }
        }
        
        List<ParameterInfo> pathParams = getPathParameters();
        if (!pathParams.isEmpty()) {
            sb.append("Path Parameters:\n");
            for (ParameterInfo param : pathParams) {
                sb.append("  ").append(param.toString()).append("\n");
            }
        }
        
        return sb.toString();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SchemaInfo for: ").append(testMethodName).append("\n");
        sb.append("Endpoint: ").append(httpMethod).append(" ").append(endpoint).append("\n");
        sb.append("Expected Status Codes: ").append(expectedStatusCodes).append("\n\n");
        
        String formattedParams = getFormattedParameters();
        if (!formattedParams.trim().isEmpty()) {
            sb.append(formattedParams).append("\n");
        }
        
        if (requestSchema != null) {
            sb.append("Request Schema:\n").append(requestSchema).append("\n");
        }
        
        if (responseSchemas != null && !responseSchemas.isEmpty()) {
            sb.append("Response Schemas:\n");
            responseSchemas.forEach((code, schema) -> {
                sb.append("  Status ").append(code).append(":\n");
                sb.append("    ").append(schema.replace("\n", "\n    ")).append("\n");
            });
        }
        
        return sb.toString();
    }

}