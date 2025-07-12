package ui.pages;

import lombok.Getter;

@Getter
public enum BankAlert {
    USER_CREATED_SUCCESSFULLY("✅ User created successfully!"),
    USERNAME_MUST_BE_BETWEEN_3_AND_15_CHARS("Username must be between 3 and 15 characters"),
    NEW_ACCOUNT_CREATED("✅ New Account Created! Account Number: "),
    NAME_UPDATED_SUCCESSFULLY("✅ Name updated successfully!"),
    NAME_MUST_CONTAIN_TWO_WORDS("Name must contain two words with letters only"),
    ENTER_VALID_NAME("❌ Please enter a valid name"),
    SUCCESSFULLY_DEPOSIT_AMOUNT("✅ Successfully deposited $%s to account %s!"),
    ENTER_VALID_AMOUNT("Please enter a valid amount."),
    DEPOSIT_LESS_OR_EQUAL_5000("Please deposit less or equal to 5000$."),
    SELECT_AN_ACCOUNT("❌ Please select an account."),
    SUCCESSFULLY_TRANSFERRED_AMOUNT("✅ Successfully transferred $%s to account %s!"),
    FILL_ALL_FIELDS_AND_CONFIRM ("Please fill all fields and confirm"),
    RECIPIENT_NAME_DOES_NOT_MATCH_REGISTERED_NAME ("❌ The recipient name does not match the registered name."),
    NO_USER_FOUND_WITH_THIS_NUMBER ("❌ No user found with this account number."),
    INSUFFICIENT_FUNDS_OR_INVALID_ACCOUNT("❌ Error: Invalid transfer: insufficient funds or invalid accounts"),
    TRANSFER_AMOUNT_CANNOT_EXCEED_10000("❌ Error: Transfer amount cannot exceed 10000")
    ;


    private final String message;
    BankAlert(String message) {
        this.message = message;
    }
}
