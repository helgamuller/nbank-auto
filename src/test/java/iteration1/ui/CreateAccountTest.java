package iteration1.ui;

import api.models.CreateAccountResponse;
import api.models.CreateUserRequest;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.UserDashboard;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateAccountTest extends BaseUiTest{

    @Test
   public void userCanCreateAccountTest(){
        //preconditions
        //STEP 1: admin logins
        //STEP 2: admin creates user
        //STEP 3: user logins
       CreateUserRequest user = AdminSteps.createUser();
       authAsUser(user);

       new UserDashboard().open().createNewAccount();

        CreateAccountResponse createdAccount = new UserSteps(user.getUsername(), user.getPassword())
                .getAllAccounts().getFirst();

      new UserDashboard().checkAlertMessageAndAccept(BankAlert.NEW_ACCOUNT_CREATED.getMessage()+createdAccount.getAccountNumber());

       assertThat(createdAccount).isNotNull();
       assertThat(createdAccount.getBalance()).isZero();
   }

   }


