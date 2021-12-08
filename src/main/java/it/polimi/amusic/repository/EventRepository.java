package it.polimi.amusic.repository;

import com.google.cloud.firestore.GeoPoint;
import it.polimi.amusic.exception.FirestoreException;
import it.polimi.amusic.model.document.EventDocument;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EventRepository {

    EventDocument save(EventDocument eventDocument);

    Optional<EventDocument> findById(String id);

    List<EventDocument> findAll();

    List<EventDocument> findByEventName(String eventName);

    List<EventDocument> findByGeoPointNearMe(GeoPoint geoPoint, double distance);


    List<EventDocument> findByParticipant(String userIdDocument) throws FirestoreException;

    List<EventDocument> findByEventDate(LocalDate localDate);

    List<EventDocument> findByEventDateBetween(LocalDate localDateStart, LocalDate localDateEnd);

    Optional<EventDocument> findByIdAfterLocalDateNow(String id);

    void deleteEvent(String id);

}
