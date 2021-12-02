package it.polimi.amusic.controller;

import it.polimi.amusic.model.document.UserDocument;
import it.polimi.amusic.model.request.RegistrationRequest;
import it.polimi.amusic.service.business.UserBusinessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("public")
public class RegistrationController {

    private final UserBusinessService userBusinessService;

    @PostMapping(value = "/public/register")
    public ResponseEntity<UserDocument> registration(@RequestBody RegistrationRequest request) {
        log.info("new POST request to /register body:{}", request);
        final UserDocument userDocument = userBusinessService.registerUser(request);
        return ResponseEntity.ok(userDocument);
    }
}
