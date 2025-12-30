package edu.qts.aianalysis;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import edu.qts.pojos.ParameterInfo;
import edu.qts.pojos.SchemaInfo;

/**
 * fields for expected headers, query params, path params
 */
public class TestFailureInfo {
    // Original fields
    private String testMethodName;
    private String testClassName;
    private String errorMessage;
    private String aiAnalysis;
    private String stackTrace;
    private LocalDateTime timestamp;
    
    //  context fields
    private String yamlFileName;
    private String endpoint;
    private String httpMethod;
    private Integer expectedStatusCode;
    private Integer actualStatusCode;
    
    // Request/Response capture fields
    private String requestBody;
    private String requestHeaders;
    private String responseBody;
    private String responseHeaders;
    private Long responseTime;
    
    // Schema information fields
    private String requestSchema;
    private String responseSchema;
    private String schemaValidationErrors;
    
    private String expectedHeaders;      // Expected headers from YAML
    private String expectedQueryParams;  // Expected query parameters from YAML
    private String expectedPathParams;   // Expected path parameters from YAML
    
    // Constructor
    public TestFailureInfo(String testMethodName, String testClassName, 
                          String errorMessage, String aiAnalysis, String stackTrace) {
        this.testMethodName = testMethodName;
        this.testClassName = testClassName;
        this.errorMessage = errorMessage;
        this.aiAnalysis = aiAnalysis;
        this.stackTrace = stackTrace;
        this.timestamp = LocalDateTime.now();
        
        this.endpoint = extractEndpointFromError(errorMessage);
        this.httpMethod = extractHttpMethodFromError(errorMessage);
    }
    
    // ============================================
    // ORIGINAL GETTERS
    // ============================================
    
    public String getTestMethodName() { return testMethodName; }
    public String getTestClassName() { return testClassName; }
    public String getErrorMessage() { return errorMessage != null ? errorMessage : "No error message available"; }
    public String getAiAnalysis() { return aiAnalysis != null ? aiAnalysis : "AI analysis not available"; }
    public String getStackTrace() { return stackTrace != null ? stackTrace : ""; }
    public String getFullTestName() { return testClassName + "." + testMethodName; }
    public String getTimestamp() { return timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")); }
    public LocalDateTime getTimestampAsDateTime() { return timestamp; }
    
    //  getters
    public String getYamlFileName() { return yamlFileName; }
    public String getEndpoint() { return endpoint; }
    public String getHttpMethod() { return httpMethod; }
    public Integer getExpectedStatusCode() { return expectedStatusCode; }
    public Integer getActualStatusCode() { return actualStatusCode; }
    public String getRequestBody() { return requestBody; }
    public String getRequestHeaders() { return requestHeaders; }
    public String getResponseBody() { return responseBody; }
    public String getResponseHeaders() { return responseHeaders; }
    public Long getResponseTime() { return responseTime; }
    public String getRequestSchema() { return requestSchema; }
    public String getResponseSchema() { return responseSchema; }
    public String getSchemaValidationErrors() { return schemaValidationErrors; }
    
    public String getExpectedHeaders() { return expectedHeaders; }
    public String getExpectedQueryParams() { return expectedQueryParams; }
    public String getExpectedPathParams() { return expectedPathParams; }
    
    // ============================================
    // SETTERS
    // ============================================
    
    public void setTestMethodName(String testMethodName) { this.testMethodName = testMethodName; }
    public void setTestClassName(String testClassName) { this.testClassName = testClassName; }
    public void setErrorMessage(String errorMessage) { 
        this.errorMessage = errorMessage;
        this.endpoint = extractEndpointFromError(errorMessage);
        this.httpMethod = extractHttpMethodFromError(errorMessage);
    }
    public void setAiAnalysis(String aiAnalysis) { this.aiAnalysis = aiAnalysis; }
    public void setStackTrace(String stackTrace) { this.stackTrace = stackTrace; }
    public void setYamlFileName(String yamlFileName) { this.yamlFileName = yamlFileName; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    public void setHttpMethod(String httpMethod) { this.httpMethod = httpMethod; }
    public void setExpectedStatusCode(Integer expectedStatusCode) { this.expectedStatusCode = expectedStatusCode; }
    public void setActualStatusCode(Integer actualStatusCode) { this.actualStatusCode = actualStatusCode; }
    public void setRequestBody(String requestBody) { this.requestBody = requestBody; }
    public void setRequestHeaders(String requestHeaders) { this.requestHeaders = requestHeaders; }
    public void setResponseBody(String responseBody) { this.responseBody = responseBody; }
    public void setResponseHeaders(String responseHeaders) { this.responseHeaders = responseHeaders; }
    public void setResponseTime(Long responseTime) { this.responseTime = responseTime; }
    public void setRequestSchema(String requestSchema) { this.requestSchema = requestSchema; }
    public void setResponseSchema(String responseSchema) { this.responseSchema = responseSchema; }
    public void setSchemaValidationErrors(String schemaValidationErrors) { this.schemaValidationErrors = schemaValidationErrors; }
    
    public void setExpectedHeaders(String expectedHeaders) { this.expectedHeaders = expectedHeaders; }
    public void setExpectedQueryParams(String expectedQueryParams) { this.expectedQueryParams = expectedQueryParams; }
    public void setExpectedPathParams(String expectedPathParams) { this.expectedPathParams = expectedPathParams; }
    
    // ============================================
    // UTILITY METHODS
    // ============================================
    
    public void setRequestResponseData(String requestBody, String requestHeaders,
                                      String responseBody, String responseHeaders, 
                                      Long responseTime) {
        this.requestBody = requestBody;
        this.requestHeaders = requestHeaders;
        this.responseBody = responseBody;
        this.responseHeaders = responseHeaders;
        this.responseTime = responseTime;
    }
    
    public boolean hasCompleteContext() {
        return requestBody != null && responseBody != null && 
               requestSchema != null && responseSchema != null;
    }
    
    public boolean hasRequestResponseData() {
        return requestBody != null && responseBody != null;
    }
    
    public boolean hasSchemaData() {
        return requestSchema != null || responseSchema != null;
    }
    
    //  Check if parameter data is available
    public boolean hasParameterData() {
        return expectedHeaders != null || expectedQueryParams != null || expectedPathParams != null;
    }
    
    public String getContextSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("TestFailureInfo[");
        summary.append("test=").append(testMethodName);
        summary.append(", endpoint=").append(httpMethod).append(" ").append(endpoint);
        summary.append(", status=").append(expectedStatusCode).append("->").append(actualStatusCode);
        summary.append(", hasRequest=").append(requestBody != null);
        summary.append(", hasResponse=").append(responseBody != null);
        summary.append(", hasSchema=").append(requestSchema != null);
        summary.append(", hasParams=").append(hasParameterData());
        summary.append("]");
        return summary.toString();
    }
    
    // ============================================
    // PRIVATE HELPER METHODS
    // ============================================
    
    private String extractEndpointFromError(String errorMessage) {
        if (errorMessage == null) return null;
        
        try {
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                "(?:https?://[^\\s]+)|(/[a-zA-Z0-9/_\\-{}]+)"
            );
            java.util.regex.Matcher matcher = pattern.matcher(errorMessage);
            
            if (matcher.find()) {
                String matched = matcher.group();
                if (matched.startsWith("http")) {
                    try {
                        return new java.net.URL(matched).getPath();
                    } catch (Exception e) {
                        return matched;
                    }
                }
                return matched;
            }
        } catch (Exception e) {
        }
        return null;
    }
    
    private String extractHttpMethodFromError(String errorMessage) {
        if (errorMessage == null) return null;
        
        String[] methods = {"GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS"};
        String upperError = errorMessage.toUpperCase();
        
        for (String method : methods) {
            if (upperError.contains(method)) {
                return method;
            }
        }
        return null;
    }
    
    public void setExpectedParametersFromSchema(SchemaInfo schemaInfo) {
        if (schemaInfo != null) {
            // Format headers for display
            List<ParameterInfo> expectedHeaders = schemaInfo.getHeaderParameters();
            if (!expectedHeaders.isEmpty()) {
                this.expectedHeaders = formatParameterList(expectedHeaders);
            }
            
            // Format query parameters
            List<ParameterInfo> expectedQueryParams = schemaInfo.getQueryParameters();
            if (!expectedQueryParams.isEmpty()) {
                this.expectedQueryParams = formatParameterList(expectedQueryParams);
            }
            
            // Format path parameters  
            List<ParameterInfo> expectedPathParams = schemaInfo.getPathParameters();
            if (!expectedPathParams.isEmpty()) {
                this.expectedPathParams = formatParameterList(expectedPathParams);
            }
        }
    }

    private String formatParameterList(List<ParameterInfo> parameters) {
        StringBuilder sb = new StringBuilder();
        for (ParameterInfo param : parameters) {
            sb.append("• ").append(param.getName())
              .append(" (").append(param.getType())
              .append(param.isRequired() ? ", required" : ", optional")
              .append(")");
            if (param.getDescription() != null) {
                sb.append(" - ").append(param.getDescription());
            }
            if (param.getExample() != null) {
                sb.append(" [example: ").append(param.getExample()).append("]");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
    //
    //TestFailureInfo{testMethodName='refreshAccessToken', testClassName='com.qmg.tests.AuthTests', 
    //endpoint='POST https://api.medullar.dev/auth/v1/auth/refresh/', statusCode=null->400, hasRequestData=true,
    //hasResponseData=true, hasSchema=false, hasParameters=false, aiAnalysis=Not Available, timestamp=2025-11-26T16:25:52.423339700}
    //
    @Override
    public String toString() {
        return "TestFailureInfo{" +
                "testMethodName='" + testMethodName + '\'' +
                ", testClassName='" + testClassName + '\'' +
                ", endpoint='" + httpMethod + " " + endpoint + '\'' +
                ", statusCode=" + expectedStatusCode + "->" + actualStatusCode +
                ", hasRequestData=" + (requestBody != null) +
                ", hasResponseData=" + (responseBody != null) +
                ", hasSchema=" + (requestSchema != null) +
                ", hasParameters=" + hasParameterData() +
                ", aiAnalysis=" + (aiAnalysis != null ? "Available" : "Not Available") +
                ", timestamp=" + timestamp +
                '}';
    }
}