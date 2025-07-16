package api.requests.steps;

import api.generators.RandomModelGenerator;
import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.CrudRequester;
import api.requests.skeleton.requesters.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

import java.util.Arrays;
import java.util.List;

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

    public static List<CreateUserResponse> getAllusers(){
        return new ValidatedCrudRequester<CreateUserResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ADMIN_USER,
                ResponseSpecs.requestReturnsOk()).getAll(CreateUserResponse[].class);

    }

}
