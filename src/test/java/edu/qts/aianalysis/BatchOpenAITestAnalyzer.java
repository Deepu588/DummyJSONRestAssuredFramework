package edu.qts.aianalysis;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.qts.utils.EnvLoader;
import edu.qts.messagemanager.TeamsWebhookSender;
import edu.qts.pojos.SchemaInfo;
import edu.qts.aianalysis.YamlSchemaExtractor;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

/**
 *  AI Analysis with Request/Response/Schema validation
 */
public class BatchOpenAITestAnalyzer {
    private static final String GEMINI_API_URL = EnvLoader.getAPIUrl1();
    private final String apiKey;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long INITIAL_RETRY_DELAY_MS = 5000;
    
    public BatchOpenAITestAnalyzer() {
        this.apiKey = EnvLoader.getAPIKey();
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(60))
            .build();
        this.objectMapper = new ObjectMapper();
    }
    
    public Map<String, String> analyzeBatchTestFailures(List<TestFailureInfo> failedTests) {
        Map<String, String> analysisResults = new HashMap<>();
        
        if (failedTests == null || failedTests.isEmpty()) {
            System.out.println("📋 No failed tests to analyze");
            return analysisResults;
        }
        
        try {
            //System.out.println(" Starting AI analysis for " + failedTests.size() + " tests...");
            
            // Enrich with schema
            enrichFailuresWithSchemaInfo(failedTests);
            
            // Create prompt
            String ultimatePrompt = createUltimateAnalysisPrompt(failedTests);
           // System.out.println(" Prompt created: " + ultimatePrompt.length() + " characters");
            
            // Call Gemini
           // System.out.println(" Calling Gemini API...");
            String response = callGeminiWithRetry(ultimatePrompt);
            System.out.println(" Gemini API responded: " + response.length() + " characters");
            System.out.println("Response "+response);
            // Parse response
          //  System.out.println(" Parsing Gemini response...");
            analysisResults = parseEnhancedBatchResponse(response, failedTests);
            //System.out.println(" Parsed " + analysisResults.size() + " analysis results");
            
            //  Show what we got
            //System.out.println("\n📋 Analysis Results:");
            for (Map.Entry<String, String> entry : analysisResults.entrySet()) {
                System.out.println("   • " + entry.getKey() + ": " + 
                                 (entry.getValue() != null ? entry.getValue().length() + " chars" : "NULL"));
            }
            
            return analysisResults;
            
        } catch (Exception e) {
            System.err.println("❌ AI analysis failed: " + e.getMessage());
            e.printStackTrace();
            
            // Return error messages for all tests
            for (TestFailureInfo test : failedTests) {
                analysisResults.put(test.getTestMethodName(), 
                    "❌ AI Analysis failed: " + e.getMessage());
            }
            return analysisResults;
        }
    }
    /**
     *  Enriches failure objects with schema information from YAML
     */
    private void enrichFailuresWithSchemaInfo(List<TestFailureInfo> failedTests) {
        System.out.println("Enriching failures with schema and parameter data...");
        
        for (TestFailureInfo failure : failedTests) {
            try {
                // Extract schema from YAML
                SchemaInfo schemaInfo = 
                    YamlSchemaExtractor.extractSchemaInfo(
                        failure.getTestMethodName(),
                        failure.getEndpoint(),
                        failure.getHttpMethod()
                    );
                
                if (schemaInfo != null) {
                    failure.setRequestSchema(schemaInfo.getRequestSchema());
                    
                    // Set response schema based on actual status code
                    String statusCode = failure.getActualStatusCode() != null ? 
                                       failure.getActualStatusCode().toString() : "200";
                    failure.setResponseSchema(schemaInfo.getResponseSchemaForStatus(statusCode));
                    
                    //  Set expected parameters from schema
                    failure.setExpectedParametersFromSchema(schemaInfo);
                    
                    System.out.println(" Schema + Parameters loaded for: " + failure.getTestMethodName());
                } else {
                    System.out.println(" No schema found for: " + failure.getTestMethodName());
                }
                
            } catch (Exception e) {
                System.err.println(" Schema extraction failed for " + 
                                 failure.getTestMethodName() + ": " + e.getMessage());
            }
        }
    }
    
    /**
     *  Creates comprehensive prompt with all context
     */
    private String createUltimateAnalysisPrompt(List<TestFailureInfo> failedTests) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("You are an EXPERT REST API Test Automation Analyst with deep knowledge of:\n");
        prompt.append("- OpenAPI/Swagger specifications\n");
        prompt.append("- JSON Schema validation\n");
        prompt.append("- REST API best practices\n");
        prompt.append("- Common API testing pitfalls\n\n");
        
        prompt.append("=== YOUR TASK ===\n");
        prompt.append("Analyze each failed test by comparing:\n");
        prompt.append("1. ACTUAL Request sent vs EXPECTED Request Schema (from YAML)\n");
        prompt.append("2. ACTUAL Response received vs EXPECTED Response Schema (from YAML)\n");
        prompt.append("3. Status codes: Expected vs Actual\n");
        prompt.append("4. Field-level mismatches in JSON payloads\n");
        prompt.append("5. Schema validation errors\n");
        prompt.append("6. ACTUAL Request Headers vs EXPECTED Request Headers (from YAML)\n");
        prompt.append("7. ACTUAL Query Parameters vs EXPECTED Query Parameters (from YAML)\n");
        prompt.append("8. ACTUAL Path Parameters vs EXPECTED Path Parameters (from YAML)\n\n");
        
        
        for (int i = 0; i < failedTests.size(); i++) {
            TestFailureInfo test = failedTests.get(i);
            prompt.append("━".repeat(80)).append("\n");
            prompt.append(String.format("TEST_%d: %s\n", i + 1, test.getTestMethodName()));
            prompt.append("━".repeat(80)).append("\n\n");
            
            // Basic info
            prompt.append("📍 ENDPOINT: ").append(test.getHttpMethod()).append(" ")
                  .append(test.getEndpoint()).append("\n");
            prompt.append("📊 STATUS: Expected=").append(test.getExpectedStatusCode())
                  .append(", Actual=").append(test.getActualStatusCode()).append("\n\n");
            
            // Error message
            String errorMsg = test.getErrorMessage();
            if (errorMsg != null && errorMsg.length() > 400) {
                errorMsg = errorMsg.substring(0, 400) + "... [truncated]";
            }
            prompt.append("❌ ERROR MESSAGE:\n").append(errorMsg).append("\n\n");
            
            // Request headers section - show BOTH actual and expected
            if (test.getRequestHeaders() != null) {
                prompt.append("📤 ACTUAL REQUEST HEADERS SENT:\n");
                prompt.append("```\n").append(test.getRequestHeaders()).append("\n```\n\n");
            }
            
            if (test.getExpectedHeaders() != null) {
                prompt.append("📋 EXPECTED REQUEST HEADERS (from YAML):\n");
                prompt.append("```yaml\n").append(test.getExpectedHeaders()).append("\n```\n\n");
            }
            
            //  Query parameters section
            if (test.getExpectedQueryParams() != null) {
                prompt.append("📋 EXPECTED QUERY PARAMETERS (from YAML):\n");
                prompt.append("```yaml\n").append(test.getExpectedQueryParams()).append("\n```\n\n");
            }
            
            //  Path parameters section  
            if (test.getExpectedPathParams() != null) {
                prompt.append("📋 EXPECTED PATH PARAMETERS (from YAML):\n");
                prompt.append("```yaml\n").append(test.getExpectedPathParams()).append("\n```\n\n");
            }
            
            // Request body and schema 
            if (test.getRequestBody() != null) {
                prompt.append("📤 ACTUAL REQUEST BODY SENT:\n");
                String reqBody = truncateIfNeeded(test.getRequestBody(), 800);
                prompt.append("```json\n").append(reqBody).append("\n```\n\n");
            }
            
            if (test.getRequestSchema() != null) {
                prompt.append("📋 EXPECTED REQUEST SCHEMA (from YAML):\n");
                String reqSchema = truncateIfNeeded(test.getRequestSchema(), 600);
                prompt.append("```yaml\n").append(reqSchema).append("\n```\n\n");
            }
            
            // Response data and schema 
            if (test.getResponseBody() != null) {
                prompt.append("📥 ACTUAL RESPONSE RECEIVED:\n");
                String resBody = truncateIfNeeded(test.getResponseBody(), 800);
                prompt.append("```json\n").append(resBody).append("\n```\n\n");
            }
            
            if (test.getResponseSchema() != null) {
                prompt.append("📋 EXPECTED RESPONSE SCHEMA (from YAML):\n");
                String resSchema = truncateIfNeeded(test.getResponseSchema(), 600);
                prompt.append("```yaml\n").append(resSchema).append("\n```\n\n");
            }
            
            prompt.append("\n");
        }
        
        prompt.append("=== REQUIRED OUTPUT FORMAT ===\n");
        prompt.append("For each test, provide this EXACT structured analysis:\n\n");
        prompt.append("ANALYSIS_FOR_TEST_[NUMBER].[TEST NAME]\n:");
        prompt.append("Root Cause: [One clear sentence identifying the PRIMARY issue]\n");
        prompt.append("Header Issues: [Compare actual vs expected headers, or 'None']\n");
        prompt.append("Query Parameter Issues: [Compare actual vs expected query params, or 'None']\n");
        prompt.append("Path Parameter Issues: [Compare actual vs expected path params, or 'None']\n");
        prompt.append("Request Body Issues: [List specific field mismatches vs schema, or 'None']\n");
        prompt.append("Response Body Issues: [List specific field mismatches vs schema, or 'None']\n");
        prompt.append("Schema Violations: [Exact validation errors: missing fields, wrong types, etc.]\n");
        prompt.append("Status Code Analysis: [Why actual differs from expected, if applicable]\n");
        prompt.append("Exact Fix:\n");
        prompt.append("  • [Step 1 with exact field/code change]\n");
        prompt.append("  • [Step 2 with exact field/code change]\n");
        prompt.append("  • [Step 3 if needed]\n");
        prompt.append("Fix Location: [Specific file/method/line if identifiable from context]\n");
        prompt.append("Prevention: [How to avoid this issue in future]\n");
        prompt.append("END_ANALYSIS_[NUMBER]\n\n");
        
        return prompt.toString();
    }
    
    /**
     *  Truncate text to token limit
     */
    private String truncateIfNeeded(String text, int maxChars) {
        if (text == null) return "";
        if (text.length() <= maxChars) return text;
        return text.substring(0, maxChars) + "\n... [truncated to save tokens]";
    }
    
    /**
     *  Proper Gemini API call
     */
    private String callGemini(String prompt) throws Exception {
        String escapedPrompt = prompt
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
        
        String requestBody = String.format(
            "{\"contents\":[{\"parts\":[{\"text\":\"%s\"}]}]}",
            escapedPrompt
        );
      //  System.out.println("PROMPT ========"+prompt);
        String urlWithKey = GEMINI_API_URL;
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(urlWithKey))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .timeout(Duration.ofMinutes(3))  // Longer timeout for complex analysis
            .build();

        HttpResponse<String> response = httpClient.send(request, 
            HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 429) {
            throw new RuntimeException("Gemini API rate limit exceeded");
        } else if (response.statusCode() != 200) {
            System.err.println("Gemini Error Response: " + response.body());
            throw new RuntimeException("Gemini API failed: " + response.statusCode());
        }
        System.out.print("#".repeat(80));
        System.out.println("Response Body of the Prompt : "+response.body());
        System.out.println("#".repeat(80));
        TeamsWebhookSender.sendMessage(response.body());
        return response.body();
    }
    
    private String callGeminiWithRetry(String prompt) throws Exception {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                return callGemini(prompt);
            } catch (Exception e) {
                lastException = e;
                
                if (e.getMessage().contains("429")) {
                    long delayMs = INITIAL_RETRY_DELAY_MS * (long) Math.pow(2, attempt - 1);
                    System.out.println("🔄 Rate limit. Retry " + attempt + "/" + MAX_RETRY_ATTEMPTS + 
                                     " in " + (delayMs/1000) + "s...");
                    Thread.sleep(delayMs);
                } else {
                    Thread.sleep(2000);
                }
            }
        }
        
        throw new RuntimeException("Gemini failed after " + MAX_RETRY_ATTEMPTS + " attempts", lastException);
    }
    
    /**
     * Parse response with new fields
     */
    private Map<String, String> parseEnhancedBatchResponse(String response, List<TestFailureInfo> failedTests) 
            throws Exception {
        Map<String, String> results = new HashMap<>();
        
        JsonNode jsonNode = objectMapper.readTree(response);
        JsonNode candidates = jsonNode.path("candidates");
        
        if (candidates.isEmpty()) {
            throw new RuntimeException("No candidates in Gemini response");
        }
        
        String aiResponse = candidates.get(0)
                                     .path("content")
                                     .path("parts")
                                     .get(0)
                                     .path("text")
                                     .asText();
       // System.out.println("Ai Response In String Format :::---> "+aiResponse);
        String[] analysisBlocks = aiResponse.split("ANALYSIS_FOR_TEST_");
        
        for (int i = 1; i < analysisBlocks.length && i <= failedTests.size(); i++) {
            try {
                String block = analysisBlocks[i];
                String endMarker = "END_ANALYSIS_" + i;
                
                if (block.contains(endMarker)) {
                    String analysis = block.substring(0, block.indexOf(endMarker)).trim();
                    String testMethodName = failedTests.get(i - 1).getTestMethodName();
                    
                    String formattedAnalysis = formatUltimateAnalysis(analysis);
                  //  System.out.println("Formatted Analysis "+formattedAnalysis);
                    results.put(testMethodName, formattedAnalysis);
                    
                   // System.out.println("  Enhanced analysis for: " + testMethodName);
                } else {
                    String testMethodName = failedTests.get(i - 1).getTestMethodName();
                    results.put(testMethodName, "⚠️ Incomplete analysis\n\n" + 
                                              block.substring(0, Math.min(block.length(), 500)));
                }
            } catch (Exception e) {
                String testMethodName = failedTests.get(i - 1).getTestMethodName();
                results.put(testMethodName, "❌ Parse error: " + e.getMessage());
            }
        }
        
        for (TestFailureInfo test : failedTests) {
            if (!results.containsKey(test.getTestMethodName())) {
                results.put(test.getTestMethodName(), "⚠️ Analysis not generated");
            }
        }
        
        return results;
    }
    
    /**
     *  Format analysis with all new sections
     */
    private String formatUltimateAnalysis(String rawAnalysis) {
        StringBuilder formatted = new StringBuilder();
        formatted.append("🤖 GEMINI AI - DEEP ANALYSIS\n");
        formatted.append("━".repeat(60)).append("\n\n");
        
        String[] lines = rawAnalysis.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("Root Cause:")) {
                formatted.append("🔍 ").append(line).append("\n\n");
            } else if (line.startsWith("Header Issues:")) {
                formatted.append("📋 ").append(line).append("\n\n");
            } else if (line.startsWith("Query Parameter Issues:")) {
                formatted.append("❓ ").append(line).append("\n\n");
            } else if (line.startsWith("Path Parameter Issues:")) {
                formatted.append("🛣️  ").append(line).append("\n\n");
            } else if (line.startsWith("Request Body Issues:")) {
                formatted.append("📤 ").append(line).append("\n\n");
            } else if (line.startsWith("Response Body Issues:")) {
                formatted.append("📥 ").append(line).append("\n\n");
            } else if (line.startsWith("Schema Violations:")) {
                formatted.append("⚠️  ").append(line).append("\n");
            } else if (line.startsWith("Status Code Analysis:")) {
                formatted.append("📊 ").append(line).append("\n\n");
            } else if (line.startsWith("Exact Fix:")) {
                formatted.append("🔧 ").append(line).append("\n");
            } else if (line.startsWith("Fix Location:")) {
                formatted.append("📍 ").append(line).append("\n\n");
            } else if (line.startsWith("Prevention:")) {
                formatted.append("🛡️  ").append(line).append("\n\n");
            } else if (!line.isEmpty()) {
                formatted.append("   ").append(line).append("\n");
            }
        }
        
        formatted.append("━".repeat(60)).append("\n");
        return formatted.toString();
    }
}