package requests;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.BaseModel;
import models.CreateUserRequest;

import static io.restassured.RestAssured.given;

public class AdminCreateUserRequester extends Request<CreateUserRequest> {
    //Hard connection: endpoint, request JSON, response JSON


    public AdminCreateUserRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    @Override
    public ValidatableResponse post(CreateUserRequest model) {
       return given()
                .spec(requestSpecification) //request Json
                .body(model)
                .post("/api/v1/admin/users")//endpoint
                .then()
                .assertThat()
                .spec(responseSpecification); //response Json
    }
}
