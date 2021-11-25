package it.polimi.amusic;


import it.polimi.amusic.external.stripe.impl.StripeServiceImpl;
import it.polimi.amusic.model.document.PaymentDocument;
import it.polimi.amusic.service.persistance.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations="classpath:application.properties")
class StripeTest {

    @Autowired
    StripeServiceImpl stripeServiceImpl;

    @Autowired
    PaymentService paymentService;

    @Test
    void contextLoads() {
    }

    @Test
    void paymentService(){
        final PaymentDocument pi_3JuMq7AjkGk83dhJ14NT2XjL = paymentService.findByIdPayment("pi_3JuMq7AjkGk83dhJ14NT2XjL");
        System.out.println(pi_3JuMq7AjkGk83dhJ14NT2XjL);
    }
}


