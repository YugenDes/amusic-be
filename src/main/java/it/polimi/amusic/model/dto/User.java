package it.polimi.amusic.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class User {

    private String id;
    private String email;
    private String photoUrl;
    private String phoneNumber;
    private String displayName;
    private String name;
    private String surname;
    private LocalDateTime lastLogin;
    private LocalDateTime createDate;
    private boolean isEmailVerified;
    private boolean accountNonExpired = true;
    private boolean accountNonLocked = true;
    private boolean credentialsNonExpired = true;
    private boolean enabled = true;

}
