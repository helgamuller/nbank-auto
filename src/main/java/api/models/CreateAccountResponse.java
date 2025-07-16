package api.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
@Data
@AllArgsConstructor //generate all these staff int bite code
@NoArgsConstructor
@Builder
public class CreateAccountResponse extends BaseModel{
    private int id;
    private String accountNumber;
    private BigDecimal balance;
    private List<Transaction> transactions;

}
