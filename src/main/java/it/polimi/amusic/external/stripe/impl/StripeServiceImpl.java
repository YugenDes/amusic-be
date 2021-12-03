package it.polimi.amusic.external.stripe.impl;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import it.polimi.amusic.exception.EventAlreadyAttenedException;
import it.polimi.amusic.exception.EventFullException;
import it.polimi.amusic.exception.EventNotFoundException;
import it.polimi.amusic.exception.UserNotFoundException;
import it.polimi.amusic.external.stripe.StripeService;
import it.polimi.amusic.external.stripe.model.CreateStripePayment;
import it.polimi.amusic.external.stripe.model.CreateStripePaymentResponse;
import it.polimi.amusic.model.document.EventDocument;
import it.polimi.amusic.model.document.UserDocument;
import it.polimi.amusic.payment.model.PaymentRequest;
import it.polimi.amusic.payment.model.PaymentResponse;
import it.polimi.amusic.service.persistance.EventService;
import it.polimi.amusic.service.persistance.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service(value = "STRIPE")
@RequiredArgsConstructor
@Slf4j
public class StripeServiceImpl implements StripeService {

    private final EventService eventService;
    private final UserService userService;

    @Value("${stripe.apikey}")
    private String stripeKey;

    private void initializeStipeKey() {
        Stripe.apiKey = stripeKey;
    }

    public PaymentResponse createPayment(PaymentRequest payment) {

        initializeStipeKey();
        CreateStripePayment paymentStripe = (CreateStripePayment) payment;

        final String id = ((UserDocument) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();

        final UserDocument userDocument = userService.findById(id).orElseThrow(() -> new UserNotFoundException("L'untente {} non é stato trovato", id));

        final EventDocument eventDocument = eventService.findById(paymentStripe.getEventDocumentId()).orElseThrow(() -> new EventNotFoundException("L'evento {} non é stato trovato", paymentStripe.getEventDocumentId()));

        if (eventDocument.getPartecipants().size() >= eventDocument.getMaxPartecipants()) {
            throw new EventFullException("L'evento ha raggiunto il numero massimo di partecipanti, Impossibile continuare con il pagamento");
        }

        if (eventDocument.getPartecipantsIds().contains(userDocument.getId())) {
            throw new EventAlreadyAttenedException("Hai già acquistato questo evento");
        }

        PaymentIntentCreateParams params =
                PaymentIntentCreateParams.builder()
                        .setAmount((long) (eventDocument.getTicketPrice() * 100))
                        .putMetadata("userDocumentId", userDocument.getId())
                        .putMetadata("eventDocumentId", eventDocument.getId())
                        .putMetadata("visible", paymentStripe.getVisible().toString())
                        .setCurrency("eur")
                        .addPaymentMethodType("card")
                        .setReceiptEmail(userDocument.getEmail())
                        .build();

        try {
            PaymentIntent paymentIntent  = PaymentIntent.create(params);
            log.info("Nuovo payament intent per l'evento {} da user {}", eventDocument.getId(), userDocument.getId());
            return new CreateStripePaymentResponse(paymentIntent.getClientSecret());
        } catch (StripeException e) {
            log.error("Errore durante la creazione del pagamento {}",e.getLocalizedMessage());
            throw new it.polimi.amusic.exception.StripeException("Errore durante la creazione del pagamento {}",e.getLocalizedMessage());
        }

    }
}
