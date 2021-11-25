package it.polimi.amusic.exception;

import it.polimi.amusic.utils.MessageBuilder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class AmusicException extends RuntimeException{

    private final String message;

    public AmusicException(String message, Object... pars) {
       this.message = MessageBuilder.buildMessage(message,pars);
    }

}
