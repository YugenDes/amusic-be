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
import it.polimi.amusic.repository.EventRepository;
import it.polimi.amusic.repository.PaymentRepository;
import it.polimi.amusic.repository.UserRepository;
import it.polimi.amusic.service.UserBusinessService;
import it.polimi.amusic.utils.MessageBuilder;
import it.polimi.amusic.utils.QRCodeGenerator;
import it.polimi.amusic.utils.TimestampUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookServiceImpl implements StripeWebhookService {

    private final UserBusinessService userBusinessService;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final PaymentRepository paymentRepository;
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
                final UserDocument userDocument = userRepository.findById(userDocumentId).orElseThrow();
                final EventDocument eventDocument = eventRepository.findById(eventDocumentId).orElseThrow();
                log.info("Charge {}", charge.getMetadata());
                log.info("Attend event {} , {}", userDocumentId, eventDocumentId);
                try {
                    userBusinessService.attendAnEvent(userDocumentId, eventDocumentId, visible);

                    paymentRepository.savePayment(new PaymentDocument()
                            .setIdEventDocument(eventDocument.getId())
                            .setIdUserDocument(userDocument.getId())
                            .setAmount(Double.valueOf(charge.getAmount()))
                            .setIdPayment(charge.getId())
                            .setDatePayment(TimestampUtils.convertLocalDateTimeToTimestamp(LocalDateTime.now()))
                            .setStatus(charge.getStatus())
                            .setVendor(PaymentProvider.STRIPE.name()));

                    emailService.sendEmail(new EmailService.EmailRequest()
                            .setEmailTo(userDocument.getEmail())
                            .setSubject("Pagamento avvenuto con successo")
                            .setText(MessageBuilder.buildMessage("L acquisto per il ticket {} é avvenuto con successo per una somma di {}€", eventDocument.getEventName(), charge.getAmount() / 100D))
                            .setHtmlText(false)
                            .setAttachment(QRCodeGenerator.generateQRCodeImage(charge.getId())));
                } catch (Exception e) {
                    emailService.sendEmail(new EmailService.EmailRequest()
                            .setEmailTo(userDocument.getEmail())
                            .setSubject(MessageBuilder.buildMessage("Errore durante la prenotazione dell'evento {}", eventDocument.getEventName()))
                            .setText(MessageBuilder.buildMessage("L acquisto per il ticket {} NON é avvenuto con successo , presto riceverà un rimborso per una somma di {}€", eventDocument.getEventName(), charge.getAmount() / 100D))
                            .setHtmlText(false));
                }
                break;
            default:
                log.warn("Evento non gestito {}", event.getType());
                break;
        }
    }

}
