package it.polimi.amusic.service.impl;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.GeoPoint;
import it.polimi.amusic.exception.EventNotFoundException;
import it.polimi.amusic.exception.FirestoreException;
import it.polimi.amusic.exception.MissingAuthenticationException;
import it.polimi.amusic.exception.UserNotFoundException;
import it.polimi.amusic.external.gcs.FileService;
import it.polimi.amusic.mapper.EventMapperDecorator;
import it.polimi.amusic.model.document.EventDocument;
import it.polimi.amusic.model.document.UserDocument;
import it.polimi.amusic.model.dto.Event;
import it.polimi.amusic.model.request.NewEventRequest;
import it.polimi.amusic.model.request.UpdateEventRequest;
import it.polimi.amusic.repository.EventRepository;
import it.polimi.amusic.repository.UserRepository;
import it.polimi.amusic.service.EventBusinessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventBusinessServiceImpl implements EventBusinessService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final FileService fileService;
    private final EventMapperDecorator eventMapper;
    private final Firestore firestore;

    /**
     * Persiste un nuovo evento
     *
     * @param request NewEventRequest
     * @return EventDocument
     */
    @Override
    public EventDocument newEvent(NewEventRequest request) {
        return eventRepository.save(eventMapper.getDocumentFromRequest(request));
    }

    /**
     * Aggiorna l'evento data la corretta request
     *
     * @param request UpdateEventRequest
     * @return EventDocument
     */
    @Override
    public EventDocument updateEvent(UpdateEventRequest request) {
        try {
            return firestore.runTransaction(transaction ->
                    eventRepository.findById(request.getEventIdDocument())
                            .map(eventDocument ->
                                    eventRepository.save(eventMapper.updateEventDocumentFromRequest(eventDocument, request)))
                            .orElseThrow(() -> new EventNotFoundException("Evento {} non trovato", request.getEventIdDocument()))

            ).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new FirestoreException("Errore durante la transazione {}", e.getLocalizedMessage());
        }
    }

    /**
     * Modifica l'immagine di copertina dell evento
     *
     * @param eventIdDocument
     * @param resource        ByteArrayResoruce
     * @return EventDocument
     */
    @Override
    public EventDocument changeImageLink(String eventIdDocument, Resource resource) {
        return eventRepository.findById(eventIdDocument)
                .map(eventDocument -> {
                    //Carico la nuova immagine
                    final String fileLink = fileService.uploadFile(resource);
                    //Elimino quella vecchia
                    fileService.deleteFile(eventDocument.getImageUrl());
                    //Setto l'immagine nuova
                    return eventRepository.save(eventDocument.setImageUrl(fileLink));
                }).orElseThrow(() -> new EventNotFoundException("Evento {} non trovato", eventIdDocument));
    }

    /**
     * Query per ottenere il DTO dell evento tramite ID
     *
     * @param id
     * @return Optional<Event>
     */
    @Override
    public Optional<Event> findEventById(String id) {
        return eventRepository.findById(id)
                .map(eventMapper::getDtoFromDocument);
    }

    /**
     * Query per ottenere il DTO dell evento tramite ID
     * I risultato viene filtrato per data
     *
     * @param id
     * @return Optional<Event>
     */
    @Override
    public Optional<Event> findEventByIdAfterLocalDateNow(String id) {
        return eventRepository.findByIdAfterLocalDateNow(id)
                .map(eventMapper::getDtoFromDocument);
    }

    /**
     * Torna tutti gli eventi
     *
     * @return List<Event>
     */
    @Override
    public List<Event> findAll() {
        return eventRepository.findAll()
                .stream()
                .map(eventMapper::getDtoFromDocument)
                .collect(Collectors.toList());
    }

    /**
     * Torna tutti gli eventi della data passata
     *
     * @param localDate
     * @return List<Event>
     */
    @Override
    public List<Event> findByEventDate(LocalDate localDate) {
        return eventRepository.findByEventDate(localDate)
                .stream()
                .map(eventMapper::getDtoFromDocument)
                .collect(Collectors.toList());
    }

    /**
     * Torna tutti gli eventi in un specifico intervallo di tempo
     *
     * @param localDateStart
     * @param localDateEnd
     * @return List<Event>
     */
    @Override
    public List<Event> findByEventDateBetween(LocalDate localDateStart, LocalDate localDateEnd) {
        return eventRepository.findByEventDateBetween(localDateStart, localDateEnd)
                .stream()
                .map(eventMapper::getDtoFromDocument)
                .collect(Collectors.toList());
    }

    /**
     * Torna tutti gli eventi a distanza fissata dal geopoint
     *
     * @param geoPoint
     * @param distance
     * @return
     */
    @Override
    public List<Event> findByGeoPointNearMe(GeoPoint geoPoint, double distance) {
        return eventRepository.findByGeoPointNearMe(geoPoint, distance)
                .stream()
                .map(eventMapper::getDtoFromDocument)
                .collect(Collectors.toList());
    }

    /**
     * Ritorna la cronologia degli eventi
     *
     * @return List<Event>
     */
    @Override
    public List<Event> getUserEventHistory() {
        return getUserFromSecurityContext()
                .getEventList()
                .stream()
                .map(this::findEventById)
                .map(event -> event.orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Recupera l ' utente dal SecurityContext ovver l' utente loggato
     *
     * @return UserDocument
     */
    private UserDocument getUserFromSecurityContext() {
        final UserDocument principal = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getPrincipal)
                .map(o -> (UserDocument) o)
                .orElseThrow(() -> new MissingAuthenticationException("Non é presente l'oggetto Authentication nel SecurityContext"));
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new UserNotFoundException("L'utente {} non é stato trovato", principal.getId()));
    }
}
