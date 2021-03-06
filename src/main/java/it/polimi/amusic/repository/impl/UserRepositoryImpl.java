package it.polimi.amusic.repository.impl;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.SetOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import it.polimi.amusic.exception.FirebaseException;
import it.polimi.amusic.exception.FirestoreException;
import it.polimi.amusic.model.document.UserDocument;
import it.polimi.amusic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Component(value = UserRepositoryImpl.NAME)
@RequiredArgsConstructor
@Slf4j
public class UserRepositoryImpl implements UserRepository {

    public static final String NAME = "UserService";
    private final Firestore firestore;
    private final FirebaseAuth firebaseAuth;

    static final String COLLECTION_NAME = "users";

    @Override
    public UserDetails loadUserByUsername(String email) {
        try {
            final UserRecord userByEmail = firebaseAuth.getUserByEmail(email);
            UserDocument userDocument = this.findByEmail(email).orElseThrow();
            if (userByEmail.isEmailVerified() && !userDocument.isEmailVerified()) {
                userDocument.setEmailVerified(true);
                userDocument = this.save(userDocument);
            }
            return userDocument;
        } catch (FirebaseAuthException e) {
            throw new FirebaseException("Errore durante il recuper del user {} : {}", email, e.getLocalizedMessage());
        }
    }


    @Override
    public Optional<UserDocument> findByEmail(String email) throws FirestoreException {
        try {
            return Optional.ofNullable(firestore.collection(COLLECTION_NAME).whereEqualTo("email", email).get().get())
                    .map(queryDocumentSnapshots -> queryDocumentSnapshots.toObjects(UserDocument.class))
                    .map(userDocuments -> !userDocuments.isEmpty() ? userDocuments.get(0) : null);
        } catch (ExecutionException | InterruptedException e) {
            throw new FirestoreException("Impossibile effettuare la query {}", e.getLocalizedMessage());
        }

    }

    @Override
    public Optional<UserDocument> findByFirebaseUid(String firebaseUid) throws com.google.cloud.firestore.FirestoreException {
        try {
            return Optional.ofNullable(firestore.collection(COLLECTION_NAME).whereEqualTo("firebaseUID", firebaseUid).get().get())
                    .map(queryDocumentSnapshots -> queryDocumentSnapshots.toObjects(UserDocument.class))
                    .map(userDocuments -> !userDocuments.isEmpty() ? userDocuments.get(0) : null);
        } catch (ExecutionException | InterruptedException e) {
            throw new FirestoreException("Impossibile effettuare la query {}", e.getLocalizedMessage());
        }
    }

    @Override
    public Optional<UserDocument> findById(String id) throws FirestoreException {
        try {
            return Optional.ofNullable(firestore.collection(COLLECTION_NAME)
                            .document(id)
                            .get().get())
                    .map(documentSnapshot -> documentSnapshot.toObject(UserDocument.class));
        } catch (ExecutionException | InterruptedException e) {
            throw new FirestoreException("Impossibile effettuare la query {}", e.getLocalizedMessage());
        }
    }

    @Override
    public List<UserDocument> findByDisplayNameStartWith(String displayName) throws com.google.cloud.firestore.FirestoreException {
        if (StringUtils.isBlank(displayName)) {
            throw new FirestoreException("Impossibile effettuare la query poich?? il campo ?? vuto");
        }

        try {
            return new ArrayList<>(Optional.ofNullable(firestore.collection(COLLECTION_NAME)
                            .whereGreaterThanOrEqualTo("displayName", displayName.toUpperCase())
                            .whereLessThanOrEqualTo("displayName", displayName.toUpperCase() + "\uf8ff")
                            .get()
                            .get())
                    .map(queryDocumentSnapshots -> queryDocumentSnapshots.toObjects(UserDocument.class))
                    .orElseThrow());
        } catch (InterruptedException | ExecutionException e) {
            throw new FirestoreException("Impossibile effettuare la query {}", e.getLocalizedMessage());
        }
    }

    @Override
    public List<UserDocument> findByNameStartWith(String name) throws com.google.cloud.firestore.FirestoreException {
        if (StringUtils.isBlank(name)) {
            throw new FirestoreException("Impossibile effettuare la query poich?? il campo ?? vuto");
        }

        try {
            return new ArrayList<>(Optional.ofNullable(firestore.collection(COLLECTION_NAME)
                            .whereGreaterThanOrEqualTo("name", name.toUpperCase())
                            .whereLessThanOrEqualTo("name", name.toUpperCase() + "\uf8ff")
                            .get()
                            .get())
                    .map(queryDocumentSnapshots -> queryDocumentSnapshots.toObjects(UserDocument.class))
                    .orElseThrow());
        } catch (InterruptedException | ExecutionException e) {
            throw new FirestoreException("Impossibile effettuare la query {}", e.getLocalizedMessage());
        }
    }

    public List<UserDocument> findBySurnameStartWith(String surname) throws com.google.cloud.firestore.FirestoreException {
        if (StringUtils.isBlank(surname)) {
            throw new FirestoreException("Impossibile effettuare la query poich?? il campo ?? vuto");
        }

        try {
            return new ArrayList<>(Optional.ofNullable(firestore.collection(COLLECTION_NAME)
                            .whereGreaterThanOrEqualTo("surname", surname.toUpperCase())
                            .whereLessThanOrEqualTo("surname", surname.toUpperCase() + "\uf8ff")
                            .get()
                            .get())
                    .map(queryDocumentSnapshots -> queryDocumentSnapshots.toObjects(UserDocument.class))
                    .orElseThrow());
        } catch (InterruptedException | ExecutionException e) {
            throw new FirestoreException("Impossibile effettuare la query {}", e.getLocalizedMessage());
        }
    }

    @Override
    public UserDocument save(UserDocument userDocument) {
        try {
            final CollectionReference users = firestore.collection(COLLECTION_NAME);
            DocumentReference document;
            if (StringUtils.isNotBlank(userDocument.getId())) {
                document = users.document(userDocument.getId());
            } else {
                document = users.document();
            }
            document.set(userDocument, SetOptions.merge())
                    .get();
            return userDocument.setId(document.getId());
        } catch (InterruptedException | ExecutionException e) {
            throw new FirestoreException("Impossibile effettuare la query {}", e.getLocalizedMessage());
        }
    }

    @Override
    public UserDocument updateFromFirebase(UserDocument userDocument, FirebaseToken firebaseToken) {

        final UserRecord userFireBase;
        try {
            userFireBase = firebaseAuth.getUser(firebaseToken.getUid());
        } catch (FirebaseAuthException e) {
            log.error("Errore durante il recupero dell userFireBase {}", e.getLocalizedMessage());
            throw new FirebaseException("Errore durante il recupero dell userFireBase  {}", e.getLocalizedMessage());
        }

        userDocument.setEmailVerified(userFireBase.isEmailVerified());
        userDocument.setLastLogin(Timestamp.ofTimeSecondsAndNanos(userFireBase.getUserMetadata().getLastRefreshTimestamp() / 1000L, 0));
        userDocument.setEnabled(!userFireBase.isDisabled());

        return save(userDocument);
    }

}
