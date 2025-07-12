package models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor //generate all these staff int bite code
@NoArgsConstructor
@Builder
public class CheckUsersAccounts extends  BaseModel{
    List <CreateAccountResponse> accounts;
}
