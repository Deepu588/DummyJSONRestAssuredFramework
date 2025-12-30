package edu.qts.utils;
import edu.qts.pojos.TestDetailDTO;
import edu.qts.aianalysis.RequestResponseCapture;
import edu.qts.pojos.TestReportData;
import java.awt.Desktop;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.model.Author;
import com.aventstack.extentreports.model.Category;
import com.aventstack.extentreports.model.Device;
import com.aventstack.extentreports.model.Test;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.qts.aianalysis.BatchOpenAITestAnalyzer;
import edu.qts.aianalysis.TestFailureInfo;
import edu.qts.aianalysis.TestFailureReportGenerator;

public class MyListeners implements ITestListener, ISuiteListener {
    private ExtentReports extent;
    private String reportPath;
    ExtentSparkReporter sparkReporter;
    private static boolean isChatbotEnabled=Boolean.parseBoolean(ConfigManager.getValue("enable-chatbot"));
    private static boolean isAiAnalysisEnabled=Boolean.parseBoolean(ConfigManager.getValue("enable-analysis-for-failedTests"));
    private static ThreadLocal<ExtentTest> test = new ThreadLocal<>();
    private static TestReportData reportData = new TestReportData();
    private static Map<ITestResult, ExtentTest> testResultMap = new ConcurrentHashMap<>();

    private BatchOpenAITestAnalyzer batchAiAnalyzer;
    private TestFailureReportGenerator reportGenerator;
    private List<TestFailureInfo> failedTests;
    private ExecutorService executorService;
    
    @Override
    public void onStart(ISuite suite) {
       // System.out.println(" Starting test suite with BATCH AI analysis: " + suite.getName());
        System.out.println("Suite started: " + suite.getName());
        System.out.println("Checkking whether chatbot is enabled or not "+isChatbotEnabled);
        if(isChatbotEnabled) {
        reportData.setSuiteName(suite.getName());
        reportData.setSuiteStartTime(new Date());
        
        GeminiProxy.start();}
        if(isAiAnalysisEnabled) {
      initializeComponents();}
    }
    
    private void initializeComponents() {
        try {
            this.batchAiAnalyzer = new BatchOpenAITestAnalyzer();
            this.reportGenerator = new TestFailureReportGenerator();
            this.failedTests = new ArrayList<>();
            this.executorService = Executors.newFixedThreadPool(1);
            
            System.out.println(" Batch AI Test Analyzer initialized successfully");
        } catch (Exception e) {
            System.err.println(" Failed to initialize batch AI analyzer: " + e.getMessage());
            System.err.println("Tests will continue without AI analysis");
        }
    }

    @Override
    public void onStart(ITestContext context) {
        reportPath = ExtentReportManager.getReportPath();
        extent = new ExtentReports();

        sparkReporter = ExtentReportManager.createSparkReporter(reportPath);
        extent.attachReporter(sparkReporter);
        ExtentReportManager.setSystemInformation(extent);
    }

    @Override
    public void onTestStart(ITestResult result) {
        ExtentTest test1 = extent.createTest(result.getMethod().getMethodName());
        TestDetailsAssigner.assignDetailsToTest(test1, result.getMethod().getMethodName());
        test1.log(Status.INFO, result.getMethod().getMethodName() + " is started just now ");
        testResultMap.put(result, test1);

        test.set(test1);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        test.get().log(Status.PASS, "Test case: " + result.getMethod().getMethodName() + " Passed");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        test.get().log(Status.FAIL, "Test case: " + result.getMethod().getMethodName() + "  Failed");
        test.get().log(Status.FAIL, result.getThrowable());
        
        String testMethodName = result.getMethod().getMethodName();
        String testClassName = result.getTestClass().getName();
        
        Throwable throwable = result.getThrowable();
        String errorMessage = throwable != null ? throwable.getMessage() : "Unknown error";
        System.out.println("Error Message startsssss---------------"+errorMessage+"----------------This is the error message for test checking its lenth");
        String stackTrace = getStackTrace(throwable);
        
        System.out.println(" Test failed: " + testClassName + "." + testMethodName);
   
        if(isAiAnalysisEnabled) {
        RequestResponseCapture.CapturedData capturedData = RequestResponseCapture.getCapturedData();
        storeFailureForBatchAnalysis(testMethodName, testClassName, errorMessage, stackTrace, capturedData);
        RequestResponseCapture.clear();}
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        test.get().log(Status.SKIP, "Test case: " + result.getMethod().getMethodName() + " Skipped");
        System.out.println(result.getThrowable());
    }

    @Override
    public void onFinish(ISuite suite) {
        if(isAiAnalysisEnabled) {
        performBatchAIAnalysis();
        
        generateFailureReport();}
    	
    	 System.out.println("Suite finished: " + suite.getName());
       if(isChatbotEnabled) {
    	 reportData.setSuiteEndTime(new Date());
         
         extractCompleteDataFromSuite(suite);
         generateChatbotDataFile(reportData);}
        flushReport();
    }

    public static ExtentTest getTest() {
        return test.get();
    }

    private void extractCompleteDataFromSuite(ISuite suite) {
        int totalTests = 0, passed = 0, failed = 0, skipped = 0;
        long totalDuration = 0;
        List<TestDetailDTO> allTests = new ArrayList<>();
        
        Map<String, ISuiteResult> results = suite.getResults();
        
        for (Map.Entry<String, ISuiteResult> entry : results.entrySet()) {
            ITestContext context = entry.getValue().getTestContext();
            
            // Process Passed Tests
            Set<ITestResult> passedTests = context.getPassedTests().getAllResults();
            passed += passedTests.size();
            for (ITestResult result : passedTests) {
                TestDetailDTO detail = createTestDetailWithExtentMetadata(result, "PASSED");
                allTests.add(detail);
                totalDuration += detail.getDuration();
            }
            
            // Process Failed Tests
            Set<ITestResult> failedTests = context.getFailedTests().getAllResults();
            failed += failedTests.size();
            for (ITestResult result : failedTests) {
                TestDetailDTO detail = createTestDetailWithExtentMetadata(result, "FAILED");
                allTests.add(detail);
                totalDuration += detail.getDuration();
            }
            
            // Process Skipped Tests
            Set<ITestResult> skippedTests = context.getSkippedTests().getAllResults();
            skipped += skippedTests.size();
            for (ITestResult result : skippedTests) {
                TestDetailDTO detail = createTestDetailWithExtentMetadata(result, "SKIPPED");
                allTests.add(detail);
                totalDuration += detail.getDuration();
            }
        }
        
        totalTests = passed + failed + skipped;
        
        reportData.setTotalTests(totalTests);
        reportData.setPassed(passed);
        reportData.setFailed(failed);
        reportData.setSkipped(skipped);
        reportData.setTotalDuration(totalDuration);
        reportData.setPassRate(totalTests > 0 ? (passed * 100.0 / totalTests) : 0);
        reportData.setTests(allTests);
    }
    
    
    private TestDetailDTO createTestDetailWithExtentMetadata(ITestResult result, String status) {
        TestDetailDTO detail = new TestDetailDTO();
        
        // ========================================
        // 1. Extract TestNG Data (from ITestResult)
        // ========================================
        detail.setTestName(result.getMethod().getMethodName());
        detail.setClassName(result.getTestClass().getName());
        detail.setStatus(status);
        detail.setDuration(result.getEndMillis() - result.getStartMillis());
        detail.setStartTime(new Date(result.getStartMillis()));
        detail.setEndTime(new Date(result.getEndMillis()));
        detail.setPriority(result.getMethod().getPriority());
        
        // TestNG Description
        String testngDescription = result.getMethod().getDescription();
        if (testngDescription != null && !testngDescription.isEmpty()) {
            detail.setDescription(testngDescription);
        }
        
        // TestNG Groups
        String[] groups = result.getMethod().getGroups();
        if (groups != null && groups.length > 0) {
            detail.setGroups(Arrays.asList(groups));
        }
        
        // Error details
        if (result.getThrowable() != null) {
            detail.setErrorMessage(result.getThrowable().getMessage());
            detail.setStackTrace(getStackTrace(result.getThrowable()));
        }
        
        // Test parameters
        Object[] parameters = result.getParameters();
        if (parameters != null && parameters.length > 0) {
            List<String> paramList = new ArrayList<>();
            for (Object param : parameters) {
                paramList.add(param != null ? param.toString() : "null");
            }
            detail.setParameters(paramList);
        }
        
        // ========================================
        // 2. Extract ExtentReports Metadata
        // ========================================
        try {
            ExtentTest extTest = testResultMap.get(result);
            
            if (extTest != null) {
                Test testModel = extTest.getModel();
                
                // Extract Extent-specific metadata
                if (testModel != null) {
                    
                    // 1. Get Description set via test.getModel().setDescription()
                    String extentDescription = testModel.getDescription();
                    if (extentDescription != null && !extentDescription.isEmpty()) {
                        // Combine with TestNG description if both exist
                        if (detail.getDescription() != null && !detail.getDescription().isEmpty()) {
                            detail.setDescription(detail.getDescription() + " | " + extentDescription);
                        } else {
                            detail.setDescription(extentDescription);
                        }
                    }
                    
                    // 2. Get Author(s) set via test.assignAuthor()
                 // 2. Get Author(s) set via test.assignAuthor()
                    Set<Author> authors = testModel.getAuthorSet();
                    if (authors != null && !authors.isEmpty()) {
                        List<String> authorNames = new ArrayList<>();
                        for (Author a : authors) {
                            if (a != null && a.getName() != null) {
                                authorNames.add(a.getName());
                            }
                        }
                        detail.setAuthors(authorNames);
                    }

                 // 3. Get Device(s) set via test.assignDevice()
                    Set<Device> devices = testModel.getDeviceSet();
                    if (devices != null && !devices.isEmpty()) {
                        List<String> deviceList = new ArrayList<>();
                        for (Device d : devices) {
                            if (d != null) {
                                deviceList.add(d.getName());
                            }
                        }
                        detail.setDevices(deviceList);
                    }

                    // 4. Get Categories set via test.assignCategory()
                    Set<Category> categories = testModel.getCategorySet();
                    if (categories != null && !categories.isEmpty()) {
                        List<String> categoryNames = new ArrayList<>();
                        for (Category c : categories) {
                            if (c != null && c.getName() != null) {
                                categoryNames.add(c.getName());
                            }
                        }

                        // Merge with TestNG groups 
                        List<String> allCategories = new ArrayList<>(
                            detail.getGroups() != null ? detail.getGroups() : new ArrayList<>()
                        );
                        allCategories.addAll(categoryNames);
                        detail.setCategories(allCategories);
                    }

                 
                }
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not extract Extent metadata for " + 
                             detail.getTestName() + ": " + e.getMessage());
        }
        
        return detail;
    }
    
    private void storeFailureForBatchAnalysis(String testMethodName, String testClassName,
            String errorMessage, String stackTrace,
            RequestResponseCapture.CapturedData capturedData) {  
TestFailureInfo failureInfo = new TestFailureInfo(
testMethodName, testClassName, errorMessage, null, stackTrace
);

failureInfo.setYamlFileName(testMethodName + ".yaml");

if (capturedData != null && capturedData.isComplete()) {
failureInfo.setHttpMethod(capturedData.httpMethod);
failureInfo.setEndpoint(capturedData.endpoint);
failureInfo.setActualStatusCode(capturedData.statusCode);
failureInfo.setResponseTime(capturedData.responseTime);
failureInfo.setRequestBody(capturedData.requestBody);
failureInfo.setRequestHeaders(capturedData.requestHeaders);
failureInfo.setResponseBody(capturedData.responseBody);
failureInfo.setResponseHeaders(capturedData.responseHeaders);

} else {
System.out.println("     No request/response data captured");
}

synchronized (failedTests) {
failedTests.add(failureInfo);
//System.out.println("@".repeat(40));
//System.out.println(failureInfo+" Yaml File Location"+failureInfo.getYamlFileName());
//System.out.println("@".repeat(40));

}

System.out.println(" Stored failure for batch analysis: " + testMethodName);
}
//    private String extractHttpMethod(String stackTrace) {
//        if (stackTrace == null) return null;
//        
//        String[] methods = {"GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS"};
//        for (String method : methods) {
//            if (stackTrace.contains(method)) {
//                return method;
//            }
//        }
//        return null;
//    }
//


private void performBatchAIAnalysis() {
    if (failedTests.isEmpty()) {
        System.out.println(" No failed tests to analyze!");
        return;
    }

    if (batchAiAnalyzer == null) {
        System.out.println(" Batch AI analyzer not available, skipping analysis");
        return;
    }

    try {
        System.out.println("\n Starting Batch AI Analysis...");
        System.out.println(" Processing " + failedTests.size() + " failed tests");
        
        Map<String, String> analysisResults = batchAiAnalyzer.analyzeBatchTestFailures(failedTests);
        
        System.out.println("\n Gemini API returned " + analysisResults.size() + " analysis results");
        
        System.out.println(" Analysis Results Debug:");
        for (Map.Entry<String, String> entry : analysisResults.entrySet()) {
            String preview = entry.getValue();
            if (preview != null && preview.length() > 100) {
                preview = preview.substring(0, 100) + "...";
            }
            System.out.println("   • " + entry.getKey() + " -> " + 
                             (preview != null ? preview.length() + " chars" : "NULL"));
        }
        
        synchronized (failedTests) {
            int successCount = 0;
            int failCount = 0;
            
            for (TestFailureInfo failureInfo : failedTests) {
                String testMethodName = failureInfo.getTestMethodName();
                String aiAnalysis = analysisResults.get(testMethodName);
                
                if (aiAnalysis != null && !aiAnalysis.trim().isEmpty()) {
                    failureInfo.setAiAnalysis(aiAnalysis);
                    successCount++;
                    System.out.println("    AI analysis SET for: " + testMethodName + 
                                     " (" + aiAnalysis.length() + " chars)");
                } else {
                    failureInfo.setAiAnalysis(" AI analysis not generated for this test. " +
                                            "Check Gemini API response or rate limits.");
                    failCount++;
                    System.out.println("    No AI analysis for: " + testMethodName);
                }
            }
            
         
        }
        
        System.out.println("\n Batch AI analysis completed!\n");
        
    } catch (Exception e) {
        System.err.println("\n Batch AI analysis FAILED with exception:");
        System.err.println("   Error: " + e.getMessage());
        e.printStackTrace();
        
        synchronized (failedTests) {
            for (TestFailureInfo failureInfo : failedTests) {
                if (failureInfo.getAiAnalysis() == null) {
                    failureInfo.setAiAnalysis(
                        "❌ AI Analysis Failed\n\n" +
                        "Error: " + e.getMessage() + "\n\n" +
                        "💡 Manual Analysis Required:\n" +
                        "• Review error message and stack trace below\n" +
                        "• Check request/response data if available\n" +
                        "• Verify API endpoint and parameters\n" +
                        "• Compare with YAML schema expectations"
                    );
                }
            }
        }
    }
}
    private void generateChatbotDataFile(TestReportData data) {
        try {
            Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .setDateFormat("yyyy-MM-dd HH:mm:ss")
                    .create();
            
            String json = gson.toJson(data);
            FileWriter writer = new FileWriter("test-output/chatbot-data.json");
            writer.write(json);
            writer.close();
            
            System.out.println("Chatbot data file generated: test-output/chatbot-data.json");
        } catch (IOException e) {
            System.err.println("Error generating chatbot data: " + e.getMessage());
        }
    }

    private String getStackTrace(Throwable throwable) {
        if (throwable == null) return "";
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

    private void generateFailureReport() {
        try {
            TestFailureReportGenerator reportGenerator = new TestFailureReportGenerator();
            
            // Create thread-safe copy
            List<TestFailureInfo> failuresCopy;
            synchronized (failedTests) {
                failuresCopy = new ArrayList<>(failedTests);
            }
            
            System.out.println(" Generating report for " + failuresCopy.size() + " failures...");
            
            String reportPath = reportGenerator.generateTxtReport(failuresCopy);
            
            if (reportPath != null) {
                System.out.println(" Report generated successfully!");
                System.out.println(" Report location: " + reportPath);
            } else {
                System.err.println(" Failed to generate report (null path returned)");
            }
            
        } catch (Exception e) {
            System.err.println(" Report generation failed with exception:");
            System.err.println("   " + e.getMessage());
            e.printStackTrace();
        }
    }


    public String getReportPath() {
    	return reportPath;
    }
    private void flushReport() {
        extent.flush();
        if(isChatbotEnabled) {
       ExtentReportManager.injectChatbotWithGeminiAI(reportData, reportPath);
        }
        File reportFile = new File(reportPath);
        try {
            Desktop.getDesktop().browse(reportFile.toURI());
            
           if(isChatbotEnabled) { GeminiProxy.stop();}
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}