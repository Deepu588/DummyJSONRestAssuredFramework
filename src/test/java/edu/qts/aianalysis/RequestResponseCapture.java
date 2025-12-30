package edu.qts.aianalysis;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Rest Assured Filter to capture request and response details
 *
 */
public class RequestResponseCapture implements Filter {
    
    private static final ThreadLocal<CapturedData> capturedData = new ThreadLocal<>();
    private static final ObjectMapper objectMapper = new ObjectMapper()
        .enable(SerializationFeature.INDENT_OUTPUT);
    
    @Override
    public Response filter(FilterableRequestSpecification requestSpec,
                          FilterableResponseSpecification responseSpec,
                          FilterContext ctx) {
        
        CapturedData data = new CapturedData();
        
        try {
            // Capture REQUEST details
            data.httpMethod = requestSpec.getMethod();
            data.endpoint = requestSpec.getURI();
            
            // Capture request body
            Object body = requestSpec.getBody();
            if (body != null) {
                data.requestBody = formatBody(body);
            }
            
            // Capture request headers (mask sensitive data)
            data.requestHeaders = captureHeaders(requestSpec.getHeaders());
            
            // Execute the actual request
            Response response = ctx.next(requestSpec, responseSpec);
            
            // Capture RESPONSE details
            data.statusCode = response.getStatusCode();
            data.responseTime = response.getTime();
            data.responseBody = response.getBody().asString();
            data.responseHeaders = captureResponseHeaders(response.getHeaders());
            
            // Store in ThreadLocal for access by listener
            capturedData.set(data);
            
            return response;
            
        } catch (Exception e) {
            System.err.println("Error capturing request/response: " + e.getMessage());
            // Still store whatever we captured
            capturedData.set(data);
            throw e;
        }
    }
    
    /**
     * Format request body (handle different types)
     */
    private String formatBody(Object body) {
        try {
            if (body instanceof String) {
                String bodyStr = (String) body;
                // Try to pretty-print if it's JSON
                try {
                    Object json = objectMapper.readValue(bodyStr, Object.class);
                    return objectMapper.writeValueAsString(json);
                } catch (Exception e) {
                    return bodyStr; // Not JSON, return as-is
                }
            } else {
                // Object - serialize to JSON
                return objectMapper.writeValueAsString(body);
            }
        } catch (Exception e) {
            return body.toString();
        }
    }
    
    /**
     * Capture request headers (mask sensitive data)
     */
    private String captureHeaders(io.restassured.http.Headers headers) {
        StringBuilder sb = new StringBuilder();
        
        headers.forEach(header -> {
            String name = header.getName();
            String value = header.getValue();
            
            // Mask sensitive headers
            if (name.toLowerCase().contains("auth") ||
                name.toLowerCase().contains("token") ||
                name.toLowerCase().contains("key") ||
                name.toLowerCase().contains("password")) {
                value = "***MASKED***";
            }
            
            sb.append(name).append(": ").append(value).append("\n");
        });
        
        return sb.toString();
    }
    
    /**
     * Capture response headers
     */
    private String captureResponseHeaders(io.restassured.http.Headers headers) {
        StringBuilder sb = new StringBuilder();
        
        headers.forEach(header -> {
            sb.append(header.getName()).append(": ")
              .append(header.getValue()).append("\n");
        });
        
        return sb.toString();
    }
    
    /**
     * Get captured data for current thread
     */
    public static CapturedData getCapturedData() {
        return capturedData.get();
    }
    
    /**
     * Clear captured data (call after processing)
     */
    public static void clear() {
        capturedData.remove();
    }
    
    /**
     * Data container for captured request/response
     */
    public static class CapturedData {
        public String httpMethod;
        public String endpoint;
        public String requestBody;
        public String requestHeaders;
        public String responseBody;
        public String responseHeaders;
        public Integer statusCode;
        public Long responseTime;
        
        public boolean isComplete() {
            return httpMethod != null && endpoint != null && statusCode != null;
        }
        
        @Override
        public String toString() {
            return "CapturedData{" +
                    "method='" + httpMethod + '\'' +
                    ", endpoint='" + endpoint + '\'' +
                    ", status=" + statusCode +
                    ", time=" + responseTime + "ms" +
                    '}';
        }
    }
}