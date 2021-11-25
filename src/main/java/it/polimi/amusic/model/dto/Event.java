package it.polimi.amusic.model.dto;

import com.google.cloud.firestore.GeoPoint;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@Accessors(chain = true)
public class Event {
    private String id;
    private String eventName;
    private String description;
    private LocalDateTime eventDatePublished;
    private LocalDateTime eventDate;
    private String imageUrl;
    private GeoPoint geoPoint;
    private Integer maxPartecipants = 50;
    private Map<String,String> partecipants = new HashMap<>();
    private Double ticketPrice;
}
