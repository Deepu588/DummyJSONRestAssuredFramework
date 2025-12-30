package edu.qts.responsevalidation;

import edu.qts.routes.JSONSchemaFiles;
import io.restassured.response.Response;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

public class ProductTest1ResponseValidation {



    public static  void validateGetProductById(Response r){
                r
                .then()
                .assertThat()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath(JSONSchemaFiles.getProductByIdSchema));

    }
    
    
    
    
    
    public static void validateProductCategories(Response r) {
    	r
    	.then()
    	.assertThat()
    	.statusCode(200)
    	.body(matchesJsonSchemaInClasspath(JSONSchemaFiles.getProductCategoriesSchema));
    }
    
    
    
    
    public static void validateGetProductsByLimit(Response r) {
    	
    	r
    	.then()
    	.assertThat()
    	.statusCode(200)
    	.body(matchesJsonSchemaInClasspath(JSONSchemaFiles.getProductsByLimitSchema));
    }
    
    
    
    
}
