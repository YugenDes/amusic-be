package it.polimi.amusic.exception;

public class FilenameNotFoundInUrlException extends AmusicException {

    public FilenameNotFoundInUrlException(String message, Object... pars) {
        super(message, pars);
    }
}
