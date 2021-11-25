package it.polimi.amusic.controller;

import it.polimi.amusic.exception.AmusicEmailException;
import it.polimi.amusic.exception.EventNotFoundException;
import it.polimi.amusic.exception.PurchaseNotFoundException;
import it.polimi.amusic.exception.UserNotFoundException;
import it.polimi.amusic.model.response.AMusicResponse;
import it.polimi.amusic.model.response.Message;
import it.polimi.amusic.model.response.MessageType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@ControllerAdvice
public class AMusicExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({
            UserNotFoundException.class,
            EventNotFoundException.class,
            PurchaseNotFoundException.class
    })
    protected ResponseEntity<AMusicResponse<Object>> handleNotFoundException(Exception ex, WebRequest request) {
        log.error(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(AMusicResponse.builder()
                .message(getMessageFromRuntimeException(ex, HttpStatus.NOT_FOUND)).build());
    }

    @ExceptionHandler({Exception.class})
    protected ResponseEntity<AMusicResponse<Object>> handleGenericException(Exception ex, WebRequest request) {
        log.error(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(AMusicResponse.builder()
                .message(getMessageFromRuntimeException(ex, HttpStatus.INTERNAL_SERVER_ERROR)).build());
    }


    private Message getMessageFromRuntimeException(Exception ex, HttpStatus httpStatus) {
        return Message.builder().text(ex.getLocalizedMessage()).code(httpStatus.name()).messageType(MessageType.error).build();
    }


}
