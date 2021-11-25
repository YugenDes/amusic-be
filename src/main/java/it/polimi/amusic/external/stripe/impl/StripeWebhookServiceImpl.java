package it.polimi.amusic.external.stripe.impl;


import com.stripe.model.Charge;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import it.polimi.amusic.exception.StripeException;
import it.polimi.amusic.external.email.EmailService;
import it.polimi.amusic.external.stripe.StripeWebhookService;
import it.polimi.amusic.model.document.EventDocument;
import it.polimi.amusic.model.document.PaymentDocument;
import it.polimi.amusic.model.document.UserDocument;
import it.polimi.amusic.payment.model.PaymentProvider;
import it.polimi.amusic.service.business.UserBusinessService;
import it.polimi.amusic.service.persistance.EventService;
import it.polimi.amusic.service.persistance.PaymentService;
import it.polimi.amusic.service.persistance.UserService;
import it.polimi.amusic.utils.MessageBuilder;
import it.polimi.amusic.utils.QRCodeGenerator;
import it.polimi.amusic.utils.TimestampUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookServiceImpl implements StripeWebhookService {

    private final UserBusinessService userBusinessService;
    private final UserService userService;
    private final EventService eventService;
    private final PaymentService paymentService;
    private final EmailService emailService;

    public void handleEvent(Event event) {
        // Deserialization failed, probably due to an API version mismatch.
        final StripeObject stripeObject = event.getDataObjectDeserializer()
                .getObject()
                .orElseThrow(() -> new StripeException("Errore durante la deserializzazione dell oggeto Stripe"));

        switch (event.getType()) {
            case "charge.succeeded":
                Charge charge = (Charge) stripeObject;
                log.info("Payment for " + charge.getAmount() + " succeeded.");
                final String userDocumentId = charge.getMetadata().get("userDocumentId");
                final String eventDocumentId = charge.getMetadata().get("eventDocumentId");
                final Boolean visible = Boolean.valueOf(charge.getMetadata().get("visible"));
                final UserDocument userDocument = userService.findById(userDocumentId).orElseThrow();
                final EventDocument eventDocument = eventService.findById(eventDocumentId).orElseThrow();

                userBusinessService.attendAnEvent(userDocumentId, eventDocumentId,visible);

                paymentService.savePayment(new PaymentDocument()
                        .setEventIdDocument(eventDocument.getId())
                        .setUserIdDocument(userDocument.getId())
                        .setAmount(Double.valueOf(charge.getAmount()))
                        .setIdPayment(charge.getId())
                        .setDatePayment(TimestampUtils.convertLocalDateTimeToTimestamp(LocalDateTime.now()))
                        .setStatus(charge.getStatus())
                        .setVendor(PaymentProvider.STRIPE.name()));

                emailService.sendEmail(new EmailService.EmailRequest()
                        .setEmailTo(userDocument.getEmail())
                        .setSubject("Pagamento avvenuto con successo")
                        .setText(MessageBuilder.buildMessage("L acquisto per il ticket {} é avvenuto con successo per una somma di {}", eventDocument.getEventName(), charge.getAmount()))
                        .setHtmlText(false)
                        .setAttachment(QRCodeGenerator.generateQRCodeImage(charge.getId())));

                break;
            default:
                log.warn("Evento non gestito {}", event.getType());
                break;
        }
    }

}
