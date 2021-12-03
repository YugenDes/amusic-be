package it.polimi.amusic.model.request;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AddFriendRequest {
    private String idUserFriendDocument;
}
