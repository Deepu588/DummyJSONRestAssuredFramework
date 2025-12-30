package edu.qts.messagemanager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import edu.qts.utils.EnvLoader;

public class TeamsWebhookSender {

    public static void sendMessage( String message) throws Exception {

    	
    
    	
    	  JsonObject geminiJson = new Gson().fromJson(message, JsonObject.class);
          String aiText = geminiJson.getAsJsonArray("candidates")
              .get(0).getAsJsonObject()
              .getAsJsonObject("content")
              .getAsJsonArray("parts")
              .get(0).getAsJsonObject()
            .get("text").getAsString();
   	           
//          
          
//          String aiText1="Here is the expert analysis for each failed test.\r\n"
//          		+ "ANALYSIS_FOR_TEST_1: Root Cause: The API response for the health check is missing the worker field, which is marked as a required property in the JSON schema used for validation. Header Issues: None Query Parameter Issues: None Path Parameter Issues: None Request Body Issues: None. This is a GET request and does not have a body. Response Body Issues: The actual response {'database':true,'task_worker':true} does not contain the required worker field. It appears the field may have been renamed to task_worker. Schema Violations: The response fails validation due to a missing required property. The exact error is: object has missing required properties (['worker']). Status Code Analysis: The Actual=200 OK status is successful, but the test correctly failed because the response payload violates the agreed-upon contract (the schema). This is a contract test failure, not a server-level error. Exact Fix: • Option A (If API is correct): Modify the JSON schema to reflect the new reality of the API. • In schemas/json-schema-ExploratorHealthCheckResponse.json, find the required array and replace 'worker' with 'task_worker'. • In the properties object, rename the 'worker' property definition to 'task_worker'. • Option B (If Schema is correct): The API must be fixed. • The backend developers for the explorator service must update the GET /common/v1/healthcheck/ endpoint to return the worker field as specified in the contract. Fix Location: The JSON Schema definition file: D:/MedullarAutomation/medullar/target/test-classes/schemas/json-schema-ExploratorHealthCheckResponse.json. Prevention: Implement contract testing using a shared specification (like OpenAPI). Any breaking change to the API response (like renaming or removing a required field) should fail the provider's build pipeline before it is deployed and breaks consumer tests. END_ANALYSIS_1\r\n"
//          		+ "ANALYSIS_FOR_TEST_2: Root Cause: The server rejected the request with a 400 Bad Request because it could not find the refresh token in the expected cookie, likely due to the test sending the cookie with an incorrect name. Header Issues: The request sent a cookie named medullar-token. However, the server's error message Missing refresh token cookie strongly implies it is looking for a cookie with a different, more specific name (e.g., refresh_token or medullar_refresh_token). Query Parameter Issues: None Path Parameter Issues: None Request Body Issues: None. While the request body is an empty JSON object, the server's error is explicitly about the cookie, indicating the request processing failed at the authentication stage before the body was evaluated. Response Body Issues: The response body {'non_field_errors':['Missing refresh token cookie.']} is an error payload that correctly identifies the problem from the server's perspective. Schema Violations: None. A schema validation for a successful response was not performed because the request failed with a 400 status code. Status Code Analysis: The Actual=400 Bad Request is the correct server response for a client request that is malformed or missing required authentication elements, such as a correctly named cookie. The test failed because it was expecting a successful response (e.g., 200 OK). Exact Fix: • Step 1: Consult the OpenAPI/Swagger documentation for the POST /auth/v1/auth/refresh/ endpoint to identify the exact name the server expects for the refresh token cookie. • Step 2: Update the test automation code that builds the request to use the correct cookie name. For example, change the code from setting medullar-token to refresh_token (or the documented name). Fix Location: The test setup or client configuration method responsible for attaching authentication tokens/cookies to the HTTP request before it is sent. Prevention: Test automation for authenticated endpoints must be built directly against the API's formal specification (OpenAPI/Swagger). Client generation tools or strict adherence to the spec can prevent mismatches in critical details like cookie names, header names, and authentication schemes. END_ANALYSIS_2";
//          
     String messagee=     MessageFormatter.buildFullAnalysisMessage(aiText);
    	System.out.println(messagee);
    	//String payload = "{ \"text\": \"Test message\" }";
    	// Using Gson (add dependency: com.google.code.gson:gson)
    	JsonObject json = new JsonObject();
    	json.addProperty("text", messagee);
    	String payload = new Gson().toJson(json);
      //  String payload = "{ \"text\": \"" +messagee.replace("\"", "'") + "\" }";
    	String webhookUrl=EnvLoader.getWebHookUrl();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(webhookUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        HttpResponse<String> response =
                HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Teams Response Code: " + response.statusCode());
        System.out.println("Response Body: " + response.body());
        if(response.statusCode()==200) {
        	System.out.print("Message Reached to Teams Channel Successfullyy......");
        }
    }
    
    
    
    
//    public static void main(String[] args) throws Exception {
//sendMessage("Hello .... ");
//    }
}
