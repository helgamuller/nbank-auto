package iteration1;

import generators.RandomData;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.CreateUserRequest;
import models.LoginUserRequest;
import models.UserRole;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import requests.AdminCreateUserRequester;
import requests.LoginUserRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.List;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.requestSpecification;

public class LoginUserTest extends BaseTest{

    @Test
    public void adminCanGenerateAuthTokenTest(){
        //create request for admin to login
        LoginUserRequest userRequest = LoginUserRequest.builder()
                .username("admin")
                .password("admin")
                .build();

        //login as an admin using request created above
        new LoginUserRequester(RequestSpecs.unauthSpec(),
                 ResponseSpecs.requestReturnsOk())
                 .post(userRequest);

    }

    @Test
    public void userCanGenerateAuthTokenTest() {
        //I create a request for create a user with creds(body) here
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUserName())
                .password(RandomData.getUserPassword())
                .role(UserRole.USER.toString())
                .build();

        //admin creates user using request created above
        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);



        new LoginUserRequester(
                RequestSpecs.unauthSpec(),
                ResponseSpecs.requestReturnsOk())
                .post(LoginUserRequest.builder().username(userRequest.getUsername())
                        .password(userRequest.getPassword()).build())
                .header("Authorization", Matchers.notNullValue());

    }
}
