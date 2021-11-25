package it.polimi.amusic.model.request;

import it.polimi.amusic.security.model.AuthProvider;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class RegistrationRequest {
    @NonNull
    private String email;
    @NonNull
    private String name;
    @NonNull
    private String surname;
    @NonNull
    private String firebaseUidToken;
    @NonNull
    private AuthProvider provider;

    public RegistrationRequest() {
    }
}
