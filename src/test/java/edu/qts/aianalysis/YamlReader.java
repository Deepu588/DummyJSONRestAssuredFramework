package edu.qts.aianalysis;

import java.awt.datatransfer.SystemFlavorMap;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * Smart YAML reading with schema extraction support
 */
public class YamlReader {
    private static final String YAML_FOLDER_PATH = "src/test/resources/yamls";
    private static final int MAX_YAML_LINES = 500; // Token optimization
    
    /**
     * Reads YAML file for test
     */
    public static String readYamlForTest(String testMethodName) {
        if (testMethodName == null || testMethodName.trim().isEmpty()) {
            return null;
        }
        
        try {
            Path yamlFolder = Paths.get(YAML_FOLDER_PATH);
            
            if (!Files.exists(yamlFolder)) {
                System.err.println("⚠️  YAML folder not found: " + YAML_FOLDER_PATH);
                return null;
            }
            
            String yamlFileName = findMatchingYamlFile(yamlFolder, testMethodName);
       //     System.out.println("YAML FILE NAME "+yamlFileName);
            if (yamlFileName != null) {
                Path yamlFilePath = yamlFolder.resolve(yamlFileName);
                String yamlContent = readAndTruncateYaml(yamlFilePath);
            //    System.out.println("  📄 Loaded YAML: " + yamlFileName + 
              //                   " (" + countLines(yamlContent) + " lines)");
               // System.out.print("&".repeat(40)+yamlContent);
                return yamlContent;
            } else {
                System.out.println("  ⚠️  No YAML file for: " + testMethodName);
                return null;
            }
            
        } catch (Exception e) {
            System.err.println("  ❌ YAML read error for " + testMethodName + ": " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Finds matching YAML file 
     */
    private static String findMatchingYamlFile(Path yamlFolder, String testMethodName) throws IOException {
        try (Stream<Path> files = Files.list(yamlFolder)) {
            return files
                .filter(Files::isRegularFile)
                .map(Path::getFileName)
                .map(Path::toString)
                .filter(fileName -> fileName.toLowerCase().endsWith(".yaml") || 
                                   fileName.toLowerCase().endsWith(".yml"))
                .filter(fileName -> {
                    String fileNameWithoutExt = fileName.replaceFirst("\\.(yaml|yml)$", "");
                    return fileNameWithoutExt.equalsIgnoreCase(testMethodName);
                })
                .findFirst()
                .orElse(null);
        }
    }
    
    /**
     * Reads and truncates YAML if too long
     */
    private static String readAndTruncateYaml(Path yamlFilePath) throws IOException {
        StringBuilder yamlContent = new StringBuilder();
        int lineCount = 0;
        boolean truncated = false;
        
        try (Stream<String> lines = Files.lines(yamlFilePath)) {
            for (String line : (Iterable<String>) lines::iterator) {
                if (lineCount >= MAX_YAML_LINES) {
                    yamlContent.append("\n# ... [YAML truncated - showing first ")
                              .append(MAX_YAML_LINES)
                              .append(" lines to save tokens]\n");
                    truncated = true;
                    break;
                }
                yamlContent.append(line).append("\n");
                lineCount++;
            }
        }
        
        if (truncated) {
            System.out.println("    ℹ️  YAML truncated from " + lineCount + " to " + 
                             MAX_YAML_LINES + " lines");
        }
        
        return yamlContent.toString();
    }
    
    /**
     * Extracts relevant YAML section based on endpoint/error
     */
    public static String extractRelevantYamlSection(String fullYaml, String errorMessage) {
        if (fullYaml == null || errorMessage == null) {
            return fullYaml;
        }
        
        String endpoint = extractEndpointFromError(errorMessage);
        
        if (endpoint == null) {
            return fullYaml;
        }
        
        // Extract section containing the endpoint
        String[] lines = fullYaml.split("\n");
        StringBuilder relevantSection = new StringBuilder();
        boolean inRelevantSection = false;
        int baseIndent = -1;
        int extractedLines = 0;
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            
            // Found endpoint
            if (line.contains(endpoint)) {
                inRelevantSection = true;
                baseIndent = getIndentLevel(line);
                
                // Add context (10 lines before)
                int contextStart = Math.max(0, i - 10);
                for (int j = contextStart; j < i; j++) {
                    relevantSection.append(lines[j]).append("\n");
                }
                
                relevantSection.append(line).append("\n");
                extractedLines++;
                continue;
            }
            
            // Continue extracting related content
            if (inRelevantSection) {
                int currentIndent = getIndentLevel(line);
                
                // Include nested content or blank lines
                if (line.trim().isEmpty() || currentIndent > baseIndent) {
                    relevantSection.append(line).append("\n");
                    extractedLines++;
                    
                    // Limit extraction
                    if (extractedLines > 100) {
                        relevantSection.append("# ... [rest omitted]\n");
                        break;
                    }
                } else if (currentIndent <= baseIndent && !line.trim().isEmpty()) {
                    break;
                }
            }
        }
        
        if (relevantSection.length() > 0) {
            System.out.println("    ✂️  Extracted " + extractedLines + 
                             " relevant lines (reduced from " + lines.length + ")");
            return relevantSection.toString();
        }
        
        //  return truncated full YAML
        return fullYaml.substring(0, Math.min(fullYaml.length(), 3000));
    }
    
    private static String extractEndpointFromError(String errorMessage) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(/[a-zA-Z0-9/_\\-{}]+)");
        java.util.regex.Matcher matcher = pattern.matcher(errorMessage);
        
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
    
    private static int getIndentLevel(String line) {
        int indent = 0;
        for (char c : line.toCharArray()) {
            if (c == ' ') indent++;
            else break;
        }
        return indent;
    }
    
    private static int countLines(String text) {
        if (text == null) return 0;
        return text.split("\n").length;
    }
}