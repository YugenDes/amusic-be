package it.polimi.amusic.service.business.impl;

import com.google.api.client.util.ArrayMap;
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
import it.polimi.amusic.model.document.*;
import it.polimi.amusic.model.dto.Event;
import it.polimi.amusic.model.dto.Friend;
import it.polimi.amusic.model.dto.User;
import it.polimi.amusic.model.request.RegistrationRequest;
import it.polimi.amusic.model.request.UpdateUserRequest;
import it.polimi.amusic.security.model.AuthProvider;
import it.polimi.amusic.service.business.UserBusinessService;
import it.polimi.amusic.service.persistance.EventService;
import it.polimi.amusic.service.persistance.RoleService;
import it.polimi.amusic.service.persistance.UserService;
import it.polimi.amusic.utils.GcsRegexFilename;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.*;
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

        final UserRecord userFireBase;

        try {
            userFireBase = firebaseAuth.getUser(request.getFirebaseUidToken());
        } catch (FirebaseAuthException e) {
            log.error("Errore durante il recupero dell userFireBase {}", e.getLocalizedMessage());
            throw new FirebaseException("Errore durante il recupero dell userFireBase  {}", e.getLocalizedMessage());
        }

        String name;
        String surname;
        if (Objects.nonNull(userFireBase.getDisplayName())) {
            name = userFireBase.getDisplayName().split(" ")[0];
            surname = userFireBase.getDisplayName().split(" ")[1];
        } else {
            //Nel caso di GITHUB non prevede un displayName
            //Sara compito dell user aggiungere un nome e un cognome
            name = "   ";
            surname = "   ";
        }


        UserDocument userDocument = new UserDocument()
                .setName(name.toUpperCase())
                .setSurname(surname.toUpperCase())
                .setDisplayName(userFireBase.getDisplayName())
                .setEmail(request.getEmail().toLowerCase())
                .setFirebaseUID(userFireBase.getUid())
                .setProvider(request.getProvider())
                .setAuthorities(Collections.singletonList(userRole))
                .setPhotoUrl(userFireBase.getPhotoUrl())
                .setPhoneNumber(userFireBase.getPhoneNumber())
                .setCreateDate(Timestamp.ofTimeSecondsAndNanos(userFireBase.getUserMetadata().getCreationTimestamp() / 1000L, 0))
                .setLastLogin(Timestamp.ofTimeSecondsAndNanos(userFireBase.getUserMetadata().getLastRefreshTimestamp() / 1000L, 0))
                .setEmailVerified(userFireBase.isEmailVerified())
                .setEnabled(!userFireBase.isDisabled())
                .setAccountNonLocked(!userFireBase.isDisabled());

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
    public UserDocument registerUser(@NonNull FirebaseToken firebaseToken) throws FirebaseException {
        return this.registerUser(new RegistrationRequest()
                .setEmail(firebaseToken.getEmail())
                .setFirebaseUidToken(firebaseToken.getUid())
                .setProvider(AuthProvider.parseValueOf((String) ((ArrayMap) firebaseToken.getClaims().get("firebase")).get("sign_in_provider"))));
    }

    @Override
    public Event attendAnEvent(@NonNull String userIdDocument, @NonNull String eventIdDocument, @NonNull Boolean visible) throws FirestoreException {
        final UserDocument userDocument = userService.findById(userIdDocument)
                .orElseThrow(() -> new UserNotFoundException("L'utente {} non é stato trovato", userIdDocument));

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
    public List<Friend> suggestedFriends() {
        final UserDocument userDocument = getUserFromSecurityContext();

        final List<EventDocument> participatedEvents = eventService.findByParticipant(userDocument.getId());

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
    public User changeProPic(@NonNull Resource resource) {
        UserDocument userDocument = getUserFromSecurityContext();

        String mediaLink = Optional.ofNullable(userDocument.getPhotoUrl())
                .map(s -> {
                    if (GcsRegexFilename.isFromGCS(s)) {
                        fileService.deleteFile(userDocument.getPhotoUrl());
                    }
                    return fileService.uploadFile(resource);
                }).orElseGet(() -> fileService.uploadFile(resource));
        userService.save(userDocument.setPhotoUrl(mediaLink));
        return userMapper.getDtoFromDocument(userDocument);
    }

    @Override
    public boolean changePassword() {
        UserDocument userDocument = getUserFromSecurityContext();
        try {
            final String generatePasswordResetLink = firebaseAuth.generatePasswordResetLink(userDocument.getEmail());
            emailService.sendEmail(new EmailService.EmailRequest()
                    .setEmailTo(userDocument.getEmail())
                    .setSubject("Cambio password")
                    .setHtmlText(false)
                    .setText("Ecco il link per cambiare password: " + generatePasswordResetLink));
            return true;
        } catch (FirebaseAuthException e) {
            throw new FirebaseException("Errore durante la generazione del link per il reset password dell user {} : {}", userDocument.getEmail(), e.getLocalizedMessage());
        }
    }

    /**
     * Dato l'userIdDocument del profilo loggato
     * Ritorno la lista di amici di quel profilo
     *
     * @return List<Friend>
     */
    @Override
    public List<Friend> getFriends() throws UserNotFoundException {
        return getUserFromSecurityContext()
                .getFirendList()
                .stream()
                //Per ogni amico mappo il document riferito all amico appena trovato in dto
                .map(userMapper::mapUserFirendDocumentToFriend)
                .collect(Collectors.toList());
    }

    @Override
    public List<Friend> addFriend(@NonNull String idUserFirendDocument) throws UserNotFoundException {
        final UserDocument userDocument = getUserFromSecurityContext();
        userService.findById(idUserFirendDocument).map(friend -> {
            final FriendDocument friendDocument = new FriendDocument()
                    .setFriendSince(Timestamp.now())
                    .setId(friend.getId());
            userDocument.addFriendIfAbsent(friendDocument);
            return userService.save(userDocument);
        }).orElseThrow(() -> new UserNotFoundException("Utente {} non trovato", idUserFirendDocument));
        return getFriends();
    }

    @Override
    public List<Friend> removeFriend(@NonNull String idUserFirendDocument) {
        final UserDocument userDocument = getUserFromSecurityContext();
        userService.findById(idUserFirendDocument).map(friend -> {
            if (!userDocument.removeFriendIfPresent(friend.getId())) {
                throw new FriendNotFoundExcpetion("L'amico {} non é stato trovato tra gli amici dell'utente {}", friend.getId(), userDocument.getId());
            }
            return userService.save(userDocument);
        }).orElseThrow(() -> new UserNotFoundException("Utente {} non trovato", idUserFirendDocument));
        return getFriends();
    }

    @Override
    public User updateUser(UpdateUserRequest request) {
        UserDocument userDocument = getUserFromSecurityContext();
        if (Objects.isNull(userDocument.getName())
                && Objects.isNull(userDocument.getSurname())) {
            Assert.isTrue(!request.getName().isBlank(), "Il campo nome non puo essere vuoto");
            Assert.isTrue(!request.getSurname().isBlank(), "Il campo nome non puo essere vuoto");
            userMapper.updateUserDocumentFromUpdateRequest(userDocument, request);
        } else {
            //Per ignorare l'update sui campi name e surname
            request.setName(null);
            request.setSurname(null);
            userMapper.updateUserDocumentFromUpdateRequest(userDocument, request);
        }
        return userMapper.getDtoFromDocument(userService.save(userDocument));
    }

    @Override
    public List<User> searchUser(String param) {

        List<UserDocument> users = new ArrayList<>();

        if (param.contains(" ")) {
            users.addAll(userService.findByDisplayNameStartWith(param));
        } else {
            users.addAll(userService.findByNameStartWith(param));
            users.addAll(userService.findBySurnameStartWith(param));
        }

        return users.stream()
                .map(userMapper::getDtoFromDocument)
                .collect(Collectors.toList());
    }

    @Override
    public User findById(String id) {
        return userService.findById(id).map(userMapper::getDtoFromDocument).orElseThrow();
    }


    private void sendEmailVerificationLink(@NonNull String email) throws AmusicEmailException {
        try {
            final String emailVerificationLink = firebaseAuth.generateEmailVerificationLink(email);
            emailService.sendEmail(new EmailService.EmailRequest()
                    .setEmailTo(email)
                    .setSubject("Verifica Email")
                    .setHtmlText(false)
                    .setText("Ecco il link per verificare l'email: " + emailVerificationLink));
        } catch (FirebaseAuthException e) {
            log.error("Firebase Auth exception ", e);
            throw new AmusicEmailException("Errore durante l invio dell email {}", e.getLocalizedMessage());
        }
    }

    private UserDocument getUserFromSecurityContext() {
        final UserDocument principal = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getPrincipal)
                .map(o -> (UserDocument) o)
                .orElseThrow(() -> new MissingAuthenticationException("Non é presente l'oggetto Authentication nel SecurityContext"));
        return userService.findById(principal.getId())
                .orElseThrow(() -> new UserNotFoundException("L'utente {} non é stato trovato", principal.getId()));
    }
}
