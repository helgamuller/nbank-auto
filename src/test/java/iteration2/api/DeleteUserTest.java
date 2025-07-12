package iteration2.api;

import api.generators.RandomModelGenerator;
import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.CrudRequester;
import api.requests.skeleton.requesters.ValidatedCrudRequester;
import api.requests.steps.AdminSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import iteration1.api.BaseTest;
import org.junit.jupiter.api.Test;

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
