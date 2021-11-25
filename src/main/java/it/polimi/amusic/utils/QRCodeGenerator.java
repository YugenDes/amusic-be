package it.polimi.amusic.utils;

import net.glxn.qrgen.javase.QRCode;

import javax.activation.DataSource;
import javax.mail.util.ByteArrayDataSource;
import java.time.LocalDateTime;

public class QRCodeGenerator {

    public static DataSource generateQRCodeImage(String text) {
        FileByteArrayResource stream = new FileByteArrayResource("Ticket.png", QRCode
                .from(text)
                .withSize(300, 300)
                .stream().toByteArray(), "Ticket " + LocalDateTime.now());
        return new ByteArrayDataSource(stream.getByteArray(), "image/png");
    }
}
