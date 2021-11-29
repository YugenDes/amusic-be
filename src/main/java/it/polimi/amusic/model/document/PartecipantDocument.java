package it.polimi.amusic.model.document;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PartecipantDocument {

    private String id;
    private String name;
    private String surname;
    private String photoUrl;
    private Boolean visible;

}
