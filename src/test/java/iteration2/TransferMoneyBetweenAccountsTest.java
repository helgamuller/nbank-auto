package iteration2;

import generators.RandomData;
import iteration1.BaseTest;
import models.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.*;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.equalTo;

public class TransferMoneyBetweenAccountsTest extends BaseTest {
    private static final float EPS = 0.0001f;

    public static Stream<Arguments> validTransferData() {
        return Stream.of(
                Arguments.of(10000.00f, 0.01f, "Transfer successful"),
                        Arguments.of(10000.00f, 9999.99f, "Transfer successful"),
                        Arguments.of(10000.00f, 10000.00f, "Transfer successful")
        );
    }

    @ParameterizedTest
    @MethodSource("validTransferData")
    public void userCanTransferValidAmountTest(Float depositAmount, Float transferAmount, String message) {
        //create model for user data
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

        //create sender account and convert response into the object
        CreateAccountResponse createSenderAccount = new CreateAccountRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null)
                .extract()
                .as(CreateAccountResponse.class);
        //extract account id
        int senderAccountId = createSenderAccount.getId();

        //create sender account and convert response into the object
        CreateAccountResponse createReceiverAccount = new CreateAccountRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null)
                .extract()
                .as(CreateAccountResponse.class);
        //extract account id
        int receiverAccountId = createReceiverAccount.getId();

        //prepare deposit data
        MakeDepositRequest makeDepositRequest = MakeDepositRequest.builder()
                .id(senderAccountId)
                .balance(depositAmount)
                .build();

        //make a deposit into sender account
        new MakeDepositRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .post(makeDepositRequest);

        //prepare transfer request data
        MakeTransferRequest transferRequest = MakeTransferRequest.builder()
                .amount(transferAmount)
                .receiverAccountId(receiverAccountId)
                .senderAccountId(senderAccountId)
                .build();

        //make transfer request
        MakeTransferResponse transfer = new MakeTransferRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .post(transferRequest)
                .extract()
                .as(MakeTransferResponse.class);

        softly.assertThat(transfer.getAmount()).isEqualTo(transferAmount);
        softly.assertThat(transfer.getSenderAccountId()).isEqualTo(senderAccountId);
        softly.assertThat(transfer.getReceiverAccountId()).isEqualTo(receiverAccountId);
        softly.assertThat(transfer.getMessage()).isEqualTo("Transfer successful");

        //check transfer-out in senderAccount
        List <Transaction> senderAccountTransactions = new RetrieveAccountTransactionsRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .get(senderAccountId);
//        System.out.println("\n--- RAW sender transactions ---");
//        senderAccountTransactions.forEach(t -> System.out.printf(
//                "id=%d  type=%s  amt=%.10f  rel=%d%n",
//                t.getId(), t.getType(), t.getAmount(), t.getRelatedAccountId()));
//        System.out.println("--------------------------------\n");

        Transaction transferOut = senderAccountTransactions.stream()
                .filter(t -> t.getType() == TransactionType.TRANSFER_OUT)
                .filter(t -> Math.abs(t.getAmount() - transferAmount) < EPS)
                .filter(t -> t.getRelatedAccountId() == receiverAccountId)
                .findFirst()
                .orElse(null);

        softly.assertThat(transferOut).isNotNull();

        //check transfer-in in receiverAccount
        List <Transaction> receiverAccountTransactions = new RetrieveAccountTransactionsRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .get(receiverAccountId);

        Transaction transferIn = receiverAccountTransactions.stream()
                .filter(t-> t.getType()==TransactionType.TRANSFER_IN)
                .filter(t->Math.abs(t.getAmount()-transferAmount)<EPS)
                .filter(t->t.getRelatedAccountId()==senderAccountId)
                .findFirst()
                .orElse(null);

        softly.assertThat(transferIn).isNotNull();

    }

    public static Stream<Arguments> invalidTransferData() {
        return Stream.of(
                Arguments.of(5000.00f, 5100.01f, "Invalid transfer: insufficient funds or invalid accounts"),
                Arguments.of(10000.00f, 10000.01f, "Invalid transfer: insufficient funds or invalid accounts"),
                Arguments.of(10000.00f, 0.00f, "Invalid transfer: insufficient funds or invalid accounts"),
                Arguments.of(10000.00f, -0.01f, "Invalid transfer: insufficient funds or invalid accounts")

        );
    }

    @ParameterizedTest
    @MethodSource("invalidTransferData")
    public void userCanNotTransferInvalidAmountTest(Float depositAmount, Float transferAmount, String errorMessage) {
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

        //create sender account and convert response into the object
        CreateAccountResponse createSenderAccount = new CreateAccountRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null)
                .extract()
                .as(CreateAccountResponse.class);

        //extract account id
        int senderAccountId = createSenderAccount.getId();

        //create sender account and convert response into the object
        CreateAccountResponse createReceiverAccount = new CreateAccountRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null)
                .extract()
                .as(CreateAccountResponse.class);

        //extract account id
        int receiverAccountId = createReceiverAccount.getId();

        //prepare deposit data
        MakeDepositRequest makeDepositRequest = MakeDepositRequest.builder()
                .id(senderAccountId)
                .balance(depositAmount)
                .build();

        //make a deposit into sender account
        new MakeDepositRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .post(makeDepositRequest);

        //prepare transfer request data
        MakeTransferRequest transferRequest = MakeTransferRequest.builder()
                .amount(transferAmount)
                .receiverAccountId(receiverAccountId)
                .senderAccountId(senderAccountId)
                .build();

        //make transfer using request above
        new MakeTransferRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsBadNoMessage())
                .post(transferRequest)
                .body(equalTo(errorMessage));


    }

    @Test
    public void unauthorizedUserCanNotMakeTransferTest() {
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

        //create sender account and convert response into the object
        CreateAccountResponse createSenderAccount = new CreateAccountRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null)
                .extract()
                .as(CreateAccountResponse.class);

        //extract account id
        int senderAccountId = createSenderAccount.getId();

        //create sender account and convert response into the object
        CreateAccountResponse createReceiverAccount = new CreateAccountRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null)
                .extract()
                .as(CreateAccountResponse.class);

        //extract account id
        int receiverAccountId = createReceiverAccount.getId();

        //prepare deposit data
        float depositAmount = RandomData.randomTransfer(0.01f, 100.0f);
        MakeDepositRequest makeDepositRequest = MakeDepositRequest.builder()
                .id(senderAccountId)
                .balance(depositAmount)
                .build();

        //make a deposit into sender account
        new MakeDepositRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .post(makeDepositRequest);

        //prepare transfer request data
        float transferAmount = depositAmount;
        MakeTransferRequest transferRequest = MakeTransferRequest.builder()
                .amount(transferAmount)
                .receiverAccountId(receiverAccountId)
                .senderAccountId(senderAccountId)
                .build();

        //make transfer using request above
        new MakeTransferRequester(
                RequestSpecs.unauthSpec(),
                ResponseSpecs.requestReturnsUnauth())
                .post(transferRequest);

        //System.out.println("Response Body: " + response.asString());
        //System.out.println("Response Status Code: " + response.getStatusCode());
    }

    @Test
    public void userCanNotMakeTransferFromNonExistingAccountTest() {
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

        //I skip creating sender account here and use variable instead
        int nonExistingAccountId = Integer.MAX_VALUE;

        //create sender account and convert response into the object
        CreateAccountResponse createReceiverAccount = new CreateAccountRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null)
                .extract()
                .as(CreateAccountResponse.class);

        //extract account id
        int receiverAccountId = createReceiverAccount.getId();

        //I don't need to make a deposit here
        //prepare transfer request data
        float transferAmount = RandomData.randomTransfer(1.0f, 100.0f);
        MakeTransferRequest transferRequest = MakeTransferRequest.builder()
                .amount(transferAmount)
                .receiverAccountId(receiverAccountId)
                .senderAccountId(nonExistingAccountId)
                .build();

        //make transfer using request above
        new MakeTransferRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsForbidden())
                .post(transferRequest);

    }

    @Test
    public void userCanNotMakeTransferToNonExistingAccountTest() {
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

        //create sender account and convert response into the object
        CreateAccountResponse createSenderAccount = new CreateAccountRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null)
                .extract()
                .as(CreateAccountResponse.class);

        //extract account id
        int senderAccountId = createSenderAccount.getId();

        //prepare deposit data
        float depositAmount = RandomData.randomTransfer(100.0f, 1000.0f);
        MakeDepositRequest makeDepositRequest = MakeDepositRequest.builder()
                .id(senderAccountId)
                .balance(depositAmount)
                .build();

        //make a deposit into sender account
        new MakeDepositRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .post(makeDepositRequest);

        //create NonExisting Receiver account
        int receiverAccountId = Integer.MAX_VALUE;

        //prepare transfer request data
        float transferAmount = depositAmount;
        MakeTransferRequest transferRequest = MakeTransferRequest.builder()
                .amount(transferAmount)
                .receiverAccountId(receiverAccountId)
                .senderAccountId(senderAccountId)
                .build();

        //make transfer using request above
        new MakeTransferRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsBadNoMessage())
                .post(transferRequest);

        //get list of sender's account transactions
        List <Transaction> senderAccountTransactions = new RetrieveAccountTransactionsRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .get(senderAccountId);
//
// check sender account transactions for our transfer
        Transaction transferOut = senderAccountTransactions.stream()
                .filter(t -> t.getType() == TransactionType.TRANSFER_OUT)
                .filter(t -> Math.abs(t.getAmount() - transferAmount) < EPS)
                .filter(t -> t.getRelatedAccountId() == receiverAccountId)
                .findFirst()
                .orElse(null);

        softly.assertThat(transferOut).isNull();

    }
    @Test
    public void userCanNotMakeTransferToSendingAccountTest() {
        //create user request
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

        //create sender account and convert response into the object
        CreateAccountResponse createSenderAccount = new CreateAccountRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null)
                .extract()
                .as(CreateAccountResponse.class);

        //extract account id
        int senderAccountId = createSenderAccount.getId();

        //prepare deposit data
        float depositAmount = RandomData.randomTransfer(100.0f, 1000.0f);
        MakeDepositRequest makeDepositRequest = MakeDepositRequest.builder()
                .id(senderAccountId)
                .balance(depositAmount)
                .build();

        //make a deposit into sender account
        new MakeDepositRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .post(makeDepositRequest);

        //prepare transfer request data where sender==receiver and sender is exists
        float transferAmount = depositAmount;
        MakeTransferRequest transferRequest = MakeTransferRequest.builder()
                .amount(transferAmount)
                .receiverAccountId(senderAccountId)
                .senderAccountId(senderAccountId)
                .build();

        //make transfer using request above
        new MakeTransferRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsBadNoMessage())
                .post(transferRequest);

        //check if transfer really wasn't sent
        //get list of all acc's trabsactions
        List <Transaction> senderAccountTransactions = new RetrieveAccountTransactionsRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .get(senderAccountId);

        //try to find our transaction
        Transaction transferOut = senderAccountTransactions.stream()
                .filter(t -> t.getType() == TransactionType.TRANSFER_OUT)
                .filter(t -> Math.abs(t.getAmount() - transferAmount) < EPS)
                .filter(t -> t.getRelatedAccountId() == senderAccountId)
                .findFirst()
                .orElse(null);

        //check that we didn't find a transaction
        softly.assertThat(transferOut).isNull();

    }
}

