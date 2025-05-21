package models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor //generate all these staff int bite code
@NoArgsConstructor
@Builder
public class MakeDepositRequest extends BaseModel{
    private int id;
    private float balance;
}
