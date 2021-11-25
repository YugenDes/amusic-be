package it.polimi.amusic.mapper;

import it.polimi.amusic.model.document.UserDocument;
import it.polimi.amusic.model.dto.Friend;
import it.polimi.amusic.service.persistance.EventService;
import it.polimi.amusic.service.persistance.UserService;
import it.polimi.amusic.utils.TimestampUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class UserMapperDecorator implements UserMapper {

    @Autowired
    private EventService eventService;
    @Autowired
    private UserService userService;
    @Autowired
    private UserMapper userMapper;

    public UserMapperDecorator() {
    }

    public UserMapperDecorator(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public UserMapperDecorator(EventService eventService, UserMapper userMapper) {
        this.eventService = eventService;
        this.userMapper = userMapper;
    }

    /**
     * Il metodo mappa un UserDocument in Friend
     * Oscurando i campi che non sono coinvolti ed evitare un data leak
     * In particolare il metodo mappa gli eventi visibili sottoscritti dall utente
     * In una mappa contenente Nome Evento e Url dell immagine
     *
     * @param userDocument
     * @return Friend dto
     */
    @Override
    public Friend mapUserFirendDocumentToFriend(UserDocument userDocument) {
        Friend friendFromUser = userMapper.mapUserFirendDocumentToFriend(userDocument);
        final Map<String, String> events = userDocument
                .getEventList()
                .stream()
                .map(eventId ->
                        eventService.findById(eventId)
                                .filter(eventDocument -> eventDocument.getPartecipants().get(friendFromUser.getId()))
                                //Ed é nei prossimi giorni
                                .filter(eventDocument -> eventDocument.getEventDate().getSeconds()
                                        > TimestampUtils.convertLocalDateToTimestamp(LocalDate.now()).getSeconds())
                                .map(eventDocument -> Map.entry(eventDocument.getEventName(), eventDocument.getImageUrl()))
                                .orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return friendFromUser.setNextEvents(events);
    }
}
