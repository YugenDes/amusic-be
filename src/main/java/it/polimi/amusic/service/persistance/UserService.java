package it.polimi.amusic.service.persistance;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.FirestoreException;
import it.polimi.amusic.model.document.UserDocument;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Optional;

public interface UserService extends UserDetailsService {

    Optional<UserDocument> findByEmail(String email) throws FirestoreException;

    Optional<UserDocument> findReferenceByEmail(String email) throws FirestoreException;

    Optional<UserDocument> findByReference(DocumentReference documentReference) throws FirestoreException;

    Optional<UserDocument> findById(String id) throws FirestoreException;

    UserDocument save(UserDocument userDocument);

}
