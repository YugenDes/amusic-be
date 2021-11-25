package it.polimi.amusic.service.persistance;

import com.google.cloud.firestore.DocumentReference;
import it.polimi.amusic.exception.FirestoreException;
import it.polimi.amusic.model.document.PaymentDocument;

import java.time.LocalDate;
import java.util.List;

public interface PaymentService {

    PaymentDocument savePayment(PaymentDocument paymentDocument) throws FirestoreException;

    PaymentDocument findById(String id) throws FirestoreException;

    PaymentDocument findByIdPayment(String idPurchase) throws FirestoreException;

    List<PaymentDocument> findByDate(LocalDate localDate) throws FirestoreException;

    List<PaymentDocument> findByUser(DocumentReference userDocument) throws FirestoreException;
}
