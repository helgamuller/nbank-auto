package iteration2.ui;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import generators.RandomData;
import iteration1.api.BaseTest;
import models.CreateUserRequest;
import models.LoginUserRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.Alert;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import requests.steps.AdminSteps;
import requests.steps.UserSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static com.codeborne.selenide.CollectionCondition.sizeGreaterThan;
import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DepositMoneyTest extends BaseTest {
    @BeforeAll
    public static void setupLocal(){
        Configuration.baseUrl = "http://192.168.1.117:3000";
        Configuration.browser = "chrome";
        Configuration.browserSize = "1920x1080";
    }

    @Test
    public void UserCanDepositValidAmountTest(){
        //preconditions
        //STEP 1: admin logins
        //STEP 2: admin creates user
        CreateUserRequest user = AdminSteps.createUser();

        //STEP 3: user logins
        String userAuthHeader = new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOk())
                .post(LoginUserRequest.builder().username(user.getUsername()).password(user.getPassword()).build())
                .extract()
                .header("Authorization");

        String accNumber = UserSteps.createAccountAndGetNumber(user);

        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);
        //STEP 4: user creates account
        //test steps
        //STEP 1: user  opens dashboard
        Selenide.open("/dashboard");
        $(byText("💰 Deposit Money")).click();
        $(byText("💰 Deposit Money")).shouldBe(visible);

        $("select.account-selector")
                .selectOptionContainingText(accNumber);
        $("select.account-selector").getSelectedOption()
                .shouldHave(text(accNumber));

        //fill amount field
        String amount = RandomData.randomTransfer(new BigDecimal(1.00) , new BigDecimal(5000.00)).toString();
        $(Selectors.byAttribute("placeholder", "Enter amount")).sendKeys
                (amount);
        $(byText("💵 Deposit")).click();
        Alert alert = switchTo().alert();
        String message  = String.format(
                "✅ Successfully deposited $%s to account %s!",
                amount,
                accNumber
        );

        assertThat(alert.getText().contains(message)).isTrue();
        alert.accept();
        //check that balance updated in account selector on Deposit page
        $(byText("💰 Deposit Money")).click();    // open Deposit page

        $("select.account-selector").shouldBe(visible);
        $$("select.account-selector option")
                .shouldHave(sizeGreaterThan(0));
        $$("select.account-selector option")
                .findBy(text(accNumber))
                .shouldHave(text("Balance: $" + amount));

        //check that acc balance changed correctly on API side
        String accountBalance =  UserSteps.getAccountBalanceByAccNumber(user, accNumber).toString();
        assertEquals(amount, accountBalance);

        int userId = AdminSteps.getUserId(user);
    }

  public static Stream<Arguments> invalidDepositData(){
     return Stream.of(
             Arguments.of("", "Please enter a valid amount."),
             Arguments.of("-1", "Please enter a valid amount."),
             Arguments.of("50001", "Please deposit less or equal to 5000$.")
     );
  }

    @ParameterizedTest
    @MethodSource("invalidDepositData")
    public void UserCanNotDepositInvalidAmountTest(String amount, String errMessage){
        CreateUserRequest user = AdminSteps.createUser();
       //user logins
        String userAuthHeader = new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOk())
                .post(LoginUserRequest.builder().username(user.getUsername()).password(user.getPassword()).build())
                .extract()
                .header("Authorization");

        String accNumber = UserSteps.createAccountAndGetNumber(user);

        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);

        //STEP 1: user  opens dashboard
        Selenide.open("/dashboard");
        $(byText("💰 Deposit Money")).click();
        $(byText("💰 Deposit Money")).shouldBe(visible);

        $("select.account-selector")
                .selectOptionContainingText(accNumber);
        $("select.account-selector").getSelectedOption()
                .shouldHave(text(accNumber));

        //fill amount field
        $(Selectors.byAttribute("placeholder", "Enter amount")).sendKeys
                (amount);
        $(byText("💵 Deposit")).click();
        Alert alert = switchTo().alert();

        assertThat(alert.getText().contains(errMessage)).isTrue();
        alert.accept();
        //check that balance updated in account selector on Deposit page
        $(byText("💰 Deposit Money")).click();    // open Deposit page

        $("select.account-selector").shouldBe(visible);
        $$("select.account-selector option")
                .shouldHave(sizeGreaterThan(0));
        $$("select.account-selector option")
                .findBy(text(accNumber))
                .shouldHave(text("Balance: $0.00"));

        //check that acc balance changed correctly on API side
        String accountBalance =  UserSteps.getAccountBalanceByAccNumber(user, accNumber).toString();
        assertEquals("0.00", accountBalance);

        int userId = AdminSteps.getUserId(user);
    }
    @Test
    public void UserCanDepositWithoutChoosingTest(){
        CreateUserRequest user = AdminSteps.createUser();

        String userAuthHeader = new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOk())
                .post(LoginUserRequest.builder().username(user.getUsername()).password(user.getPassword()).build())
                .extract()
                .header("Authorization");

        String accNumber = UserSteps.createAccountAndGetNumber(user);

        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);

        Selenide.open("/dashboard");
        $(byText("💰 Deposit Money")).click();
        $(byText("💰 Deposit Money")).shouldBe(visible);

        String amount = RandomData.randomTransfer(new BigDecimal(1.00) , new BigDecimal(5000.00)).toString();
        $(Selectors.byAttribute("placeholder", "Enter amount")).sendKeys
                (amount);
        $(byText("💵 Deposit")).click();
        Alert alert = switchTo().alert();

        assertThat(alert.getText().contains("Please select an account.")).isTrue();
        alert.accept();
        //check that balance NOT updated in account selector on Deposit page
        $(byText("💰 Deposit Money")).click();    // open Deposit page
        $("select.account-selector").shouldBe(visible);
        $$("select.account-selector option")
                .shouldHave(sizeGreaterThan(0));
        $$("select.account-selector option")
                .findBy(text(accNumber))
                .shouldHave(text("Balance: $0.00"));

        //check that acc balance  didn't change on API side as well
        String accountBalance =  UserSteps.getAccountBalanceByAccNumber(user, accNumber).toString();
        assertEquals("0.00", accountBalance);

        int userId = AdminSteps.getUserId(user);
    }
}
