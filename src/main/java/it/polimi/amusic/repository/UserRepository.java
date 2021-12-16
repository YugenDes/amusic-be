package it.polimi.amusic.repository;

import com.google.cloud.firestore.FirestoreException;
import com.google.firebase.auth.FirebaseToken;
import it.polimi.amusic.model.document.UserDocument;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends UserDetailsService {

    Optional<UserDocument> findByEmail(String email) throws FirestoreException;

    Optional<UserDocument> findByFirebaseUid(String firebaseUid) throws FirestoreException;

    Optional<UserDocument> findById(String id) throws FirestoreException;

    List<UserDocument> findByDisplayNameStartWith(String param) throws FirestoreException;

    List<UserDocument> findByNameStartWith(String param) throws FirestoreException;

    List<UserDocument> findBySurnameStartWith(String param) throws FirestoreException;

    UserDocument save(UserDocument userDocument);

    UserDocument updateFromFirebase(UserDocument userDocument, FirebaseToken firebaseToken);

}
