package it.polimi.amusic.exception;

public class UserOperationException extends AmusicException {
    public UserOperationException(String message, Object... pars) {
        super(message, pars);
    }
}
