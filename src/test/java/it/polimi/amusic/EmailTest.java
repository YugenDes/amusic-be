package it.polimi.amusic;


import it.polimi.amusic.external.email.EmailService;
import it.polimi.amusic.utils.QRCodeGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.properties")
class EmailTest {

    @Autowired
    EmailService emailService;

    @Test
    void inviaEmailQrCode() {
        emailService.sendEmail(new EmailService.EmailRequest()
                .setText("TEST QRCODE")
                .setSubject("TEST QRCODE")
                .setHtmlText(false)
                .setEmailTo("andrea.messina220399@gmail.com")
                .setAttachment(QRCodeGenerator.generateQRCodeImage("TEST")));
    }

}
