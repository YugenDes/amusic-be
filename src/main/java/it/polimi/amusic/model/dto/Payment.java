package it.polimi.amusic.model.dto;

import it.polimi.amusic.payment.model.PaymentProvider;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class Payment {
    private String id;
    private String idPayment;
    private String status;
    private LocalDateTime datePayment;
    private Double amount;
    private PaymentProvider vendor;
    private Event event;
}
