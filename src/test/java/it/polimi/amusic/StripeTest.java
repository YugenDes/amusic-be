package it.polimi.amusic;


import it.polimi.amusic.external.stripe.impl.StripeServiceImpl;
import it.polimi.amusic.external.stripe.model.CreateStripePayment;
import it.polimi.amusic.external.stripe.model.CreateStripePaymentResponse;
import it.polimi.amusic.model.document.PaymentDocument;
import it.polimi.amusic.model.document.UserDocument;
import it.polimi.amusic.payment.model.PaymentProvider;
import it.polimi.amusic.repository.PaymentRepository;
import it.polimi.amusic.repository.UserRepository;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.properties")
class StripeTest {

    @Autowired
    StripeServiceImpl stripeServiceImpl;

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    UserRepository userRepository;

    @Test
    void contextLoads() {
        final UserDocument userDocument = userRepository.findById("puLxmw6ozrb7X7IuVWkr").orElseThrow();
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDocument, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void createPaymentIntent() {
        final CreateStripePaymentResponse clientSecret = (CreateStripePaymentResponse) stripeServiceImpl.createPayment(new CreateStripePayment()
                .setEventDocumentId("icuXDgj7mPkhY9Rln5cf")
                .setVisible(true)
                .setProvider(PaymentProvider.STRIPE)
        );
        Assertions.assertTrue(StringUtils.isNotBlank(clientSecret.getClientSecret()), "Il client secret non puo essere nullo");
    }

    @Test
    void paymentService() {
        final PaymentDocument findPayment = paymentRepository.findByIdPayment("pi_3JuMq7AjkGk83dhJ14NT2XjL");
        Assertions.assertEquals(findPayment.getVendor(), PaymentProvider.STRIPE.name(), "Il pagameno non é stato effettuato su stripe");
    }

}


