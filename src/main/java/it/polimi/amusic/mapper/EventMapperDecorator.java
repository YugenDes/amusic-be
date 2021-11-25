package it.polimi.amusic.mapper;

import com.firebase.geofire.core.GeoHash;
import com.google.cloud.firestore.GeoPoint;
import it.polimi.amusic.model.document.EventDocument;
import it.polimi.amusic.model.dto.Event;
import it.polimi.amusic.model.request.NewEventRequest;
import it.polimi.amusic.model.request.UpdateEventRequest;
import it.polimi.amusic.service.persistance.UserService;
import it.polimi.amusic.utils.TimestampUtils;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class EventMapperDecorator implements EventMapper {

    @Autowired
    private UserService userService;
    @Autowired
    private EventMapper eventMapper;

    public EventMapperDecorator() {
    }

    public EventMapperDecorator(EventMapper eventMapper) {
        this.eventMapper = eventMapper;
    }

    public EventMapperDecorator(UserService userService, EventMapper eventMapper) {
        this.userService = userService;
        this.eventMapper = eventMapper;
    }

    /**
     * Questo metodo mappa un EventDocument nel suo relativo DTO
     * Oscurando gli utenti registrati che non vogliono essere visibli al FE
     *
     * @param document
     * @return Event
     */
    @Override
    public Event getDtoFromDocument(EventDocument document) {
        final Event dtoFromDocument = eventMapper.getDtoFromDocument(document);
        //Costruisco la mappa Nome - Foto dei partecipanti visibili all evento
        final Map<String, String> visibleUsers = document.getPartecipants()
                .entrySet()
                .stream()
                //Filtro per quegli utenti che hanno il visible a true
                .filter(Map.Entry::getValue)
                .map(userDocumentVisibleEntry ->
                        //Recupero l user dal db e costruisco una mappa Nome - Foto
                        userService.findById(userDocumentVisibleEntry.getKey())
                                .map(userDocument -> Map.entry(userDocument.getDisplayName(), userDocument.getPhotoUrl()))
                                .orElse(null)
                )
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return dtoFromDocument.setPartecipants(visibleUsers);
    }

    @Override
    public EventDocument getDocumentFromRequest(NewEventRequest request) {
        EventDocument event = eventMapper.getDocumentFromRequest(request);
        event.setEventDatePublished(LocalDateTime.now())
                .setGeoPoint(new GeoPoint(request.getLat(), request.getLon()))
                .setGeoHash(new GeoHash(request.getLat(), request.getLon()).getGeoHashString());
        return event;
    }

    @Override
    public EventDocument updateEventDocumentFromRequest(EventDocument document, UpdateEventRequest request) {
        EventDocument eventDocument = eventMapper.updateEventDocumentFromRequest(document, request);
        eventDocument
                .setGeoPoint(new GeoPoint(request.getLat(), request.getLon()))
                .setGeoHash(new GeoHash(request.getLat(), request.getLon()).getGeoHashString());
        return null;
    }
}
