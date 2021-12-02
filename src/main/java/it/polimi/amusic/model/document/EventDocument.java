package it.polimi.amusic.model.document;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.GeoPoint;
import com.google.cloud.firestore.annotation.DocumentId;
import it.polimi.amusic.utils.TimestampUtils;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.cloud.gcp.data.firestore.Document;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    private List<PartecipantDocument> partecipants = new ArrayList<>();
    private List<String> partecipantsIds = new ArrayList<>();
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

    public boolean addPartecipantIfAbsent(PartecipantDocument partecipantDocument) {
        if (!partecipantsIds.contains(partecipantDocument.getId()) && partecipants.size() < maxPartecipants) {
            partecipants.add(partecipantDocument);
            partecipantsIds.add(partecipantDocument.getId());
            return true;
        } else {
            return false;
        }
    }
}
