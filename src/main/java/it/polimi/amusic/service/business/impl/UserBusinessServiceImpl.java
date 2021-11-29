package it.polimi.amusic.service.business.impl;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import it.polimi.amusic.exception.*;
import it.polimi.amusic.external.email.EmailService;
import it.polimi.amusic.external.gcs.FileService;
import it.polimi.amusic.mapper.EventMapperDecorator;
import it.polimi.amusic.mapper.UserMapperDecorator;
import it.polimi.amusic.model.document.FriendDocument;
import it.polimi.amusic.model.document.PartecipantDocument;
import it.polimi.amusic.model.document.RoleDocument;
import it.polimi.amusic.model.document.UserDocument;
import it.polimi.amusic.model.dto.Event;
import it.polimi.amusic.model.dto.Friend;
import it.polimi.amusic.model.request.RegistrationRequest;
import it.polimi.amusic.security.model.AuthProvider;
import it.polimi.amusic.service.business.UserBusinessService;
import it.polimi.amusic.service.persistance.EventService;
import it.polimi.amusic.service.persistance.RoleService;
import it.polimi.amusic.service.persistance.UserService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserBusinessServiceImpl implements UserBusinessService {

    private final Firestore firestore;
    private final RoleService roleService;
    private final FirebaseAuth firebaseAuth;
    private final EmailService emailService;
    private final UserService userService;
    private final EventService eventService;
    private final FileService fileService;
    private final UserMapperDecorator userMapper;
    private final EventMapperDecorator eventMapper;

    @Override
    public UserDocument registerUser(@NonNull RegistrationRequest request) throws FirebaseException {

        final Optional<UserDocument> byEmail = userService.findByEmail(request.getEmail());

        if (byEmail.isPresent()) {
            throw new UserAlreadyRegisteredException("Utente giá registrato {}", request.getEmail());
        }

        final RoleDocument userRole = roleService.findByAuthority(RoleDocument.RoleEnum.USER);

//        final UserRecord.CreateRequest createRequest = new UserRecord.CreateRequest()
//                .setEmail(request.getEmail())
//                .setPassword(request.getPassword())
//                .setDisplayName(request.getName() + " " + request.getSurname())
//
//        final UserRecord userFireBase;
//
//        try {
//            userFireBase = firebaseAuth.createUser(createRequest);
//        } catch (FirebaseAuthException e) {
//            log.error("Errore durante la creazione dell userFireBase {}", e.getLocalizedMessage());
//            throw new FirebaseException("Errore durante la creazione dell userFireBase {}", e.getLocalizedMessage());
//        }

        final UserRecord userFireBase;

        try {
            userFireBase = firebaseAuth.getUser(request.getFirebaseUidToken());
        } catch (FirebaseAuthException e) {
            log.error("Errore durante il recupero dell userFireBase {}", e.getLocalizedMessage());
            throw new FirebaseException("Errore durante il recupero dell userFireBase  {}", e.getLocalizedMessage());
        }

        UserDocument userDocument = new UserDocument()
                .setDisplayName(userFireBase.getDisplayName())
                .setEmail(request.getEmail())
                .setFirebaseUID(request.getFirebaseUidToken())
                .setProvider(request.getProvider())
                .setAuthorities(Collections.singletonList(userRole))
                .setPhotoUrl(userFireBase.getPhotoUrl())
                .setPhoneNumber(userFireBase.getPhoneNumber())
                .setCreateDate(Timestamp.ofTimeMicroseconds(userFireBase.getUserMetadata().getCreationTimestamp()))
                .setLastLogin(Timestamp.ofTimeMicroseconds(userFireBase.getUserMetadata().getLastRefreshTimestamp()))
                .setEmailVerified(userFireBase.isEmailVerified())
                .setEnabled(userFireBase.isDisabled())
                .setAccountNonLocked(userFireBase.isDisabled());

        try {
            return firestore.runTransaction(transaction -> {
                sendEmailVerificationLink(request.getEmail());
                return userService.save(userDocument);
            }).get();

        } catch (AmusicEmailException | ExecutionException | InterruptedException e) {
            throw new RegistrationException("Errore durante la registrazione {}", e.getLocalizedMessage());
        }
    }

    @Override
    public UserDocument registerUser(@NonNull FirebaseToken request) throws FirebaseException {
        return this.registerUser(new RegistrationRequest()
                .setEmail(request.getEmail())
                .setName(request.getName())
                .setFirebaseUidToken(request.getUid())
                .setSurname(request.getName())
                .setProvider(AuthProvider.valueOf(request.getIssuer())));
    }

    @Override
    public Event attendAnEvent(@NonNull String userIdDocument, @NonNull String eventIdDocument, @NonNull Boolean visible) throws FirestoreException {
        final UserDocument userDocument = userService.findById(userIdDocument)
                .orElseThrow(() -> new UserNotFoundException("User {} non trovato", userIdDocument));
        try {
            return firestore.runTransaction(transaction ->
                    eventService.findById(eventIdDocument)
                            .map(eventDocument -> {
                                eventDocument.addPartecipantIfAbsent(new PartecipantDocument()
                                        .setId(userDocument.getId())
                                        .setName(userDocument.getName())
                                        .setSurname(userDocument.getSurname())
                                        .setVisible(visible)
                                        .setPhotoUrl(userDocument.getPhotoUrl()));
                                userDocument.addEventIfAbsent(eventDocument.getId());
                                userService.save(userDocument);
                                return eventService.save(eventDocument);
                            })
                            .map(eventMapper::getDtoFromDocument)
                            .orElseThrow(() -> new EventNotFoundException("Evento {} non trovato", eventIdDocument))).get();
        } catch (ExecutionException | InterruptedException e) {
            throw new FirestoreException("Impossibile effettuare la query {}", e.getLocalizedMessage());
        }
    }

    @Override
    public List<UserDocument> suggestedFriends(@NonNull String idUserDocument) {
//        final List<EventDocument> participatedEvents = userService.findById(idUserDocument).map(userDocument ->
//                eventService.findByParticipant(userDocument.getId())
//                        .stream()
//                        .sorted(Comparator.comparing(EventDocument::getEventDate))
//                        .limit(10)
//                        .collect(Collectors.toList())
//        ).orElseThrow(() -> new UserNotFoundException("Utente con idDocument {} non trovato", idUserDocument));
//        Map<String, List<String>> relationship = new ConcurrentHashMap<>();
//
//        participatedEvents
//                .parallelStream()
//                .forEach(eventDocument ->
//                        relationship.putAll(eventDocument.getPartecipants()
//                                .entrySet()
//                                .stream()
//                                .filter(userIdDocument -> !relationship.containsKey(userIdDocument))
//                                .map(stringBooleanEntry ->
//                                        userService.findById(stringBooleanEntry.getKey())
//                                                .orElseGet(() -> {
//                                                    log.warn("User {} non trovato", stringBooleanEntry.getKey());
//                                                    return null;
//                                                }))
//                                .filter(Objects::nonNull)
//                                .collect(Collectors.toMap(UserDocument::getId, UserDocument::getFirendList))));
//
//        SuggestFriendsDFS<String> core = new SuggestFriendsDFS<>();
//
//        relationship.forEach((user, friends) -> friends.forEach(friend -> core.addFriendship(user, friend)));
//
//        return core.getSuggestedFriends(idUserDocument, 2)
//                .stream()
//                .map(id -> userService.findById(id)
//                        .orElseGet(() -> {
//                            log.warn("User {} non trovato", id);
//                            return null;
//                        }))
//                .filter(Objects::nonNull)
//                .collect(Collectors.toList());
        return null;
    }

    @Override
    public UserDocument changeProPic(@NonNull String userIdDocument, @NonNull Resource resource) {
        final UserDocument userDocument = userService.findById(userIdDocument)
                .orElseThrow(() -> new UserNotFoundException("L'utente {} non é stato trovato", userIdDocument));
        if (fileService.deleteFile(userDocument.getPhotoUrl())) {
            final String fileName = fileService.uploadFile(resource);
            return userService.save(userDocument.setPhotoUrl(fileName));
        }
        return userDocument;
    }

    @Override
    public boolean changePassword(@NonNull String email) {
        try {
            final String generatePasswordResetLink = firebaseAuth.generatePasswordResetLink(email);
            emailService.sendEmail(new EmailService.EmailRequest()
                    .setEmailTo(email)
                    .setSubject("Cambio password")
                    .setText("Ecco il link per cambiare password: " + generatePasswordResetLink));
            return true;
        } catch (FirebaseAuthException e) {
            throw new FirebaseException("Errore durante la generazione del link per il reset password dell user {} : {}", email, e.getLocalizedMessage());
        }
    }

    /**
     * Dato l'userIdDocument del profilo loggato
     * Ritorno la lista di amici di quel profilo
     *
     * @param idUserDocument id document
     * @return List<Friend>
     */
    @Override
    public List<Friend> getFriends(@NonNull String idUserDocument) throws UserNotFoundException {
        return userService.findById(idUserDocument)
                .map(userDocument -> userDocument
                        .getFirendList()
                        .stream()
                        //Per ogni amico mappo il document riferito all amico appena trovato in dto
                        .map(userMapper::mapUserFirendDocumentToFriend)
                        .collect(Collectors.toList()))
                .orElseThrow(() -> new UserNotFoundException("Utente {} non trovato", idUserDocument));
    }

    @Override
    public List<Friend> addFriend(@NonNull String idUserFirendDocument) throws UserNotFoundException {
        final UserDocument principal = (UserDocument) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        final UserDocument userDocument = userService.findById(principal.getId()).map(user ->
                userService.findById(idUserFirendDocument).map(friend -> {
                    final FriendDocument friendDocument = new FriendDocument()
                            .setFriendSince(Timestamp.now())
                            .setId(friend.getId());
                    user.addFriendIfAbsent(friendDocument);
                    return userService.save(user);
                }).orElseThrow(() -> new UserNotFoundException("Utente {} non trovato", idUserFirendDocument))
        ).orElseThrow(() -> new UserNotFoundException("Utente {} non trovato", principal.getId()));
        return getFriends(userDocument.getId());
    }


    private void sendEmailVerificationLink(@NonNull String email) throws AmusicEmailException {
        try {
            final String emailVerificationLink = firebaseAuth.generateEmailVerificationLink(email);
            emailService.sendEmail(new EmailService.EmailRequest()
                    .setEmailTo(email)
                    .setSubject("Verifica Email")
                    .setText("Ecco il link per verificare l'email: " + emailVerificationLink));
        } catch (FirebaseAuthException e) {
            log.error("Firebase Auth exception ", e);
            throw new AmusicEmailException("Errore durante l invio dell email {}", e.getLocalizedMessage());
        }
    }
}
