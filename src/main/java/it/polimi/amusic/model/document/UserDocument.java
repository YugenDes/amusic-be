package it.polimi.amusic.model.document;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.DocumentId;
import it.polimi.amusic.security.model.AuthProvider;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.cloud.gcp.data.firestore.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Data
@Accessors(chain = true)
@Document(collectionName = "users")
public class UserDocument implements UserDetails {

    @DocumentId
    private String id;
    private String firebaseUID;
    private String email;
    private AuthProvider provider;
    private List<RoleDocument> authorities;
    private String photoUrl;
    private String phoneNumber;
    private String displayName;
    private List<String> firendList = new ArrayList<>();
    private List<String> eventList = new ArrayList<>();
    private Timestamp lastLogin;
    private Timestamp createDate;
    private boolean emailVerified;
    private boolean accountNonExpired = true;
    private boolean accountNonLocked = true;
    private boolean credentialsNonExpired = true;
    private boolean enabled = true;

    //Ignore
    //CustomClassMapper warn
    private String password;
    private String username;
    private boolean accountLocked;
    private boolean accountExpired;
    private boolean credentialsExpired;

    @Override

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public boolean addEventIfAbsent(String eventIdDocument) {
        if (!eventList.contains(eventIdDocument)) {
            return eventList.add(eventIdDocument);
        } else {
            return false;
        }
    }

    public boolean addFriendIfAbsent(String userIdDocument) {
        if (!firendList.contains(userIdDocument) && !userIdDocument.equals(this.id)) {
            return firendList.add(userIdDocument);
        } else {
            return false;
        }
    }


}
