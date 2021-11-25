package it.polimi.amusic.service.persistance.impl;

import com.google.cloud.firestore.Firestore;
import it.polimi.amusic.exception.FirestoreException;
import it.polimi.amusic.model.document.RoleDocument;
import it.polimi.amusic.service.persistance.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleServiceImpl implements RoleService {

    private final Firestore firestore;

    @Override
    public RoleDocument findByAuthority(RoleDocument.RoleEnum roleEnum) throws FirestoreException {
        try {
            return firestore.collection("roles").document(roleEnum.name()).get().get().toObject(RoleDocument.class);
        } catch (InterruptedException | ExecutionException e) {
            throw new FirestoreException("Impossibile effettuare la query {}", e.getLocalizedMessage());
        }
    }
}
