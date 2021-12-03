package it.polimi.amusic.model.document;

import com.google.cloud.Timestamp;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode
public class FriendDocument {

    private String id;
    private Timestamp friendSince;

}
