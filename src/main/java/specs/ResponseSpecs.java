package specs;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.ResponseSpecification;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;

public class ResponseSpecs {
    private ResponseSpecs(){}

    private static ResponseSpecBuilder defaultResponseBuilder(){
        return new ResponseSpecBuilder();
    }
    //201
    public static ResponseSpecification entityWasCreated(){
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_CREATED)
                .build();
    }
//200
    public static ResponseSpecification requestReturnsOk(){
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .build();
    }
//400
    public static ResponseSpecification requestReturnsBadRequest(String errorKey, String errorValue){
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
                .expectBody(errorKey, Matchers.equalTo(errorValue))
                .build();
    }
    //400
    public static ResponseSpecification requestReturnsBadNoMessage(){
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
                .build();
    }
//401
    public static ResponseSpecification requestReturnsUnauth(){
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_UNAUTHORIZED)
                .build();
    }
    //403
    public static ResponseSpecification requestReturnsForbidden(){
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_FORBIDDEN)
                .build();
    }
}
