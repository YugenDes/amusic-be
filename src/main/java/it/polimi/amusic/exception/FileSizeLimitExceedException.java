package it.polimi.amusic.exception;

public class FileSizeLimitExceedException extends AmusicException {

    public FileSizeLimitExceedException(String message, Object... pars) {
        super(message, pars);
    }
}
