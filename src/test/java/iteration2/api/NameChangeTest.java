package iteration2.api;

import api.generators.RandomModelGenerator;
import api.models.ChangeNameRequest;
import api.models.ChangeNameResponse;
import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import api.models.comparison.ModelAssertions;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.CrudRequester;
import api.requests.skeleton.requesters.ValidatedCrudRequester;
import api.requests.steps.AdminSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import iteration1.api.BaseTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class NameChangeTest extends BaseTest {

    @Test
    public void userCanChangeNameToValidOneTest() throws NoSuchFieldException, IllegalAccessException {
        CreateUserRequest userRequest = AdminSteps.createUser();
        //create requestJSON
        ChangeNameRequest changeName = RandomModelGenerator.generate(ChangeNameRequest.class);

        //change a name via validated requester with response model as a parameter
         ChangeNameResponse changeNameResponse =  new ValidatedCrudRequester<ChangeNameResponse>(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOk())
                .update(changeName);

        ModelAssertions.assertThatModels(changeName, changeNameResponse).match();
        softly.assertThat(changeNameResponse.getMessage())
                .as("Message should be correct")
                .isEqualTo("Profile updated successfully");

//      //get user profile and check name change
        CreateUserResponse checkProfile = new ValidatedCrudRequester<CreateUserResponse>(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.CUSTOMER_PROFILE_GET,
                ResponseSpecs.requestReturnsOk())
                .getAll();

        softly.assertThat(checkProfile.getName())
                .as("Name value from profile must be the same as we pass")
                .isEqualTo(changeName.getName());

        int userId = AdminSteps.getUserId(userRequest);


    }
    public static Stream<Arguments> invalidNameData(){
        return Stream.of(
                Arguments.of("", "Name must contain two words with letters only"),
                Arguments.of("   ", "Name must contain two words with letters only"),
                Arguments.of("olga", "Name must contain two words with letters only"),
                Arguments.of("olga olga olga", "Name must contain two words with letters only"),
                Arguments.of("!12$%^& *()", "Name must contain two words with letters only")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidNameData")
    public void userCanNotChangeNameToInvalidOneTest(String name, String errMessage) {
        //admin creates user
        CreateUserRequest userRequest = AdminSteps.createUser();

        //create request JSON with parameters from method source
        ChangeNameRequest changeName = ChangeNameRequest.builder()
                .name(name)
                .build();
        //change a name
        new CrudRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsBadRawMessage(errMessage))
                .update(changeName);

        CreateUserResponse checkProfile = new ValidatedCrudRequester<CreateUserResponse>(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.CUSTOMER_PROFILE_GET,
                ResponseSpecs.requestReturnsOk())
                .getAll();

        softly.assertThat(checkProfile.getName()).isNull();

        int userId = AdminSteps.getUserId(userRequest);

    }
    @Test
    public void unauthorizedUserCanNotChangeNameTest() {

        ChangeNameRequest changeName = RandomModelGenerator.generate(ChangeNameRequest.class);
        new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsUnauth())
                .update(changeName);

    }
}


