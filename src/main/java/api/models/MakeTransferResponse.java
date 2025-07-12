package api.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class MakeTransferResponse extends BaseModel{
    private int receiverAccountId;
    private BigDecimal amount;
    private String message;
    private int senderAccountId;

}
