package it.polimi.amusic.mapper;

import it.polimi.amusic.model.document.UserDocument;
import it.polimi.amusic.model.dto.Friend;
import it.polimi.amusic.model.dto.User;
import it.polimi.amusic.utils.TimestampUtils;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE , uses = TimestampUtils.class)
@DecoratedWith(UserMapperDecorator.class)
public interface UserMapper {

    User getDtoFromDocument(UserDocument userDocument);

    Friend mapUserFirendDocumentToFriend(UserDocument userDocument);

}
