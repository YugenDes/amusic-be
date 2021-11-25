package it.polimi.amusic.service.persistance.impl;

import com.google.cloud.firestore.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import it.polimi.amusic.exception.FirebaseException;
import it.polimi.amusic.exception.FirestoreException;
import it.polimi.amusic.model.document.UserDocument;
import it.polimi.amusic.service.persistance.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Service(value = UserServiceImpl.NAME)
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    public static final String NAME = "UserService";
    private final Firestore firestore;
    private final FirebaseAuth firebaseAuth;

    @Override
    public UserDetails loadUserByUsername(String email) {
        try {
            final UserRecord userByEmail = firebaseAuth.getUserByEmail(email);
            UserDocument userDocument = this.findByEmail(email).orElseThrow();
            if (userByEmail.isEmailVerified() && !userDocument.isEmailVerified()) {
                userDocument.setEmailVerified(true);
                userDocument =  this.save(userDocument);
            }
            return userDocument;
        } catch (FirebaseAuthException e) {
            throw new FirebaseException("Errore durante il recuper del user {} : {}", email, e.getLocalizedMessage());
        }
    }


    @Override
    public Optional<UserDocument> findByEmail(String email) throws FirestoreException {
        try {
            return Optional.ofNullable(firestore.collection("users").whereEqualTo("email", email).get().get())
                    .map(queryDocumentSnapshots -> queryDocumentSnapshots.toObjects(UserDocument.class))
                    .map(userDocuments -> userDocuments.size() > 0 ? userDocuments.get(0) : null);
        } catch (ExecutionException | InterruptedException e) {
            throw new FirestoreException("Impossibile effettuare la query {}", e.getLocalizedMessage());
        }

    }

    @Override
    public Optional<UserDocument> findReferenceByEmail(String email) throws FirestoreException {
        try {
            return Optional.ofNullable(firestore.collection("users")
                            .whereEqualTo("email", email).get().get())
                    .flatMap(queryDocumentSnapshots -> queryDocumentSnapshots.toObjects(UserDocument.class)
                            .stream()
                            .findFirst());
        } catch (ExecutionException | InterruptedException e) {
            throw new FirestoreException("Impossibile effettuare la query {}", e.getLocalizedMessage());
        }
    }

    @Override
    public Optional<UserDocument> findByReference(DocumentReference documentReference) throws FirestoreException {
        try {
            return Optional.ofNullable(firestore.collection("users")
                            .document(documentReference.getId())
                            .get().get())
                    .map(documentSnapshot -> documentSnapshot.toObject(UserDocument.class));
        } catch (ExecutionException | InterruptedException e) {
            throw new FirestoreException("Impossibile effettuare la query {}", e.getLocalizedMessage());
        }
    }

    @Override
    public Optional<UserDocument> findById(String id) throws FirestoreException {
        try {
            return Optional.ofNullable(firestore.collection("users")
                            .document(id)
                            .get().get())
                    .map(documentSnapshot -> documentSnapshot.toObject(UserDocument.class));
        } catch (ExecutionException | InterruptedException e) {
            throw new FirestoreException("Impossibile effettuare la query {}", e.getLocalizedMessage());
        }
    }

    @Override
    public UserDocument save(UserDocument userDocument) {
        try {
            final CollectionReference users = firestore.collection("users");
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

}
