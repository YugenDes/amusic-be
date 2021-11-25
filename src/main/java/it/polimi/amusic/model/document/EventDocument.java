package it.polimi.amusic.model.document;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.GeoPoint;
import com.google.cloud.firestore.annotation.DocumentId;
import it.polimi.amusic.utils.TimestampUtils;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.cloud.gcp.data.firestore.Document;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

@Data
@Accessors(chain = true)
@Document(collectionName = "events")
public class EventDocument implements Serializable {

    @DocumentId
    private String id;
    private String eventName;
    private String description;
    private Timestamp eventDatePublished;
    private Timestamp eventDate;
    private String imageUrl;
    private GeoPoint geoPoint;
    private String geoHash;
    private Integer maxPartecipants = 50;
    private Map<String, Boolean> partecipants = new HashMap<>();
    private Double ticketPrice;


    public EventDocument setEventDatePublished(Timestamp eventDatePublished) {
        this.eventDatePublished = eventDatePublished;
        return this;
    }

    public EventDocument setEventDatePublished(LocalDateTime eventDatePublished) {
        this.eventDatePublished = TimestampUtils.convertLocalDateTimeToTimestamp(eventDatePublished);
        return this;
    }

    public EventDocument setEventDate(Timestamp eventDate) {
        this.eventDate = eventDate;
        return this;
    }

    public EventDocument setEventDate(LocalDateTime eventDate) {
        this.eventDate = TimestampUtils.convertLocalDateTimeToTimestamp(eventDate);
        return this;
    }

    public boolean addPartecipantIfAbsent(String userIdDocument,Boolean visible) {
        if (Objects.isNull(partecipants.get(userIdDocument)) && partecipants.size() < maxPartecipants) {
            return partecipants.put(userIdDocument,visible);
        } else {
            return false;
        }
    }
}
