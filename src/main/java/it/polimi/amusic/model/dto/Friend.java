package it.polimi.amusic.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Accessors(chain = true)
public class Friend {

    private String id;
    private String photoUrl;
    private String displayName;
    private LocalDateTime lastLogin;
    private LocalDate friendSince;
    private List<Event> nextEvents;

}
