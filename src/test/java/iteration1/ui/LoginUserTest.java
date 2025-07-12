package iteration1.ui;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import models.CreateUserRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import requests.steps.AdminSteps;
import static com.codeborne.selenide.Selenide.$;

public class LoginUserTest {
//    @BeforeAll
//    public static void setupSelenoid(){
//        Configuration.remote = "http://localhost:4444/wd/hub";
//        Configuration.baseUrl = "http://192.168.1.117:3000";
//        Configuration.browser = "chrome";
//        Configuration.browserSize = "1920x1080";
//
//        Configuration.browserCapabilities.setCapability("selenoid:options",
//                Map.of("enableVNC", true, "enableLog", true)
//                );
//    }
@BeforeAll
    public static void setupLocal(){
        Configuration.baseUrl = "http://192.168.1.117:3000";
        Configuration.browser = "chrome";
        Configuration.browserSize = "1920x1080";

}
    @Test
    public void adminCanLoginWithCorrectDataTest(){
        CreateUserRequest admin = CreateUserRequest.builder()
                .username("admin")
                .password("admin")
                .build();

        Selenide.open("/login");

        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(admin.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(admin.getPassword());
        $("button").click();

        $(Selectors.byText("Admin Panel")).shouldBe(Condition.visible);

    }

    @Test
    public void userCanLoginWithCorrectDataTest() throws InterruptedException {
        CreateUserRequest user = AdminSteps.createUser();

        Selenide.open("/login");
        Thread.sleep(5000);
        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(user.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(user.getPassword());
        $("button").click();

        $(Selectors.byClassName("welcome-text")).shouldBe(Condition.visible).shouldHave(Condition.text("Welcome, noname!"));
        Thread.sleep(5000);

    }
}
