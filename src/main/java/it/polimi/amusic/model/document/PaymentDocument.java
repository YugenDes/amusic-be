package it.polimi.amusic.model.document;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.DocumentId;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.cloud.gcp.data.firestore.Document;

import java.io.Serializable;

@Data
@Accessors(chain = true)
@Document(collectionName = "payments")
public class PaymentDocument implements Serializable {

    @DocumentId
    private String id;
    private String idPayment;
    private String status;
    private Timestamp datePayment;
    private Double amount;
    private String vendor;
    private String userIdDocument;
    private String eventIdDocument;


}
