package it.polimi.amusic.external.email.impl;

import it.polimi.amusic.exception.AmusicEmailException;
import it.polimi.amusic.external.email.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    //Utilizzo il bean di Spring per inviare Email

    private final JavaMailSender javaMailSender;

    @Override
    public boolean sendEmail(EmailRequest request) throws AmusicEmailException {
        try {
            //Creo un MimeMessage cosi da poter inserire allegati
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom("noreply@polimi-amusic.appspotmail.com");
            helper.setTo(request.getEmailTo());
            helper.setSubject(request.getSubject());
            helper.setText(request.getText(), request.getHtmlText());
            if (Objects.nonNull(request.getAttachment())) {
                helper.addInline("Ticket", request.getAttachment());
                helper.addAttachment(request.getAttachment().getName(), request.getAttachment());
            }
              javaMailSender.send(message);
            return true;
        } catch (MailException | MessagingException e) {
            log.error("Errore durante l'invio dell mail : {}", e.getLocalizedMessage());
            throw new AmusicEmailException("Errore durante l'invio dell mail : {}", e.getLocalizedMessage());
        }
    }
}
