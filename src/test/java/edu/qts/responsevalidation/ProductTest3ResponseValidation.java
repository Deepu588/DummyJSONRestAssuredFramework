package edu.qts.responsevalidation;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import edu.qts.routes.JSONSchemaFiles;
import io.restassured.response.Response;

public class ProductTest3ResponseValidation {

	
	
	
	
	public static void validateProductCategoriesList(Response r) {
		r.then().statusCode(200).assertThat()
        .body(matchesJsonSchemaInClasspath(JSONSchemaFiles.getProductCategoriesListSchema));

	}
	
	
	public static void validateSelectProductPrice(Response r) {
		r.then().statusCode(200).assertThat()
        .body(matchesJsonSchemaInClasspath(JSONSchemaFiles.selectProductPriceSchema));

	}
	
	
	public static void validateSelectproductTitle(Response r) {
		r.then().statusCode(200).assertThat()
        .body(matchesJsonSchemaInClasspath(JSONSchemaFiles.selectProductTitleSchema));

	}
	
	
	
	
}
