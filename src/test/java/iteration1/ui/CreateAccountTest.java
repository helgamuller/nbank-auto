package iteration1.ui;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import models.CreateAccountResponse;
import models.CreateUserRequest;
import models.LoginUserRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import requests.steps.AdminSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.codeborne.selenide.Selenide.*;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class CreateAccountTest {
    @BeforeAll
    public static void setupLocal(){
        Configuration.baseUrl = "http://192.168.1.117:3000";
        Configuration.browser = "chrome";
        Configuration.browserSize = "1920x1080";
    }
    @Test
   public void userCanCreateAccountTest(){
        //preconditions
        //STEP 1: admin logins
        //STEP 2: admin creates user
        //STEP 3: user logins
       CreateUserRequest user = AdminSteps.createUser();
       String userAuthHeader = new CrudRequester(
               RequestSpecs.unauthSpec(),
               Endpoint.LOGIN,
               ResponseSpecs.requestReturnsOk())
               .post(LoginUserRequest.builder().username(user.getUsername()).password(user.getPassword()).build())
               .extract()
               .header("Authorization");

       Selenide.open("/");
       executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);

       Selenide.open("/dashboard");
       $(Selectors.byText("➕ Create New Account")).click();

       Alert alert = switchTo().alert();
       String alertText = alert.getText();

       assertThat(alertText).contains("✅ New Account Created! Account Number:");

       alert.accept();
       Pattern pattern = Pattern.compile("Account Number: (\\w+)");
       Matcher matcher = pattern.matcher(alertText);
       matcher.find();

       String createdAccNumber = matcher.group(1);

       //test steps
        //STEP 4: user creates account
       CreateAccountResponse[] existingUserAccounts = given()
               .spec(RequestSpecs.authAsUserSpec(user.getUsername(), user.getPassword()))
               .get("http://localhost:4111/api/v1/customer/accounts")
               .then().assertThat()
               .extract().as(CreateAccountResponse[].class);

       assertThat(existingUserAccounts).hasSize(1);

       CreateAccountResponse createdAccount = existingUserAccounts[0];

       assertThat(createdAccount).isNotNull();
       assertThat(createdAccount.getBalance()).isZero();
   }

   }


