package it.polimi.amusic.controller;

import it.polimi.amusic.exception.FileSizeLimitExceedException;
import it.polimi.amusic.model.document.UserDocument;
import it.polimi.amusic.model.dto.Friend;
import it.polimi.amusic.model.dto.User;
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

    @GetMapping("/private/user/friends")
    public AMusicResponse<List<Friend>> getUserFirends() {
        final UserDocument principal = (UserDocument) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("New request to /user/friends {}", principal.getId());
        final List<Friend> friends = userBusinessService.getFriends();
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
    public AMusicResponse<List<User>> searchUser(@RequestParam("search") String param) {
        log.info("New request to /private/user?search= {}", param);
        final List<User> users = userBusinessService.searchUser(param);
        return AMusicResponse.<List<User>>builder().body(users).build();
    }

    @PostMapping("/private/user/uploadPhoto")
    public AMusicResponse<User> uploadPhoto(@RequestParam("file") MultipartFile multipartFile) {
        if (multipartFile.getSize() > fileSizeLimitInByte) {
            throw new FileSizeLimitExceedException("Il file supera i limiti di 2MB : {}MB", multipartFile.getSize() / byteSize);
        }
        final UserDocument principal = (UserDocument) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("New request to /private/user/uploadPhoto {} size {}", principal.getId(), multipartFile.getSize());
        final User user = userBusinessService.changeProPic(multipartFile.getResource());
        return AMusicResponse.<User>builder().body(user).build();
    }
}
