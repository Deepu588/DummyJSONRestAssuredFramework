package edu.qts.aianalysis;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import edu.qts.utils.ConfigManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Enhanced Report Generator - Uses ONLY methods that exist in TestFailureInfo
 */
public class TestFailureReportGenerator {
    private final String reportFolder;
    private final ObjectMapper objectMapper;

    public TestFailureReportGenerator() {
        this.reportFolder = ConfigManager.getProjectDirectoryForFailureReport();
        this.objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        createReportDirectory();
    }

    private void createReportDirectory() {
        try {
            Path reportPath = Paths.get(reportFolder);
            if (!Files.exists(reportPath)) {
                Files.createDirectories(reportPath);
            }
        } catch (Exception e) {
            System.err.println("Failed to create report directory: " + e.getMessage());
        }
    }

    /**
     * Generate comprehensive text report with all context
     */
    public String generateTxtReport(List<TestFailureInfo> failures) {
        if (failures.isEmpty()) {
            System.out.println("📋 No failures to generate report for");
            return null;
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String fileName = String.format("AI-test-failures-%s.txt", timestamp);
        String filePath = reportFolder + "/" + fileName;

        try {
            writeTextReport(failures, filePath);
            System.out.println("📄 Report generated successfully: " + filePath);
            return filePath;
        } catch (Exception e) {
            System.err.println("Failed to generate report: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private void writeTextReport(List<TestFailureInfo> failures, String filePath) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath))) {
        	System.out.println("%".repeat(50));
        	System.out.println(failures);
        	System.out.println("%".repeat(50));

            // ============ HEADER ============
            writer.write("=".repeat(100));
            writer.newLine();
            writer.write("    🤖 REST ASSURED TEST FAILURE ANALYSIS REPORT WITH AI");
            writer.newLine();
            writer.write("=".repeat(100));
            writer.newLine();
            writer.write("Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            writer.newLine();
            writer.write("Total Failed Tests: " + failures.size());
            writer.newLine();
            writer.write("Analysis Method: Batch AI Processing with Complete Context");
            writer.newLine();
            
            // Context availability stats
            long withRequestResponse = failures.stream()
                .filter(f -> f.getRequestBody() != null && f.getResponseBody() != null)
                .count();
            long withSchema = failures.stream()
                .filter(f -> f.getRequestSchema() != null || f.getResponseSchema() != null)
                .count();
            
            writer.write("Tests with Request/Response: " + withRequestResponse + "/" + failures.size());
            writer.newLine();
            writer.write("Tests with Schema Data: " + withSchema + "/" + failures.size());
            writer.newLine();
            writer.write("=".repeat(100));
            writer.newLine();
            writer.newLine();

            // ============ SUMMARY SECTION ============
            writer.write("📊 FAILURE SUMMARY");
            writer.newLine();
            writer.write("-".repeat(50));
            writer.newLine();
            
            for (int i = 0; i < failures.size(); i++) {
                TestFailureInfo failure = failures.get(i);
                String endpoint = failure.getEndpoint() != null ? failure.getEndpoint() : "N/A";
                String method = failure.getHttpMethod() != null ? failure.getHttpMethod() : "";
                String status = failure.getActualStatusCode() != null ? 
                               failure.getActualStatusCode().toString() : "?";
                
                writer.write(String.format("%d. %s [%s %s -> %s] [%s]", 
                    i + 1, 
                    failure.getTestMethodName(),
                    method,
                    truncate(endpoint, 30),
                    status,
                    failure.getTimestamp()));
                writer.newLine();
            }
            writer.newLine();
            writer.write("=".repeat(100));
            writer.newLine();
            writer.newLine();

            // ============ DETAILED ANALYSIS ============
            for (int i = 0; i < failures.size(); i++) {
                writeDetailedFailureAnalysis(writer, failures.get(i), i + 1);
            }
            
            // ============ FOOTER ============
            writeReportFooter(writer);
        }
    }

    private void writeDetailedFailureAnalysis(BufferedWriter writer, 
            TestFailureInfo failure, int index) 
throws IOException {

writer.write(String.format("🔍 FAILURE ANALYSIS #%d", index));
writer.newLine();
writer.write("-".repeat(80));
writer.newLine();

// -------- BASIC INFO --------
writer.write("Test Method: " + failure.getTestMethodName());
writer.newLine();
writer.write("Test Class: " + failure.getTestClassName());
writer.newLine();
writer.write("Full Test Name: " + failure.getFullTestName());
writer.newLine();
writer.write("Failure Time: " + failure.getTimestamp());
writer.newLine();

if (failure.getYamlFileName() != null) {
writer.write("YAML File: " + failure.getYamlFileName());
writer.newLine();
}
writer.newLine();

// -------- API DETAILS --------
if (failure.getHttpMethod() != null || failure.getEndpoint() != null) {
writer.write("🌐 API CALL DETAILS");
writer.newLine();
writer.write("-".repeat(40));
writer.newLine();

if (failure.getHttpMethod() != null) {
writer.write("HTTP Method: " + failure.getHttpMethod());
writer.newLine();
}
if (failure.getEndpoint() != null) {
writer.write("Endpoint: " + failure.getEndpoint());
writer.newLine();
}
if (failure.getExpectedStatusCode() != null) {
writer.write("Expected Status Code: " + failure.getExpectedStatusCode());
writer.newLine();
}
if (failure.getActualStatusCode() != null) {
writer.write("Actual Status Code: " + failure.getActualStatusCode());
writer.newLine();
}
if (failure.getResponseTime() != null) {
writer.write("Response Time: " + failure.getResponseTime() + " ms");
writer.newLine();
}
writer.newLine();
}

// -------- AI ANALYSIS SECTION 
writer.write("🤖 AI ANALYSIS:");
writer.newLine();
writer.write("-".repeat(40));
writer.newLine();

String aiAnalysis = failure.getAiAnalysis(); // Now can be null

// Debug output
System.out.println("DEBUG [Report] Test: " + failure.getTestMethodName());
System.out.println("DEBUG [Report] AI Analysis: " + 
(aiAnalysis == null ? "NULL" : 
aiAnalysis.substring(0, Math.min(50, aiAnalysis.length())) + "..."));

if (isValidAIAnalysis(aiAnalysis)) {
writer.write(aiAnalysis);
writer.newLine();
} else {
writer.write("⚠️  AI analysis not available for this test.");
writer.newLine();
writer.newLine();
writer.write("Possible reasons:");
writer.newLine();
writer.write("• Gemini API rate limit reached");
writer.newLine();
writer.write("• Network connectivity issues");
writer.newLine();
writer.write("• API service temporarily unavailable");
writer.newLine();
writer.write("• Analysis parsing failed");
writer.newLine();
writer.newLine();
writer.write("💡 Manual Analysis Suggestions:");
writer.newLine();
writer.write("• Check error message and stack trace below");
writer.newLine();
writer.write("• Verify API endpoint and parameters");
writer.newLine();
writer.write("• Review request/response data if available");
writer.newLine();
writer.write("• Compare actual vs expected schemas");
writer.newLine();
}
writer.newLine();
writer.newLine();
        // -------- REQUEST DATA --------
        if (failure.getRequestBody() != null || failure.getRequestHeaders() != null) {
            writer.write("📤 REQUEST DATA SENT");
            writer.newLine();
            writer.write("-".repeat(40));
            writer.newLine();
            
            if (failure.getRequestHeaders() != null) {
                writer.write("Headers:");
                writer.newLine();
                writer.write(indent(failure.getRequestHeaders(), 2));
                writer.newLine();
            }
            
            if (failure.getRequestBody() != null) {
                writer.write("Body:");
                writer.newLine();
                writer.write(indent(formatJson(failure.getRequestBody()), 2));
                writer.newLine();
            }
            writer.newLine();
        }

        // -------- REQUEST SCHEMA --------
        if (failure.getRequestSchema() != null) {
            writer.write("📋 EXPECTED REQUEST SCHEMA (from YAML)");
            writer.newLine();
            writer.write("-".repeat(40));
            writer.newLine();
            writer.write(indent(failure.getRequestSchema(), 2));
            writer.newLine();
            writer.newLine();
        }

        // -------- RESPONSE DATA --------
        if (failure.getResponseBody() != null || failure.getResponseHeaders() != null) {
            writer.write("📥 RESPONSE DATA RECEIVED");
            writer.newLine();
            writer.write("-".repeat(40));
            writer.newLine();
            
            if (failure.getResponseHeaders() != null) {
                writer.write("Headers:");
                writer.newLine();
                writer.write(indent(failure.getResponseHeaders(), 2));
                writer.newLine();
            }
            
            if (failure.getResponseBody() != null) {
                writer.write("Body:");
                writer.newLine();
                writer.write(indent(formatJson(failure.getResponseBody()), 2));
                writer.newLine();
            }
            writer.newLine();
        }

        // -------- RESPONSE SCHEMA --------
        if (failure.getResponseSchema() != null) {
            writer.write("📋 EXPECTED RESPONSE SCHEMA (from YAML)");
            writer.newLine();
            writer.write("-".repeat(40));
            writer.newLine();
            writer.write(indent(failure.getResponseSchema(), 2));
            writer.newLine();
            writer.newLine();
        }

        // -------- SCHEMA VALIDATION ERRORS --------
        if (failure.getSchemaValidationErrors() != null && 
            !failure.getSchemaValidationErrors().trim().isEmpty()) {
            writer.write("⚠️  SCHEMA VALIDATION ERRORS");
            writer.newLine();
            writer.write("-".repeat(40));
            writer.newLine();
            writer.write(indent(failure.getSchemaValidationErrors(), 2));
            writer.newLine();
            writer.newLine();
        }

        // -------- ERROR MESSAGE --------
        writer.write("❌ ERROR DETAILS:");
        writer.newLine();
        writer.write("-".repeat(40));
        writer.newLine();
        
        String errorMessage = failure.getErrorMessage();
        if (errorMessage != null && !errorMessage.trim().isEmpty()) {
            String[] errorLines = errorMessage.split("\n");
            for (String line : errorLines) {
                writer.write(line);
                writer.newLine();
            }
        } else {
            writer.write("No error message available");
            writer.newLine();
        }
        writer.newLine();

        // -------- STACK TRACE (First 20 lines) --------
        String stackTrace = failure.getStackTrace();
        if (stackTrace != null && !stackTrace.trim().isEmpty()) {
            writer.write("📋 STACK TRACE (First 20 lines):");
            writer.newLine();
            writer.write("-".repeat(40));
            writer.newLine();
            
            String[] stackLines = stackTrace.split("\n");
            int linesToShow = Math.min(20, stackLines.length);
            
            for (int i = 0; i < linesToShow; i++) {
                writer.write(stackLines[i]);
                writer.newLine();
            }
            
            if (stackLines.length > 20) {
                writer.write("... (truncated - see logs for full stack trace)");
                writer.newLine();
            }
            writer.newLine();
        }

        writer.write("=".repeat(100));
        writer.newLine();
        writer.newLine();
    }

    private void writeReportFooter(BufferedWriter writer) throws IOException {
        writer.write("📋 REPORT FOOTER");
        writer.newLine();
        writer.write("-".repeat(50));
        writer.newLine();
        writer.write("This comprehensive report was generated using AI-powered test failure analysis.");
        writer.newLine();
        writer.write("Analysis includes: Request/Response data, YAML schemas, and AI insights.");
        writer.newLine();
        writer.write("For questions or issues, please contact the QA automation team.");
        writer.newLine();
        writer.write("Report generated at: " + 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        writer.newLine();
        writer.write("=".repeat(100));
    }
    private boolean isValidAIAnalysis(String analysis) {
        if (analysis == null || analysis.trim().isEmpty()) {
            return false;
        }
        
        String trimmed = analysis.trim();
        if (trimmed.startsWith("⚠️") || 
            trimmed.startsWith("❌") ||
            trimmed.startsWith("AI analysis not") ||
            trimmed.startsWith("AI Analysis Failed")) {
            return false;
        }
        
        if (analysis.contains("GEMINI AI") || 
            analysis.contains("Root Cause:") ||
            analysis.contains("Request Issues:") ||
            analysis.contains("Response Issues:") ||
            analysis.contains("Schema Violations:") ||
            analysis.contains("Exact Fix:") ||
            analysis.contains("🔍") ||
            analysis.contains("━━━━")) {
            return true;
        }
        
       
        return analysis.length() > 150;
    }

    
    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength - 3) + "..." : text;
    }

    private String indent(String text, int spaces) {
        if (text == null) return "";
        String prefix = " ".repeat(spaces);
        return prefix + text.replace("\n", "\n" + prefix);
    }

    private String formatJson(String json) {
        if (json == null) return "";
        try {
            Object jsonObj = objectMapper.readValue(json, Object.class);
            return objectMapper.writeValueAsString(jsonObj);
        } catch (Exception e) {
            return json;
        }
    }
}