package requests.skeleton.requesters;


import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.BaseModel;
import requests.skeleton.Endpoint;
import requests.skeleton.HttpRequest;
import requests.skeleton.interfaces.CrudEndpointInterface;


//use this requester for positive tests
public class ValidatedCrudRequester<T> extends HttpRequest implements CrudEndpointInterface {
    private CrudRequester crudRequester;

    public ValidatedCrudRequester(RequestSpecification requestSpecification, Endpoint endpoint, ResponseSpecification responseSpecification) {
        super(requestSpecification, endpoint, responseSpecification);
        this.crudRequester = new CrudRequester(requestSpecification, endpoint, responseSpecification);
    }

    //here we will receive a model object as a result of post method
    @Override
    public T post(BaseModel model) {
        return (T) crudRequester.post(model).extract().as(endpoint.getResponseModel());
    }

    @Override
    public T getAll()  {
        return (T) crudRequester.getAll().extract().as(endpoint.getResponseModel());
    }

    @Override
    public T get(int id) {
        return (T) crudRequester.get(id).extract().as(endpoint.getResponseModel());
    }


    @Override
    public T update(BaseModel model) {
        return (T) crudRequester.update(model).extract().as(endpoint.getResponseModel());
    }

    @Override
    public void delete(int id) {
    }

}