package iteration2.ui;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import generators.RandomModelGenerator;
import iteration1.api.BaseTest;
import models.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import requests.skeleton.requesters.ValidatedCrudRequester;
import requests.steps.AdminSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.Assertions.assertThat;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserChangeNameTest extends BaseTest {
    @BeforeAll
    public static void setupLocal() {
        Configuration.baseUrl = "http://192.168.1.117:3000";
        Configuration.browser = "chrome";
        Configuration.browserSize = "1920x1080";
    }

    @Test
    public void UserCanChangeNameTest() {
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
        $(Selectors.byText("Noname")).click();

        $(Selectors.byText("✏️ Edit Profile"))
                .shouldBe(visible);
        String newName = RandomModelGenerator.generate(ChangeNameRequest.class).getName();
        $(Selectors.byAttribute("placeholder", "Enter new name")).sendKeys(newName);
        $(Selectors.byText("💾 Save Changes")).click();

        Alert alert = switchTo().alert();
        assertThat(alert.getText().contains("✅ Name updated successfully!")).isTrue();
        alert.accept();
        //check name change on the API side
        String nameAfterChange = new ValidatedCrudRequester<CreateUserResponse>(
                RequestSpecs.authAsUserSpec(user.getUsername(),user.getPassword()),
                Endpoint.CUSTOMER_PROFILE_GET,
                ResponseSpecs.requestReturnsOk())
                .getAll().getName();

        assertEquals(nameAfterChange, newName);
        //reload a page because name updates only after page reloading
        Selenide.refresh();
        $(Selectors.byText(newName)).shouldBe(visible);

        int userId = AdminSteps.getUserId(user);

    }

    @Test
    public void UserCanNotChangeNameToIncorrectTest() {
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
        $(Selectors.byText("Noname")).click();

        $(Selectors.byText("✏️ Edit Profile"))
                .shouldBe(visible);
        String newName = RandomModelGenerator.generate(CreateUserRequest.class).getUsername();
        $(Selectors.byAttribute("placeholder", "Enter new name")).sendKeys(newName);
        $(Selectors.byText("💾 Save Changes")).click();

        Alert alert = switchTo().alert();
        assertThat(alert.getText().contains("Name must contain two words with letters only")).isTrue();
        alert.accept();
        //check name didn't change on the API side
        String nameAfterChange = new ValidatedCrudRequester<CreateUserResponse>(
                RequestSpecs.authAsUserSpec(user.getUsername(),user.getPassword()),
                Endpoint.CUSTOMER_PROFILE_GET,
                ResponseSpecs.requestReturnsOk())
                .getAll().getName();

        assertThat(nameAfterChange).isNull();

        //reload a page because name updates only after page reloading
        Selenide.refresh();
        $(Selectors.byText("Noname")).shouldBe(visible);

        int userId = AdminSteps.getUserId(user);

    }

    @Test
    public void UserCanNotChangeNameToEmptyTest() {
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
        $(Selectors.byText("Noname")).click();

        $(Selectors.byText("✏️ Edit Profile"))
                .shouldBe(visible);

        $(Selectors.byAttribute("placeholder", "Enter new name")).sendKeys("");
        $(Selectors.byText("💾 Save Changes")).click();

        Alert alert = switchTo().alert();
        assertThat(alert.getText().contains("❌ Please enter a valid name")).isTrue();
        alert.accept();
        //check name didn't change on the API side
        String nameAfterChange = new ValidatedCrudRequester<CreateUserResponse>(
                RequestSpecs.authAsUserSpec(user.getUsername(),user.getPassword()),
                Endpoint.CUSTOMER_PROFILE_GET,
                ResponseSpecs.requestReturnsOk())
                .getAll().getName();

        assertThat(nameAfterChange).isNull();

        //reload a page because name updates only after page reloading
        Selenide.refresh();
        $(Selectors.byText("Noname")).shouldBe(visible);

        int userId = AdminSteps.getUserId(user);
    }
}