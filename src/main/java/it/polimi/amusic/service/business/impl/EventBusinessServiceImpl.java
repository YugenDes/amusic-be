package it.polimi.amusic.service.business.impl;

import com.google.cloud.firestore.Firestore;
import it.polimi.amusic.exception.EventNotFoundException;
import it.polimi.amusic.exception.FirestoreException;
import it.polimi.amusic.external.gcs.FileService;
import it.polimi.amusic.mapper.EventMapperDecorator;
import it.polimi.amusic.model.document.EventDocument;
import it.polimi.amusic.model.request.NewEventRequest;
import it.polimi.amusic.model.request.UpdateEventRequest;
import it.polimi.amusic.service.business.EventBusinessService;
import it.polimi.amusic.service.persistance.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventBusinessServiceImpl implements EventBusinessService {

    private final EventService eventService;
    private final FileService fileService;
    private final EventMapperDecorator eventMapper;
    private final Firestore firestore;

    @Override
    public EventDocument newEvent(NewEventRequest request) {
        return eventService.save(eventMapper.getDocumentFromRequest(request));
    }

    @Override
    public EventDocument updateEvent(UpdateEventRequest request) {
        try {
            return firestore.runTransaction(transaction ->
                    eventService.findById(request.getEventIdDocument())
                            .map(eventDocument ->
                                    eventService.save(eventMapper.updateEventDocumentFromRequest(eventDocument, request)))
                            .orElseThrow(() -> new EventNotFoundException("Evento {} non trovato", request.getEventIdDocument()))

            ).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new FirestoreException("Errore durante la transazione {}", e.getLocalizedMessage());
        }
    }

    @Override
    public EventDocument changeImageLink(String eventIdDocument, Resource resource) {
        final String linkFile = fileService.uploadFile(resource);
        return eventService.findById(eventIdDocument)
                .map(eventDocument -> {
                    fileService.deleteFile(eventDocument.getImageUrl());
                    return eventService.save(eventDocument.setImageUrl(linkFile));
                }).orElseThrow(() -> new EventNotFoundException("Evento {} non trovato", eventIdDocument));
    }
}
