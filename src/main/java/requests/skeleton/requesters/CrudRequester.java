package requests.skeleton.requesters;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.BaseModel;
import requests.skeleton.Endpoint;
import requests.skeleton.HttpRequest;
import requests.skeleton.interfaces.CrudEndpointInterface;

import static io.restassured.RestAssured.given;

public class CrudRequester extends HttpRequest implements CrudEndpointInterface {
    public CrudRequester(RequestSpecification requestSpecification, Endpoint endpoint, ResponseSpecification responseSpecification) {
        super(requestSpecification, endpoint, responseSpecification);
    }

    @Override
    public ValidatableResponse post(BaseModel model) {
        var body = model == null ? "" : model;
        return given()
                .spec(requestSpecification) //request Json
                .body(body)
                .post(endpoint.getUrl())//endpoint
                .then()
                .assertThat()
                .spec(responseSpecification); //response Json
    }

    @Override
    public ValidatableResponse get(int id) {
        return given()
                .spec(requestSpecification)
                .pathParam("id", id)
                .get(endpoint.getUrl())
                .then()
                .assertThat()
                .spec(responseSpecification);
    }


    @Override
    public ValidatableResponse getAll() {
        return given()
                .spec(requestSpecification) //request Json
                .get(endpoint.getUrl())//endpoint
                .then()
                .assertThat()
                .spec(responseSpecification);
    }

    @Override
    public ValidatableResponse update(BaseModel model) {
        return given()
                .spec(requestSpecification)
                .body(model)
                .put(endpoint.getUrl())
                .then()
                .assertThat()
                .spec(responseSpecification);
    }

    @Override
    public void delete(int id) {
        given()
                .spec(requestSpecification)
                .pathParam("id", id)
                .delete(endpoint.getUrl())
                .then()
                .assertThat()
                .spec(responseSpecification);
    }
}
