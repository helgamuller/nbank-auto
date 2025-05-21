package requests;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.BaseModel;

public abstract class Request <T extends BaseModel> {
    protected RequestSpecification requestSpecification;
    protected ResponseSpecification responseSpecification;
//here we pass headers, content type etc
    public Request( RequestSpecification requestSpecification, ResponseSpecification responseSpecification){
        this.requestSpecification = requestSpecification;
        this.responseSpecification = responseSpecification;
    }
//s an interface that represents the response of an HTTP request that can be validated. It provides
// a fluent API for asserting various properties of the response, such as status codes, headers, cookies, and body content.
    //so here we create an abstract method, in which we pass model(content for post method aka JSON)
    public abstract ValidatableResponse post(T model);
}
