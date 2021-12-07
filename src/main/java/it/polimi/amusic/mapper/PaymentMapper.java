package it.polimi.amusic.mapper;

import it.polimi.amusic.model.document.PaymentDocument;
import it.polimi.amusic.model.dto.Payment;
import it.polimi.amusic.payment.model.PaymentProvider;
import it.polimi.amusic.utils.TimestampUtils;
import org.mapstruct.*;

import java.util.List;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        imports = {TimestampUtils.class, PaymentProvider.class})
@DecoratedWith(PaymentMapperDecorator.class)
public interface PaymentMapper {

    @Mapping(target = "datePayment", expression = "java(TimestampUtils.convertTimestampToLocalDateTime(paymentDocument.getDatePayment()))")
    @Mapping(target = "vendor", expression = "java(PaymentProvider.valueOf(paymentDocument.getVendor()))")
    Payment getDtoFromDocument(PaymentDocument paymentDocument);

    @Mapping(target = "datePayment", expression = "java(TimestampUtils.convertTimestampToLocalDateTime(paymentDocument.getDatePayment()))")
    @Mapping(target = "vendor", expression = "java(PaymentProvider.valueOf(paymentDocument.getVendor()))")
    @Named("getDtoNoEventFromDocument")
    Payment getDtoNoEventFromDocument(PaymentDocument paymentDocument);

    List<Payment> getDtosFromDocuments(List<PaymentDocument> paymentDocumentList);

}
