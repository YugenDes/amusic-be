package it.polimi.amusic.exception;

public class UserAlreadyRegisteredException extends AmusicException {

    public UserAlreadyRegisteredException(String message, Object... pars) {
        super(message, pars);
    }
}
