package iteration1.ui;

import api.models.CreateUserRequest;
import api.specs.RequestSpecs;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import iteration1.api.BaseTest;
import org.junit.jupiter.api.BeforeAll;

import static com.codeborne.selenide.Selenide.executeJavaScript;

public class BaseUiTest extends BaseTest {
    @BeforeAll
    public static void setupLocal(){
        Configuration.baseUrl = api.configs.Config.getProperty("uiBaseUrl");
        Configuration.browser = api.configs.Config.getProperty("browser");
        Configuration.browserSize = api.configs.Config.getProperty("browserSize");
//        Configuration.browserCapabilities.setCapability("selenoid:options",
//                Map.of("enableVNC", true, "enableLog", true))
//                ;
    }

    public void authAsUser(String username, String password){
        Selenide.open("/");
        String userAuthHeader = RequestSpecs.getUserAuthHeader(username, password);
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);
    }
    public void authAsUser(CreateUserRequest createUserRequest){
        authAsUser(createUserRequest.getUsername(),createUserRequest.getPassword());
    }
}
