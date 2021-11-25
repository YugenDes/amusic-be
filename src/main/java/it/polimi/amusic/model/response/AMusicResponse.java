package it.polimi.amusic.model.response;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AMusicResponse<T> {

    private T body;

    @Singular
    private List<Message> messages = new ArrayList<>();

}
