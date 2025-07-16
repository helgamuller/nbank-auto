package iteration1.ui;

import api.generators.RandomModelGenerator;
import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import api.models.comparison.ModelAssertions;
import api.requests.steps.AdminSteps;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selectors;
import org.junit.jupiter.api.Test;
import ui.pages.AdminPanel;
import ui.pages.BankAlert;

import static com.codeborne.selenide.Selenide.$;
import static org.assertj.core.api.Assertions.assertThat;


public class CreateUserTest extends BaseUiTest{
    @Test
    public void adminCanCreateUserTest() throws NoSuchFieldException, IllegalAccessException {
        //create new user with role=admin
        //authentificate as admin
        CreateUserRequest admin = CreateUserRequest.getAdmin();
        authAsUser(admin);

        //admin creates user
        CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class); //generate data

        new AdminPanel().open().createUser(newUser.getUsername(), newUser.getPassword())
                .checkAlertMessageAndAccept(BankAlert.USER_CREATED_SUCCESSFULLY)
                .getAllUsers().findBy(Condition.exactText(newUser.getUsername() + "\nUSER")).shouldBe(Condition.visible);

        //check that user is created in API
        CreateUserResponse createdUser = AdminSteps.getAllusers().stream()
                .filter(user->user.getUsername().equals(newUser.getUsername()))
                .findFirst()
                .get();

        ModelAssertions.assertThatModels(newUser, createdUser).match();
    }

    @Test

    public void adminCanNotCreateUserWithInvaidDataTest(){
        CreateUserRequest admin = CreateUserRequest.getAdmin();
        authAsUser(admin);

        //admin creates user
        CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class);
        newUser.setUsername("a");

        new AdminPanel().open().createUser(newUser.getUsername(), newUser.getPassword())
                .checkAlertMessageAndAccept(BankAlert.USERNAME_MUST_BE_BETWEEN_3_AND_15_CHARS)
                .getAllUsers().findBy(Condition.exactText(newUser.getUsername() + "\nUSER")).shouldNotBe(Condition.exist);

        //check that user is NOT represented on UI
        ElementsCollection allUsersFromDashboard = $(Selectors.byText("All Users")).parent()
                .findAll("li");
        allUsersFromDashboard.findBy(Condition.exactText(newUser.getUsername() + "\nUSER")).shouldNotBe(Condition.exist);

        //check that user is NOT created in API
        long usersWithTheSameUsernameAsNewUser = AdminSteps.getAllusers().stream()
                .filter(user->user.getUsername().equals(newUser.getUsername()))
                .count();
        assertThat(usersWithTheSameUsernameAsNewUser).isZero();
    }
}
