package edu.qts.responsevalidation;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import edu.qts.routes.JSONSchemaFiles;
import io.restassured.response.Response;

public class ProductTest2ResponseValidation {

	
	
	
	

    public static  void validateAddProduct(Response r){
                r
                .then()
                .assertThat()
                .statusCode(201)
                .body(matchesJsonSchemaInClasspath(JSONSchemaFiles.addProductSchema));

    }
    
    

    public static  void validateDeleteProductById(Response r){
                r
                .then()
                .assertThat()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath(JSONSchemaFiles.deleteProductByIdSchema));

    }
    
    
    public static  void validateUpdateProductById(Response r){
        r
        .then()
        .assertThat()
        .statusCode(200)
        .body(matchesJsonSchemaInClasspath(JSONSchemaFiles.updateProductByIdSchema));

}

    
}
