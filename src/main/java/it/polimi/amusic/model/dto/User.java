package it.polimi.amusic.model.dto;

import com.google.cloud.Timestamp;
import it.polimi.amusic.model.document.RoleDocument;
import it.polimi.amusic.security.model.AuthProvider;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class User {

    private String id;
    private String email;
    private String photoUrl;
    private String phoneNumber;
    private String displayName;
    private LocalDateTime lastLogin;
    private LocalDateTime createDate;
    private boolean isEmailVerified;
    private boolean accountNonExpired= true;
    private boolean accountNonLocked= true;
    private boolean credentialsNonExpired = true;
    private boolean enabled= true;

}
