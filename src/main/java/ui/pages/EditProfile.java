package ui.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

import static com.codeborne.selenide.Selenide.$;

@Getter
public class EditProfile extends BasePage<EditProfile>{
    private SelenideElement editProfileHeader = $(Selectors.byText("✏️ Edit Profile"));
    private SelenideElement nameInput = $(Selectors.byAttribute("placeholder", "Enter new name"));
    private SelenideElement saveChangesButton = $(Selectors.byText("💾 Save Changes"));

    @Override
    public String url() {
        return "/edit-profile";
    }

    public EditProfile changeName(String name){
        nameInput.sendKeys(name);
        saveChangesButton.click();
        return  this;
    }

    public EditProfile refreshPage(){
        Selenide.refresh();
        return this;
    }

}
