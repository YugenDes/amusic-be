package it.polimi.amusic.model.document;

import com.google.cloud.firestore.annotation.DocumentId;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.cloud.gcp.data.firestore.Document;
import org.springframework.security.core.GrantedAuthority;

@Data
@Document(collectionName = "roles")
@Accessors(chain = true)
public class RoleDocument implements GrantedAuthority {

    public enum RoleEnum{
        ADMIN,USER
    }

    @DocumentId
    private String id;
    private RoleEnum authority;
    @Override
    public String getAuthority() {
        return authority.name();
    }
}
