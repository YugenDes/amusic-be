package it.polimi.amusic.payment;

import it.polimi.amusic.payment.model.PaymentProvider;
import it.polimi.amusic.payment.model.PaymentRequest;
import it.polimi.amusic.payment.model.PaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentManagerServiceImpl implements PaymentManagerService{

    private final ApplicationContext context;

    public PaymentResponse pay(PaymentRequest request) {
        final PaymentProvider provider = request.getProvider();
        return ((PaymentService) context.getBean(provider.toString())).createPayment(request);
    }
}
