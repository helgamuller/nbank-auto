package iteration1;

import generators.RandomModelGenerator;
import models.CreateUserRequest;
import models.CreateUserResponse;
import models.comparison.ModelAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import requests.skeleton.requesters.ValidatedCrudRequester;
import requests.steps.AdminSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.stream.Stream;


public class CreateUserTest extends BaseTest{

//Username must contain only letters, digits, dashes, underscores, and dots
    @Test
    public void adminCanCreateUserWithValidData() throws NoSuchFieldException, IllegalAccessException {
        CreateUserRequest userRequest =
                RandomModelGenerator.generate(CreateUserRequest.class);
        CreateUserResponse createUserResponse = new ValidatedCrudRequester<CreateUserResponse>
                (RequestSpecs.adminSpec(),
                        Endpoint.ADMIN_USER,
                        ResponseSpecs.entityWasCreated())
                .post(userRequest);
        userId = AdminSteps.getUserId(userRequest);
        ModelAssertions.assertThatModels(userRequest, createUserResponse).match();

    }

    public static Stream<Arguments> userInvalidData(){
        return Stream.of(
                Arguments.of("   ", "Password33$", "USER", "username", new String[]{
                        "Username must contain only letters, digits, dashes, underscores, and dots",
                        "Username cannot be blank"
                }),
                Arguments.of("ab", "Password33$", "USER", "username", new String[]{"Username must be between 3 and 15 characters"}),
                Arguments.of("abc$", "Password33$", "USER", "username", new String[]{"Username must contain only letters, digits, dashes, underscores, and dots"}),
                Arguments.of("abc%", "Password33$", "USER", "username", new String[]{"Username must contain only letters, digits, dashes, underscores, and dots"})
        );
    }
    @ParameterizedTest
    @MethodSource("userInvalidData")
    public void adminCanNotCreateUserWithInvalidData(String username, String password, String role, String errorKey, String[] errorValue){
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(username) //I use here parameter from method source because I need specific data to be checked and lead to fail
                .password(password)
                .role(role)
                .build();

//admin creates user
        //we use CrudRequester because it's negative test, and we don't need response body model -
        //we already have assertion in response
       new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ADMIN_USER,
                ResponseSpecs.requestReturnsBadRequest(errorKey,errorValue))
                .post(userRequest);

    }
}
