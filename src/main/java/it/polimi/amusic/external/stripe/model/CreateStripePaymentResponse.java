package it.polimi.amusic.external.stripe.model;

import it.polimi.amusic.payment.model.PaymentResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
@Getter
public class CreateStripePaymentResponse extends PaymentResponse {
    private String clientSecret;
}
