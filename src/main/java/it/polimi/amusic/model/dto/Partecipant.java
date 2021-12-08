package it.polimi.amusic.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Partecipant {

    private String id;
    private String name;
    private String surname;
    private String photoUrl;
}
