package models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class MakeTransferRequest extends BaseModel{
    private int senderAccountId;
    private int receiverAccountId;
    private float amount;

}
