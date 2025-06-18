package specs;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.ResponseSpecification;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;


public class ResponseSpecs {
    private ResponseSpecs() {
    }

    private static ResponseSpecBuilder defaultResponseBuilder() {
        return new ResponseSpecBuilder();
    }

    //201
    public static ResponseSpecification entityWasCreated() {
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_CREATED)
                .build();
    }

    //200
    public static ResponseSpecification requestReturnsOk() {
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .build();
    }

    //400 with json response
    //В Rest-assured при обращении к массиву возвращается java.util.List, поэтому матчеры должны работать с коллекцией.
    public static ResponseSpecification requestReturnsBadRequest(String errorKey, String... errorMessages) {
        ResponseSpecBuilder builder =  defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_BAD_REQUEST);
        for(String msg: errorMessages){
            builder.expectBody(errorKey, Matchers.hasItem(msg));
        }
        return builder.build();
    }

    //400 without response?
    public static ResponseSpecification requestReturnsBadNoMessage() {
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
                .build();
    }

    //400
    public static ResponseSpecification requestReturnsBadRawMessage(String expectedMsg) {
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
                .expectBody(Matchers.equalTo(expectedMsg))   // whole body is the text
                .build();
    }

    //401
    public static ResponseSpecification requestReturnsUnauth() {
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_UNAUTHORIZED)
                .build();
    }

    //403
    public static ResponseSpecification requestReturnsForbidden() {
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_FORBIDDEN)
                .build();
    }

    //200
    public static ResponseSpecification requestReturnOkWithRawMessage(String expectedMsg) {
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody(Matchers.equalTo(expectedMsg))   // whole body is the text
                .build();
    }

    public static ResponseSpecification requestReturnsNotFound(String errMessage) {
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_NOT_FOUND)
                .expectBody(Matchers.equalTo(errMessage))
                .build();
    }
}