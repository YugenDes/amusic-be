package it.polimi.amusic.repository;

import it.polimi.amusic.exception.FirestoreException;
import it.polimi.amusic.model.document.PaymentDocument;

import java.time.LocalDate;
import java.util.List;

public interface PaymentRepository {

    PaymentDocument savePayment(PaymentDocument paymentDocument) throws FirestoreException;

    PaymentDocument findById(String id) throws FirestoreException;

    PaymentDocument findByIdPayment(String idPurchase) throws FirestoreException;

    List<PaymentDocument> findByDate(LocalDate localDate) throws FirestoreException;

    List<PaymentDocument> findByUser(String idUserDocument) throws FirestoreException;

    PaymentDocument findByUserAndEvent(String idUserDocument, String idEventDocument) throws FirestoreException;
}
