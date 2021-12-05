package it.polimi.amusic.repository;

import it.polimi.amusic.exception.FirestoreException;
import it.polimi.amusic.model.document.PaymentDocument;
import it.polimi.amusic.model.dto.Payment;

import java.time.LocalDate;
import java.util.List;

public interface PaymentRepository {

    PaymentDocument savePayment(PaymentDocument paymentDocument) throws FirestoreException;

    PaymentDocument findById(String id) throws FirestoreException;

    PaymentDocument findByIdPayment(String idPurchase) throws FirestoreException;

    List<PaymentDocument> findByDate(LocalDate localDate) throws FirestoreException;

    List<Payment> findByUser(String idUserDocument) throws FirestoreException;

    Payment findByUserAndEvent(String idUserDocument, String idEventDocument) throws FirestoreException;
}
