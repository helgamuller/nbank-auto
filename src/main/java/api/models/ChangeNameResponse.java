package api.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor //generate all these staff int bite code
@NoArgsConstructor
@Builder
public class ChangeNameResponse extends BaseModel{
    String message;
    CreateUserResponse customer;
}
