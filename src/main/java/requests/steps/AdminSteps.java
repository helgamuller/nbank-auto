package requests.steps;

import generators.RandomModelGenerator;
import models.CreateUserRequest;
import models.CreateUserResponse;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import requests.skeleton.requesters.ValidatedCrudRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.Arrays;

public class AdminSteps {
    public static CreateUserRequest createUser(){
        CreateUserRequest userRequest =
                RandomModelGenerator.generate(CreateUserRequest.class);

//admin creates user
        new ValidatedCrudRequester<CreateUserResponse>
                (RequestSpecs.adminSpec(),
                        Endpoint.ADMIN_USER,
                        ResponseSpecs.entityWasCreated())
                .post(userRequest);
        return userRequest;
    }

    public static int getUserId(CreateUserRequest userRequest){
        CreateUserResponse[] users = new CrudRequester(RequestSpecs.adminSpec(),
                Endpoint.ADMIN_USER,
                ResponseSpecs.requestReturnsOk())
                .getAll()
                .extract()
                .as(CreateUserResponse[].class);

        return Arrays.stream(users)
                .filter(u->u.getUsername().equals(userRequest.getUsername()))
                .map(CreateUserResponse::getId)
                .findFirst()
                .orElse(null);
    }

    public static void deleteUser(int userId){
        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ADMIN_DELETE,
                ResponseSpecs.requestReturnsOk())
                .delete(userId);

    }
    public static CreateUserResponse[] getListIfUsers(){
        return new CrudRequester(RequestSpecs.adminSpec(),
                Endpoint.ADMIN_USER,
                ResponseSpecs.requestReturnsOk())
                .getAll()
                .extract()
                .as(CreateUserResponse[].class);
    }
}
