package it.polimi.amusic.mapper;

import com.firebase.geofire.core.GeoHash;
import com.google.cloud.firestore.GeoPoint;
import it.polimi.amusic.model.document.EventDocument;
import it.polimi.amusic.model.dto.Event;
import it.polimi.amusic.model.request.NewEventRequest;
import it.polimi.amusic.model.request.UpdateEventRequest;
import it.polimi.amusic.service.persistance.UserService;
import it.polimi.amusic.utils.TimestampUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, uses = {TimestampUtils.class, GeoPoint.class, GeoHash.class})
@DecoratedWith(EventMapperDecorator.class)
public interface EventMapper {

    @Mapping(target = "id", ignore = true)
    EventDocument updateEventDocument(@MappingTarget EventDocument document, Event dto);

    Event getDtoFromDocument(EventDocument document);

    // @Mapping(target = "geoPoint", expression = "java(new GeoPoint(request.getLat(), request.getLon()))")
    //@Mapping(target = "geoHash", expression = "java(new GeoHash(request.getLat(), request.getLon()).getGeoHashString())")
    EventDocument getDocumentFromRequest(NewEventRequest request);

    @Mapping(target = "id", ignore = true)
        // @Mapping(target = "geoPoint", expression = "java(new GeoPoint(request.getLat(), request.getLon()))")
        // @Mapping(target = "geoHash", expression = "java(new GeoHash(request.getLat(), request.getLon()).getGeoHashString())")
    EventDocument updateEventDocumentFromRequest(@MappingTarget EventDocument document, UpdateEventRequest request);
}
