package it.polimi.amusic.model.request;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class RemoveFriendRequest {
    private String idUserFriendDocument;
}
