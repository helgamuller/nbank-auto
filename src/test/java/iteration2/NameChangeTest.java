package iteration2;

import generators.RandomData;
import iteration1.BaseTest;
import models.ChangeNameRequest;
import models.ChangeNameResponse;
import models.CreateUserRequest;
import models.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.AdminCreateUserRequester;
import requests.ChangeNameRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.stream.Stream;

public class NameChangeTest extends BaseTest {

    @Test
    public void userCanChangeNameToValidOneTest() {
        //create data instance for user creation
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUserName())
                .password(RandomData.getUserPassword())
                .role(UserRole.USER.toString())
                .build();
        //admin creates user
        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        //create request JSON
        ChangeNameRequest changeName = ChangeNameRequest.builder()
                .name(RandomData.getNewUserName())
                .build();
        //change a name
         ChangeNameResponse changeNameResponse =  new ChangeNameRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .post(changeName)
                 .extract()
                 .as(ChangeNameResponse.class);

        softly.assertThat(changeName.getName()).isEqualTo(changeNameResponse.getCustomer().getName());
        softly.assertThat("Profile updated successfully").isEqualTo(changeNameResponse.getMessage());


    }
    public static Stream<Arguments> invalidNameData(){
        return Stream.of(
                Arguments.of(""),
                Arguments.of("   "),
                Arguments.of("olga"),
                Arguments.of("olga olga olga"),
                Arguments.of("!12$%^& *()")
                // Arguments.of(null)


        );

    }

    @ParameterizedTest
    @MethodSource("invalidNameData")
    public void userCanNotChangeNameToInvalidOneTest(String name) {
        //create user data
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUserName())
                .password(RandomData.getUserPassword())
                .role(UserRole.USER.toString())
                .build();
        //admin creates user
        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        //create request JSON
        ChangeNameRequest changeName = ChangeNameRequest.builder()
                .name(name)
                .build();
        //change a name
        new ChangeNameRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsBadNoMessage())
                .post(changeName);



    }
    @Test
    public void unauthorizedUserCanNotChangeNameTest() {

        ChangeNameRequest changeName = ChangeNameRequest.builder()
                .name(RandomData.getNewUserName())
                .build();

        new ChangeNameRequester(
                RequestSpecs.unauthSpec(),
                ResponseSpecs.requestReturnsUnauth()
        );

    }
}


