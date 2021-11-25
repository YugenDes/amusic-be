package it.polimi.amusic.model.request;

import com.google.cloud.firestore.GeoPoint;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class UpdateEventRequest {

    private String eventIdDocument;
    private String name;
    private String description;
    private LocalDateTime eventDate;
    private Integer maxPartecipants;
    private Double ticketPrice;
    private Long lat;
    private Long lon;

}
