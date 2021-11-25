package it.polimi.amusic.exception;

public class EventFullException extends AmusicException {
    public EventFullException(String message, Object... pars) {
        super(message, pars);
    }
}
