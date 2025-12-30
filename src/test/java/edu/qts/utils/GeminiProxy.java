package edu.qts.utils;
import com.sun.net.httpserver.*;
import com.google.gson.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Scanner;
import java.util.stream.Collectors;

public class GeminiProxy {
    private static HttpServer server;
    private static final String API_KEY = EnvLoader.getAPIUrl();
    
    public static void start() {
        if (server != null) {
            System.out.println(" Proxy already running on port 8088");
            return;
        }
        
        try {
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", 8088), 0);
            
            // Health endpoint
            server.createContext("/health", ex -> {
                ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                String response = "{\"status\":\"ok\"}";
                byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
                ex.sendResponseHeaders(200, bytes.length);
                ex.getResponseBody().write(bytes);
                ex.getResponseBody().close();
            });
            
            // Chat endpoint
            server.createContext("/api/chat", ex -> {
                try {
                    // CORS headers
                    ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                    ex.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
                    ex.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
                    
                    // Handle preflight
                    if ("OPTIONS".equals(ex.getRequestMethod())) {
                        ex.sendResponseHeaders(204, -1);
                        return;
                    }
                    
                    if (!"POST".equals(ex.getRequestMethod())) {
                        sendJsonError(ex, "Only POST allowed");
                        return;
                    }
                    
                    System.out.println(" Received request");
                    
                    // Read body
                    String body = new BufferedReader(
                        new InputStreamReader(ex.getRequestBody(), StandardCharsets.UTF_8))
                        .lines().collect(Collectors.joining("\n"));
                    
                    System.out.println(" Body length: " + body.length() + " chars");
                    
                    // Forward to Gemini
                    String geminiResponse = callGemini(body);
                    
                    System.out.println(" Got AI response\n");
                    System.out.println("Responser:  "+geminiResponse);
                    
                    
                    if(geminiResponse.equals("AI service is currently busy. Please try again in 30 seconds.")) {
                        sendJsonError(ex, geminiResponse);
                       return;

                    }
                    
                    // Parse Gemini response
                    JsonObject geminiJson = new Gson().fromJson(geminiResponse, JsonObject.class);
                    String aiText = geminiJson.getAsJsonArray("candidates")
                        .get(0).getAsJsonObject()
                        .getAsJsonObject("content")
                        .getAsJsonArray("parts")
                        .get(0).getAsJsonObject()
                      .get("text").getAsString();
//                    
                    
                 // Parse Gemini response
//                    JsonObject geminiJson = new Gson().fromJson(geminiResponse, JsonObject.class);
//                    String aiText = geminiJson.get("response").getAsString();
//                    boolean success = geminiJson.get("success").getAsBoolean();

//                    // Use the response directly
//                    JsonObject response = new JsonObject();
//                    response.addProperty("success", success);
//                    response.addProperty("response", aiText);
//
//                    sendJsonResponse(ex, 200, response.toString());
//                    
                    
                    // Wrap in success response
                    JsonObject response = new JsonObject();
                    response.addProperty("success", true);
                    response.addProperty("response", aiText);
                    
                    sendJsonResponse(ex, 200, response.toString());
                   
                    
                } catch (Exception e) {
                    System.err.println(" Error: " + e.getMessage());
                    e.printStackTrace();
                    sendJsonError(ex, e.getMessage());
                }
            });
            
            server.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(10));
            server.start();
            
            System.out.println(" PROXY SERVER STARTED");
            System.out.println(" URL: http://localhost:8088/api/chat");
            
        } catch (BindException e) {
            System.err.println(" Port 8088 already in use!");
        } catch (Exception e) {
            System.err.println(" Failed to start: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void sendJsonResponse(HttpExchange ex, int code, String json) throws IOException {
        ex.getResponseHeaders().set("Content-Type", "application/json");
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.sendResponseHeaders(code, bytes.length);
        OutputStream os = ex.getResponseBody();
        os.write(bytes);
        os.flush();
        os.close();
    }
    
    private static void sendJsonError(HttpExchange ex, String message) throws IOException {
        JsonObject error = new JsonObject();
       // error.addProperty("success", false);
        error.addProperty("error", message);
        sendJsonResponse(ex, 500, error.toString());
    }
    
//    private static String callGemini(String requestBody) throws IOException {
//        String apiUrl = API_KEY;
//        System.out.println("Request Body ::: "+requestBody);
//        if (apiUrl == null || apiUrl.isEmpty()) {
//            throw new IOException("API Key not configured");
//        }
//        
//        System.out.println("🔗 Calling Gemini...");
//        
//        URL url = new URL(apiUrl);
//        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//        
//        try {
//            conn.setRequestMethod("POST");
//            conn.setRequestProperty("Content-Type", "application/json");
//            conn.setDoOutput(true);
//            conn.setConnectTimeout(30000);
//            conn.setReadTimeout(30000);
//            
//            try (OutputStream os = conn.getOutputStream()) {
//                os.write(requestBody.getBytes(StandardCharsets.UTF_8));
//                os.flush();
//            }
//            
//            int code = conn.getResponseCode();
//            System.out.println("📡 Gemini response: " + code);
//            
//            if (code == 200) {
//                try (BufferedReader br = new BufferedReader(
//                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
//                    return br.lines().collect(Collectors.joining("\n"));
//                }
//            } else {
//                try (BufferedReader br = new BufferedReader(
//                        new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
//                    String error = br.lines().collect(Collectors.joining("\n"));
//                    if (error.contains("overloaded") || error.contains("503")) {
//                        return "AI service is currently busy. Please try again in 30 seconds.";
//                    } 
//                    throw new IOException("Gemini error (" + code + "): " + error);
//                }
//            }
//        } finally {
//           // conn.disconnect();
//        }
//        
//        
//        //System.out.println("Enter ok to Stop Server");        
//    }
//    
    
    private static String callGemini(String requestBody) throws IOException {
        String apiUrl = API_KEY;
        System.out.println("Request Body ::: " + requestBody);
        
        if (apiUrl == null || apiUrl.isEmpty()) {
            throw new IOException("API Key not configured");
        }
        
        
        // Retry configuration
        int maxRetries = 3;
        int retryDelay = 2000; // 2 seconds
        String lastError = "";
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(apiUrl);
                conn = (HttpURLConnection) url.openConnection();
                
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(30000);
                conn.setReadTimeout(30000);
                
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(requestBody.getBytes(StandardCharsets.UTF_8));
                    os.flush();
                }
                
                int code = conn.getResponseCode();
                System.out.println("📡 Gemini response (attempt " + attempt + "/" + maxRetries + "): " + code);
                
                if (code == 200) {
                    try (BufferedReader br = new BufferedReader(
                            new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                        return br.lines().collect(Collectors.joining("\n"));
                    }
                } else {
                    try (BufferedReader br = new BufferedReader(
                            new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                        String error = br.lines().collect(Collectors.joining("\n"));
                        lastError = error;
                        System.out.println("Errrror;;;;; "+error);
                        if (error.contains("overloaded") || error.contains("503") || error.contains("429")) {
                            if (attempt < maxRetries) {
                                System.out.println(" Gemini busy, retrying in " + retryDelay + "ms...");
                                Thread.sleep(retryDelay);
                                retryDelay *= 2; 
                                continue; 
                            } else {
                            //	System.out.println("Err:::::------ "+conn.getErrorStream());
                                return "AI service is currently busy. Please try again in 30 seconds.";
                            }
                        } else {
                            // Non-retryable error
                            throw new IOException("Gemini error (" + code + "): " + error);
                        }
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Request interrupted", e);
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }
        
        return "AI service is currently busy. Please try again in 30 seconds.";
    }
    
    
    
    
    
    
    
    
    
    public static void stop() throws Exception {
    	
	System.out.println("Enter 'ok' to stop the server");
		
		
		Scanner scanner=new Scanner(System.in);
		String input=scanner.next();
		
		//Thread.sleep(Duration.ofSeconds(2));
		
		if(input=="ok") {
			GeminiProxy.stop();
			  if (server != null) {
		            server.stop(0);
		            server = null;
		            System.out.println(" Proxy stopped");
		}
		}
		
		
      
    }
}