package api.requests.skeleton.requesters;


import api.models.BaseModel;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.HttpRequest;
import api.requests.skeleton.interfaces.CrudEndpointInterface;
import api.requests.skeleton.interfaces.GetAllEndpointInterface;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import java.util.Arrays;
import java.util.List;


//use this requester for positive tests
public class ValidatedCrudRequester<T> extends HttpRequest implements CrudEndpointInterface, GetAllEndpointInterface {
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

    @Override
    public List<T> getAll(Class<?> clazz) {
       T[] array = (T[]) crudRequester.getAll(clazz).extract().as(clazz);
       return Arrays.asList(array);
    }
}