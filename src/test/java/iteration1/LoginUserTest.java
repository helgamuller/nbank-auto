package iteration1;

import models.*;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import requests.skeleton.requesters.ValidatedCrudRequester;
import requests.steps.AdminSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

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
