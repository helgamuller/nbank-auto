package iteration2;

import generators.RandomData;
import iteration1.BaseTest;
import models.*;
import org.assertj.core.api.BooleanAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.AdminCreateUserRequester;
import requests.CreateAccountRequester;
import requests.MakeDepositRequester;
import requests.RetrieveAccountTransactionsRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DepositMoneyOnAccountTest extends BaseTest {


    public static Stream<Arguments> validDepositData() {
        return Stream.of(
                Arguments.of(0.01f),
                Arguments.of(4999.99f),
                Arguments.of(5000.00f),
                Arguments.of(100.00f)

        );
    }

    @ParameterizedTest
    @MethodSource("validDepositData")
    public void userCanDepositValidAmountTest(Float amount) {
        //create user data
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUserName())
                .password(RandomData.getUserPassword())
                .role(UserRole.USER.toString())
                .build();
        //admin create user
        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);
        //create new account  and return response as an object
        CreateAccountResponse createAccount = new CreateAccountRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null)
                .extract()
                .as(CreateAccountResponse.class);
        //extract account id
        int account = createAccount.getId();

        //make a deposit
        MakeDepositRequest makeDepositRequest = MakeDepositRequest.builder()
                .id(account)
                .balance(amount)
                .build();

        CreateAccountResponse makeDeposit = new MakeDepositRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .post(makeDepositRequest)
                .extract()
                .as(CreateAccountResponse.class);

        List<Transaction> transactions = makeDeposit.getTransactions();
        boolean isDepositFound = transactions.stream()
                .anyMatch(transaction -> TransactionType.DEPOSIT.equals(transaction.getType()) &&
                (transaction.getAmount()==amount));

        BooleanAssert isDepositFoundInResponse = softly.assertThat(isDepositFound).isTrue();


        //1. get list of transactions as a list of model.Transactions
        List <Transaction> accountTransactions = new RetrieveAccountTransactionsRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .get(account);
        //check that deposit transactions exist on transactions endpoint

        assertEquals(accountTransactions, transactions);

    }

    public static Stream<Arguments> invalidDepositData() {
        return Stream.of(
                Arguments.of(0.0f),
                Arguments.of(-0.01f),
                Arguments.of(5000.01f)
                //Arguments.of("string")

        );
    }

    @ParameterizedTest
    @MethodSource("invalidDepositData")
    public void userCanNotDepositInvalidAmountTest(Float amount) {
        //create user by admin
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUserName())
                .password(RandomData.getUserPassword())
                .role(UserRole.USER.toString())
                .build();
        //admin create user
        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);
        //create new account  and return response as an object
        CreateAccountResponse createAccount = new CreateAccountRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null)
                .extract()
                .as(CreateAccountResponse.class);
        //extract account id
        int account = createAccount.getId();

        //make a deposit
        MakeDepositRequest makeDepositRequest = MakeDepositRequest.builder()
                .id(account)
                .balance(amount)
                .build();

                 new MakeDepositRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsBadNoMessage())
                .post(makeDepositRequest);


                //.statusCode(HttpStatus.SC_BAD_REQUEST);
        //check if deposit contains provided value

    }
    @Test
    public void unauthorizedUserCanNotMakeDepositTest() {
        Float amount = RandomData.randomTransfer(1.0f, 100.0f);
        //create user by admin
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUserName())
                .password(RandomData.getUserPassword())
                .role(UserRole.USER.toString())
                .build();
        //admin create user
        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);
        //create new account  and return response as an object
        CreateAccountResponse createAccount = new CreateAccountRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null)
                .extract()
                .as(CreateAccountResponse.class);
        //extract account id
        int account = createAccount.getId();

        //make a deposit
        MakeDepositRequest makeDepositRequest = MakeDepositRequest.builder()
                .id(account)
                .balance(amount)
                .build();

        new MakeDepositRequester(
                RequestSpecs.unauthSpec(),
                ResponseSpecs.requestReturnsUnauth())
                .post(makeDepositRequest);


    }
    @Test
    public void userCanNotMakeDepositToAccountWhichNotExistsTest(){
        //create user by admin
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUserName())
                .password(RandomData.getUserPassword())
                .role(UserRole.USER.toString())
                .build();
        //admin create user
        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        int account = Integer.MAX_VALUE;
        float amount = RandomData.randomTransfer(1.0f, 100.0f);

        //make a deposit
        MakeDepositRequest makeDepositRequest = MakeDepositRequest.builder()
                .id(account)
                .balance(amount)
                .build();

        new MakeDepositRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsForbidden())
                .post(makeDepositRequest);
    }
}
