package it.polimi.amusic.controller;

import it.polimi.amusic.exception.FileSizeLimitExceedException;
import it.polimi.amusic.model.document.UserDocument;
import it.polimi.amusic.model.dto.Friend;
import it.polimi.amusic.model.dto.User;
import it.polimi.amusic.model.request.AddFriendRequest;
import it.polimi.amusic.model.request.RemoveFriendRequest;
import it.polimi.amusic.model.request.UpdateUserRequest;
import it.polimi.amusic.model.response.AMusicResponse;
import it.polimi.amusic.service.business.UserBusinessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserBusinessService userBusinessService;
    private final Long byteSize = 1048576L;
    @Value("${file.size.limit.byte}")
    private Long fileSizeLimitInByte;

    @GetMapping("/profile")
    public AMusicResponse<User> getUser() {
        log.info("New request to /profile {}", getUserIdDocumentFromSecurityContext());
        final User byId = userBusinessService.findById(getUserIdDocumentFromSecurityContext());
        return AMusicResponse.<User>builder().body(byId).build();
    }

    @GetMapping("/private/user/friends")
    public AMusicResponse<List<Friend>> getUserFirends() {
        log.info("New request to /user/friends {}", getUserIdDocumentFromSecurityContext());
        final List<Friend> friends = userBusinessService.getFriends();
        return AMusicResponse.<List<Friend>>builder().body(friends).build();
    }

    @GetMapping("/private/user/suggestedFriends")
    public AMusicResponse<List<Friend>> getSuggestedFriends() {
        log.info("New request to /private/user/suggestedFriends {}", getUserIdDocumentFromSecurityContext());
        final List<Friend> friends = userBusinessService.suggestedFriends();
        return AMusicResponse.<List<Friend>>builder().body(friends).build();
    }

    @PostMapping("/private/user/addFriend")
    public AMusicResponse<List<Friend>> addFriend(@RequestBody AddFriendRequest request) {
        log.info("New request to private/user/addFirend {} , {}", request.getIdUserFriendDocument(), getUserIdDocumentFromSecurityContext());
        final List<Friend> friends = userBusinessService.addFriend(request.getIdUserFriendDocument());
        return AMusicResponse.<List<Friend>>builder().body(friends).build();
    }

    @DeleteMapping("/private/user/removeFriend")
    public AMusicResponse<List<Friend>> removeFriend(@RequestBody RemoveFriendRequest request) {
        log.info("New request to private/user/removeFriend {} , {}", request.getIdUserFriendDocument(), getUserIdDocumentFromSecurityContext());
        final List<Friend> friends = userBusinessService.removeFriend(request.getIdUserFriendDocument());
        return AMusicResponse.<List<Friend>>builder().body(friends).build();
    }

    @PutMapping("/private/user/update")
    public AMusicResponse<User> updateUser(@RequestBody UpdateUserRequest updateUserRequest) {
        log.info("New request to /private/user/update {} , {}", updateUserRequest, getUserIdDocumentFromSecurityContext());
        final User user = userBusinessService.updateUser(updateUserRequest);
        return AMusicResponse.<User>builder().body(user).build();
    }

    @GetMapping("/private/user")
    public AMusicResponse<List<User>> searchUser(@RequestParam("search") String param) {
        log.info("New request to /private/user?search={} , {}", param, getUserIdDocumentFromSecurityContext());
        final List<User> users = userBusinessService.searchUser(param);
        return AMusicResponse.<List<User>>builder().body(users).build();
    }

    @PostMapping(value = "/private/user/uploadPhoto", produces = "application/json")
    public AMusicResponse<User> uploadPhoto(@RequestParam("file") MultipartFile multipartFile) {
        if (multipartFile.getSize() > fileSizeLimitInByte) {
            throw new FileSizeLimitExceedException("Il file supera i limiti di 2MB : {}MB", multipartFile.getSize() / byteSize);
        }
        log.info("New request to /private/user/uploadPhoto {} size {}", getUserIdDocumentFromSecurityContext(), multipartFile.getSize());
        final User user = userBusinessService.changeProPic(multipartFile.getResource());
        return AMusicResponse.<User>builder().body(user).build();
    }


    @PostMapping("/private/user/changePassword")
    public AMusicResponse<Boolean> changePassword() {
        log.info("New request to /private/user/changePassword {} ", getUserIdDocumentFromSecurityContext());
        final boolean response = userBusinessService.changePassword();
        return AMusicResponse.<Boolean>builder().body(response).build();
    }

    private String getUserIdDocumentFromSecurityContext() {
        return ((UserDocument) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
    }
}
