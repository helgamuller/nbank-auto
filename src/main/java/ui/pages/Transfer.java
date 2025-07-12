package ui.pages;


import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;

public class Transfer extends  BasePage<Transfer>{
private SelenideElement recipientName = $(Selectors.byAttribute("Placeholder", "Enter recipient name"));
private SelenideElement recipientAccount = $(Selectors.byAttribute("Placeholder", "Enter recipient account number"));
private SelenideElement confirmCheck = $(Selectors.byId("confirmCheck"));
private SelenideElement sendTransferButton  = $(Selectors.byText("🚀 Send Transfer"));


    public Transfer sendTransfer(String senderAccNumber, String recipName, String recipientAccountNumber,  String amount) {
        accountSelector.click();
        accountSelector.selectOptionContainingText(senderAccNumber);
        recipientName.sendKeys(recipName);
        recipientAccount.sendKeys(recipientAccountNumber);
        enterAmount.sendKeys(amount);
        confirmCheck.click();
        sendTransferButton.click();
        return this;
    }
    public Transfer sendTransferNoConfirmation(String senderAccNumber, String recipName, String recipientAccountNumber,  String amount) {
        accountSelector.click();
        accountSelector.selectOptionContainingText(senderAccNumber);
        recipientName.sendKeys(recipName);
        recipientAccount.sendKeys(recipientAccountNumber);
        enterAmount.sendKeys(amount);
        sendTransferButton.click();
        return this;
    }


    @Override
    public String url() {
        return "/transfer";
    }

}
