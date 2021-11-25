package it.polimi.amusic.service.persistance.impl;

import com.firebase.geofire.core.GeoHash;
import com.google.cloud.firestore.*;
import it.polimi.amusic.exception.FirestoreException;
import it.polimi.amusic.mapper.EventMapperDecorator;
import it.polimi.amusic.model.document.EventDocument;
import it.polimi.amusic.model.dto.Event;
import it.polimi.amusic.service.persistance.EventService;
import it.polimi.amusic.utils.GeoUtils;
import it.polimi.amusic.utils.TimestampUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final Firestore firestore;
    private final EventMapperDecorator eventMapperDecorator;

    @Override
    public EventDocument save(EventDocument eventDocument) throws FirestoreException {
        try {
            final CollectionReference events = firestore.collection("events");
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
            return Optional.ofNullable(firestore.collection("events")
                            .document(id)
                            .get().get())
                    .map(documentSnapshot -> documentSnapshot.toObject(EventDocument.class));
        } catch (ExecutionException | InterruptedException e) {
            throw new FirestoreException("Impossibile effettuare la query {}", e.getLocalizedMessage());
        }
    }

    @Override
    public List<Event> findAll() {
        try {
            return Optional.ofNullable(firestore
                            .collection("events")
                            .limit(50)
                            .get()
                            .get())
                    .map(queryDocumentSnapshots -> queryDocumentSnapshots.toObjects(EventDocument.class))
                    .orElseThrow()
                    .stream()
                    .map(eventMapperDecorator::getDtoFromDocument)
                    .collect(Collectors.toList());
        } catch (ExecutionException | InterruptedException e) {
            throw new FirestoreException("Impossibile effettuare la query {}", e.getLocalizedMessage());
        }
    }

    @Override
    public List<EventDocument> findByEventName(String eventName) throws FirestoreException {
        try {
            return Optional.ofNullable(firestore.collection("events").whereArrayContains("eventName", eventName)
                            .get()
                            .get())
                    .map(queryDocumentSnapshots -> queryDocumentSnapshots.toObjects(EventDocument.class))
                    .orElseThrow();
        } catch (InterruptedException | ExecutionException e) {
            throw new FirestoreException("Impossibile effettuare la query {}", e.getLocalizedMessage());
        }
    }

    @Override
    public List<Event> findByGeoPointNearMe(GeoPoint center, double distance) {

        final GeoPoint min = GeoUtils.boundingGeoPoints(center, distance).get(0);
        final GeoPoint max = GeoUtils.boundingGeoPoints(center, distance).get(1);

        String minGeoHashString = new GeoHash(min.getLatitude(), min.getLongitude()).getGeoHashString();
        String maxGeoHashString = new GeoHash(max.getLatitude(), max.getLongitude()).getGeoHashString();

        try {
            return Optional.ofNullable(firestore.collection("events")
                            .orderBy("geoHash")
                            .startAt(minGeoHashString)
                            .endAt(maxGeoHashString)
                            .get()
                            .get())
                    .map(queryDocumentSnapshots -> queryDocumentSnapshots.toObjects(EventDocument.class))
                    .orElseThrow()
                    .parallelStream()
                    .filter(eventDocument ->
                            GeoUtils.distance(center, eventDocument.getGeoPoint()) <= distance)
                    .map(eventMapperDecorator::getDtoFromDocument)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new FirestoreException("Impossibile effettuare la query {}", e.getLocalizedMessage());
        }
    }

    @Override
    public List<EventDocument> findByPartecipant(String userIdDocument) throws FirestoreException {
        try {
            return Optional.ofNullable(firestore.collection("events")
                            .whereArrayContains("partecipants", userIdDocument)
                            .get().get())
                    .map(queryDocumentSnapshots -> queryDocumentSnapshots.toObjects(EventDocument.class))
                    .orElseThrow();
        } catch (InterruptedException | ExecutionException e) {
            throw new FirestoreException("Impossibile effettuare la query {}", e.getLocalizedMessage());
        }
    }

    @Override
    public List<Event> findByEventDate(LocalDate localDate) throws FirestoreException {
        try {
            return Optional.ofNullable(firestore.collection("events")
                            .whereGreaterThanOrEqualTo("eventDate", TimestampUtils.convertLocalDateToTimestamp(localDate))
                            .whereLessThanOrEqualTo("eventDate", TimestampUtils.convertLocalDateToTimestamp(localDate.plusDays(1)))
                            .get()
                            .get())
                    .map(queryDocumentSnapshots -> queryDocumentSnapshots.toObjects(EventDocument.class))
                    .orElseThrow()
                    .stream()
                    .map(eventMapperDecorator::getDtoFromDocument)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new FirestoreException("Impossibile effettuare la query {}", e.getLocalizedMessage());
        }
    }

    @Override
    public List<Event> findByEventDateBetween(LocalDate localDateStart, LocalDate localDateEnd) {
        try {
            return Optional.ofNullable(firestore.collection("events")
                            .whereGreaterThanOrEqualTo("eventDate", TimestampUtils.convertLocalDateToTimestamp(localDateStart))
                            .whereLessThanOrEqualTo("eventDate", TimestampUtils.convertLocalDateToTimestamp(localDateEnd.plusDays(1)))
                            .get()
                            .get())
                    .map(queryDocumentSnapshots -> queryDocumentSnapshots.toObjects(EventDocument.class))
                    .orElseThrow()
                    .stream()
                    .map(eventMapperDecorator::getDtoFromDocument)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new FirestoreException("Impossibile effettuare la query {}", e.getLocalizedMessage());
        }
    }

    @Override
    public void deleteEvent(String id) {
        try {
            firestore.collection("events")
                    .document(id)
                    .delete().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new FirestoreException("Impossibile effettuare la query {}", e.getLocalizedMessage());
        }
    }

}
