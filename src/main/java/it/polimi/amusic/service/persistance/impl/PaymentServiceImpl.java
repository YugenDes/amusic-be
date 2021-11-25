package it.polimi.amusic.service.persistance.impl;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.SetOptions;
import it.polimi.amusic.exception.FirestoreException;
import it.polimi.amusic.exception.PurchaseNotFoundException;
import it.polimi.amusic.mapper.PaymentMapperDecorator;
import it.polimi.amusic.model.document.PaymentDocument;
import it.polimi.amusic.model.dto.Payment;
import it.polimi.amusic.service.persistance.PaymentService;
import it.polimi.amusic.utils.TimestampUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final Firestore firestore;

    private final PaymentMapperDecorator paymentMapper;

    @Override
    public PaymentDocument savePayment(PaymentDocument paymentDocument) throws FirestoreException {
        try {
            final CollectionReference events = firestore.collection("payments");
            DocumentReference document;
            if (StringUtils.isNotBlank(paymentDocument.getId())) {
                document = events.document(paymentDocument.getId());
            } else {
                document = events.document();
            }
            document.set(paymentDocument, SetOptions.merge())
                    .get();
            return paymentDocument.setId(document.getId());
        } catch (InterruptedException | ExecutionException e) {
            throw new FirestoreException("Impossibile effettuare la query {}", e.getLocalizedMessage());
        }
    }

    @Override
    public PaymentDocument findById(String id)  throws FirestoreException {
        try {
            return Optional.ofNullable(firestore.collection("payments")
                            .document(id)
                            .get()
                            .get())
                    .map(documentSnapshot -> documentSnapshot.toObject(PaymentDocument.class))
                    .orElseThrow(() -> new PurchaseNotFoundException("Acquisto con id {} non trovato", id));
        } catch (InterruptedException | ExecutionException e) {
            throw new FirestoreException("Impossibile effettuare la query {}", e.getLocalizedMessage());
        }
    }

    @Override
    public PaymentDocument findByIdPayment(String idPayment) throws FirestoreException {
        try {
            return Optional.ofNullable(firestore.collection("payments")
                            .whereEqualTo("idPayment",idPayment)
                            .get()
                            .get())
                    .map(documentSnapshot -> documentSnapshot.toObjects(PaymentDocument.class)
                            .stream()
                            .findFirst()
                            .orElseThrow(() -> new PurchaseNotFoundException("Acquisto con idPurchase {} non trovato", idPayment)))
                    .orElseThrow(() -> new PurchaseNotFoundException("Acquisto con idPurchase {} non trovato", idPayment));
        } catch (InterruptedException | ExecutionException e) {
            throw new FirestoreException("Impossibile effettuare la query {}", e.getLocalizedMessage());
        }
    }

    @Override
    public List<PaymentDocument> findByDate(LocalDate localDate) throws FirestoreException {
        try {
            return Optional.ofNullable(firestore.collection("payments")
                            .whereGreaterThanOrEqualTo("datePayment", TimestampUtils.convertLocalDateToTimestamp(localDate))
                            .whereLessThanOrEqualTo("datePayment", TimestampUtils.convertLocalDateToTimestamp(localDate.plusDays(1)))
                            .get()
                            .get())
                    .map(queryDocumentSnapshots -> queryDocumentSnapshots.toObjects(PaymentDocument.class))
                    .orElseThrow();
        } catch (InterruptedException | ExecutionException e) {
            throw new FirestoreException("Impossibile effettuare la query {}", e.getLocalizedMessage());
        }
    }

    @Override
    public List<Payment> findByUser(String idUserDocument) throws FirestoreException {
        try {
            return Optional.ofNullable(firestore.collection("payments")
                            .whereEqualTo("idUserDocument", idUserDocument)
                            .get().get())
                    .map(queryDocumentSnapshots -> queryDocumentSnapshots.toObjects(PaymentDocument.class))
                    .map(paymentMapper::getDtosFromDocuments)
                    .orElseThrow(() -> new PurchaseNotFoundException("Acquisti per user {} non trovati", idUserDocument));
        } catch (ExecutionException | InterruptedException e) {
            throw new FirestoreException("Impossibile effettuare la query {}", e.getLocalizedMessage());
        }
    }
}
