package iteration2.ui;


import api.generators.RandomData;
import api.models.CreateUserRequest;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import iteration1.ui.BaseUiTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ui.pages.BankAlert;
import ui.pages.Deposit;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static com.codeborne.selenide.Condition.text;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DepositMoneyTest extends BaseUiTest {

    @Test
    public void userCanDepositValidAmountTest(){
        CreateUserRequest user = AdminSteps.createUser();
        authAsUser(user);

        String accNumber = UserSteps.createAccountAndGetNumber(user);
        String amount = RandomData.randomTransfer(new BigDecimal(1.00) , new BigDecimal(5000.00)).toString();
        String message  = String.format(
                //"✅ Successfully deposited $%s to account %s!",
                BankAlert.SUCCESSFULLY_DEPOSIT_AMOUNT.getMessage(),
                amount,
                accNumber
        );

        new Deposit().open().makeDeposit(accNumber, amount).checkAlertMessageAndAccept(message).open()
                        .selectAnOption(accNumber)
                        .getAccountFromDropdown()
                        .shouldHave(text("Balance: $" + amount));

        //check that acc balance changed correctly on API side
        String accountBalance = UserSteps.getAccountBalanceByAccNumber(user, accNumber).toString();
        assertEquals(amount, accountBalance);

        int userId = AdminSteps.getUserId(user);
    }

  public static Stream<Arguments> invalidDepositData(){
     return Stream.of(
             Arguments.of("", BankAlert.ENTER_VALID_AMOUNT),
             Arguments.of("-1", BankAlert.ENTER_VALID_AMOUNT),
             Arguments.of("50001", BankAlert.DEPOSIT_LESS_OR_EQUAL_5000)
     );
  }

    @ParameterizedTest
    @MethodSource("invalidDepositData")
    public void userCanNotDepositInvalidAmountTest(String amount, BankAlert bankAlert){
        CreateUserRequest user = AdminSteps.createUser();
        authAsUser(user);

        String accNumber = UserSteps.createAccountAndGetNumber(user);

        new Deposit().open().makeDeposit(accNumber, amount).checkAlertMessageAndAccept(bankAlert)
                .open()
                        .selectAnOption(accNumber)
                                .getAccountFromDropdown()
                                        .shouldHave(text("Balance: $0.00"));

//

        //check that acc balance changed correctly on API side
        String accountBalance =  UserSteps.getAccountBalanceByAccNumber(user, accNumber).toString();
        assertEquals("0.00", accountBalance);

        int userId = AdminSteps.getUserId(user);
    }
    @Test
    public void userCanNotDepositWithoutChoosingAccountTest(){
        CreateUserRequest user = AdminSteps.createUser();

        authAsUser(user);

        String accNumber = UserSteps.createAccountAndGetNumber(user);
        String amount = RandomData.randomTransfer(new BigDecimal(1.00) , new BigDecimal(5000.00)).toString();

        new Deposit().open().makeDeposit("-- Choose an account --", amount).checkAlertMessageAndAccept(BankAlert.SELECT_AN_ACCOUNT)
                      .open()
                         .selectAnOption(accNumber)
           .getAccountFromDropdown()
           .shouldHave(text("Balance: $0.00"));


        //check that acc balance  didn't change on API side as well
        String accountBalance =  UserSteps.getAccountBalanceByAccNumber(user, accNumber).toString();
        assertEquals("0.00", accountBalance);

        int userId = AdminSteps.getUserId(user);
    }
}
