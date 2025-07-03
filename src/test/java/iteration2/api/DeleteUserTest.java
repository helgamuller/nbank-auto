package iteration2.api;

import generators.RandomModelGenerator;
import iteration1.api.BaseTest;

import models.CreateUserRequest;
import models.CreateUserResponse;
import org.junit.jupiter.api.Test;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import requests.skeleton.requesters.ValidatedCrudRequester;
import requests.steps.AdminSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.Arrays;

public class DeleteUserTest extends BaseTest {

    @Test
    public void adminCanDeleteExistingUser(){
        CreateUserRequest userRequest =
                RandomModelGenerator.generate(CreateUserRequest.class);
        ValidatedCrudRequester<CreateUserResponse> requester = new ValidatedCrudRequester<>(
                RequestSpecs.adminSpec(),
                Endpoint.ADMIN_USER,
                ResponseSpecs.entityWasCreated());
        CreateUserResponse userResponse = requester.post(userRequest);
        int userId = userResponse.getId();
        String okMessage = "User with ID " + userId +" deleted successfully.";
        //deleteUser
        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ADMIN_DELETE,
                ResponseSpecs.requestReturnOkWithRawMessage(okMessage))
                .delete(userId);
        //let's get list of users and check that user was deleted
        CreateUserResponse[] users = AdminSteps.getListIfUsers();
        Boolean isExist = Arrays.stream(users)
                        .anyMatch(u->u.getId()==userId);
        softly.assertThat(isExist).isFalse();

    }
    @Test
    public void adminCanNotDeleteNonExistingUser(){
        int userId = Integer.MAX_VALUE;
        String errMessage ="Error: User with ID " + userId +" not found.";
        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ADMIN_DELETE,
                ResponseSpecs.requestReturnsNotFound(errMessage));
    }

    @Test
    public void unauthorizedAdminCanNotDeleteUser(){
        CreateUserRequest userRequest = AdminSteps.createUser();
        int userId = AdminSteps.getUserId(userRequest);
        new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.ADMIN_DELETE,
                ResponseSpecs.requestReturnsUnauth());

        CreateUserResponse[] users = AdminSteps.getListIfUsers();
        Boolean isExist = Arrays.stream(users)
                .anyMatch(u->u.getId()==userId);
        softly.assertThat(isExist).isTrue();
    }
}
