package it.polimi.amusic.exception;

public class EventAlreadyAttenedException extends AmusicException {

    public EventAlreadyAttenedException(String message, Object... pars) {
        super(message, pars);
    }
}
