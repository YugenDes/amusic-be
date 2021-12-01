package it.polimi.amusic.service.persistance;

import com.google.cloud.firestore.FirestoreException;
import it.polimi.amusic.model.document.UserDocument;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Optional;

public interface UserService extends UserDetailsService {

    Optional<UserDocument> findByEmail(String email) throws FirestoreException;

    Optional<UserDocument> findById(String id) throws FirestoreException;

    List<UserDocument> findByEmailStartWith(String param) throws FirestoreException;

    List<UserDocument> findByDisplayNameStartWith(String param) throws FirestoreException;

    UserDocument save(UserDocument userDocument);

}
