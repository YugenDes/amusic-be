package it.polimi.amusic.exception;

public class EventNotFoundException extends AmusicException {

    public EventNotFoundException(String message, Object... pars) {
        super(message, pars);
    }
}
