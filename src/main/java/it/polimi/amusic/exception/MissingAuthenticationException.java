package it.polimi.amusic.exception;

public class MissingAuthenticationException extends AmusicException {
    public MissingAuthenticationException(String message, Object... pars) {
        super(message, pars);
    }
}
