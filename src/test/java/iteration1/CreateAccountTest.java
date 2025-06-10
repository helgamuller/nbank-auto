package iteration1;

import models.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.ValidatedCrudRequester;
import requests.steps.AdminSteps;
import requests.steps.UserSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;
import java.util.Arrays;
import java.util.List;

public class CreateAccountTest extends BaseTest {

    @Test
    public void userCanCreateAccountTest() {
        CreateUserRequest userRequest = AdminSteps.createUser();

//create new account and return it's number
        int accountId =  UserSteps.createAccountAndGetId(userRequest);

        //запросить все аккаунты и проверить, что созданный акк есть
//I use list because response started from [] that means it's an array
        CreateAccountResponse[] arr =
                new ValidatedCrudRequester<CreateAccountResponse[]>(
                        RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                        Endpoint.CUSTOMER_ACCOUNTS,
                        ResponseSpecs.requestReturnsOk())
                        .getAll();

        List<CreateAccountResponse> accounts = Arrays.asList(arr);

        Assertions.assertTrue(
                accounts.stream()
                        .anyMatch(acc -> acc.getId() == accountId));

        int userId = AdminSteps.getUserId(userRequest);
        AdminSteps.deleteUser(userId);
    }
}
