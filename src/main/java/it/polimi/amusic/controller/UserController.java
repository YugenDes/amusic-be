package it.polimi.amusic.controller;

import it.polimi.amusic.model.dto.Friend;
import it.polimi.amusic.model.response.AMusicResponse;
import it.polimi.amusic.service.business.UserBusinessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserBusinessService userBusinessService;

    @GetMapping("/private/user/{idUserDocument}/friends")
    public AMusicResponse<List<Friend>> getUserFirends(@PathVariable("idUserDocument") String idDocument) {
        log.info("New request to /user/friends {}", idDocument);
        final List<Friend> friends = userBusinessService.getFriends(idDocument);
        return AMusicResponse.<List<Friend>>builder().body(friends).build();
    }

    @PostMapping("/private/user/addFirend")
    public AMusicResponse<List<Friend>> addFriend(@RequestBody String idUserFriendDocument) {
        log.info("New request to private/user/addFirend {}", idUserFriendDocument);
        final List<Friend> friends = userBusinessService.addFriend(idUserFriendDocument);
        return AMusicResponse.<List<Friend>>builder().body(friends).build();
    }

}
