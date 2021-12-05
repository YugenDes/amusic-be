package it.polimi.amusic.mapper;

import it.polimi.amusic.model.document.PaymentDocument;
import it.polimi.amusic.model.dto.Payment;
import it.polimi.amusic.service.EventBusinessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public abstract class PaymentMapperDecorator implements PaymentMapper {

    @Autowired
    EventBusinessService eventBusinessService;

    @Autowired
    PaymentMapper paymentMapper;

    @Override
    public Payment getDtoFromDocument(PaymentDocument paymentDocument) {
        final Payment dtoFromDocument = paymentMapper.getDtoFromDocument(paymentDocument);
        eventBusinessService.findEventById(paymentDocument.getIdEventDocument())
                .ifPresent(dtoFromDocument::setEvent);
        return dtoFromDocument;
    }
}
