package edu.qts.tests;

import edu.qts.responsevalidation.ProductTest1ResponseValidation;
import edu.qts.routes.JSONSchemaFiles;
import edu.qts.routes.Routes;
import edu.qts.utils.ExtractDataFromExcel;
import edu.qts.utils.PayloadHelper;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

public class ProductTest1 extends BaseApiTest{

    @Test(priority=0)
    public void getProductById() throws Exception{

        Response res= RestAssured
                        .given().pathParam(PayloadHelper.getIdKey(),PayloadHelper.generateRandomNumber())
                        .when()
                        .get(Routes.getProductByIdUrl);
        ProductTest1ResponseValidation.validateGetProductById(res);
    }
    
    
    
    @Test(priority=1)
    public void getProductCategories() {
    	
    Response r=	given().when().get(Routes.getProductCategoriesurl);  	
    ProductTest1ResponseValidation.validateProductCategories(r);
    }
    
    
    
    @Test(priority=2,enabled=false)
    public void getProductsByLimit() throws Exception  {
    	Response r= 	given().queryParam(PayloadHelper.getLimitKey(), PayloadHelper.generateRandomNumber()).when().get(Routes.getProductsByLimitUrl);
    	ProductTest1ResponseValidation.validateGetProductsByLimit(r);
    }
    
    
    
}
