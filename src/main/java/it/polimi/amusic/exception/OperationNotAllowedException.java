package it.polimi.amusic.exception;

public class OperationNotAllowedException extends AmusicException {
    public OperationNotAllowedException(String message, Object... pars) {
        super(message, pars);
    }
}
