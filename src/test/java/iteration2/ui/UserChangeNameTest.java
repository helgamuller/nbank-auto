package iteration2.ui;

import api.generators.RandomModelGenerator;
import api.models.ChangeNameRequest;
import api.models.CreateUserRequest;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import iteration1.ui.BaseUiTest;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.EditProfile;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserChangeNameTest extends BaseUiTest {

    @Test
    public void UserCanChangeNameTest() {
        CreateUserRequest user = AdminSteps.createUser();
        authAsUser(user);

        String newName = RandomModelGenerator.generate(ChangeNameRequest.class).getName();

        new EditProfile().open().changeName(newName).checkAlertMessageAndAccept(BankAlert.NAME_UPDATED_SUCCESSFULLY)
                .refreshPage().getName().shouldBe(visible).shouldHave(text(newName));

        //check name change on the API side
        String nameAfterChange = UserSteps.getNewName(user);
        assertEquals(nameAfterChange, newName);

        int userId = AdminSteps.getUserId(user);
    }

    @Test
    public void UserCanNotChangeNameToIncorrectTest() {
        CreateUserRequest user = AdminSteps.createUser();
        authAsUser(user);

        String newName = RandomModelGenerator.generate(CreateUserRequest.class).getUsername();

        new EditProfile().open().changeName(newName).checkAlertMessageAndAccept(BankAlert.NAME_MUST_CONTAIN_TWO_WORDS)
                .refreshPage().getName().shouldBe(visible).shouldHave(text("Noname"));

        String nameAfterChange = UserSteps.getNewName(user);
        assertThat(nameAfterChange).isNull();

        int userId = AdminSteps.getUserId(user);
    }

    @Test
    public void UserCanNotChangeNameToEmptyTest() {
        CreateUserRequest user = AdminSteps.createUser();
        authAsUser(user);

        new EditProfile().open().changeName("").checkAlertMessageAndAccept(BankAlert.ENTER_VALID_NAME)
                        .refreshPage().getName().shouldBe(visible).shouldHave(text("Noname"));

        //check name didn't change on the API side
        String nameAfterChange = UserSteps.getNewName(user);

        assertThat(nameAfterChange).isNull();

        int userId = AdminSteps.getUserId(user);
    }
}