package ui.pages;


import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;

@Getter
public class Deposit extends BasePage<Deposit> {
    private SelenideElement depositButton = $(byText("💵 Deposit"));

    @Override
    public String url() {
        return "/deposit";
    }
    //private SelenideElement accountsDropdown = $("select.account-selector");
    //private SelenideElement accountFromDropdown = $("select.account-selector option:checked");

    public Deposit makeDeposit(String accNumber, String amount) {
        accountSelector.click();
        accountSelector.selectOptionContainingText(accNumber);
        enterAmount.sendKeys(amount);
        depositButton.click();
        return this;
    }

//    public Deposit selectAnOption(String accNumber) {
//        accountSelector.selectOptionContainingText(accNumber);
//        return this;
//    }
}