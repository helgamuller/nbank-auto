package iteration1.ui;

import com.codeborne.selenide.*;
import generators.RandomModelGenerator;
import models.CreateUserRequest;
import models.CreateUserResponse;
import models.comparison.ModelAssertions;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import specs.RequestSpecs;

import java.util.Arrays;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.switchTo;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CreateUserTest {
    @BeforeAll
    public static void setupLocal(){
        Configuration.baseUrl = "http://192.168.1.117:3000";
        Configuration.browser = "chrome";
        Configuration.browserSize = "1920x1080";

    }
    @Test
    public void adminCanCreateUserTest() throws NoSuchFieldException, IllegalAccessException {
        //admin logs in
        CreateUserRequest admin = CreateUserRequest.builder()
                .username("admin")
                .password("admin")
                .build();

        Selenide.open("/login");

        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(admin.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(admin.getPassword());
        $("button").click();

        $(Selectors.byText("Admin Panel")).shouldBe(visible);

        //admin creates user
        CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class);
        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(newUser.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(newUser.getPassword());
        $(Selectors.byText("Add User")).click();

        //alert that User created successfully!
        Alert alert = switchTo().alert();
        assertEquals("✅ User created successfully!", alert.getText());

        alert.accept();

        //check that user is represented on UI

        ElementsCollection allUsersFromDashboard = $(Selectors.byText("All Users")).parent()
                .findAll("li");
        allUsersFromDashboard.findBy(Condition.exactText(newUser.getUsername() + "\nUSER")).shouldBe(Condition.visible);

        //check that user is created in API
        CreateUserResponse[] users = given()
                .spec(RequestSpecs.adminSpec())
                .get("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract().as(CreateUserResponse[].class);

        CreateUserResponse createdUser = Arrays.stream(users)
                .filter(user->user.getUsername().equals(newUser.getUsername()))
                .findFirst()
                .get();

        ModelAssertions.assertThatModels(newUser, createdUser).match();
    }

    @Test
    public void adminCanNotCreateUserWithInvaidDataTest(){
        CreateUserRequest admin = CreateUserRequest.builder()
                .username("admin")
                .password("admin")
                .build();

        Selenide.open("/login");

        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(admin.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(admin.getPassword());
        $("button").click();

        $(Selectors.byText("Admin Panel")).shouldBe(visible);

        //admin creates user
        CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class);
        newUser.setUsername("a");
        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(newUser.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(newUser.getPassword());
        $(Selectors.byText("Add User")).click();

        //alert that User created successfully!
        Alert alert = switchTo().alert();
        assertThat(alert.getText().contains("Username must be between 3 and 15 characters")).isTrue();

        alert.accept();

        //check that user is NOT represented on UI

        ElementsCollection allUsersFromDashboard = $(Selectors.byText("All Users")).parent()
                .findAll("li");
        allUsersFromDashboard.findBy(Condition.exactText(newUser.getUsername() + "\nUSER")).shouldNotBe(Condition.exist);

        //check that user is NOT created in API

        CreateUserResponse[] users = given()
                .spec(RequestSpecs.adminSpec())
                .get("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract().as(CreateUserResponse[].class);

        long usersWithTheSameUsernameAsNewUser = Arrays.stream(users)
                .filter(user->user.getUsername().equals(newUser.getUsername()))
                .count();
        assertThat(usersWithTheSameUsernameAsNewUser).isZero();
    }
}
