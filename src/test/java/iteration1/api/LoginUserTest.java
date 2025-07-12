package iteration1.api;

import api.models.CreateUserRequest;
import api.models.LoginUserRequest;
import api.models.LoginUserResponse;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.CrudRequester;
import api.requests.skeleton.requesters.ValidatedCrudRequester;
import api.requests.steps.AdminSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

public class LoginUserTest extends BaseTest{

    @Test
    public void adminCanGenerateAuthTokenTest(){
        //create request for admin to login
        LoginUserRequest userRequest = LoginUserRequest.builder()
                .username("admin")
                .password("admin")
                .build();

        //login as an admin using request created above
        new ValidatedCrudRequester<LoginUserResponse> //у САши тут почему-то CreateUserRequest
                (RequestSpecs.unauthSpec(),
                 Endpoint.LOGIN,
                 ResponseSpecs.requestReturnsOk())
                 .post(userRequest);
    }

    @Test
    public void userCanGenerateAuthTokenTest() {
        CreateUserRequest userRequest = AdminSteps.createUser();

        new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOk())
                .post(LoginUserRequest.builder().username(userRequest.getUsername())
                        .password(userRequest.getPassword()).build())
                .header("Authorization", Matchers.notNullValue());

        int userId = AdminSteps.getUserId(userRequest);

    }
}
