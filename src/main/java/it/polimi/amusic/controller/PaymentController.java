package it.polimi.amusic.controller;

import it.polimi.amusic.model.document.UserDocument;
import it.polimi.amusic.model.dto.Payment;
import it.polimi.amusic.model.response.AMusicResponse;
import it.polimi.amusic.payment.PaymentManagerService;
import it.polimi.amusic.payment.model.PaymentRequest;
import it.polimi.amusic.payment.model.PaymentResponse;
import it.polimi.amusic.service.persistance.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentManagerService paymentManagerService;
    private final PaymentService paymentService;

    @PostMapping(value = "/private/pay")
    public AMusicResponse<PaymentResponse> createPayment(@RequestBody PaymentRequest payment){
        log.info("new POST request to /pay body:{}", payment);
        final PaymentResponse payamentResponse = paymentManagerService.pay(payment);
        return AMusicResponse.<PaymentResponse>builder().body(payamentResponse).build();
    }

    @GetMapping(value = "/private/payment/history")
    public AMusicResponse<List<Payment>> getPaymentHistory(){
        final UserDocument principal = (UserDocument) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("new GET request to /payment/history {}" ,principal.getId());
        final List<Payment> history = paymentService.findByUser(principal.getId());
        return AMusicResponse.<List<Payment>>builder().body(history).build();
    }
}
