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

    @Mapping(target = "name", expression = "java(userDocument.getName().charAt(0 ) + userDocument.getName().substring(1).toLowerCase())")
    @Mapping(target = "surname", expression = "java(userDocument.getSurname().charAt(0 ) + userDocument.getSurname().substring(1).toLowerCase())")
    @Mapping(target = "birthDay", expression = "java(TimestampUtils.convertTimestampToLocalDate(userDocument.getBirthDay()))")
    User getDtoFromDocument(UserDocument userDocument);

    Friend mapUserFirendDocumentToFriend(FriendDocument friendDocument);

    @Mapping(target = "displayName", expression = "java(userDocument.getName().toLowerCase()+' '+userDocument.getSurname().substring(1).toLowerCase())")
    Friend mapUserDocumentToFriend(UserDocument userDocument);

    @Mapping(target = "birthDay", expression = "java(TimestampUtils.convertLocalDateToTimestamp(request.getBirthDay()))")
    UserDocument updateUserDocumentFromUpdateRequest(@MappingTarget UserDocument document, UpdateUserRequest request);


    Partecipant mapUserDocumentToPartecipant(UserDocument userDocument);

    @AfterMapping
    default User afterMappingRca(UserDocument userDocument, @MappingTarget User user) {
        return user.setDisplayName(user.getName() + " " + user.getSurname());
    }
}
