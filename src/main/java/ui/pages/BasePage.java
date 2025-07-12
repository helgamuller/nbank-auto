package ui.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;
import org.openqa.selenium.Alert;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.switchTo;
import static org.assertj.core.api.Assertions.assertThat;

@Getter
public abstract class BasePage<T extends BasePage> {
    protected SelenideElement usernameInput = $(Selectors.byAttribute("placeholder", "Username"));
    protected SelenideElement passwordInput = $(Selectors.byAttribute("placeholder", "Password"));
    protected SelenideElement name = $(".user-name");
    protected SelenideElement accountSelector = $("select.account-selector");
    protected SelenideElement accountFromDropdown = $("select.account-selector option:checked");
    protected SelenideElement enterAmount = $(Selectors.byAttribute("placeholder", "Enter amount"));


    public abstract String url();

    public T open() {
        return Selenide.open(url(), (Class<T>) this.getClass());
    }

    public <T extends BasePage> T getPage(Class<T> pageClass) { return Selenide.page(pageClass); }

    public T checkAlertMessageAndAccept(String bankAlert) {
        Alert alert = switchTo().alert();
        assertThat(alert.getText()).contains(bankAlert);
        alert.accept();
        return (T) this;
    }
    public T checkAlertMessageAndAccept(BankAlert bankAlert){
        Alert alert = switchTo().alert();
        assertThat(alert.getText().contains(bankAlert.getMessage())).isTrue();
        alert.accept();
        return (T) this;
    }
    public T selectAnOption(String accNumber) {
        accountSelector.selectOptionContainingText(accNumber);
        return (T) this;
    }
}
