package it.polimi.amusic.mapper;

import com.firebase.geofire.core.GeoHash;
import com.google.cloud.firestore.GeoPoint;
import it.polimi.amusic.model.document.EventDocument;
import it.polimi.amusic.model.document.PartecipantDocument;
import it.polimi.amusic.model.document.UserDocument;
import it.polimi.amusic.model.dto.Event;
import it.polimi.amusic.model.dto.Partecipant;
import it.polimi.amusic.model.request.NewEventRequest;
import it.polimi.amusic.model.request.UpdateEventRequest;
import it.polimi.amusic.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public abstract class EventMapperDecorator implements EventMapper {


    @Autowired
    private EventMapper eventMapper;

    @Autowired
    private UserRepository userRepository;


    public EventMapperDecorator() {
    }

    public EventMapperDecorator(EventMapper eventMapper) {
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
        Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .filter(o -> o instanceof UserDocument)
                .map(o -> (UserDocument) o)
                .ifPresent(userDocument -> dtoFromDocument.setBought(userDocument.getEventList().contains(document.getId())));
        final List<Partecipant> visibleUsers = document.getPartecipants()
                .stream()
                //Filtro per quegli utenti che hanno il visible a true
                .filter(PartecipantDocument::getVisible)
                .map(this::getPartecipantDtoFromDocument)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
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


    @Override
    public Partecipant getPartecipantDtoFromDocument(PartecipantDocument partecipantDocument) {
        final Partecipant partecipantDtoFromDocument = eventMapper.getPartecipantDtoFromDocument(partecipantDocument);
        userRepository.findById(partecipantDocument.getId())
                .ifPresent(userDocument1 -> partecipantDtoFromDocument.setPhotoUrl(userDocument1.getPhotoUrl()));
        return partecipantDtoFromDocument;
    }
}
