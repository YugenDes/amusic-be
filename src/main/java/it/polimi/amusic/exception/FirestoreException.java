package it.polimi.amusic.exception;

public class FirestoreException extends AmusicException {

    public FirestoreException(String message, Object... pars) {
        super(message, pars);
    }
}
