package it.polimi.amusic.controller;

import com.stripe.model.*;
import com.stripe.net.Webhook;
import it.polimi.amusic.external.stripe.StripeWebhookService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookController {

    private static final String STRIPE_HTTP_HEADER = "Stripe-Signature";
    private final StripeWebhookService stripeWebhookService;

    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    @SneakyThrows
    @PostMapping("/public/stripe/webhook")
    public String handleStripeEvent(@RequestBody String payload,
                                    @RequestHeader(STRIPE_HTTP_HEADER) String signature) {

        if (Objects.isNull(signature)) {
            log.warn("Chiamata sospetta all'endpoint /stripe/webhook {}",payload);
            return "";
        }

        Event event = Webhook.constructEvent(payload, signature, endpointSecret);
        stripeWebhookService.handleEvent(event);
        return "";
    }
}
