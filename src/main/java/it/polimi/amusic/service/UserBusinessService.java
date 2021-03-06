package it.polimi.amusic.service;

import com.google.firebase.auth.FirebaseToken;
import it.polimi.amusic.exception.FirebaseException;
import it.polimi.amusic.exception.FirestoreException;
import it.polimi.amusic.model.document.UserDocument;
import it.polimi.amusic.model.dto.Event;
import it.polimi.amusic.model.dto.Friend;
import it.polimi.amusic.model.dto.User;
import it.polimi.amusic.model.request.RegistrationRequest;
import it.polimi.amusic.model.request.UpdateUserRequest;
import lombok.NonNull;
import org.springframework.core.io.Resource;

import java.util.List;

public interface UserBusinessService {

    UserDocument registerUser(@NonNull RegistrationRequest request) throws FirebaseException;

    UserDocument registerUser(@NonNull FirebaseToken request) throws FirebaseException;

    Event attendAnEvent(@NonNull String userIdDocument, @NonNull String eventIdDocument, @NonNull Boolean visible) throws FirestoreException;

    List<Friend> suggestedFriends();

    User changeProPic(@NonNull Resource resource);

    boolean changePassword();

    List<Friend> getFriends();

    List<Friend> addFriend(@NonNull String idUserFirendDocument);

    List<Friend> removeFriend(@NonNull String idUserFirendDocument);

    User updateUser(UpdateUserRequest request);

    List<User> searchUser(String param);

    User findById(String id);

}
