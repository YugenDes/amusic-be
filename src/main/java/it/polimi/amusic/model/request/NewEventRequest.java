package it.polimi.amusic.model.request;


import com.google.cloud.Timestamp;
import com.google.cloud.firestore.GeoPoint;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@Accessors(chain = true)
public class NewEventRequest {
    private String eventName;
    private String description;
    private LocalDateTime eventDate;
    private String imageUrl;
    private Long lat;
    private Long lon;
    private Integer maxPartecipants;
    private Double ticketPrice;
}
