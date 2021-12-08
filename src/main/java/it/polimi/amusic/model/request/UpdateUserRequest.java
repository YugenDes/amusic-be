package it.polimi.amusic.model.request;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@Data
@Accessors(chain = true)
public class UpdateUserRequest {
    private LocalDate birthDay;
    private String city;
    private String sex;
    private String name;
    private String surname;
}
