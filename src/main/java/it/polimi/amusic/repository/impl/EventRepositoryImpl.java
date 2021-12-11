package it.polimi.amusic.repository.impl;

import com.firebase.geofire.core.GeoHash;
import com.google.cloud.firestore.*;
import it.polimi.amusic.exception.FirestoreException;
import it.polimi.amusic.mapper.EventMapperDecorator;
import it.polimi.amusic.model.document.EventDocument;
import it.polimi.amusic.repository.EventRepository;
import it.polimi.amusic.utils.GeoUtils;
import it.polimi.amusic.utils.TimestampUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class EventRepositoryImpl implements EventRepository {

    private final Firestore firestore;
    private final EventMapperDecorator eventMapper;

    static final String EVENT_DATE = "eventDate";
    static final String EVENT_NAME = "eventName";
    static final String PARTECIPANTS = "partecipantsIds";
    static final String COLLECTION_NAME = "events";

    @Override
    public EventDocument save(EventDocument eventDocument) throws FirestoreException {
        try {
            final CollectionReference events = firestore.collection(COLLECTION_NAME);
            DocumentReference document;
            if (StringUtils.isNotBlank(eventDocument.getId())) {
                document = events.document(eventDocument.getId());
            } else {
                document = events.document();
            }
            document.set(eventDocument, SetOptions.merge())
                    .get();
            return eventDocument.setId(document.getId());
        } catch (InterruptedException | ExecutionException e) {
            throw new FirestoreException("Impossibile effettuare la query {}", e.getLocalizedMessage());
        }
    }

    @Override
    public Optional<EventDocument> findById(String id) throws FirestoreException {
        try {
            return Optional.ofNullable(firestore.collection(COLLECTION_NAME)
                            .document(id)
                            .get().get())
                    .map(documentSnapshot -> documentSnapshot.toObject(EventDocument.class));
        } catch (ExecutionException | InterruptedException e) {
            throw new FirestoreException("Impossibile effettuare la query {}", e.getLocalizedMessage());
        }
    }

    @Override
    public Optional<EventDocument> findByIdAfterLocalDateNow(String id) {
        try {
            return Optional.ofNullable(firestore.collection(COLLECTION_NAME)
                            .document(id)
                            .get().get())
                    .map(documentSnapshot -> documentSnapshot.toObject(EventDocument.class))
                    .filter(eventDocument -> eventDocument.getEventDate().compareTo(TimestampUtils.convertLocalDateToTimestamp(LocalDate.now())) > 0);
        } catch (ExecutionException | InterruptedException e) {
            throw new FirestoreException("Impossibile effettuare la query {}", e.getLocalizedMessage());
        }
    }

    @Override
    public List<EventDocument> findAll() {
        try {
            return Optional.ofNullable(firestore
                            .collection(COLLECTION_NAME)
                            .limit(20)
                            .get()
                            .get())
                    .map(queryDocumentSnapshots -> queryDocumentSnapshots.toObjects(EventDocument.class))
                    .orElseThrow();
        } catch (ExecutionException | InterruptedException e) {
            throw new FirestoreException("Impossibile effettuare la query {}", e.getLocalizedMessage());
        }
    }

    @Override
    public List<EventDocument> findByEventName(String eventName) throws FirestoreException {
        try {
            return Optional.ofNullable(firestore.collection(COLLECTION_NAME).whereArrayContains(EVENT_NAME, eventName)
                            .get()
                            .get())
                    .map(queryDocumentSnapshots -> queryDocumentSnapshots.toObjects(EventDocument.class))
                    .orElseThrow();
        } catch (InterruptedException | ExecutionException e) {
            throw new FirestoreException("Impossibile effettuare la query {}", e.getLocalizedMessage());
        }
    }

    @Override
    public List<EventDocument> findByGeoPointNearMe(GeoPoint center, double distance) {

        //Creo il boundingBox
        final GeoPoint min = GeoUtils.boundingGeoPoints(center, distance).get(0);
        final GeoPoint max = GeoUtils.boundingGeoPoints(center, distance).get(1);

        //Hasho i geoPoint
        String minGeoHashString = new GeoHash(min.getLatitude(), min.getLongitude()).getGeoHashString();
        String maxGeoHashString = new GeoHash(max.getLatitude(), max.getLongitude()).getGeoHashString();

        try {
            return Optional.ofNullable(firestore.collection(COLLECTION_NAME)
                            .orderBy("geoHash")
                            .startAt(minGeoHashString)
                            .endAt(maxGeoHashString)
                            .get()
                            .get())
                    .map(queryDocumentSnapshots -> queryDocumentSnapshots.toObjects(EventDocument.class))
                    .orElseThrow()
                    .parallelStream()
                    //Tramite la funzione distanza filtro gli eventi effettivamente a distanza cercata
                    //Per evitare i falsi positivi
                    .filter(eventDocument ->
                            GeoUtils.distance(center, eventDocument.getGeoPoint()) <= distance)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new FirestoreException("Impossibile effettuare la query {}", e.getLocalizedMessage());
        }
    }


    @Override
    public List<EventDocument> findByParticipant(String userIdDocument) throws FirestoreException {
        try {
            return Optional.ofNullable(firestore.collection(COLLECTION_NAME)
                            .whereArrayContains(PARTECIPANTS, userIdDocument)
                            .orderBy("eventDate", Query.Direction.DESCENDING)
                            .limit(10)
                            .get().get())
                    .map(queryDocumentSnapshots -> queryDocumentSnapshots.toObjects(EventDocument.class))
                    .orElseThrow();
        } catch (InterruptedException | ExecutionException e) {
            throw new FirestoreException("Impossibile effettuare la query {}", e.getLocalizedMessage());
        }
    }

    @Override
    public List<EventDocument> findByEventDate(LocalDate localDate) throws FirestoreException {
        try {
            return Optional.ofNullable(firestore.collection(COLLECTION_NAME)
                            .whereGreaterThanOrEqualTo(EVENT_DATE, Objects.requireNonNull(TimestampUtils.convertLocalDateToTimestamp(localDate)))
                            .whereLessThanOrEqualTo(EVENT_DATE, Objects.requireNonNull(TimestampUtils.convertLocalDateToTimestamp(localDate.plusDays(1))))
                            .get()
                            .get())
                    .map(queryDocumentSnapshots -> queryDocumentSnapshots.toObjects(EventDocument.class))
                    .orElseThrow();
        } catch (InterruptedException | ExecutionException e) {
            throw new FirestoreException("Impossibile effettuare la query {}", e.getLocalizedMessage());
        }
    }

    @Override
    public List<EventDocument> findByEventDateBetween(LocalDate localDateStart, LocalDate localDateEnd) {
        try {
            return Optional.ofNullable(firestore.collection(COLLECTION_NAME)
                            .whereGreaterThanOrEqualTo(EVENT_DATE, Objects.requireNonNull(TimestampUtils.convertLocalDateToTimestamp(localDateStart)))
                            .whereLessThanOrEqualTo(EVENT_DATE, Objects.requireNonNull(TimestampUtils.convertLocalDateToTimestamp(localDateEnd.plusDays(1))))
                            .get()
                            .get())
                    .map(queryDocumentSnapshots -> queryDocumentSnapshots.toObjects(EventDocument.class))
                    .orElseThrow();
        } catch (InterruptedException | ExecutionException e) {
            throw new FirestoreException("Impossibile effettuare la query {}", e.getLocalizedMessage());
        }
    }

    @Override
    public void deleteEvent(String id) {
        try {
            firestore.collection(COLLECTION_NAME)
                    .document(id)
                    .delete().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new FirestoreException("Impossibile effettuare la query {}", e.getLocalizedMessage());
        }
    }

}
