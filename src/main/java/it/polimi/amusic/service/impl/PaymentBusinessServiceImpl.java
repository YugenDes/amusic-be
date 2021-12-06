package it.polimi.amusic.service.impl;

import it.polimi.amusic.exception.FirestoreException;
import it.polimi.amusic.mapper.PaymentMapperDecorator;
import it.polimi.amusic.model.dto.Payment;
import it.polimi.amusic.repository.PaymentRepository;
import it.polimi.amusic.service.PaymentBusinessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentBusinessServiceImpl implements PaymentBusinessService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapperDecorator paymentMapper;

    /**
     * Ritorna la lista dei pagamenti effettuati da quell utente
     *
     * @param idUserDocument
     * @return List<Payment> lista dei pagamenti
     * @throws FirestoreException
     */
    @Override
    public List<Payment> findByUser(String idUserDocument) throws FirestoreException {
        return paymentRepository
                .findByUser(idUserDocument)
                .stream()
                .map(paymentMapper::getDtoFromDocument)
                .collect(Collectors.toList());
    }

    /**
     * Ritorna le informazioni di pagamento
     * Di un dato evento per un dato utente
     *
     * @param idUserDocument
     * @param idEventDocument
     * @return PaymentDto
     * @throws FirestoreException
     */
    @Override
    public Payment getInfoPaymentFromEvent(String idUserDocument, String idEventDocument) throws FirestoreException {
        return paymentMapper.getDtoNoEventFromDocument(paymentRepository.findByUserAndEvent(idUserDocument, idEventDocument));
    }
}
