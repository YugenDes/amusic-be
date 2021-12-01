package it.polimi.amusic.controller;

import it.polimi.amusic.model.dto.Friend;
import it.polimi.amusic.model.dto.User;
import it.polimi.amusic.model.request.UpdateUserRequest;
import it.polimi.amusic.model.response.AMusicResponse;
import it.polimi.amusic.service.business.UserBusinessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @PutMapping("/private/user/update")
    public AMusicResponse<User> updateUser(@RequestBody UpdateUserRequest request) {
        log.info("New request to /private/user/update {}", request);
        final User user = userBusinessService.updateUser(request);
        return AMusicResponse.<User>builder().body(user).build();
    }

    @GetMapping("/private/user")
    public AMusicResponse<List<User>> updateUser(@RequestParam("search") String param) {
        log.info("New request to /private/user?search= {}", param);
        final List<User> users = userBusinessService.searchUser(param);
        return AMusicResponse.<List<User>>builder().body(users).build();
    }

}
