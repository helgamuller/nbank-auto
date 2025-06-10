package iteration2;

import generators.RandomData;
import iteration1.BaseTest;
import models.*;
import models.comparison.ModelAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import requests.skeleton.requesters.ValidatedCrudRequester;
import requests.steps.AdminSteps;
import requests.steps.UserSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static java.math.RoundingMode.HALF_UP;

public class TransferMoneyBetweenAccountsTest extends BaseTest {

    public static Stream<Arguments> validTransferData() {
        return Stream.of(
                Arguments.of(new BigDecimal("5000.00"), new BigDecimal("0.01"), "Transfer successful"),
                Arguments.of(new BigDecimal("5000.00"), new BigDecimal("9999.99"), "Transfer successful"),
                Arguments.of(new BigDecimal("5000.00"), new BigDecimal("10000.00"), "Transfer successful")
        );
    }

    @ParameterizedTest
    @MethodSource("validTransferData")
    public void userCanTransferValidAmountTest(BigDecimal depositAmount, BigDecimal transferAmount, String message) throws NoSuchFieldException, IllegalAccessException {
        //admin creates user
        CreateUserRequest userRequest = AdminSteps.createUser();

        // user creates sender account and gets id
        int senderAccountId = UserSteps.createAccountAndGetId(userRequest);

        //user creates a receiving account and gets it's id
        int receiverAccountId = UserSteps.createAccountAndGetId(userRequest);

        //user makes two deposits into sender account(because max deposit ==5000, while max transfer ==10000)
        UserSteps.makeDeposit(depositAmount, senderAccountId, userRequest);
        UserSteps.makeDeposit(depositAmount, senderAccountId, userRequest);

        //prepare transfer request data
        MakeTransferRequest transferRequest = MakeTransferRequest.builder()
                .amount(transferAmount)
                .receiverAccountId(receiverAccountId)
                .senderAccountId(senderAccountId)
                .build();

        //send transfer and get response as a class
        MakeTransferResponse transferResponse = new ValidatedCrudRequester<MakeTransferResponse>(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.ACCOUNT_TRANSFER,
                ResponseSpecs.requestReturnsOk())
                .post(transferRequest);

        softly.assertThat(transferResponse.getMessage()).isEqualTo(message);
        ModelAssertions.assertThatModels(transferRequest, transferResponse).match();

        //check transfer-out in senderAccount transactions
        List <Transaction> senderAccountTransactions = UserSteps.getAccountTransactions(userRequest, senderAccountId);
        Transaction transferOut = senderAccountTransactions.stream()
                .filter(t -> t.getType() == TransactionType.TRANSFER_OUT)
                .filter(t -> t.getAmount().setScale(2, HALF_UP) .compareTo(transferAmount.setScale(2, HALF_UP)) == 0)
                .filter(t -> t.getRelatedAccountId() == receiverAccountId)
                .findFirst()
                .orElse(null);

        softly.assertThat(transferOut).isNotNull();

        //check transfer-in in receiverAccount
        Transaction transferIn = UserSteps.getTransferFromTransactions(userRequest,
                receiverAccountId,senderAccountId,transferAmount, TransactionType.TRANSFER_IN);
        softly.assertThat(transferIn).isNotNull();

        //check that sender's acc balance decreased by transfer amount
        //calculate expected amount
        BigDecimal expectedSenderAccBalance = depositAmount
                .add(depositAmount)          // because I did two deposits to reaching out the maximum allowed transfer amount
                .subtract(transferAmount)
                .setScale(2, HALF_UP);

         BigDecimal actualSenderAccBalance = UserSteps.getAccountBalance(userRequest, senderAccountId);
         softly.assertThat(actualSenderAccBalance).isEqualByComparingTo(expectedSenderAccBalance);

        //check thar receivers' acc balance increased by transfer amount
        //here I only need to check that balance is equal to transferAmount
        BigDecimal actualReceiverAccBalance =  UserSteps.getAccountBalance(userRequest, receiverAccountId);
        softly.assertThat(actualReceiverAccBalance).isEqualByComparingTo(transferAmount);

        int userId = AdminSteps.getUserId(userRequest);
        AdminSteps.deleteUser(userId);
    }

    public static Stream<Arguments> invalidTransferData() {
        return Stream.of(
                Arguments.of(new BigDecimal("1000.00"), new BigDecimal("5100.00"), "Invalid transfer: insufficient funds or invalid accounts"),
                Arguments.of(new BigDecimal("5000.00"), new BigDecimal("10000.01"), "Transfer amount cannot exceed 10000"),
                Arguments.of(new BigDecimal("5000.00"), new BigDecimal("0.00"), "Invalid transfer: insufficient funds or invalid accounts"),
                Arguments.of(new BigDecimal("5000.00"), new BigDecimal("-0.01"), "Invalid transfer: insufficient funds or invalid accounts")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidTransferData")
    public void userCanNotTransferInvalidAmountTest(BigDecimal depositAmount, BigDecimal transferAmount, String errorMessage) {
        CreateUserRequest userRequest = AdminSteps.createUser();
        int senderAccountId = UserSteps.createAccountAndGetId(userRequest);
        int receiverAccountId = UserSteps.createAccountAndGetId(userRequest);
        // I made three deposits into sender account - because maximum deposit amount is 5000, but I need to check 10000.01 amount
        //for this test
        UserSteps.makeDeposit(depositAmount, senderAccountId, userRequest);
        UserSteps.makeDeposit(depositAmount, senderAccountId, userRequest);
        UserSteps.makeDeposit(depositAmount, senderAccountId, userRequest);

        MakeTransferRequest transferRequest = MakeTransferRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(transferAmount)
                .build();

        new CrudRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.ACCOUNT_TRANSFER,
                ResponseSpecs.requestReturnsBadRawMessage(errorMessage))
                .post(transferRequest);

        //I need a triple deposit amount because I made tree deposits
        BigDecimal tripleDeposit = depositAmount.add(depositAmount).add(depositAmount);
        BigDecimal actualSenderAccBalance =  UserSteps.getAccountBalance(userRequest, senderAccountId);
        softly.assertThat(actualSenderAccBalance).isEqualByComparingTo(tripleDeposit);

        BigDecimal actualReceiverAccBalance =  UserSteps.getAccountBalance(userRequest, receiverAccountId);
        softly.assertThat(actualReceiverAccBalance).isZero();

        int userId = AdminSteps.getUserId(userRequest);
        AdminSteps.deleteUser(userId);
    }

    @Test
    public void unauthorizedUserCanNotMakeTransferTest() {
        CreateUserRequest userRequest = AdminSteps.createUser();
        int senderAccountId = UserSteps.createAccountAndGetId(userRequest);
        int receiverAccountId = UserSteps.createAccountAndGetId(userRequest);
        BigDecimal depositAmount = RandomData.randomTransfer(new BigDecimal("100.00"), new BigDecimal("200.00"));
        UserSteps.makeDeposit(depositAmount, senderAccountId, userRequest);
        BigDecimal transferAmount = RandomData.randomTransfer(new BigDecimal("1.00"), depositAmount);
        MakeTransferRequest transferRequest = MakeTransferRequest.builder()
                .amount(transferAmount)
                .receiverAccountId(receiverAccountId)
                .senderAccountId(senderAccountId)
                .build();

        new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.ACCOUNT_TRANSFER,
                ResponseSpecs.requestReturnsUnauth())
                .post(transferRequest);

        //let's check that senders' and receivers' accounts balances hasn't changed
        BigDecimal actualSenderAccBalance =  UserSteps.getAccountBalance(userRequest, senderAccountId);
        softly.assertThat(actualSenderAccBalance).isEqualByComparingTo(depositAmount);

        BigDecimal actualReceiverAccBalance =  UserSteps.getAccountBalance(userRequest, receiverAccountId);
        softly.assertThat(actualReceiverAccBalance).isZero();

        int userId = AdminSteps.getUserId(userRequest);
        AdminSteps.deleteUser(userId);
 }

    @Test
    public void userCanNotMakeTransferFromNonExistingAccountTest() {
        CreateUserRequest userRequest = AdminSteps.createUser();

        //I skip creating sender account here and use variable instead
        int nonExistingAccountId = Integer.MAX_VALUE;
        int receiverAccountId = UserSteps.createAccountAndGetId(userRequest);

        //I don't need to make a deposit here so I proceed to preparing transfer data
        BigDecimal transferAmount = RandomData.randomTransfer(new BigDecimal("1.00"), new BigDecimal("100.00"));
        MakeTransferRequest transferRequest = MakeTransferRequest.builder()
                .amount(transferAmount)
                .receiverAccountId(receiverAccountId)
                .senderAccountId(nonExistingAccountId)
                .build();

        new CrudRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.ACCOUNT_TRANSFER,
                ResponseSpecs.requestReturnsForbidden())
                .post(transferRequest);

        BigDecimal actualReceiverAccBalance =  UserSteps.getAccountBalance(userRequest, receiverAccountId);
        softly.assertThat(actualReceiverAccBalance)
                .as("Receiver's balance must remain unchanged")
                .isEqualByComparingTo(new BigDecimal("0.00"));

        int userId = AdminSteps.getUserId(userRequest);
        AdminSteps.deleteUser(userId);
    }

    @Test
    public void userCanNotMakeTransferToNonExistingAccountTest() {
        CreateUserRequest userRequest =  AdminSteps.createUser();
        int senderAccountId = UserSteps.createAccountAndGetId(userRequest);
        BigDecimal depositAmount = RandomData.randomTransfer( new BigDecimal("100.00"), new BigDecimal("1000.00"));
        UserSteps.makeDeposit(depositAmount, senderAccountId, userRequest);
        int receiverAccountId = Integer.MAX_VALUE;
        BigDecimal transferAmount = depositAmount;

        MakeTransferRequest transferRequest = MakeTransferRequest.builder()
                .amount(transferAmount)
                .receiverAccountId(receiverAccountId)
                .senderAccountId(senderAccountId)
                .build();

        new CrudRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.ACCOUNT_TRANSFER,
                ResponseSpecs.requestReturnsBadNoMessage())
                .post(transferRequest);

        Transaction transferOut = UserSteps.getTransferFromTransactions
                (userRequest, senderAccountId, receiverAccountId, transferAmount, TransactionType.TRANSFER_OUT);
        softly.assertThat(transferOut).isNull();

        Transaction transferIn = UserSteps.getTransferFromTransactions
                (userRequest, senderAccountId, receiverAccountId, transferAmount, TransactionType.TRANSFER_IN);
        softly.assertThat(transferIn).isNull();
    }
    @Test
    public void userCanMakeTransferToSendingAccountTest() {
        CreateUserRequest userRequest = AdminSteps.createUser();
        int senderAccountId = UserSteps.createAccountAndGetId(userRequest);
        BigDecimal depositAmount = RandomData.randomTransfer( new BigDecimal("100.00"), new BigDecimal("1000.00"));
        UserSteps.makeDeposit(depositAmount, senderAccountId, userRequest);
        BigDecimal transferAmount = depositAmount;
        UserSteps.makeTransfer(transferAmount, senderAccountId, senderAccountId, userRequest);

        Transaction transferOut = UserSteps.getTransferFromTransactions
                (userRequest, senderAccountId, senderAccountId,  transferAmount, TransactionType.TRANSFER_OUT);

        softly.assertThat(transferOut).isNotNull();

        BigDecimal actualSenderAccBalance = UserSteps.getAccountBalance(userRequest, senderAccountId);
        softly.assertThat(actualSenderAccBalance)
                .as("Sender account balance must remain unchanged")
                .isEqualByComparingTo(depositAmount);

        int userId = AdminSteps.getUserId(userRequest);
        AdminSteps.deleteUser(userId);
    }
}

