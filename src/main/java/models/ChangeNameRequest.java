package models;

import generators.GeneratingRule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor //generate all these staff int bite code
@NoArgsConstructor
@Builder
public class ChangeNameRequest extends BaseModel{
    @GeneratingRule(regex = "^[A-Za-z]{5}[ ]{1}[A-Za-z]$")
    private String name;
}
