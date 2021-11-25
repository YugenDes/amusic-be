package it.polimi.amusic.exception;

public class AmusicEmailException extends AmusicException {
    public AmusicEmailException(String message, Object... pars) {
        super(message, pars);
    }
}
