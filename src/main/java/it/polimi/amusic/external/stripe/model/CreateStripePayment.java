package it.polimi.amusic.external.stripe.model;

import com.google.firebase.database.annotations.NotNull;
import it.polimi.amusic.payment.model.PaymentProvider;
import it.polimi.amusic.payment.model.PaymentRequest;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ToString
public class CreateStripePayment extends PaymentRequest {
    @NotNull
    private String eventDocumentId;
    @NotNull
    private Boolean visible;

    public CreateStripePayment() {
        super(PaymentProvider.STRIPE);
    }
}
