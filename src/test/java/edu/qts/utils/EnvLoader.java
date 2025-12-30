package edu.qts.utils;
import io.github.cdimascio.dotenv.Dotenv;

public class EnvLoader {

    private static final Dotenv dotenv = Dotenv.load();

    public static String get(String key) {
        return dotenv.get(key);
    }
    
    
    
    
    
    public static String getAPIKey() {
    	return dotenv.get("API_KEY");
    }
    
    
    public static String getAPIUrl() {
    	return dotenv.get("API_URL");
    }
    
    
    public static String getAPIRequestBuilder() {
    	return dotenv.get("API_REQUEST_BUILDER");
    	
    	
    }
    public static String getAPIUrl1() {
    	return dotenv.get("API_URL1");
    }
    public static String getWebHookUrl() {
    	return dotenv.get("WEBHOOK_URL");
    }
    
    
    
    public static void main(String[] args) {
    System.out.println(	getAPIKey());
    System.out.println(getAPIUrl());
    System.out.println(getAPIUrl1());
    System.out.println(getAPIRequestBuilder());
    System.out.println(getWebHookUrl());
    	
    	
    }
   
}
