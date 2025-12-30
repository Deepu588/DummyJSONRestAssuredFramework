package edu.qts.tests;

import org.testng.annotations.AfterMethod;
//import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;

//import com.qmg.Routes.Routes;
//import com.qmg.Utils.TokenManager;
import edu.qts.aianalysis.RequestResponseCapture;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;

/**
 * Base class for all API tests.
 * Handles authentication and common setup.
 */
public abstract class BaseApiTest {
    
    //protected static final String LOGIN_ENDPOINT = Routes.loginUrl;
   // protected static final String REFRESH_ENDPOINT = Routes.refreshTokenUrl;
    
   // protected static TokenManager tokenManager;
    
    @BeforeSuite
    public static void globalSetup() {
        // Configure RestAssured
       RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
       // RestAssured.baseURI = BASE_URL;
       RestAssured.filters(new RequestResponseCapture());


    }



    @AfterMethod
    public void cleanup() {
        RequestResponseCapture.clear();
    }
    
}