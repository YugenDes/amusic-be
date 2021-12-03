package it.polimi.amusic.mapper;

import it.polimi.amusic.model.document.FriendDocument;
import it.polimi.amusic.model.document.UserDocument;
import it.polimi.amusic.model.dto.Event;
import it.polimi.amusic.model.dto.Friend;
import it.polimi.amusic.service.persistance.EventService;
import it.polimi.amusic.service.persistance.UserService;
import it.polimi.amusic.utils.TimestampUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
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


    @Override
    public Friend mapUserFirendDocumentToFriend(FriendDocument friendDocument) {
        final UserDocument firendUserDocument = userService.findById(friendDocument.getId()).orElseThrow();
        final Friend friend = this.mapUserDocumentToFriend(firendUserDocument);
        friend.setFriendSince(TimestampUtils.convertTimestampToLocalDate(friendDocument.getFriendSince()));
        return friend;
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
    public Friend mapUserDocumentToFriend(UserDocument userDocument) {
        Friend friendFromUser = userMapper.mapUserDocumentToFriend(userDocument);
        final List<Event> events = userDocument
                .getEventList()
                .stream()
                .map(eventId ->
                        //Ed é nei prossimi giorni
                        eventService.findEventByIdAfterLocalDateNow(eventId)
                                .orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return friendFromUser.setNextEvents(events);
    }
}
