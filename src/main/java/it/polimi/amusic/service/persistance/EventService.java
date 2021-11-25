package it.polimi.amusic.service.persistance;

import com.google.cloud.firestore.GeoPoint;
import it.polimi.amusic.exception.FirestoreException;
import it.polimi.amusic.model.document.EventDocument;
import it.polimi.amusic.model.dto.Event;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EventService {

    EventDocument save(EventDocument eventDocument);

    Optional<EventDocument> findById(String id);

    Optional<Event> findEventById(String id);

    List<Event> findAll();

    List<EventDocument> findByEventName(String eventName);

    List<Event> findByGeoPointNearMe(GeoPoint geoPoint , double distance);

    List<EventDocument> findByPartecipant(String userIdDocument) throws FirestoreException;

    List<Event> findByEventDate(LocalDate localDate);

    List<Event> findByEventDateBetween(LocalDate localDateStart, LocalDate localDateEnd);

    void deleteEvent(String id);

}
