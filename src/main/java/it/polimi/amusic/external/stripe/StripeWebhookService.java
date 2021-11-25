package it.polimi.amusic.external.stripe;

import com.stripe.model.Event;

public interface StripeWebhookService {

     void handleEvent(Event event);

}
