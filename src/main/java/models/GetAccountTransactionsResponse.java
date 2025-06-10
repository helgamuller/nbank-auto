package models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;
@Data
@AllArgsConstructor //generate all thes staff int bite code
@NoArgsConstructor
@Builder
public class GetAccountTransactionsResponse extends BaseModel{
    List<Transaction> transactions;

}
