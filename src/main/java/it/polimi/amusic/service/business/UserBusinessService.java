package it.polimi.amusic.service.business;

import com.google.firebase.auth.FirebaseToken;
import it.polimi.amusic.model.dto.Event;
import it.polimi.amusic.model.dto.User;
import lombok.NonNull;
import it.polimi.amusic.exception.FirebaseException;
import it.polimi.amusic.exception.FirestoreException;
import it.polimi.amusic.model.document.EventDocument;
import it.polimi.amusic.model.document.UserDocument;
import it.polimi.amusic.model.dto.Friend;
import it.polimi.amusic.model.request.LoginRequest;
import it.polimi.amusic.model.request.RegistrationRequest;
import lombok.NonNull;
import org.springframework.core.io.Resource;

import java.util.List;

public interface UserBusinessService {

    UserDocument registerUser(@NonNull RegistrationRequest request) throws FirebaseException;

    UserDocument registerUser(@NonNull FirebaseToken request) throws FirebaseException;

    String login(LoginRequest loginRequest);

    Event attendAnEvent(@NonNull String userIdDocument, @NonNull String eventIdDocument, @NonNull Boolean visible) throws FirestoreException;

    List<UserDocument> suggestedFriends(@NonNull String idUserDocument);

    UserDocument changeProPic(@NonNull String userIdDocument, @NonNull Resource resource);

    boolean changePassword(@NonNull String email);

    List<Friend> getFriends(@NonNull String idUserDocument);

    List<Friend> addFriend(@NonNull String idUserFirendDocument);



}
