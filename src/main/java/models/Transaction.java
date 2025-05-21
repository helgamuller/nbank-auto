package models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;

@Data
@AllArgsConstructor //generate all these staff int bite code
@NoArgsConstructor
@Builder
public class Transaction extends BaseModel{
    private int id;
    private float amount;
    private TransactionType type;
    private String timestamp;
    private int relatedAccountId;

}
