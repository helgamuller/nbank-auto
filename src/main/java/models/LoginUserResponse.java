package models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor //generate all thes staff int bite code
@NoArgsConstructor
@Builder
public class LoginUserResponse extends BaseModel {
    private String username;
    private String role;
}
