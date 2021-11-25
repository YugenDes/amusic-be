package it.polimi.amusic.model.request;


import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class LoginRequest {
    @NonNull
    private String email;
    @NonNull
    private String password;
}
