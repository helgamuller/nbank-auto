package iteration2.ui;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import generators.RandomData;
import generators.RandomModelGenerator;
import iteration1.api.BaseTest;
import models.ChangeNameRequest;
import models.CreateUserRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import requests.steps.AdminSteps;
import requests.steps.UserSteps;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static com.codeborne.selenide.CollectionCondition.sizeGreaterThan;
import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MakeTransferTest extends BaseTest {
    @BeforeAll
    public static void setupLocal() {
        Configuration.baseUrl = "http://192.168.1.117:3000";
        Configuration.browser = "chrome";
        Configuration.browserSize = "1920x1080";
    }

    @Test
    public void UserCanMakeTransferTest() {
        //preconditions
        CreateUserRequest user = AdminSteps.createUser();
        UserSteps.saveAuthHeaderToLocalStorage(user);
        String name = UserSteps.userChangesTheirName(user);
        int senderAccountId =  UserSteps.createAccountAndGetId(user);
        //int recipientAccountId =  UserSteps.createAccountAndGetId(user);
        String recipientAccountNumber =  UserSteps.createAccountAndGetNumber(user);
        UserSteps.makeDeposit(new BigDecimal("5000.00"), senderAccountId, user);

        //steps
        Selenide.open("/dashboard");
        $(Selectors.byText("🔄 Make a Transfer")).click();

        $(Selectors.byText("🔄 Make a Transfer")).shouldBe(visible);
        $("select.account-selector").shouldBe(visible);
        $$("select.account-selector option")
                .shouldHave(sizeGreaterThan(0));
        $("select.account-selector").selectOptionByValue(String.valueOf(senderAccountId));
        $(Selectors.byAttribute("Placeholder", "Enter recipient name")).sendKeys(name);
        $(Selectors.byAttribute("Placeholder", "Enter recipient account number")).sendKeys(recipientAccountNumber);
        BigDecimal amount = RandomData.randomTransfer(new BigDecimal("100.00"), new BigDecimal("5000.00"));
        $(Selectors.byAttribute("Placeholder", "Enter amount")).sendKeys(amount.toString());
        $(Selectors.byId("confirmCheck")).click();
        $(Selectors.byText("🚀 Send Transfer")).click();

        Alert alerts = switchTo().alert();
        String message  = String.format(
                "✅ Successfully transferred $%s to account %s!",
                amount,
                recipientAccountNumber
        );
        assertThat(alerts.getText().contains(message)).isTrue();
        Selenide.refresh();
        $("select.account-selector").shouldBe(visible);
        //check that sender balance updated on UI
        BigDecimal senderBalanceUi = new BigDecimal("5000.00").subtract(amount)
                .setScale(2, RoundingMode.HALF_UP);
        $$("select.account-selector option")
                .findBy(value(String.valueOf(senderAccountId)))
                .shouldHave(text("Balance: $" + senderBalanceUi));

        //check that receiver balance updated on UI
        $$("select.account-selector option")
                .findBy(text(recipientAccountNumber))
                .shouldHave(text("Balance: $" + amount));

        BigDecimal senderBalanceApi= UserSteps.getAccountBalance(user, senderAccountId);
        assertEquals(senderBalanceUi, senderBalanceApi);

        BigDecimal recipientBalanceApi= UserSteps.getAccountBalanceByAccNumber(user, recipientAccountNumber);
        assertEquals(amount, recipientBalanceApi);

        userId = AdminSteps.getUserId(user);
    }
    @Test
    @DisplayName("User  can not send a transfer until choose an account")
    public void UserCanNotSendTransferWithoutChoosingAccountTest() {
        //preconditions
        CreateUserRequest user = AdminSteps.createUser();
        UserSteps.saveAuthHeaderToLocalStorage(user);
        String name = UserSteps.userChangesTheirName(user);
        int senderAccountId =  UserSteps.createAccountAndGetId(user);
        //int recipientAccountId =  UserSteps.createAccountAndGetId(user);
        String recipientAccountNumber =  UserSteps.createAccountAndGetNumber(user);
        BigDecimal deposit = new BigDecimal("5000.00");
        UserSteps.makeDeposit(deposit, senderAccountId, user);

        //steps
        Selenide.open("/dashboard");
        $(Selectors.byText("🔄 Make a Transfer")).click();

        $(Selectors.byText("🔄 Make a Transfer")).shouldBe(visible);
        $("select.account-selector").shouldBe(visible);
        $$("select.account-selector option")
                .shouldHave(sizeGreaterThan(0));
        //$("select.account-selector").selectOptionByValue(String.valueOf(senderAccountId));
        $(Selectors.byAttribute("Placeholder", "Enter recipient name")).sendKeys(name);
        $(Selectors.byAttribute("Placeholder", "Enter recipient account number")).sendKeys(recipientAccountNumber);
        BigDecimal amount = RandomData.randomTransfer(new BigDecimal("100.00"), deposit);
        $(Selectors.byAttribute("Placeholder", "Enter amount")).sendKeys(amount.toString());
        $(Selectors.byId("confirmCheck")).click();
        $(Selectors.byText("🚀 Send Transfer")).click();

        Alert alerts = switchTo().alert();
        assertThat(alerts.getText().contains("Please fill all fields and confirm")).isTrue();
        Selenide.refresh();
        $("select.account-selector").shouldBe(visible);
        //check that sender balance updated on UI
        $$("select.account-selector option")
                .findBy(value(String.valueOf(senderAccountId)))
                .shouldHave(text("Balance: $" + deposit));

        //check that receiver balance updated on UI
        $$("select.account-selector option")
                .findBy(text(recipientAccountNumber))
                .shouldHave(text("Balance: $0.00" ));

        BigDecimal senderBalanceApi=UserSteps.getAccountBalance(user, senderAccountId);
        assertEquals(deposit, senderBalanceApi);

        BigDecimal recipientBalanceApi= UserSteps.getAccountBalanceByAccNumber(user, recipientAccountNumber);
        assertThat(recipientBalanceApi).isZero();

        userId = AdminSteps.getUserId(user);

    }

    @Test
    @DisplayName("User  can not send a transfer until input a recipient account number")
    public void UserCanNotSendTransferWithEmptyRecipientAccountNumberTest() {
        //preconditions
        CreateUserRequest user = AdminSteps.createUser();
        UserSteps.saveAuthHeaderToLocalStorage(user);
        String name = UserSteps.userChangesTheirName(user);
        int senderAccountId =  UserSteps.createAccountAndGetId(user);
        //int recipientAccountId =  UserSteps.createAccountAndGetId(user);
        //String recipientAccountNumber =  UserSteps.createAccountAndGetNumber(user);
        BigDecimal deposit = new BigDecimal("5000.00");
        UserSteps.makeDeposit(deposit, senderAccountId, user);

        //steps
        Selenide.open("/dashboard");
        $(Selectors.byText("🔄 Make a Transfer")).click();

        $(Selectors.byText("🔄 Make a Transfer")).shouldBe(visible);
        $("select.account-selector").shouldBe(visible);
        $$("select.account-selector option")
                .shouldHave(sizeGreaterThan(0));
        $("select.account-selector").selectOptionByValue(String.valueOf(senderAccountId));
        $(Selectors.byAttribute("Placeholder", "Enter recipient name")).sendKeys(name);
        //$(Selectors.byAttribute("Placeholder", "Enter recipient account number")).sendKeys(recipientAccountNumber);
        BigDecimal amount = RandomData.randomTransfer(new BigDecimal("100.00"), deposit);
        $(Selectors.byAttribute("Placeholder", "Enter amount")).sendKeys(amount.toString());
        $(Selectors.byId("confirmCheck")).click();
        $(Selectors.byText("🚀 Send Transfer")).click();

        Alert alerts = switchTo().alert();
        assertThat(alerts.getText().contains("Please fill all fields and confirm")).isTrue();
        Selenide.refresh();
        $("select.account-selector").shouldBe(visible);
        //check that sender balance wasn't updated on UI
        $$("select.account-selector option")
                .findBy(value(String.valueOf(senderAccountId)))
                .shouldHave(text("Balance: $" + deposit));

        //check that receiver balance updated on UI
//        $$("select.account-selector option")
//                .findBy(text(recipientAccountNumber))
//                .shouldHave(text("Balance: $0.00" ));

        BigDecimal senderBalanceApi=UserSteps.getAccountBalance(user, senderAccountId);
        assertEquals(deposit, senderBalanceApi);

//        BigDecimal recipientBalanceApi= UserSteps.getAccountBalanceByAccNumber(user, recipientAccountNumber);
//        assertThat(recipientBalanceApi).isZero();
        userId = AdminSteps.getUserId(user);

    }

    @Test
    @DisplayName("User can not send a transfer with empty amount")
    public void UserCanNotSendTransferWithEmptyAmountTest() {
        //preconditions
        CreateUserRequest user = AdminSteps.createUser();
        UserSteps.saveAuthHeaderToLocalStorage(user);
        String name = UserSteps.userChangesTheirName(user);
        int senderAccountId =  UserSteps.createAccountAndGetId(user);
        //int recipientAccountId =  UserSteps.createAccountAndGetId(user);
        String recipientAccountNumber =  UserSteps.createAccountAndGetNumber(user);
        BigDecimal deposit = new BigDecimal("5000.00");
        UserSteps.makeDeposit(deposit, senderAccountId, user);

        //steps
        Selenide.open("/dashboard");
        $(Selectors.byText("🔄 Make a Transfer")).click();

        $(Selectors.byText("🔄 Make a Transfer")).shouldBe(visible);
        $("select.account-selector").shouldBe(visible);
        $$("select.account-selector option")
                .shouldHave(sizeGreaterThan(0));
        $("select.account-selector").selectOptionByValue(String.valueOf(senderAccountId));
        $(Selectors.byAttribute("Placeholder", "Enter recipient name")).sendKeys(name);
        $(Selectors.byAttribute("Placeholder", "Enter recipient account number")).sendKeys(recipientAccountNumber);
        //BigDecimal amount = RandomData.randomTransfer(new BigDecimal("100.00"), deposit);
        //$(Selectors.byAttribute("Placeholder", "Enter amount")).sendKeys(amount.toString());
        $(Selectors.byId("confirmCheck")).click();
        $(Selectors.byText("🚀 Send Transfer")).click();

        Alert alerts = switchTo().alert();
        assertThat(alerts.getText().contains("Please fill all fields and confirm")).isTrue();
        Selenide.refresh();
        $("select.account-selector").shouldBe(visible);
        //check that sender balance wasn't updated on UI
        $$("select.account-selector option")
                .findBy(value(String.valueOf(senderAccountId)))
                .shouldHave(text("Balance: $" + deposit));

        BigDecimal senderBalanceApi=UserSteps.getAccountBalance(user, senderAccountId);
        assertEquals(deposit, senderBalanceApi);

        userId = AdminSteps.getUserId(user);

    }

    @Test
    @DisplayName("User  can not send a transfer without check confirmation box")
    public void UserCanNotSendTransferWithoutConfirmationTest() {
        //preconditions
        CreateUserRequest user = AdminSteps.createUser();
        UserSteps.saveAuthHeaderToLocalStorage(user);
        String name = UserSteps.userChangesTheirName(user);
        int senderAccountId =  UserSteps.createAccountAndGetId(user);
        //int recipientAccountId =  UserSteps.createAccountAndGetId(user);
        String recipientAccountNumber =  UserSteps.createAccountAndGetNumber(user);
        BigDecimal deposit = new BigDecimal("5000.00");
        UserSteps.makeDeposit(deposit, senderAccountId, user);

        //steps
        Selenide.open("/dashboard");
        $(Selectors.byText("🔄 Make a Transfer")).click();

        $(Selectors.byText("🔄 Make a Transfer")).shouldBe(visible);
        $("select.account-selector").shouldBe(visible);
        $$("select.account-selector option")
                .shouldHave(sizeGreaterThan(0));
        $("select.account-selector").selectOptionByValue(String.valueOf(senderAccountId));
        $(Selectors.byAttribute("Placeholder", "Enter recipient name")).sendKeys(name);
        $(Selectors.byAttribute("Placeholder", "Enter recipient account number")).sendKeys(recipientAccountNumber);
        BigDecimal amount = RandomData.randomTransfer(new BigDecimal("100.00"), deposit);
        $(Selectors.byAttribute("Placeholder", "Enter amount")).sendKeys(amount.toString());
        //$(Selectors.byId("confirmCheck")).click();
        $(Selectors.byText("🚀 Send Transfer")).click();

        Alert alerts = switchTo().alert();
        assertThat(alerts.getText().contains("Please fill all fields and confirm")).isTrue();
        alerts.accept();
        Selenide.refresh();
        $("select.account-selector").shouldBe(visible);
        //check that sender balance wasn't updated on UI
        $$("select.account-selector option")
                .findBy(value(String.valueOf(senderAccountId)))
                .shouldHave(text("Balance: $" + deposit));

        //check that receiver balance wasn't updated on UI
        $$("select.account-selector option")
                .findBy(text(recipientAccountNumber))
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
    public void UserCanNotSendTransferToUserWithUnknownNameTest() {
        //preconditions
        CreateUserRequest user = AdminSteps.createUser();
        UserSteps.saveAuthHeaderToLocalStorage(user);
        CreateUserRequest recipientUser = AdminSteps.createUser();//creates a recipient user
        UserSteps.userChangesTheirName(recipientUser);
        int senderAccountId =  UserSteps.createAccountAndGetId(user);
        String nonExistingName = RandomModelGenerator.generate(ChangeNameRequest.class).getName();
        String recipientAccountNumber =  UserSteps.createAccountAndGetNumber(recipientUser);
        BigDecimal deposit = new BigDecimal("5000.00");
        UserSteps.makeDeposit(deposit, senderAccountId, user);

        //steps
        Selenide.open("/dashboard");
        $(Selectors.byText("🔄 Make a Transfer")).click();

        $(Selectors.byText("🔄 Make a Transfer")).shouldBe(visible);
        $("select.account-selector").shouldBe(visible);
        $$("select.account-selector option")
                .shouldHave(sizeGreaterThan(0));
        BigDecimal amount = RandomData.randomTransfer(new BigDecimal("1.00"), deposit);
        $("select.account-selector").selectOptionByValue(String.valueOf(senderAccountId));
        $(Selectors.byAttribute("Placeholder", "Enter recipient name")).sendKeys(nonExistingName);
        $(Selectors.byAttribute("Placeholder", "Enter recipient account number")).sendKeys(recipientAccountNumber);
        $(Selectors.byAttribute("Placeholder", "Enter amount")).sendKeys(amount.toString());
        $(Selectors.byId("confirmCheck")).click();
        $(Selectors.byText("🚀 Send Transfer")).click();

        Alert alerts = switchTo().alert();
        assertThat(alerts.getText().contains("❌ The recipient name does not match the registered name.")).isTrue();
        alerts.accept();
        Selenide.refresh();
        $("select.account-selector").shouldBe(visible);
        //check that sender balance wasn't updated on UI
        $$("select.account-selector option")
                .findBy(value(String.valueOf(senderAccountId)))
                .shouldHave(text("Balance: $" + deposit));

        //check that receiver balance wasn't updated on UI

        BigDecimal senderBalanceApi=UserSteps.getAccountBalance(user, senderAccountId);
        assertEquals(deposit, senderBalanceApi);

        BigDecimal recipientBalanceApi= UserSteps.getAccountBalanceByAccNumber(recipientUser, recipientAccountNumber);
        assertThat(recipientBalanceApi).isZero();
        userId = AdminSteps.getUserId(user);

    }


    @Test
    @DisplayName("User  can not send a transfer to account that doesn't exist")
    public void UserCanNotSendTransferToAccountWhichNotExistTest() {
        //preconditions
        CreateUserRequest user = AdminSteps.createUser();
        UserSteps.saveAuthHeaderToLocalStorage(user);
        int senderAccountId =  UserSteps.createAccountAndGetId(user);
        String nonExistingRecipientAccount = RandomModelGenerator.generate(CreateUserRequest.class).getUsername();
        BigDecimal deposit = new BigDecimal("5000.00");
        UserSteps.makeDeposit(deposit, senderAccountId, user);

        //steps
        Selenide.open("/dashboard");
        $(Selectors.byText("🔄 Make a Transfer")).click();

        $(Selectors.byText("🔄 Make a Transfer")).shouldBe(visible);
        $("select.account-selector").shouldBe(visible);
        $$("select.account-selector option")
                .shouldHave(sizeGreaterThan(0));
        BigDecimal amount = RandomData.randomTransfer(new BigDecimal("1.00"), deposit);
        $("select.account-selector").selectOptionByValue(String.valueOf(senderAccountId));
        //$(Selectors.byAttribute("Placeholder", "Enter recipient name")).sendKeys(nonExistingName);
        $(Selectors.byAttribute("Placeholder", "Enter recipient account number")).sendKeys(nonExistingRecipientAccount);
        $(Selectors.byAttribute("Placeholder", "Enter amount")).sendKeys(amount.toString());
        $(Selectors.byId("confirmCheck")).click();
        $(Selectors.byText("🚀 Send Transfer")).click();

        Alert alerts = switchTo().alert();
        assertThat(alerts.getText().contains("❌ No user found with this account number.")).isTrue();
        alerts.accept();
        Selenide.refresh();
        $("select.account-selector").shouldBe(visible);
        //check that sender balance wasn't updated on UI
        $$("select.account-selector option")
                .findBy(value(String.valueOf(senderAccountId)))
                .shouldHave(text("Balance: $" + deposit));

        //check that receiver balance wasn't updated on UI
        BigDecimal senderBalanceApi=UserSteps.getAccountBalance(user, senderAccountId);
        assertEquals(deposit, senderBalanceApi);

        userId = AdminSteps.getUserId(user);

    }

    @Test
    @DisplayName("User  can not transfer invalid amount")
    public void UserCanNotSendIncorrectAmountTest() {
        //preconditions
        CreateUserRequest user = AdminSteps.createUser();
        UserSteps.saveAuthHeaderToLocalStorage(user);
        CreateUserRequest recipientUser = AdminSteps.createUser();//creates a recipient user
        String recipientName =  UserSteps.userChangesTheirName(recipientUser);
        int senderAccountId =  UserSteps.createAccountAndGetId(user);
        String recipientAccountNumber =  UserSteps.createAccountAndGetNumber(recipientUser);
        BigDecimal deposit = new BigDecimal("5000.00");
        UserSteps.makeDeposit(deposit, senderAccountId, user);

        //steps
        Selenide.open("/dashboard");
        $(Selectors.byText("🔄 Make a Transfer")).click();

        $(Selectors.byText("🔄 Make a Transfer")).shouldBe(visible);
        $("select.account-selector").shouldBe(visible);
        $$("select.account-selector option")
                .shouldHave(sizeGreaterThan(0));
        BigDecimal amount = new BigDecimal("-1.00");
        $("select.account-selector").selectOptionByValue(String.valueOf(senderAccountId));
        $(Selectors.byAttribute("Placeholder", "Enter recipient name")).sendKeys(recipientName);
        $(Selectors.byAttribute("Placeholder", "Enter recipient account number")).sendKeys(recipientAccountNumber);
        $(Selectors.byAttribute("Placeholder", "Enter amount")).sendKeys(amount.toString());
        $(Selectors.byId("confirmCheck")).click();
        $(Selectors.byText("🚀 Send Transfer")).click();

        Alert alerts = switchTo().alert();
        assertThat(alerts.getText().contains("❌ Error: Invalid transfer: insufficient funds or invalid accounts")).isTrue();
        alerts.accept();
        Selenide.refresh();
        $("select.account-selector").shouldBe(visible);
        //check that sender balance wasn't updated on UI
        $$("select.account-selector option")
                .findBy(value(String.valueOf(senderAccountId)))
                .shouldHave(text("Balance: $" + deposit));

        //check that receiver balance wasn't updated on UI
        BigDecimal senderBalanceApi=UserSteps.getAccountBalance(user, senderAccountId);
        assertEquals(deposit, senderBalanceApi);
        BigDecimal recipientBalanceApi=UserSteps.getAccountBalanceByAccNumber(recipientUser, recipientAccountNumber);
        assertThat(recipientBalanceApi).isZero();

        userId = AdminSteps.getUserId(user);

    }

    @Test
    @DisplayName("User  can not send a transfer to account that doesn't exist")
    public void userCanNotSendTransferToAccountWhichNotExistTest(){
        //preconditions
        CreateUserRequest user = AdminSteps.createUser();
        UserSteps.saveAuthHeaderToLocalStorage(user);
        int senderAccountId =  UserSteps.createAccountAndGetId(user);
        String nonExistingRecipientAccount = RandomModelGenerator.generate(CreateUserRequest.class).getUsername();
        BigDecimal deposit = new BigDecimal("5000.00");
        UserSteps.makeDeposit(deposit, senderAccountId, user);

        //steps
        Selenide.open("/dashboard");
        $(Selectors.byText("🔄 Make a Transfer")).click();

        $(Selectors.byText("🔄 Make a Transfer")).shouldBe(visible);
        $("select.account-selector").shouldBe(visible);
        $$("select.account-selector option")
                .shouldHave(sizeGreaterThan(0));
        BigDecimal amount = RandomData.randomTransfer(new BigDecimal("1.00"), deposit);
        $("select.account-selector").selectOptionByValue(String.valueOf(senderAccountId));
        //$(Selectors.byAttribute("Placeholder", "Enter recipient name")).sendKeys(nonExistingName);
        $(Selectors.byAttribute("Placeholder", "Enter recipient account number")).sendKeys(nonExistingRecipientAccount);
        $(Selectors.byAttribute("Placeholder", "Enter amount")).sendKeys(amount.toString());
        $(Selectors.byId("confirmCheck")).click();
        $(Selectors.byText("🚀 Send Transfer")).click();

        Alert alerts = switchTo().alert();
        assertThat(alerts.getText().contains("❌ No user found with this account number.")).isTrue();
        alerts.accept();
        Selenide.refresh();
        $("select.account-selector").shouldBe(visible);
        //check that sender balance wasn't updated on UI
        $$("select.account-selector option")
                .findBy(value(String.valueOf(senderAccountId)))
                .shouldHave(text("Balance: $" + deposit));

        //check that receiver balance wasn't updated on UI
        BigDecimal senderBalanceApi=UserSteps.getAccountBalance(user, senderAccountId);
        assertEquals(deposit, senderBalanceApi);

        userId = AdminSteps.getUserId(user);

    }

    @Test
    @DisplayName("User  can not make transfer which exceeds $10000.00")
    public void UserCanNotSendAmountOverLimitTest() {
        //preconditions
        CreateUserRequest user = AdminSteps.createUser();
        UserSteps.saveAuthHeaderToLocalStorage(user);
        CreateUserRequest recipientUser = AdminSteps.createUser();//creates a recipient user
        String recipientName =  UserSteps.userChangesTheirName(recipientUser);
        int senderAccountId =  UserSteps.createAccountAndGetId(user);
        String recipientAccountNumber =  UserSteps.createAccountAndGetNumber(recipientUser);
        BigDecimal deposit = new BigDecimal("5000.00");
        UserSteps.makeDeposit(deposit, senderAccountId, user);
        UserSteps.makeDeposit(deposit, senderAccountId, user);
        UserSteps.makeDeposit(deposit, senderAccountId, user);

        //steps
        Selenide.open("/dashboard");
        $(Selectors.byText("🔄 Make a Transfer")).click();

        $(Selectors.byText("🔄 Make a Transfer")).shouldBe(visible);
        $("select.account-selector").shouldBe(visible);
        $$("select.account-selector option")
                .shouldHave(sizeGreaterThan(0));
        BigDecimal amount = RandomData.randomTransfer(new BigDecimal("10001.00"), new BigDecimal("15000.00"));
        $("select.account-selector").selectOptionByValue(String.valueOf(senderAccountId));
        $(Selectors.byAttribute("Placeholder", "Enter recipient name")).sendKeys(recipientName);
        $(Selectors.byAttribute("Placeholder", "Enter recipient account number")).sendKeys(recipientAccountNumber);
        $(Selectors.byAttribute("Placeholder", "Enter amount")).sendKeys(amount.toString());
        $(Selectors.byId("confirmCheck")).click();
        $(Selectors.byText("🚀 Send Transfer")).click();

        Alert alerts = switchTo().alert();
        assertThat(alerts.getText().contains("❌ Error: Transfer amount cannot exceed 10000")).isTrue();
        alerts.accept();
        Selenide.refresh();
        $("select.account-selector").shouldBe(visible);
        //check that sender balance wasn't updated on UI
        $$("select.account-selector option")
                .findBy(value(String.valueOf(senderAccountId)))
                .shouldHave(text("Balance: $" + deposit.multiply(new BigDecimal("3"))));

        //check that receiver balance wasn't updated on UI
        BigDecimal senderBalanceApi=UserSteps.getAccountBalance(user, senderAccountId);
        assertEquals(deposit.multiply(new BigDecimal("3")), senderBalanceApi);
        BigDecimal recipientBalanceApi=UserSteps.getAccountBalanceByAccNumber(recipientUser, recipientAccountNumber);
        assertThat(recipientBalanceApi).isZero();

        userId = AdminSteps.getUserId(user);

    }
}


