package it.polimi.amusic.exception;

public class PurchaseNotFoundException extends AmusicException {

    public PurchaseNotFoundException(String message, Object... pars) {
        super(message, pars);
    }
}
