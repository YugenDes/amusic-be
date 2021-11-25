package it.polimi.amusic.exception;

public class StripeException extends AmusicException {

    public StripeException(String message, Object... pars) {
        super(message, pars);
    }
}
