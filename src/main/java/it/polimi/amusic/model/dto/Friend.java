package it.polimi.amusic.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Accessors(chain = true)
public class Friend {

    private String id;
    private String photoUrl;
    private String displayName;
    private LocalDateTime lastLogin;
    private Map<String,String> nextEvents;

}
