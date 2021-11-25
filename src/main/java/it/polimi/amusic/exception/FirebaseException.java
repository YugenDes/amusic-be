package it.polimi.amusic.exception;

public class FirebaseException extends AmusicException {
    public FirebaseException(String message, Object... pars) {
        super(message, pars);
    }
}
