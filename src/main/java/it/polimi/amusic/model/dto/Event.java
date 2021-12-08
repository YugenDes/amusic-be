package it.polimi.amusic.model.dto;

import com.google.cloud.firestore.GeoPoint;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class Event {
    private String id;
    private String eventName;
    private String description;
    private String phoneNumber;
    //Via CAP Comune
    private String address;
    private LocalDateTime eventDatePublished;
    private LocalDateTime eventDate;
    private String imageUrl;
    private GeoPoint geoPoint;
    private Integer maxPartecipants = 50;
    private List<Partecipant> partecipants = new ArrayList<>();
    private Double ticketPrice;
}
