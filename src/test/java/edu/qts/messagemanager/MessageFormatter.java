package edu.qts.messagemanager;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class MessageFormatter {

	public static String buildFullAnalysisMessage(String aiOutput) {
System.out.println(aiOutput);
	    Map<String, String> tests = splitTests(aiOutput);

	    StringBuilder sb = new StringBuilder();
	    sb.append("🤖 **AI ANALYSIS SUMMARY**\n\n");

	    for (Map.Entry<String, String> entry : tests.entrySet()) {

	        String key = entry.getKey();
	        String block = entry.getValue();

	        String[] parts = key.split("\\|");
	        System.out.println("Printing Parts "+Arrays.toString(parts));
	        String testId = parts[0];       
	        String testName = parts[1];    

	        // Extract all fields
	        String rootCause     = extractSingle(block, "Root Cause:");
	        String headerIssues  = extractSingle(block, "Header Issues:");
	        String queryIssues   = extractSingle(block, "Query Parameter Issues:");
	        String pathIssues    = extractSingle(block, "Path Parameter Issues:");
	        String requestIssues = extractSingle(block, "Request Body Issues:");

	        String responseIssues = extractBetween(block, "Response Body Issues:", "Schema Violations:");
	        String schemaIssues   = extractBetween(block, "Schema Violations:", "Status Code Analysis:");
	        String exactFix       = extractBetween(block, "Exact Fix:", "Fix Location:");
	        String fixLocation    = extractBetween(block, "Fix Location:", "Prevention:");
	        String prevention     = extractBetween(block, "Prevention:", "END_ANALYSIS");

	        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
	        sb.append("🧪 **")
	          .append(testId.replace("ANALYSIS_FOR_", "")) 
	          .append(" — ")
	          .append(testName)
	          .append("**\n");
	        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");

	        sb.append("🔍 **Root Cause:**\n").append(rootCause).append("\n\n");
	        sb.append("📋 **Header Issues:**\n").append(headerIssues).append("\n\n");
	        sb.append("❓ **Query Parameter Issues:**\n").append(queryIssues).append("\n\n");
	        sb.append("🛣 **Path Parameter Issues:**\n").append(pathIssues).append("\n\n");
	        sb.append("📤 **Request Body Issues:**\n").append(requestIssues).append("\n\n");
	        sb.append("📥 **Response Body Issues:**\n").append(responseIssues).append("\n\n");
	        sb.append("⚠ **Schema Violations:**\n").append(schemaIssues).append("\n\n");
	        sb.append("🔧 **Exact Fix:**\n").append(exactFix).append("\n\n");
	        sb.append("📍 **Fix Location:**\n").append(fixLocation).append("\n\n");
	        sb.append("🛡 **Prevention:**\n").append(prevention).append("\n\n");
	    }

	    return sb.toString();
	}

	
	
	
	
	public static String extractBetween(String block, String startLabel, String endLabel) {
	    try {
	        int start = block.indexOf(startLabel);
	        if (start == -1) return "Not Available";

	        start += startLabel.length();

	        int end = block.indexOf(endLabel, start);
	        if (end == -1) end = block.length();

	        return block.substring(start, end).trim();

	    } catch (Exception e) {
	        return "Not Available";
	    }
	}

	public static String extractSingle(String block, String label) {
	    try {
	        int start = block.indexOf(label);
	        if (start == -1) return "Not Available";

	        start += label.length();

	        int nextLabel = block.indexOf("\n", start);
	        if (nextLabel == -1) nextLabel = block.length();

	        return block.substring(start, nextLabel).trim();

	    } catch (Exception e) {
	        return "Not Available";
	    }
	}


	public static Map<String, String> splitTests(String aiOutput) {

	    Map<String, String> tests = new LinkedHashMap<>();

	    String[] parts = aiOutput.split("END_ANALYSIS_");

	    for (String part : parts) {

	        if (part.contains("ANALYSIS_FOR_TEST_")) {

	            // Extract Test ID
	            int idStart = part.indexOf("ANALYSIS_FOR_TEST_");
	            int dotIndex = part.indexOf(".", idStart); 
	            int newlineAfterDot = part.indexOf("\n", dotIndex);

	            String testId = "ANALYSIS_FOR_TEST_UNKNOWN";
	            String testName = "Unknown Test";

	            // Extract test ID (before dot)
	            if (dotIndex != -1) {
	                testId = part.substring(idStart, dotIndex).trim();
	            }

	            // Extract test name (after dot, before newline or colon)
	            if (dotIndex != -1) {

	                int nameStart = dotIndex + 1;

	                int boundary1 = part.indexOf("\n", nameStart);
	                int boundary2 = part.indexOf(":", nameStart);

	                int end = Integer.MAX_VALUE;

	                if (boundary1 != -1) end = Math.min(end, boundary1);
	                if (boundary2 != -1) end = Math.min(end, boundary2);

	                if (end == Integer.MAX_VALUE) {
	                    testName = part.substring(nameStart).trim();
	                } else {
	                    testName = part.substring(nameStart, end).trim();
	                }
	            }

	            tests.put(testId + "|" + testName, part);
	        }
	    }
System.out.println(tests);
	    return tests;
	}



	public static String formatTestOutput(
	        String testId,
	        String rootCause,
	        String headerIssues,
	        String queryIssues,
	        String pathIssues,
	        String requestIssues,
	        String responseIssues,
	        String schemaIssues,
	        String fix,
	        String fixLocation,
	        String prevention) {

	    StringBuilder sb = new StringBuilder();

	    sb.append("🔍 **").append(testId).append("**\n");
	    sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");

	    sb.append("🧩 **Root Cause:**\n")
	            .append(rootCause).append("\n\n");

	    sb.append("📋 **Header Issues:**\n")
	            .append(headerIssues).append("\n\n");

	    sb.append("❓ **Query Parameter Issues:**\n")
	            .append(queryIssues).append("\n\n");

	    sb.append("🛣 **Path Parameter Issues:**\n")
	            .append(pathIssues).append("\n\n");

	    sb.append("📤 **Request Body Issues:**\n")
	            .append(requestIssues).append("\n\n");

	    sb.append("📥 **Response Body Issues:**\n")
	            .append(responseIssues).append("\n\n");

	    sb.append("⚠ **Schema Violations:**\n")
	            .append(schemaIssues).append("\n\n");

	    sb.append("🔧 **Exact Fix:**\n")
	            .append(fix).append("\n\n");

	    sb.append("📍 **Fix Location:**\n")
	            .append(fixLocation).append("\n\n");

	    sb.append("🛡 **Prevention:**\n")
	            .append(prevention).append("\n\n");

	    sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");

	    return sb.toString();
	}

}
