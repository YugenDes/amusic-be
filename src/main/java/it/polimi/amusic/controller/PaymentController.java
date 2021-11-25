package it.polimi.amusic.controller;

import it.polimi.amusic.model.response.AMusicResponse;
import it.polimi.amusic.payment.PaymentManagerService;
import it.polimi.amusic.payment.model.PaymentRequest;
import it.polimi.amusic.payment.model.PaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentManagerService paymentManagerService;

    @PostMapping(value = "/private/pay")
    public AMusicResponse<PaymentResponse> createPayment(@RequestBody PaymentRequest payment){
        log.info("new POST request to /pay body:{}", payment);
        final PaymentResponse payamentResponse = paymentManagerService.pay(payment);
        return AMusicResponse.<PaymentResponse>builder().body(payamentResponse).build();
    }
}
