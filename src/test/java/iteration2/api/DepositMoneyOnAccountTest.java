package iteration2.api;

import api.generators.RandomData;
import api.models.*;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.CrudRequester;
import api.requests.skeleton.requesters.ValidatedCrudRequester;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import iteration1.api.BaseTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;


public class DepositMoneyOnAccountTest extends BaseTest {

    public static Stream<Arguments> validDepositData() {
        return Stream.of(
                Arguments.of(new BigDecimal("0.01")),
                Arguments.of(new BigDecimal("4999.99")),
                Arguments.of(new BigDecimal("5000.00"))
        );
    }

    @ParameterizedTest
    @MethodSource("validDepositData")
    public void userCanDepositValidAmountTest(BigDecimal amount) {
        CreateUserRequest userRequest = AdminSteps.createUser();
        int accountId = UserSteps.createAccountAndGetId(userRequest);

        //make a deposit request
        MakeDepositRequest makeDepositRequest = MakeDepositRequest.builder()
                .id(accountId)
                .balance(amount)
                .build();

        CreateAccountResponse makeDeposit = new ValidatedCrudRequester<CreateAccountResponse>(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.ACCOUNTS_DEPOSIT,
                ResponseSpecs.requestReturnsOk())
                .post(makeDepositRequest);

        //Check that transaction==deposit exists in a list of account's transactions
        List<Transaction> accountTransactions = makeDeposit.getTransactions();
        boolean isDepositFound = accountTransactions.stream()
                .anyMatch(transaction -> TransactionType.DEPOSIT.equals(transaction.getType()) &&
                (transaction.getAmount().compareTo(amount) == 0));
        softly.assertThat(isDepositFound)
                .as("Deposit transaction should be present in response")
                .isTrue();

        //1. get list of transactions (model.Transactions) from transactions endpoint for our accountId
        List<Transaction> transactions =  UserSteps.getAccountTransactions(userRequest, accountId);
        softly.assertThat(transactions)
                .as("Full transaction list should include the deposit transaction(s)")
                .containsAll(accountTransactions);

        BigDecimal balance = UserSteps.getAccountBalance(userRequest, accountId);
        softly.assertThat(balance)
                .as("Account balance should equal deposit amount")
                .isEqualByComparingTo(amount);

        int userId = AdminSteps.getUserId(userRequest);

    }

    public static Stream<Arguments> invalidDepositData() {
        return Stream.of(
                Arguments.of(new BigDecimal("0.00"), "Invalid account or amount"),
                Arguments.of(new BigDecimal("-0.01"), "Invalid account or amount"),
                Arguments.of(new BigDecimal("5000.01"), "Deposit amount exceeds the 5000 limit")
        );
    }
    @ParameterizedTest
    @MethodSource("invalidDepositData")
    public void userCanNotDepositInvalidAmountTest(BigDecimal amount, String errorMessage) {
        //user is created by admin
        CreateUserRequest userRequest = AdminSteps.createUser();
        int accountId = UserSteps.createAccountAndGetId(userRequest);

        //user makes a deposit
       MakeDepositRequest makeDepositRequest = MakeDepositRequest.builder()
               .balance(amount)
               .id(accountId)
               .build();

        new CrudRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.ACCOUNTS_DEPOSIT,
                ResponseSpecs.requestReturnsBadRawMessage(errorMessage))
                .post(makeDepositRequest);

        List<Transaction> transactions =  UserSteps.getAccountTransactions(userRequest, accountId);
        BigDecimal balance =  UserSteps.getAccountBalance(userRequest, accountId);

        softly.assertThat(transactions)
                .as("No transactions should be recorded when deposit is rejected")
                .isEmpty();
        softly.assertThat(balance)
                .as("Balance must remain unchanged")
                .isEqualByComparingTo("0.00");

        int userId = AdminSteps.getUserId(userRequest);

    }
    @Test
    public void unauthorizedUserCanNotMakeDepositTest() {
        BigDecimal amount = RandomData.randomTransfer(new BigDecimal("1.00"),new BigDecimal("100.00"));
        //create user by admin
        CreateUserRequest userRequest = AdminSteps.createUser();
        int accountId = UserSteps.createAccountAndGetId(userRequest);

        //make a deposit request
        MakeDepositRequest makeDepositRequest = MakeDepositRequest.builder()
                .id(accountId)
                .balance(amount)
                .build();

        new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.ACCOUNTS_DEPOSIT,
                ResponseSpecs.requestReturnsUnauth())
                .post(makeDepositRequest);

        List<Transaction> transactions =  UserSteps.getAccountTransactions(userRequest, accountId);
        BigDecimal balance =  UserSteps.getAccountBalance(userRequest, accountId);
        softly.assertThat(transactions)
                .as("No transactions should be recorded when deposit is rejected")
                .isEmpty();
        softly.assertThat(balance)
                .as("Balance must remain unchanged")
                .isEqualTo("0.00");

        int userId = AdminSteps.getUserId(userRequest);

    }

    @Test
    public void userCanNotMakeDepositToAccountWhichNotExistsTest(){
        //create user by admin
        CreateUserRequest userRequest = AdminSteps.createUser();
        int account = Integer.MAX_VALUE;
        BigDecimal amount = RandomData.randomTransfer(new BigDecimal("1.00"),new BigDecimal("100.00"));

        //make a deposit
        MakeDepositRequest makeDepositRequest = MakeDepositRequest.builder()
                .id(account)
                .balance(amount)
                .build();

        new CrudRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.ACCOUNTS_DEPOSIT,
                ResponseSpecs.requestReturnsForbidden())
                .post(makeDepositRequest);

    }
}
