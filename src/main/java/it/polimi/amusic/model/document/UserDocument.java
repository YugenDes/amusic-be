package it.polimi.amusic.model.document;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.DocumentId;
import it.polimi.amusic.security.model.AuthProvider;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.cloud.gcp.data.firestore.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
@Accessors(chain = true)
@Document(collectionName = "users")
@EqualsAndHashCode
public class UserDocument implements UserDetails {

    @DocumentId
    private String id;
    private String firebaseUID;
    private String email;
    private AuthProvider provider;
    private List<RoleDocument> authorities;
    private String photoUrl;
    private String phoneNumber;
    private String name;
    private String surname;
    private String displayName;
    private Timestamp birthDay;
    private String city;
    private String sex;
    private List<FriendDocument> firendList = new ArrayList<>();
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

    public boolean addFriendIfAbsent(FriendDocument friendDocument) {
        if (!firendList.contains(friendDocument) && !friendDocument.getId().equals(this.id)) {
            return firendList.add(friendDocument);
        } else {
            return false;
        }
    }

    public boolean removeFriendIfPresent(String idFriendDocument) {
        if (firendList.stream().anyMatch(friendDocument -> friendDocument.getId().equals(idFriendDocument)) && !idFriendDocument.equals(this.id)) {
            return firendList.remove(firendList
                    .stream()
                    .filter(friendDocument -> friendDocument.getId().equals(idFriendDocument))
                    .findFirst()
                    .orElse(null));
        } else {
            return false;
        }
    }
}


