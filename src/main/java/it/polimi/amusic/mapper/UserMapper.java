package it.polimi.amusic.mapper;

import it.polimi.amusic.model.document.FriendDocument;
import it.polimi.amusic.model.document.UserDocument;
import it.polimi.amusic.model.dto.Friend;
import it.polimi.amusic.model.dto.Partecipant;
import it.polimi.amusic.model.dto.User;
import it.polimi.amusic.model.request.UpdateUserRequest;
import it.polimi.amusic.utils.TimestampUtils;
import org.mapstruct.*;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, uses = TimestampUtils.class)
@DecoratedWith(UserMapperDecorator.class)
public interface UserMapper {

    User getDtoFromDocument(UserDocument userDocument);

    Friend mapUserFirendDocumentToFriend(FriendDocument friendDocument);

    Friend mapUserDocumentToFriend(UserDocument userDocument);

    @Mapping(target = "birthDay", expression = "java(TimestampUtils.convertLocalDateToTimestamp(request.getBirthDay()))")
    UserDocument updateUserDocumentFromUpdateRequest(@MappingTarget UserDocument document, UpdateUserRequest request);


    Partecipant mapUserDocumentToPartecipant(UserDocument userDocument);

}
