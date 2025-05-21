package iteration1;

import generators.RandomData;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import models.CreateUserRequest;
import models.CreateUserResponse;
import models.UserRole;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.AdminCreateUserRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.List;
import java.util.stream.Stream;

import static io.restassured.RestAssured.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CreateUserTest extends BaseTest{


//Username must contain only letters, digits, dashes, underscores, and dots



    @Test
    public void adminCanCreateUserWithValidData(){
        //create userRequest(creds) with Random data for user creation
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUserName())
                .password(RandomData.getUserPassword())
                .role(UserRole.USER.toString())
                .build();
//admin creates user
        CreateUserResponse createUserResponse =  new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest)
                .extract()
                .as(CreateUserResponse.class);

        softly.assertThat(userRequest.getUsername()).isEqualTo(createUserResponse.getUsername());
        softly.assertThat(userRequest.getPassword()).isNotEqualTo(createUserResponse.getPassword());
        softly.assertThat(userRequest.getRole()).isEqualTo(createUserResponse.getRole());
    }

    public static Stream<Arguments> userInvalidData(){
        return Stream.of(
                Arguments.of("   ", "Password33$", "USER", "username", "Username cannot be blank"),
                Arguments.of("ab", "Password33$", "USER", "username", "Username must be between 3 and 15 characters"),
                Arguments.of("abc$", "Password33$", "USER", "username", "Username must contain only letters, digits, dashes, underscores, and dots"),
                Arguments.of("abc%", "Password33$", "USER", "username", "Username must contain only letters, digits, dashes, underscores, and dots")
        );
    }
    @ParameterizedTest
    @MethodSource("userInvalidData")
    public void adminCanNotCreateUserWithInvalidData(String username, String password, String role, String errorKey, String errorValue){

        //create userRequest(creds) with Random data for user creation
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(username)
                .password(password)
                .role(role)
                .build();
//admin creates user
       new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.requestReturnsBadRequest(errorKey,errorValue))
                .post(userRequest);


    }
}
