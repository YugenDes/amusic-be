package it.polimi.amusic.exception;

public class UserNotFoundException extends AmusicException {
    public UserNotFoundException(String message, Object... pars) {
        super(message, pars);
    }
}
