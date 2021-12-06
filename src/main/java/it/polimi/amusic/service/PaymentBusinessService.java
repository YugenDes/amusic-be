package it.polimi.amusic.service;

import it.polimi.amusic.exception.FirestoreException;
import it.polimi.amusic.model.dto.Payment;

import java.util.List;

public interface PaymentBusinessService {

    List<Payment> findByUser(String idUserDocument) throws FirestoreException;

    Payment getInfoPaymentFromEvent(String idUserDocument, String idEventDocument) throws FirestoreException;
}
