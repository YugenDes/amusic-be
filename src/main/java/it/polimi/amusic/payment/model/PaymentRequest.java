package it.polimi.amusic.payment.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import it.polimi.amusic.external.stripe.model.CreateStripePayment;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "provider",
        visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = CreateStripePayment.class, name = "STRIPE")
})
public abstract class PaymentRequest {
    @NonNull
    private PaymentProvider provider;
}
