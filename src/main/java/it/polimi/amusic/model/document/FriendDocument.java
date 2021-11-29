package it.polimi.amusic.model.document;

import com.google.cloud.Timestamp;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class FriendDocument {

    private String id;
    private Timestamp friendSince;

}
