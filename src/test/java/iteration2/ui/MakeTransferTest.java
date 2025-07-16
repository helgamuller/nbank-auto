package iteration2.ui;

import api.generators.RandomData;
import api.generators.RandomModelGenerator;
import api.models.ChangeNameRequest;
import api.models.CreateUserRequest;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import iteration1.ui.BaseUiTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.Transfer;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static com.codeborne.selenide.Condition.text;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MakeTransferTest extends BaseUiTest {

    @Test
    public void userCanMakeTransferTest() {
        //preconditions
        CreateUserRequest user = AdminSteps.createUser();
        authAsUser(user);
        String name = UserSteps.userChangesTheirName(user);
        int senderAccountId =  UserSteps.createAccountAndGetId(user);
        String recipientAccountNumber =  UserSteps.createAccountAndGetNumber(user);
        UserSteps.makeDeposit(new BigDecimal("5000.00"), senderAccountId, user);
       //prepare data for transfer
        BigDecimal amount = RandomData.randomTransfer(new BigDecimal("100.00"), new BigDecimal("5000.00"));
        String message  = String.format(
                BankAlert.SUCCESSFULLY_TRANSFERRED_AMOUNT.getMessage(),
                amount,
                recipientAccountNumber
        );
        String senderAccNumber = "ACC"+ senderAccountId;

        //prepare data for asserts
        BigDecimal senderBalanceUi = new BigDecimal("5000.00").subtract(amount)
                .setScale(2, RoundingMode.HALF_UP);

        new Transfer().open().sendTransfer(senderAccNumber, name, recipientAccountNumber, amount.toString()).checkAlertMessageAndAccept(message)
                .open().selectAnOption(senderAccNumber)
                .getAccountFromDropdown()
                .shouldHave(text("Balance: $" + senderBalanceUi));

        new Transfer().open().selectAnOption(recipientAccountNumber)
                        .getAccountFromDropdown()
                                .shouldHave(text("Balance: $" + amount));

        //check that balances updated on API
        BigDecimal senderBalanceApi= UserSteps.getAccountBalance(user, senderAccountId);
        assertEquals(senderBalanceUi, senderBalanceApi);

        BigDecimal recipientBalanceApi= UserSteps.getAccountBalanceByAccNumber(user, recipientAccountNumber);
        assertEquals(amount, recipientBalanceApi);

        userId = AdminSteps.getUserId(user);
    }
    @Test
    @DisplayName("User  can not send a transfer until choose a sender account")
    public void UserCanNotSendTransferWithoutChoosingAccountTest() {
        //preconditions
        CreateUserRequest user = AdminSteps.createUser();
        authAsUser(user);
        String name = UserSteps.userChangesTheirName(user);
        int senderAccountId =  UserSteps.createAccountAndGetId(user);
        String recipientAccountNumber =  UserSteps.createAccountAndGetNumber(user);
        BigDecimal deposit = new BigDecimal("5000.00");
        UserSteps.makeDeposit(deposit, senderAccountId, user);
        BigDecimal amount = RandomData.randomTransfer(new BigDecimal("100.00"), deposit);

        // String senderAccNumber = "ACC"+ senderAccountId;
        //steps
       new Transfer().open().sendTransfer("",name, recipientAccountNumber, amount.toString())
                       .checkAlertMessageAndAccept(BankAlert.FILL_ALL_FIELDS_AND_CONFIRM)
                               .open()
                                       .selectAnOption("ACC" +senderAccountId)
                                               .getAccountFromDropdown()
                                                       .shouldHave(text("Balance: $" + deposit));


       new Transfer()
               .open()
               .selectAnOption(recipientAccountNumber)
                       .getAccountFromDropdown()
                               .shouldHave(text("Balance: $0.00" ));

        BigDecimal senderBalanceApi=UserSteps.getAccountBalance(user, senderAccountId);
        assertEquals(deposit, senderBalanceApi);

        BigDecimal recipientBalanceApi= UserSteps.getAccountBalanceByAccNumber(user, recipientAccountNumber);
        assertThat(recipientBalanceApi).isZero();

        userId = AdminSteps.getUserId(user);

    }

    @Test
    @DisplayName("User  can not send a transfer until input a recipient account number")
    public void userCanNotSendTransferWithEmptyRecipientAccountNumberTest() {
        //preconditions
        CreateUserRequest user = AdminSteps.createUser();
        authAsUser(user);
        String name = UserSteps.userChangesTheirName(user);
        int senderAccountId =  UserSteps.createAccountAndGetId(user);
        BigDecimal deposit = new BigDecimal("5000.00");
        UserSteps.makeDeposit(deposit, senderAccountId, user);
        String senderAccNumber = "ACC"+ senderAccountId;
        BigDecimal amount = RandomData.randomTransfer(new BigDecimal("100.00"), deposit);

        //steps
        new Transfer()
                .open()
                .sendTransfer(senderAccNumber, name, "", amount.toString())
                        .checkAlertMessageAndAccept(BankAlert.FILL_ALL_FIELDS_AND_CONFIRM)
                                .selectAnOption(senderAccNumber)
                                        .getAccountFromDropdown()
                                                .shouldHave(text("Balance: $" + deposit));


        BigDecimal senderBalanceApi=UserSteps.getAccountBalance(user, senderAccountId);
        assertEquals(deposit, senderBalanceApi);

        userId = AdminSteps.getUserId(user);
    }

    @Test
    @DisplayName("User can not send a transfer with empty amount")
    public void userCanNotSendTransferWithEmptyAmountTest() {
        //preconditions
        CreateUserRequest user = AdminSteps.createUser();
        authAsUser(user);
        String name = UserSteps.userChangesTheirName(user);
        int senderAccountId =  UserSteps.createAccountAndGetId(user);
        String recipientAccNumber =  UserSteps.createAccountAndGetNumber(user);
        BigDecimal deposit = new BigDecimal("5000.00");
        UserSteps.makeDeposit(deposit, senderAccountId, user);
        String senderAccNumber = "ACC"+senderAccountId;

        //steps
        new Transfer().open().sendTransfer(senderAccNumber, name, recipientAccNumber, "")
                        .checkAlertMessageAndAccept(BankAlert.FILL_ALL_FIELDS_AND_CONFIRM)
                        .open()
                        .selectAnOption(senderAccNumber)
                                .getAccountFromDropdown()
                                        .shouldHave(text("Balance: $" + deposit));

        BigDecimal senderBalanceApi=UserSteps.getAccountBalance(user, senderAccountId);
        assertEquals(deposit, senderBalanceApi);

        userId = AdminSteps.getUserId(user);

    }

    @Test
    @DisplayName("User  can not send a transfer without checking confirmation box")
    public void userCanNotSendTransferWithoutConfirmationTest() {
        //preconditions
        CreateUserRequest user = AdminSteps.createUser();
        authAsUser(user);
        String name = UserSteps.userChangesTheirName(user);
        int senderAccountId =  UserSteps.createAccountAndGetId(user);
        String recipientAccountNumber =  UserSteps.createAccountAndGetNumber(user);
        BigDecimal deposit = new BigDecimal("5000.00");
        UserSteps.makeDeposit(deposit, senderAccountId, user);
        String senderAccNumber = "ACC"+ senderAccountId;
        BigDecimal amount = RandomData.randomTransfer(new BigDecimal("100.00"), deposit);

        //steps
         new Transfer().open().sendTransferNoConfirmation(senderAccNumber,name, recipientAccountNumber, amount.toString())
                 .checkAlertMessageAndAccept(BankAlert.FILL_ALL_FIELDS_AND_CONFIRM)
                 .selectAnOption(senderAccNumber)
                 .getAccountFromDropdown()
                 .shouldHave(text("Balance: $" + deposit));

        //check that recipient balance wasn't updated on UI
        new Transfer().open().selectAnOption(recipientAccountNumber)
                        .getAccountFromDropdown()
                                .shouldHave(text("Balance: $0.00" ));

        BigDecimal senderBalanceApi=UserSteps.getAccountBalance(user, senderAccountId);
        assertEquals(deposit, senderBalanceApi);

        BigDecimal recipientBalanceApi= UserSteps.getAccountBalanceByAccNumber(user, recipientAccountNumber);
        assertThat(recipientBalanceApi).isZero();
        userId = AdminSteps.getUserId(user);
    }

    @Test
    @DisplayName("User  can not send a transfer to account if field Recipient Name: doesn't contain actual user name of account owner")
    //при разработке теста вылезла интересная проблема - если у владельца аккаунта получателя не было изменено имя, а осталось значение null,
    //то система не валидирует значение поля Имя, ей достаточно того, что аккаунт существует в системе - можно указать любое валидное (из двух слов)
    // имя и все получится.
    // Так что null тут не проверяется.
    public void userCanNotSendTransferToUserWithUnknownNameTest() {
        //preconditions
        CreateUserRequest user = AdminSteps.createUser();
        authAsUser(user);
        CreateUserRequest recipientUser = AdminSteps.createUser();//creates a recipient user
        UserSteps.userChangesTheirName(recipientUser);
        int senderAccountId =  UserSteps.createAccountAndGetId(user);
        String nonExistingName = RandomModelGenerator.generate(ChangeNameRequest.class).getName();
        String recipientAccountNumber =  UserSteps.createAccountAndGetNumber(recipientUser);
        BigDecimal deposit = new BigDecimal("5000.00");
        UserSteps.makeDeposit(deposit, senderAccountId, user);
        String senderAccNumber = "ACC" + senderAccountId;
        BigDecimal amount = RandomData.randomTransfer(new BigDecimal("1.00"), deposit);

        //steps
        new Transfer().open().sendTransfer(senderAccNumber, nonExistingName, recipientAccountNumber, amount.toString())
                        .checkAlertMessageAndAccept(BankAlert.RECIPIENT_NAME_DOES_NOT_MATCH_REGISTERED_NAME)
                                .open()
                                        .selectAnOption(senderAccNumber)
                                                .getAccountFromDropdown()
                                                .shouldHave(text("Balance: $" + deposit));

        //check that the balances weren't updated on API
        BigDecimal senderBalanceApi=UserSteps.getAccountBalance(user, senderAccountId);
        assertEquals(deposit, senderBalanceApi);

        BigDecimal recipientBalanceApi= UserSteps.getAccountBalanceByAccNumber(recipientUser, recipientAccountNumber);
        assertThat(recipientBalanceApi).isZero();

        userId = AdminSteps.getUserId(user);
    }


    @Test
    @DisplayName("User  can not send a transfer to account that doesn't exist")
    public void userCanNotSendTransferToAccountNotExistTest() {
        //preconditions
        CreateUserRequest user = AdminSteps.createUser();
        String name = UserSteps.userChangesTheirName(user);
        authAsUser(user);
        int senderAccountId =  UserSteps.createAccountAndGetId(user);
        String nonExistingRecipientAccount = RandomModelGenerator.generate(CreateUserRequest.class).getUsername();
        BigDecimal deposit = new BigDecimal("5000.00");
        UserSteps.makeDeposit(deposit, senderAccountId, user);
        String senderAccNumber = "ACC"+ senderAccountId;
        BigDecimal amount = RandomData.randomTransfer(new BigDecimal("1.00"), deposit);


        //steps
        new Transfer().open().sendTransfer(senderAccNumber, name, nonExistingRecipientAccount, amount.toString())
                        .checkAlertMessageAndAccept(BankAlert.NO_USER_FOUND_WITH_THIS_NUMBER)
                                .open()
                                        .selectAnOption(senderAccNumber)
                                                .getAccountFromDropdown()
                                                        .shouldHave(text("Balance: $" + deposit));

        //check that sender's balance wasn't updated on API
        BigDecimal senderBalanceApi=UserSteps.getAccountBalance(user, senderAccountId);
        assertEquals(deposit, senderBalanceApi);

        userId = AdminSteps.getUserId(user);

    }

    @Test
    @DisplayName("User  can not transfer invalid amount")
    public void userCanNotSendIncorrectAmountTest() {
        //preconditions
        CreateUserRequest user = AdminSteps.createUser();
        authAsUser(user);
        CreateUserRequest recipientUser = AdminSteps.createUser();//creates a recipient user
        String recipientName =  UserSteps.userChangesTheirName(recipientUser);
        int senderAccountId =  UserSteps.createAccountAndGetId(user);
        String recipientAccountNumber =  UserSteps.createAccountAndGetNumber(recipientUser);
        BigDecimal deposit = new BigDecimal("5000.00");
        UserSteps.makeDeposit(deposit, senderAccountId, user);
        BigDecimal amount = new BigDecimal("-1.00");
        String senderAccNumber = "ACC"+ senderAccountId;

        //steps
        new Transfer().open().sendTransfer(senderAccNumber, recipientName, recipientAccountNumber, amount.toString())
                        .checkAlertMessageAndAccept(BankAlert.INSUFFICIENT_FUNDS_OR_INVALID_ACCOUNT)
                                .open()
                                        .selectAnOption(senderAccNumber)
                                                .getAccountFromDropdown()
                                                        .shouldHave(text("Balance: $" + deposit));


        //check that balances weren't updated on API
        BigDecimal senderBalanceApi=UserSteps.getAccountBalance(user, senderAccountId);
        assertEquals(deposit, senderBalanceApi);
        BigDecimal recipientBalanceApi=UserSteps.getAccountBalanceByAccNumber(recipientUser, recipientAccountNumber);
        assertThat(recipientBalanceApi).isZero();

        userId = AdminSteps.getUserId(user);
    }


    @Test
    @DisplayName("User  can not make transfer which exceeds $10000.00")
    public void userCanNotSendAmountOverLimitTest() {
        //preconditions
        CreateUserRequest user = AdminSteps.createUser();
        authAsUser(user);
        CreateUserRequest recipientUser = AdminSteps.createUser();//creates a recipient user
        String recipientName =  UserSteps.userChangesTheirName(recipientUser);
        int senderAccountId =  UserSteps.createAccountAndGetId(user);
        String recipientAccountNumber =  UserSteps.createAccountAndGetNumber(recipientUser);
        BigDecimal deposit = new BigDecimal("5000.00");
        UserSteps.makeDeposit(deposit, senderAccountId, user);
        UserSteps.makeDeposit(deposit, senderAccountId, user);
        UserSteps.makeDeposit(deposit, senderAccountId, user);
        String senderAccNumber = "ACC"+ senderAccountId;
        BigDecimal amount = RandomData.randomTransfer(new BigDecimal("10001.00"), new BigDecimal("15000.00"));

        //steps
        new Transfer().open().sendTransfer(senderAccNumber, recipientName, recipientAccountNumber, amount.toString())
                        .checkAlertMessageAndAccept(BankAlert.TRANSFER_AMOUNT_CANNOT_EXCEED_10000)
                                .open()
                                        .selectAnOption(senderAccNumber)
                                                .getAccountFromDropdown()
                                                        .shouldHave(text("Balance: $" + deposit.multiply(new BigDecimal("3"))));

        //check that balances weren't updated on API
        BigDecimal senderBalanceApi=UserSteps.getAccountBalance(user, senderAccountId);
        assertEquals(deposit.multiply(new BigDecimal("3")), senderBalanceApi);
        BigDecimal recipientBalanceApi=UserSteps.getAccountBalanceByAccNumber(recipientUser, recipientAccountNumber);
        assertThat(recipientBalanceApi).isZero();

        userId = AdminSteps.getUserId(user);

    }
}


