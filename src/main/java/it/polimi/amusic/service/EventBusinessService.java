package it.polimi.amusic.service;

import com.google.cloud.firestore.GeoPoint;
import it.polimi.amusic.model.document.EventDocument;
import it.polimi.amusic.model.dto.Event;
import it.polimi.amusic.model.request.NewEventRequest;
import it.polimi.amusic.model.request.UpdateEventRequest;
import org.springframework.core.io.Resource;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EventBusinessService {

    EventDocument newEvent(NewEventRequest request);

    EventDocument updateEvent(UpdateEventRequest request);

    EventDocument changeImageLink(String eventIdDocument, Resource resource);

    Optional<Event> findEventById(String id);

    Optional<Event> findEventByIdAfterLocalDateNow(String id);

    List<Event> findAll();

    List<Event> findByEventDate(LocalDate localDate);

    List<Event> findByEventDateBetween(LocalDate localDateStart, LocalDate localDateEnd);

    List<Event> findByGeoPointNearMe(GeoPoint geoPoint, double distance);

    List<Event> getUserEventHistory();


}
