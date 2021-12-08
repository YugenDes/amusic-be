package it.polimi.amusic.service.impl;

import com.google.api.client.util.ArrayMap;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.Firestore;
import com.google.common.collect.Sets;
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
import it.polimi.amusic.repository.EventRepository;
import it.polimi.amusic.repository.RoleRepository;
import it.polimi.amusic.repository.UserRepository;
import it.polimi.amusic.security.model.AuthProvider;
import it.polimi.amusic.service.UserBusinessService;
import it.polimi.amusic.utils.BFS.Graph;
import it.polimi.amusic.utils.BFS.Vertex;
import it.polimi.amusic.utils.BFS.WeightedEdge;
import it.polimi.amusic.utils.GcsRegexFilename;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

import static it.polimi.amusic.utils.SuggestedFriendBFS.findMaxCost;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserBusinessServiceImpl implements UserBusinessService {

    private final Firestore firestore;
    private final RoleRepository roleRepository;
    private final FirebaseAuth firebaseAuth;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final FileService fileService;
    private final UserMapperDecorator userMapper;
    private final EventMapperDecorator eventMapper;

    /**
     * Registro l'utente che atterra per la prima volta sulla piattaforma
     *
     * @param request RegistrationRequest
     * @return UserDocument
     * @throws FirebaseException
     */
    @Override
    public UserDocument registerUser(@NonNull RegistrationRequest request) throws FirebaseException {

        //Cerco se é gia presente nel db
        final Optional<UserDocument> byEmail = userRepository.findByEmail(request.getEmail());

        if (byEmail.isPresent()) {
            throw new UserAlreadyRegisteredException("Utente giá registrato {}", request.getEmail());
        }

        //Assegno il ruolo da USER
        final RoleDocument userRole = roleRepository.findByAuthority(RoleDocument.RoleEnum.USER);

        final UserRecord userFireBase;

        //Converto il token a UserRecord di firebase
        try {
            userFireBase = firebaseAuth.getUser(request.getFirebaseUidToken());
        } catch (FirebaseAuthException e) {
            log.error("Errore durante il recupero dell userFireBase {}", e.getLocalizedMessage());
            throw new FirebaseException("Errore durante il recupero dell userFireBase  {}", e.getLocalizedMessage());
        }

        //Converto dispalyName in name e surname
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

        //Nel caso di login da socil media prendo la foto
        String photoUrl = userFireBase.getPhotoUrl();

        //Se non é presente assegno l'immagine del profilo default
        if (StringUtils.isBlank(photoUrl)) {
            photoUrl = FileService.BASE_USER_PHOTO_URL;
        }

        //Creo l'utente
        UserDocument userDocument = new UserDocument()
                .setName(name.toUpperCase())
                .setSurname(surname.toUpperCase())
                .setDisplayName(userFireBase.getDisplayName())
                .setEmail(request.getEmail().toLowerCase())
                .setFirebaseUID(userFireBase.getUid())
                .setProvider(request.getProvider())
                .setAuthorities(Collections.singletonList(userRole))
                .setPhotoUrl(photoUrl)
                .setPhoneNumber(userFireBase.getPhoneNumber())
                .setCreateDate(Timestamp.ofTimeSecondsAndNanos(userFireBase.getUserMetadata().getCreationTimestamp() / 1000L, 0))
                .setLastLogin(Timestamp.ofTimeSecondsAndNanos(userFireBase.getUserMetadata().getLastRefreshTimestamp() / 1000L, 0))
                .setEmailVerified(userFireBase.isEmailVerified())
                .setEnabled(!userFireBase.isDisabled())
                .setAccountNonLocked(!userFireBase.isDisabled());

        try {
            return firestore.runTransaction(transaction -> {
                //Mando l'email di verifica email
                sendEmailVerificationLink(request.getEmail());
                return userRepository.save(userDocument);
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

    /**
     * L'utente loggato che ha acqusiato il biglietto viene registrato nei partecipanti dell' evento
     *
     * @param userIdDocument
     * @param eventIdDocument
     * @param visible         visibilita' partecipazione
     * @return Event
     * @throws FirestoreException
     */
    @Override
    public Event attendAnEvent(@NonNull String userIdDocument, @NonNull String eventIdDocument, @NonNull Boolean visible) throws FirestoreException {
        final UserDocument userDocument = userRepository.findById(userIdDocument)
                .orElseThrow(() -> new UserNotFoundException("L'utente {} non é stato trovato", userIdDocument));

        try {
            return firestore.runTransaction(transaction ->
                    //Trovo l' evento
                    eventRepository.findById(eventIdDocument)
                            .map(eventDocument -> {
                                //Aggiungo l' user ai parcetipanti
                                eventDocument.addPartecipantIfAbsent(new PartecipantDocument()
                                        .setId(userDocument.getId())
                                        .setName(userDocument.getName())
                                        .setSurname(userDocument.getSurname())
                                        .setVisible(visible)
                                        .setPhotoUrl(userDocument.getPhotoUrl()));
                                //Aggiungo l evento alla lista degli eventi dell utente
                                userDocument.addEventIfAbsent(eventDocument.getId());
                                //Persisto
                                userRepository.save(userDocument);
                                return eventRepository.save(eventDocument);
                            })
                            .map(eventMapper::getDtoFromDocument)
                            .orElseThrow(() -> new EventNotFoundException("Evento {} non trovato", eventIdDocument))).get();
        } catch (ExecutionException | InterruptedException e) {
            throw new FirestoreException("Impossibile effettuare la query {}", e.getLocalizedMessage());
        }
    }

    @Override
    public List<Friend> suggestedFriends() {
        //Recupero l'utenete loggato
        UserDocument userDocument = getUserFromSecurityContext();

        //Recupero gli eventi a cui ha partecipato l utente loggato
        final List<EventDocument> byParticipant = eventRepository.findByParticipant(userDocument.getId());

        //Se l'utente loggato non ha partecipato a nessun evento
        //Non suggerisco nessun amico
        if (byParticipant.size() == 0) {
            return new ArrayList<>();
        }

        /*
        Creo la mappa contenente tutti i partecipanti
        e la frequenza agli eventi a cui ha partecipato l'utente loggato
        */
        final Map<String, Long> idUserFrequencyOnAllEventsMap = byParticipant
                .stream()
                //Prendo ogni lista di partecipanti di ogni evento
                .map(EventDocument::getPartecipantsIds)
                //Appiattisco lo stream da Stream<List<String>> in Stream<String>
                .flatMap(Collection::stream)
                //Raggruppo le stringhe per la loro frequenza
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        //Creo una lista di key ordinata per value
        final List<String> idUserFrequencyOnAllEventsListOrdered = idUserFrequencyOnAllEventsMap.entrySet()
                .stream()
                .sorted((o1, o2) -> (int) (o1.getValue() - o2.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        //Set degli amici dell'utente
        final Set<String> idFriendsOfUserLogged = userDocument
                .getFirendList()
                .stream()
                .map(FriendDocument::getId)
                .collect(Collectors.toSet());

        /*
         * Faccio la differenza tra due insieme ,
         * il primo é l'insieme dei partecipanti agli eventi a cui ha partecipato l utente loggato
         * il secondo é l'insieme composto dagli amici del'utente loggato
         * Cosi da avere l'insieme degli amici che effettivamente sono stati ad un evento con l utente loggato
         */
        final Sets.SetView<String> intersection = Sets.intersection(Sets.newHashSet(idUserFrequencyOnAllEventsMap.keySet()), Sets.newHashSet(idFriendsOfUserLogged));

        final Map<String, List<String>> idFriendListFriend = intersection
                //Per ogni amico
                .stream()
                //Cerco l'utente
                .map(s -> userRepository.findById(s)
                        .orElse(null))
                .filter(Objects::nonNull)
                //Creo una mappa con chiave l'id e valore la lista id degli amici dell amico
                .collect(Collectors.toMap(UserDocument::getId, friendDocument -> friendDocument.getFirendList()
                        .stream()
                        //Rimuovo la bidirezione
                        .filter(friendDocument1 -> !friendDocument1.getId().equals(userDocument.getId()))
                        //Filtro gli amici degli amici prendondo solo quelli presenti all evento
                        .filter(friendDocument1 -> !idUserFrequencyOnAllEventsListOrdered.contains(friendDocument1.getId()))
                        .map(FriendDocument::getId)
                        .collect(Collectors.toList())));
        //Per ogni amico di amico presente all evento dell utente loggato
        final List<String> maxFrequency = idFriendListFriend.values()
                .stream()
                .flatMap(Collection::stream)
                //Rimuovo i duplicati
                .distinct()
                //Ordino la lista dal piu frequente al meno
                .sorted((o1, o2) -> (int) (idUserFrequencyOnAllEventsMap.get(o1) - idUserFrequencyOnAllEventsMap.get(o2)))
                //Prendo i primi 6
                .limit(6)
                .collect(Collectors.toList());

        List<Friend> suggestedFriend;

        //Mappo gli amici trovati in utenti e poi in DTO
        suggestedFriend = maxFrequency.stream()
                .map(s -> userRepository.findById(s).orElse(null))
                .filter(Objects::nonNull)
                .map(userMapper::mapUserDocumentToFriend).collect(Collectors.toList());

        //Se l'utente non ha nessun amico intermedio
        //Restituisco i primi 6 piu frequenti agli eventi
        if (suggestedFriend.size() == 0) {
            suggestedFriend = idUserFrequencyOnAllEventsListOrdered
                    .stream()
                    .limit(6)
                    .map(s -> userRepository.findById(s).orElse(null))
                    .filter(Objects::nonNull)
                    .map(userMapper::mapUserDocumentToFriend).collect(Collectors.toList());
        }
        return suggestedFriend;

    }


    @Deprecated
    public List<Friend> getSuggestedFriends() {

        UserDocument userDocument = getUserFromSecurityContext();

        final Set<String> idFirendsOfUserLogged = userDocument
                .getFirendList()
                .stream()
                .map(FriendDocument::getId)
                .collect(Collectors.toSet());

        final List<EventDocument> events = eventRepository.findByParticipant(userDocument.getId());

        final Map<String, Long> idFriendsOfUserLoggedInTheSameEventsWithFrequencyMap = events.stream().map(eventDocument ->
                        // Ottengo l'intersezione dei due set a un costo di O(n+m)
                        Sets.intersection(Sets.newHashSet(idFirendsOfUserLogged), Sets.newHashSet(eventDocument.getPartecipantsIds())))
                //Appitisco lo stream di liste innestate
                .flatMap(Collection::stream)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        final Map<String, List<String>> listIdFirendsOfFriendPresentInTheEvent = idFriendsOfUserLoggedInTheSameEventsWithFrequencyMap
                .entrySet()
                .stream()
                .map(idFriendFrequencyEntry -> userRepository.findById(idFriendFrequencyEntry.getKey()).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(UserDocument::getId, userFriend -> userFriend.getFirendList().stream().map(FriendDocument::getId).collect(Collectors.toList())));


        final Map<String, Map<String, Long>> idFriendsOfFriendsInTheSameEventsWithFrequencyMap = listIdFirendsOfFriendPresentInTheEvent
                .entrySet()
                .stream()
                //Per ogni Entry <id Amico,Lista id Amici dell Amico>
                .map(idFriendListIdFriend ->
                        //Creo una nuova entry con chiave l'id amico
                        //E come valore una Mappa contenente per ogni amico di amico la frequenza negli eventi
                        Map.entry(idFriendListIdFriend.getKey(), events.stream()
                                //TODO aggiungere controllo evento sia presente anche user document
                                .filter(eventDocument -> eventDocument.getPartecipantsIds().contains(idFriendListIdFriend.getKey()))
                                .map(eventDocument -> Sets.intersection(Sets.newHashSet(idFriendListIdFriend.getValue()), Sets.newHashSet(eventDocument.getPartecipantsIds()))
                                ).flatMap(Collection::stream)
                                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        final double numberOfEvents = events.size();

        final List<WeightedEdge> fristLayerOfFriends = idFriendsOfUserLoggedInTheSameEventsWithFrequencyMap.entrySet().stream().map(stringLongEntry -> {
            double probability = stringLongEntry.getValue() / numberOfEvents;
            return new WeightedEdge(userDocument.getId(), stringLongEntry.getKey(), probability);
        }).collect(Collectors.toList());

        final List<WeightedEdge> secondLayerOfFriend = idFriendsOfFriendsInTheSameEventsWithFrequencyMap
                .entrySet()
                .stream()
                .map(stringMapEntry ->
                        stringMapEntry.getValue().entrySet().stream().map(stringLongEntry -> {
                            double probabilityReferredToFristLayerFriend = (double) stringLongEntry.getValue() / idFriendsOfUserLoggedInTheSameEventsWithFrequencyMap.get(stringMapEntry.getKey());
                            return new WeightedEdge(stringMapEntry.getKey(), stringLongEntry.getKey(), probabilityReferredToFristLayerFriend);
                        }).collect(Collectors.toList())
                ).flatMap(Collection::stream)
                .collect(Collectors.toList());

        List<WeightedEdge> allEdge = new ArrayList<>(fristLayerOfFriends);
        allEdge.addAll(secondLayerOfFriend);

        Graph graph = new Graph(allEdge);

        final Vertex maxCost = findMaxCost(graph, userDocument.getId());

        return Arrays.asList(userMapper.mapUserDocumentToFriend(userRepository.findById(maxCost.vertex).orElseThrow()));
    }

    /**
     * Cambio Immagine del profilo
     *
     * @param resource
     * @return User
     */
    @Override
    public User changeProPic(@NonNull Resource resource) {
        UserDocument userDocument = getUserFromSecurityContext();

        //Se l' utente ha la foto
        String mediaLink = Optional.ofNullable(userDocument.getPhotoUrl())
                .map(s -> {
                    /*
                    Se la foto proviene dal bucket di GCP
                    E' possibile che se viene effettuato il login
                    Con un social media la foto venga presa da li
                    */
                    if (GcsRegexFilename.isFromGCS(s) && !s.equals(FileService.BASE_USER_PHOTO_URL)) {
                        //Cancello la foto dal bucket
                        fileService.deleteFile(userDocument.getPhotoUrl());
                    }
                    //Carico la foto
                    return fileService.uploadFile(resource);
                })
                //Altrimenti carico la foto e sostiuisco il link
                .orElseGet(() -> fileService.uploadFile(resource));
        userRepository.save(userDocument.setPhotoUrl(mediaLink));
        return userMapper.getDtoFromDocument(userDocument);
    }

    /**
     * Invia email per il reset password
     *
     * @return operationStatus
     */
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

    /**
     * Aggiunge l'utente selezionato alla lista degli amici dell utente loggato
     *
     * @param idUserFirendDocument
     * @return
     * @throws UserNotFoundException
     */
    @Override
    public List<Friend> addFriend(@NonNull String idUserFirendDocument) throws UserNotFoundException {
        final UserDocument userDocument = getUserFromSecurityContext();
        //Trovo l' utente da aggiungere agli amici
        userRepository.findById(idUserFirendDocument).map(friend -> {
            //Lo mappo
            final FriendDocument friendDocument = new FriendDocument()
                    .setFriendSince(Timestamp.now())
                    .setId(friend.getId());
            //Se non 'e gia presente lo aggiungo
            if (!userDocument.addFriendIfAbsent(friendDocument)) {
                //Se no exception
                throw new UserOperationException("E' giá tuo amico");
            }
            //Salvo l' utente
            return userRepository.save(userDocument);
            //Se non trovo l' utente da aggiungere exception
        }).orElseThrow(() -> new UserNotFoundException("Utente {} non trovato", idUserFirendDocument));
        //Ritorno la lista degli amici dell utente loggato
        return getFriends();
    }

    /**
     * Rimuove l' amico selezionato dalla lista degli amici dell utente loggato
     *
     * @param idUserFirendDocument
     * @return List<Friend>
     */
    @Override
    public List<Friend> removeFriend(@NonNull String idUserFirendDocument) {
        final UserDocument userDocument = getUserFromSecurityContext();
        userRepository.findById(idUserFirendDocument).map(friend -> {
            if (!userDocument.removeFriendIfPresent(friend.getId())) {
                throw new FriendNotFoundExcpetion("L'amico {} non é stato trovato tra gli amici dell'utente {}", friend.getId(), userDocument.getId());
            }
            return userRepository.save(userDocument);
        }).orElseThrow(() -> new UserNotFoundException("Utente {} non trovato", idUserFirendDocument));
        return getFriends();
    }

    /**
     * Aggiorna l' utente tramite la request
     *
     * @param request UpdateUserRequest
     * @return User
     */
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
        return userMapper.getDtoFromDocument(userRepository.save(userDocument));
    }

    /**
     * Permette la ricerca dell utente tramite nome e cognome
     * Se la ricerca contiene uno spazio ricerco per displayName che contiente sia nome che cognome
     * Se la ricerca contiente '@' ricerca per email
     *
     * @param param stringa di ricerca (Nome || Cognome || Email)
     * @return List<User>
     */
    @Override
    public List<User> searchUser(String param) {

        UserDocument userDocument = getUserFromSecurityContext();

        List<UserDocument> users = new ArrayList<>();

        if (param.contains(" ")) {
            users.addAll(userRepository.findByDisplayNameStartWith(param));
        } else if (param.contains("@")) {
            userRepository.findByEmail(param).ifPresent(users::add);
        } else {
            users.addAll(userRepository.findByNameStartWith(param));
            users.addAll(userRepository.findBySurnameStartWith(param));
        }

        return users.stream()
                //Rimuovo i possibili doppioni
                .distinct()
                //Rimuovo lo stesso utente che ha cercato
                .filter(usersDocuments -> !usersDocuments.getId().equals(userDocument.getId()))
                //Rimuovo gli utenti gia' amici
                .filter(usersDocuments -> userDocument.getFirendList().stream().noneMatch(friendDocument -> friendDocument.getId().equals(usersDocuments.getId())))
                .map(userMapper::getDtoFromDocument)
                .collect(Collectors.toList());
    }

    /**
     * Query per trovare l' utente tramite id
     *
     * @param id
     * @return User
     */
    @Override
    public User findById(String id) {
        return userRepository.findById(id).map(userMapper::getDtoFromDocument).orElseThrow();
    }


    /**
     * Invia l' email di conferma dell' account
     *
     * @param email
     * @throws AmusicEmailException
     */
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

    /**
     * Recupera l ' utente dal SecurityContext ovver l' utente loggato
     *
     * @return UserDocument
     */
    private UserDocument getUserFromSecurityContext() {
        final UserDocument principal = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getPrincipal)
                .map(o -> (UserDocument) o)
                .orElseThrow(() -> new MissingAuthenticationException("Non é presente l'oggetto Authentication nel SecurityContext"));
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new UserNotFoundException("L'utente {} non é stato trovato", principal.getId()));
    }
}
