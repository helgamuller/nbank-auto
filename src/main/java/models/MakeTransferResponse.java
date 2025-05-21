package models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class MakeTransferResponse extends BaseModel{
    private int receiverAccountId;
    private float amount;
    private String message;
    private int senderAccountId;

}
