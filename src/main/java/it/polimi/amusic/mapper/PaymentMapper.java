package it.polimi.amusic.mapper;

import com.firebase.geofire.core.GeoHash;
import com.google.cloud.firestore.GeoPoint;
import it.polimi.amusic.model.document.PaymentDocument;
import it.polimi.amusic.model.dto.Payment;
import it.polimi.amusic.utils.TimestampUtils;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, uses = TimestampUtils.class)
@DecoratedWith(PaymentMapperDecorator.class)
public interface PaymentMapper {

    @Mapping(target = "datePayment" ,expression = "java(TimestampUtils.convertTimestampToLocalDateTime(paymentDocument.getDatePayment()))")
    @Mapping(target = "vendor" ,expression = "java(PaymentProvider.valueOf(paymentDocument.getVendor()))")
    Payment getDtoFromDocument(PaymentDocument paymentDocument);

    List<Payment> getDtosFromDocuments(List<PaymentDocument> paymentDocumentList);

}
