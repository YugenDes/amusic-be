package it.polimi.amusic.service.business;

import it.polimi.amusic.model.document.EventDocument;
import it.polimi.amusic.model.request.NewEventRequest;
import it.polimi.amusic.model.request.UpdateEventRequest;
import org.springframework.core.io.Resource;

public interface EventBusinessService {

    EventDocument newEvent(NewEventRequest request);

    EventDocument updateEvent(UpdateEventRequest request);

    EventDocument changeImageLink(String eventIdDocument,Resource resource);

}
