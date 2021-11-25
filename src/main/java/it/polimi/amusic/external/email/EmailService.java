package it.polimi.amusic.external.email;

import com.google.firebase.database.annotations.NotNull;
import it.polimi.amusic.exception.AmusicEmailException;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.activation.DataSource;

public interface EmailService {

    boolean sendEmail(EmailRequest request) throws AmusicEmailException;

    /**
     * Classe request per poter costruire l'email da inviare
     * tramite il service
     */
    @Data
    @Accessors(chain = true)
    class EmailRequest {
        @NotNull
        private String emailTo;
        @NotNull
        private String subject;
        @NotNull
        private String text;
        @NotNull
        private Boolean htmlText;
        private DataSource attachment;
    }

}
