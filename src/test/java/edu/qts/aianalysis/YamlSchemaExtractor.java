//package com.qmg.aianalysis;
//
//import org.yaml.snakeyaml.Yaml;
//import java.io.FileInputStream;
//import java.io.InputStream;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.*;
//
///**
// * Extracts request/response schemas from OpenAPI/Swagger YAML files
// * Works with YAML files in src/test/resources/yamls/
// */
//public class YamlSchemaExtractor {
//    private static final String YAML_FOLDER_PATH = "src/test/resources/yamls";
//    
//    /**
//     * Extracts complete schema information for a test method
//     * @param testMethodName Name of the test method (matches YAML file name)
//     * @param endpoint API endpoint path (e.g., /api/users)
//     * @param httpMethod HTTP method (GET, POST, PUT, DELETE, etc.)
//     * @return SchemaInfo object containing request/response schemas
//     */
//    public static SchemaInfo extractSchemaInfo(String testMethodName, String endpoint, String httpMethod) {
//        if (testMethodName == null || testMethodName.trim().isEmpty()) {
//            return null;
//        }
//        
//        try {
//            // Read YAML file using YamlReader
//            String yamlContent = YamlReader.readYamlForTest(testMethodName);
//            if (yamlContent == null) {
//                System.out.println("  ⚠️  No YAML file found for: " + testMethodName);
//                return null;
//            }
//            
//            // Parse YAML
//            Yaml yaml = new Yaml();
//            Map<String, Object> yamlMap = yaml.load(yamlContent);
//            
//            // Create schema info object
//            SchemaInfo schemaInfo = new SchemaInfo();
//            schemaInfo.setTestMethodName(testMethodName);
//            schemaInfo.setEndpoint(endpoint);
//            schemaInfo.setHttpMethod(httpMethod);
//            
//            // Extract schemas from paths section
//            extractSchemasFromYaml(yamlMap, schemaInfo, endpoint, httpMethod);
//            
//            return schemaInfo;
//            
//        } catch (Exception e) {
//            System.err.println("  ❌ Error extracting schema for " + testMethodName + ": " + e.getMessage());
//            e.printStackTrace();
//            return null;
//        }
//    }
//    
//    /**
//     * Extract schemas from parsed YAML map
//     */
//    private static void extractSchemasFromYaml(Map<String, Object> yamlMap, SchemaInfo schemaInfo, 
//                                               String endpoint, String httpMethod) {
//        try {
//            // Get paths section
//            Map<String, Object> paths = (Map<String, Object>) yamlMap.get("paths");
//            if (paths == null) {
//                System.out.println("    ⚠️  No 'paths' section in YAML");
//                return;
//            }
//            
//            // Find matching endpoint (try exact match first, then flexible)
//            Map<String, Object> endpointSpec = findEndpointSpec(paths, endpoint);
//            
//            if (endpointSpec != null && httpMethod != null) {
//                // Get method spec (get, post, put, delete, etc.)
//                Map<String, Object> methodSpec = (Map<String, Object>) 
//                    endpointSpec.get(httpMethod.toLowerCase());
//                
//                if (methodSpec != null) {
//                    // Extract request schema
//                    String requestSchema = extractRequestSchema(methodSpec);
//                    schemaInfo.setRequestSchema(requestSchema);
//                    
//                    // Extract response schemas
//                    Map<String, String> responseSchemas = extractResponseSchemas(methodSpec);
//                    schemaInfo.setResponseSchemas(responseSchemas);
//                    
//                    // Extract expected status codes
//                    List<String> statusCodes = extractStatusCodes(methodSpec);
//                    schemaInfo.setExpectedStatusCodes(statusCodes);
//                    
//                    System.out.println("    ✅ Extracted schemas for: " + httpMethod + " " + endpoint);
//                } else {
//                    System.out.println("    ⚠️  Method '" + httpMethod + "' not found in endpoint spec");
//                }
//            } else {
//                System.out.println("    ⚠️  Endpoint not found in YAML: " + endpoint);
//            }
//            
//        } catch (Exception e) {
//            System.err.println("    ❌ Error parsing YAML structure: " + e.getMessage());
//        }
//    }
//    
//    /**
//     * Find endpoint spec with flexible matching
//     */
//    private static Map<String, Object> findEndpointSpec(Map<String, Object> paths, String endpoint) {
//        if (endpoint == null) return null;
//        
//        // Try exact match first
//        if (paths.containsKey(endpoint)) {
//            return (Map<String, Object>) paths.get(endpoint);
//        }
//        
//        // Try case-insensitive match
//        for (Map.Entry<String, Object> entry : paths.entrySet()) {
//            if (entry.getKey().equalsIgnoreCase(endpoint)) {
//                return (Map<String, Object>) entry.getValue();
//            }
//        }
//        
//        // Try partial match (if endpoint contains the path)
//        for (Map.Entry<String, Object> entry : paths.entrySet()) {
//            if (endpoint.contains(entry.getKey()) || entry.getKey().contains(endpoint)) {
//                System.out.println("    ℹ️  Using partial match: " + entry.getKey());
//                return (Map<String, Object>) entry.getValue();
//            }
//        }
//        
//        return null;
//    }
//    
//    /**
//     * Extract request body schema from method spec
//     */
//    private static String extractRequestSchema(Map<String, Object> methodSpec) {
//        try {
//            Map<String, Object> requestBody = (Map<String, Object>) methodSpec.get("requestBody");
//            if (requestBody == null) {
//                return null; // No request body for this endpoint
//            }
//            
//            Map<String, Object> content = (Map<String, Object>) requestBody.get("content");
//            if (content == null) {
//                return null;
//            }
//            
//          
//            // Get first content type (usually application/json)
//            Object firstContent = content.values().iterator().next();
//            if (firstContent instanceof Map) {
//                Map<String, Object> contentMap = (Map<String, Object>) firstContent;
//                Object schema = contentMap.get("schema");
//                return formatSchema(schema);
//            }
//            
//        } catch (Exception e) {
//            System.err.println("      Error extracting request schema: " + e.getMessage());
//        }
//        return null;
//    }
//    
//    /**
//     * Extract response schemas for all status codes
//     */
//    private static Map<String, String> extractResponseSchemas(Map<String, Object> methodSpec) {
//        Map<String, String> responseSchemas = new HashMap<>();
//        
//        try {
//            Map<String, Object> responses = (Map<String, Object>) methodSpec.get("responses");
//            if (responses == null) {
//                return responseSchemas;
//            }
//            
//            // Iterate through each status code
//            for (Map.Entry<String, Object> entry : responses.entrySet()) {
//                String statusCode = entry.getKey();
//                Map<String, Object> responseSpec = (Map<String, Object>) entry.getValue();
//                
//                if (responseSpec != null) {
//                    Map<String, Object> content = (Map<String, Object>) responseSpec.get("content");
//                    if (content != null) {
//                        // Get first content type
//                        Object firstContent = content.values().iterator().next();
//                        if (firstContent instanceof Map) {
//                            Map<String, Object> contentMap = (Map<String, Object>) firstContent;
//                            Object schema = contentMap.get("schema");
//                            String formattedSchema = formatSchema(schema);
//                            if (formattedSchema != null) {
//                                responseSchemas.put(statusCode, formattedSchema);
//                            }
//                        }
//                    }
//                }
//            }
//            
//        } catch (Exception e) {
//            System.err.println("      Error extracting response schemas: " + e.getMessage());
//        }
//        
//        return responseSchemas;
//    }
//    
//    /**
//     * Extract expected status codes from method spec
//     */
//    private static List<String> extractStatusCodes(Map<String, Object> methodSpec) {
//        List<String> statusCodes = new ArrayList<>();
//        
//        try {
//            Map<String, Object> responses = (Map<String, Object>) methodSpec.get("responses");
//            if (responses != null) {
//                statusCodes.addAll(responses.keySet());
//            }
//        } catch (Exception e) {
//            System.err.println("      Error extracting status codes: " + e.getMessage());
//        }
//        
//        return statusCodes;
//    }
//    
//    /**
//     * Format schema object to readable string
//     */
//    private static String formatSchema(Object schema) {
//        if (schema == null) {
//            return null;
//        }
//        
//        try {
//            StringBuilder formatted = new StringBuilder();
//            formatSchemaRecursive(schema, formatted, 0);
//            return formatted.toString();
//        } catch (Exception e) {
//            return schema.toString();
//        }
//    }
//    
//    /**
//     * Recursively format schema with proper indentation
//     */
//    private static void formatSchemaRecursive(Object schema, StringBuilder sb, int indent) {
//        if (schema == null) {
//            return;
//        }
//        
//        if (schema instanceof Map) {
//            Map<String, Object> schemaMap = (Map<String, Object>) schema;
//            String indentation = "  ".repeat(indent);
//            
//            // Handle type
//            if (schemaMap.containsKey("type")) {
//                sb.append(indentation).append("type: ").append(schemaMap.get("type")).append("\n");
//            }
//            
//            // Handle description
//            if (schemaMap.containsKey("description")) {
//                sb.append(indentation).append("description: ").append(schemaMap.get("description")).append("\n");
//            }
//            
//            // Handle properties
//            if (schemaMap.containsKey("properties")) {
//                sb.append(indentation).append("properties:\n");
//                Map<String, Object> properties = (Map<String, Object>) schemaMap.get("properties");
//                
//                for (Map.Entry<String, Object> prop : properties.entrySet()) {
//                    sb.append(indentation).append("  ").append(prop.getKey()).append(":\n");
//                    formatSchemaRecursive(prop.getValue(), sb, indent + 2);
//                }
//            }
//            
//            // Handle required fields
//            if (schemaMap.containsKey("required")) {
//                List<String> required = (List<String>) schemaMap.get("required");
//                sb.append(indentation).append("required: ").append(required).append("\n");
//            }
//            
//            // Handle array items
//            if (schemaMap.containsKey("items")) {
//                sb.append(indentation).append("items:\n");
//                formatSchemaRecursive(schemaMap.get("items"), sb, indent + 1);
//            }
//            
//            // Handle enum
//            if (schemaMap.containsKey("enum")) {
//                sb.append(indentation).append("enum: ").append(schemaMap.get("enum")).append("\n");
//            }
//            
//            // Handle references
//            if (schemaMap.containsKey("$ref")) {
//                sb.append(indentation).append("$ref: ").append(schemaMap.get("$ref")).append("\n");
//            }
//            
//            // Handle format
//            if (schemaMap.containsKey("format")) {
//                sb.append(indentation).append("format: ").append(schemaMap.get("format")).append("\n");
//            }
//            
//        } else {
//            sb.append(schema.toString()).append("\n");
//        }
//    }
//    
//    /**
//     * Schema information container class
//     */
//    public static class SchemaInfo {
//        private String testMethodName;
//        private String endpoint;
//        private String httpMethod;
//        private String requestSchema;
//        private Map<String, String> responseSchemas;
//        private List<String> expectedStatusCodes;
//        
//        public SchemaInfo() {
//            this.responseSchemas = new HashMap<>();
//            this.expectedStatusCodes = new ArrayList<>();
//        }
//        
//        // Getters
//        public String getTestMethodName() {
//            return testMethodName;
//        }
//        
//        public String getEndpoint() {
//            return endpoint;
//        }
//        
//        public String getHttpMethod() {
//            return httpMethod;
//        }
//        
//        public String getRequestSchema() {
//            return requestSchema;
//        }
//        
//        public Map<String, String> getResponseSchemas() {
//            return responseSchemas;
//        }
//        
//        public List<String> getExpectedStatusCodes() {
//            return expectedStatusCodes;
//        }
//        
//        /**
//         * Get response schema for specific status code
//         */
//        public String getResponseSchemaForStatus(String statusCode) {
//            if (responseSchemas == null) {
//                return null;
//            }
//            return responseSchemas.get(statusCode);
//        }
//        
//        // Setters
//        public void setTestMethodName(String testMethodName) {
//            this.testMethodName = testMethodName;
//        }
//        
//        public void setEndpoint(String endpoint) {
//            this.endpoint = endpoint;
//        }
//        
//        public void setHttpMethod(String httpMethod) {
//            this.httpMethod = httpMethod;
//        }
//        
//        public void setRequestSchema(String requestSchema) {
//            this.requestSchema = requestSchema;
//        }
//        
//        public void setResponseSchemas(Map<String, String> responseSchemas) {
//            this.responseSchemas = responseSchemas;
//        }
//        
//        public void setExpectedStatusCodes(List<String> expectedStatusCodes) {
//            this.expectedStatusCodes = expectedStatusCodes;
//        }
//        
//        @Override
//        public String toString() {
//            StringBuilder sb = new StringBuilder();
//            sb.append("SchemaInfo for: ").append(testMethodName).append("\n");
//            sb.append("Endpoint: ").append(httpMethod).append(" ").append(endpoint).append("\n");
//            sb.append("Expected Status Codes: ").append(expectedStatusCodes).append("\n\n");
//            
//            if (requestSchema != null) {
//                sb.append("Request Schema:\n").append(requestSchema).append("\n");
//            }
//            
//            if (responseSchemas != null && !responseSchemas.isEmpty()) {
//                sb.append("Response Schemas:\n");
//                responseSchemas.forEach((code, schema) -> {
//                    sb.append("  Status ").append(code).append(":\n");
//                    sb.append("    ").append(schema.replace("\n", "\n    ")).append("\n");
//                });
//            }
//            
//            return sb.toString();
//        }
//    }
//}



package edu.qts.aianalysis;

import org.yaml.snakeyaml.Yaml;

import edu.qts.pojos.ParameterInfo;
import edu.qts.pojos.SchemaInfo;

import java.util.*;

/**
 *  Now extracts headers, query params, and path params too
 */
public class YamlSchemaExtractor {
    private static final String YAML_FOLDER_PATH = "src/test/resources/yamls";
    
    public static SchemaInfo extractSchemaInfo(String testMethodName, String endpoint, String httpMethod) {
        if (testMethodName == null || testMethodName.trim().isEmpty()) {
            return null;
        }
        
        try {
            String yamlContent = YamlReader.readYamlForTest(testMethodName);
            if (yamlContent == null) {
                System.out.println("    No YAML file found for: " + testMethodName);
                return null;
            }
            
            Yaml yaml = new Yaml();
            Map<String, Object> yamlMap = yaml.load(yamlContent);
            
            SchemaInfo schemaInfo = new SchemaInfo();
            schemaInfo.setTestMethodName(testMethodName);
            schemaInfo.setEndpoint(endpoint);
            schemaInfo.setHttpMethod(httpMethod);
            
            extractSchemasFromYaml(yamlMap, schemaInfo, endpoint, httpMethod);
            
            return schemaInfo;
            
        } catch (Exception e) {
            System.err.println("  ❌ Error extracting schema for " + testMethodName + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    private static void extractSchemasFromYaml(Map<String, Object> yamlMap, SchemaInfo schemaInfo, 
                                               String endpoint, String httpMethod) {
        try {
            Map<String, Object> paths = (Map<String, Object>) yamlMap.get("paths");
            if (paths == null) {
                System.out.println("      No 'paths' section in YAML");
                return;
            }
            
            Map<String, Object> endpointSpec = findEndpointSpec(paths, endpoint);
            
            if (endpointSpec != null && httpMethod != null) {
                Map<String, Object> methodSpec = (Map<String, Object>) 
                    endpointSpec.get(httpMethod.toLowerCase());
                
                if (methodSpec != null) {
                    // Extract request body schema
                    String requestSchema = extractRequestSchema(methodSpec);
                    schemaInfo.setRequestSchema(requestSchema);
                    
                    // Extract response schemas
                    Map<String, String> responseSchemas = extractResponseSchemas(methodSpec);
                    schemaInfo.setResponseSchemas(responseSchemas);
                    
                    // Extract expected status codes
                    List<String> statusCodes = extractStatusCodes(methodSpec);
                    schemaInfo.setExpectedStatusCodes(statusCodes);
                    
                    //  Extract parameters (headers, query, path)
                    Map<String, List<ParameterInfo>> parameters = extractParameters(methodSpec);
                    schemaInfo.setParameters(parameters);
                    
                    System.out.println("     Extracted schemas + parameters for: " + httpMethod + " " + endpoint);
                } else {
                    System.out.println("      Method '" + httpMethod + "' not found in endpoint spec");
                }
            } else {
                System.out.println("      Endpoint not found in YAML: " + endpoint);
            }
            
        } catch (Exception e) {
            System.err.println("     Error parsing YAML structure: " + e.getMessage());
        }
    }
    
    //  Extract all parameters
    private static Map<String, List<ParameterInfo>> extractParameters(Map<String, Object> methodSpec) {
        Map<String, List<ParameterInfo>> parameters = new HashMap<>();
        parameters.put("header", new ArrayList<>());
        parameters.put("query", new ArrayList<>());
        parameters.put("path", new ArrayList<>());
        
        try {
            List<Map<String, Object>> paramsList = (List<Map<String, Object>>) methodSpec.get("parameters");
            if (paramsList == null) {
                return parameters; // No parameters defined
            }
            
            for (Map<String, Object> param : paramsList) {
                String paramType = (String) param.get("in"); // header, query, path
                String name = (String) param.get("name");
                Boolean required = (Boolean) param.get("required");
                String description = (String) param.get("description");
                
                // Get schema for type information
                Map<String, Object> schema = (Map<String, Object>) param.get("schema");
                String type = schema != null ? (String) schema.get("type") : "string";
                Object defaultValue = schema != null ? schema.get("default") : null;
                Object exampleValue = schema != null ? schema.get("example") : param.get("example");
                
                ParameterInfo paramInfo = new ParameterInfo();
                paramInfo.setName(name);
                paramInfo.setType(type);
                paramInfo.setRequired(required != null ? required : false);
                paramInfo.setDescription(description);
                paramInfo.setDefaultValue(defaultValue);
                paramInfo.setExample(exampleValue);
                
                if (paramType != null && parameters.containsKey(paramType)) {
                    parameters.get(paramType).add(paramInfo);
                }
            }
            
        } catch (Exception e) {
            System.err.println("      Error extracting parameters: " + e.getMessage());
        }
        
        return parameters;
    }
    
    private static Map<String, Object> findEndpointSpec(Map<String, Object> paths, String endpoint) {
        if (endpoint == null) return null;
        
        if (paths.containsKey(endpoint)) {
            return (Map<String, Object>) paths.get(endpoint);
        }
        
        for (Map.Entry<String, Object> entry : paths.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(endpoint)) {
                return (Map<String, Object>) entry.getValue();
            }
        }
        
        for (Map.Entry<String, Object> entry : paths.entrySet()) {
            if (endpoint.contains(entry.getKey()) || entry.getKey().contains(endpoint)) {
                System.out.println("    ℹ️  Using partial match: " + entry.getKey());
                return (Map<String, Object>) entry.getValue();
            }
        }
        
        return null;
    }
    
    private static String extractRequestSchema(Map<String, Object> methodSpec) {
        try {
            Map<String, Object> requestBody = (Map<String, Object>) methodSpec.get("requestBody");
            if (requestBody == null) {
                return null;
            }
            
            Map<String, Object> content = (Map<String, Object>) requestBody.get("content");
            if (content == null) {
                return null;
            }
            
            Object firstContent = content.values().iterator().next();
            if (firstContent instanceof Map) {
                Map<String, Object> contentMap = (Map<String, Object>) firstContent;
                Object schema = contentMap.get("schema");
                return formatSchema(schema);
            }
            
        } catch (Exception e) {
            System.err.println("      Error extracting request schema: " + e.getMessage());
        }
        return null;
    }
    
    private static Map<String, String> extractResponseSchemas(Map<String, Object> methodSpec) {
        Map<String, String> responseSchemas = new HashMap<>();
        
        try {
            Map<String, Object> responses = (Map<String, Object>) methodSpec.get("responses");
            if (responses == null) {
                return responseSchemas;
            }
            
            for (Map.Entry<String, Object> entry : responses.entrySet()) {
                String statusCode = entry.getKey();
                Map<String, Object> responseSpec = (Map<String, Object>) entry.getValue();
                
                if (responseSpec != null) {
                    Map<String, Object> content = (Map<String, Object>) responseSpec.get("content");
                    if (content != null) {
                        Object firstContent = content.values().iterator().next();
                        if (firstContent instanceof Map) {
                            Map<String, Object> contentMap = (Map<String, Object>) firstContent;
                            Object schema = contentMap.get("schema");
                            String formattedSchema = formatSchema(schema);
                            if (formattedSchema != null) {
                                responseSchemas.put(statusCode, formattedSchema);
                            }
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("      Error extracting response schemas: " + e.getMessage());
        }
        
        return responseSchemas;
    }
    
    private static List<String> extractStatusCodes(Map<String, Object> methodSpec) {
        List<String> statusCodes = new ArrayList<>();
        
        try {
            Map<String, Object> responses = (Map<String, Object>) methodSpec.get("responses");
            if (responses != null) {
                statusCodes.addAll(responses.keySet());
            }
        } catch (Exception e) {
            System.err.println("      Error extracting status codes: " + e.getMessage());
        }
        
        return statusCodes;
    }
    
    private static String formatSchema(Object schema) {
        if (schema == null) {
            return null;
        }
        
        try {
            StringBuilder formatted = new StringBuilder();
            formatSchemaRecursive(schema, formatted, 0);
            return formatted.toString();
        } catch (Exception e) {
            return schema.toString();
        }
    }
    
    private static void formatSchemaRecursive(Object schema, StringBuilder sb, int indent) {
        if (schema == null) {
            return;
        }
        
        if (schema instanceof Map) {
            Map<String, Object> schemaMap = (Map<String, Object>) schema;
            String indentation = "  ".repeat(indent);
            
            if (schemaMap.containsKey("type")) {
                sb.append(indentation).append("type: ").append(schemaMap.get("type")).append("\n");
            }
            
            if (schemaMap.containsKey("description")) {
                sb.append(indentation).append("description: ").append(schemaMap.get("description")).append("\n");
            }
            
            if (schemaMap.containsKey("properties")) {
                sb.append(indentation).append("properties:\n");
                Map<String, Object> properties = (Map<String, Object>) schemaMap.get("properties");
                
                for (Map.Entry<String, Object> prop : properties.entrySet()) {
                    sb.append(indentation).append("  ").append(prop.getKey()).append(":\n");
                    formatSchemaRecursive(prop.getValue(), sb, indent + 2);
                }
            }
            
            if (schemaMap.containsKey("required")) {
                List<String> required = (List<String>) schemaMap.get("required");
                sb.append(indentation).append("required: ").append(required).append("\n");
            }
            
            if (schemaMap.containsKey("items")) {
                sb.append(indentation).append("items:\n");
                formatSchemaRecursive(schemaMap.get("items"), sb, indent + 1);
            }
            
            if (schemaMap.containsKey("enum")) {
                sb.append(indentation).append("enum: ").append(schemaMap.get("enum")).append("\n");
            }
            
            if (schemaMap.containsKey("$ref")) {
                sb.append(indentation).append("$ref: ").append(schemaMap.get("$ref")).append("\n");
            }
            
            if (schemaMap.containsKey("format")) {
                sb.append(indentation).append("format: ").append(schemaMap.get("format")).append("\n");
            }
            
        } else {
            sb.append(schema.toString()).append("\n");
        }
    }
}