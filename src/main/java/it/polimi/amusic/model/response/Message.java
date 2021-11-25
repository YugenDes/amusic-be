package it.polimi.amusic.model.response;

import lombok.*;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Message {

    private String code;
    private String text;

    @Builder.Default
    private MessageType messageType = MessageType.error;
    private String request;
    @Singular
    private Map<String, String[]> parameters;
}
