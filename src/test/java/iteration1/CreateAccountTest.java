package iteration1;

import generators.RandomData;
import models.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import requests.AdminCreateUserRequester;
import requests.CheckAccountsRequester;
import requests.CreateAccountRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.List;

import static io.restassured.RestAssured.given;

public class CreateAccountTest extends BaseTest {

    @Test
    public void userCanCreateAccountTest() {

        //here we create request(creds)to pass  in AdminCreatesUser test
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUserName())
                .password(RandomData.getUserPassword())
                .role(UserRole.USER.toString())
                .build();

        //here I created request(creds of earlier created user) to pass into Login user test
//        LoginUserRequest loginUserRequest = LoginUserRequest.builder()
//                .username(userRequest.getUsername())
//                .password(userRequest.getPassword())
//                .build();

        //here admin creates user. I pass admin spec(auth admin) in request and receive 201 as a response.
        //I also pass userRequest(Data class (username, password serialized as Json) into post method.)
        //It's not a post() of API, it's post() of AdminCreateUserRequester class instance which takes creds (model) as an argument.
        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);
//create mew account and return it's number
        String account = new CreateAccountRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                        .post(null)
                .extract()
                .path("accountNumber");



        //user get Token
        //here I implement login as  a newly created user and receive a token which I am going to use in further operations
        //So firstly, I created a requester with Req and Resp specs as params, then I added post method with model as an argument,
        //and also because .post() returns a ValidateResponse I proceeded chain of methods with extract() to extract header.
//        String userAuthHeader = new LoginUserRequester(
//                RequestSpecs.unauthSpec(),
//                ResponseSpecs.requestReturnsOk())
//                        .post(loginUserRequest)
//                                .extract()
//                                        .header("Authorization");


//запросить все аккаунты и проверить, что созданный есть

//I use list because response started from [] that means it's an array
        List <CreateAccountResponse> accounts = new CheckAccountsRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .post(null)
                .extract()
                .jsonPath()
                .getList("", CreateAccountResponse.class);


        Assertions.assertTrue(accounts.stream()
                .anyMatch(acc-> acc.getAccountNumber().equals(account)));


    }
}
