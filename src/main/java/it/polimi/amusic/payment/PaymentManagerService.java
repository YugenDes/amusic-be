package it.polimi.amusic.payment;

import it.polimi.amusic.payment.model.PaymentRequest;
import it.polimi.amusic.payment.model.PaymentResponse;


public interface PaymentManagerService {

    PaymentResponse pay(PaymentRequest request);
}
