package it.polimi.amusic.repository.impl;

import com.google.cloud.firestore.Firestore;
import it.polimi.amusic.exception.FirestoreException;
import it.polimi.amusic.model.document.RoleDocument;
import it.polimi.amusic.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoleRepositoryImpl implements RoleRepository {

    private final Firestore firestore;

    static final String COLLECTION_NAME = "roles";

    @Override
    public RoleDocument findByAuthority(RoleDocument.RoleEnum roleEnum) throws FirestoreException {
        try {
            return firestore.collection(COLLECTION_NAME).document(roleEnum.name()).get().get().toObject(RoleDocument.class);
        } catch (InterruptedException | ExecutionException e) {
            throw new FirestoreException("Impossibile effettuare la query {}", e.getLocalizedMessage());
        }
    }
}
