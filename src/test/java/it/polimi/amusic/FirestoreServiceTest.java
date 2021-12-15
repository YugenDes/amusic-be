package it.polimi.amusic;

import com.firebase.geofire.core.GeoHash;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.GeoPoint;
import it.polimi.amusic.external.gcs.FileService;
import it.polimi.amusic.model.document.EventDocument;
import it.polimi.amusic.model.document.RoleDocument;
import it.polimi.amusic.model.document.UserDocument;
import it.polimi.amusic.model.dto.Event;
import it.polimi.amusic.model.dto.Friend;
import it.polimi.amusic.repository.EventRepository;
import it.polimi.amusic.repository.RoleRepository;
import it.polimi.amusic.repository.UserRepository;
import it.polimi.amusic.security.model.AuthProvider;
import it.polimi.amusic.service.EventBusinessService;
import it.polimi.amusic.service.UserBusinessService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@SpringBootTest
@Slf4j
class FirestoreServiceTest {

    @Autowired
    EventRepository eventRepository;

    @Autowired
    EventBusinessService eventBusinessService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserBusinessService userBusinessService;

    @Autowired
    FileService fileService;

    @Autowired
    Firestore firestore;

    @Autowired
    RoleRepository roleRepository;


    void contextLoads() {
        final UserDocument userDocument = userRepository.findById("riesgZgx9d4CpW8yTose").orElseThrow();
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDocument, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }


    @Test
    void saveNewEvent() {
        final EventDocument qube = eventRepository.save(new EventDocument()
                .setEventName("Qube")
                .setEventDate(LocalDateTime.of(2021, 12, 13, 23, 00))
                .setEventDatePublished(LocalDateTime.now())
                .setDescription("Qube")
                .setGeoPoint(new GeoPoint(41.897845844351465, 12.540060246022259))
                .setGeoHash(new GeoHash(41.897845844351465, 12.540060246022259).getGeoHashString())
                .setMaxPartecipants(100)
                .setTicketPrice(12.5D));

        final EventDocument piper = eventRepository.save(new EventDocument()
                .setEventName("Piper Club")
                .setEventDate(LocalDateTime.of(2021, 12, 14, 23, 00))
                .setEventDatePublished(LocalDateTime.now())
                .setDescription("Piper Club")
                .setGeoPoint(new GeoPoint(41.919022344359966, 12.501152542236955))
                .setGeoHash(new GeoHash(41.919022344359966, 12.501152542236955).getGeoHashString())
                .setMaxPartecipants(200)
                .setTicketPrice(20D));

        final EventDocument meeting = eventRepository.save(new EventDocument()
                .setEventName("Meeting Place Bar")
                .setEventDate(LocalDateTime.of(2021, 12, 13, 19, 00))
                .setEventDatePublished(LocalDateTime.now())
                .setDescription("Meeting Place Bar")
                .setGeoPoint(new GeoPoint(41.91382665676414, 12.52141626922569))
                .setGeoHash(new GeoHash(41.91382665676414, 12.52141626922569).getGeoHashString())
                .setMaxPartecipants(2)
                .setTicketPrice(9D));

        final EventDocument tBar = eventRepository.save(new EventDocument()
                .setEventName("T Bar Ostiense")
                .setEventDate(LocalDateTime.of(2021, 12, 15, 22, 30))
                .setEventDatePublished(LocalDateTime.now())
                .setDescription("T Bar Ostiense")
                .setGeoPoint(new GeoPoint(41.863255373433056, 12.478290459422745))
                .setGeoHash(new GeoHash(41.863255373433056, 12.478290459422745).getGeoHashString())
                .setMaxPartecipants(100)
                .setTicketPrice(18.5D));

        Assertions.assertNotNull(tBar.getId());
        Assertions.assertNotNull(meeting.getId());
        Assertions.assertNotNull(piper.getId());
        Assertions.assertNotNull(qube.getId());

    }

    @Test
    void saveNewEventSingle() {
        final EventDocument event = eventRepository.save(new EventDocument()
                .setEventName("Studio 7")
                .setEventDate(LocalDateTime.of(2021, 12, 15, 21, 00))
                .setEventDatePublished(LocalDateTime.now())
                .setDescription("Musica eclettica in un locale multisala alla moda rivolto ad over 30, con divani bianchi e pareti a specchio. Puro divertimento per gli over del nostro quartiere. Vi aspettano giochi di vario tipo, bella musica anni 90 e drink da urlo!\n" +
                        "Evento della serata ore 21.00 gara di coppia a... venite a scoprirlo!")
                .setGeoPoint(new GeoPoint(41.973516471434536, 12.442224500451731))
                .setGeoHash(new GeoHash(41.973516471434536, 12.442224500451731).getGeoHashString())
                .setMaxPartecipants(100)
                .setAddress("Via di Grottarossa, 175, 00189 Roma RM")
                .setPhoneNumber("+393356581983")
                .setTicketPrice(18.5D));
        Assertions.assertNotNull(event.getId());
    }


    @Test
    void findEvent() {
        contextLoads();
        final List<Event> byEventDate = eventBusinessService.findByEventDate(LocalDate.of(2021, 12, 15));
        Assertions.assertFalse(byEventDate.isEmpty(), "Deve esserci un evento");
    }

    @Test
    void findEventByPartecipantsEmail() {
        contextLoads();
        String eventDocumentId = "DlDXMobm0uoGrsV4uqXg";
        userRepository.findById("2BMY2q1MKsOP54r5KV2N")
                .map(userDocument -> userBusinessService.attendAnEvent(userDocument.getId(), eventDocumentId, true))
                .map(Event::getPartecipants)
                .orElseThrow();

        final EventDocument eventDocument1 = eventRepository
                .findByParticipant("2BMY2q1MKsOP54r5KV2N")
                .stream()
                .filter(eventDocument -> eventDocument.getId().equals(eventDocumentId))
                .findFirst()
                .orElseThrow();

        Assertions.assertEquals(eventDocument1.getId(), eventDocumentId, "Impossilbe non trovare l'evento dato un partecipante appena iscritto");
    }

    @SneakyThrows
    @Test
    void findGeoPoint() {
        contextLoads();
        //final GeoPoint casa = new GeoPoint(41.908761427826434, 12.545459410032102);
        String eventDocumentId = "icuXDgj7mPkhY9Rln5cf";
        final EventDocument eventDocument = eventRepository.findById(eventDocumentId).orElseThrow();
        final List<Event> byGeoPointNearMe = eventBusinessService.findByGeoPointNearMe(eventDocument.getGeoPoint(), 1);
        final Boolean presente = byGeoPointNearMe
                .stream()
                .anyMatch(event -> event.getId().equals(eventDocumentId));
        Assertions.assertTrue(presente, "Non é possibile non trovare un evento vicono a esso dato lo stesso evento");
    }


    @Test
    void saveUserTest() {

        UserDocument userDocument = new UserDocument()
                .setName("Andrea")
                .setSurname("Messina")
                .setEmail("andrea@test.it")
                .setProvider(AuthProvider.AMUSIC)
                .setCreateDate(Timestamp.now())
                .setEnabled(true)
                .setDisplayName("YugenDesu")
                .setLastLogin(Timestamp.now())
                .setAuthorities(Collections.singletonList(roleRepository.findByAuthority(RoleDocument.RoleEnum.USER)))
                .setEmailVerified(false);


        UserDocument userDocument1 = new UserDocument()
                .setName("Alberto")
                .setSurname("Manu")
                .setEmail("albi@test.it")
                .setProvider(AuthProvider.FACEBOOK)
                .setCreateDate(Timestamp.now())
                .setEnabled(true)
                .setDisplayName("AlbiManu")
                .setLastLogin(Timestamp.now())
                .setAuthorities(Collections.singletonList(roleRepository.findByAuthority(RoleDocument.RoleEnum.USER)))
                .setEmailVerified(false);

        UserDocument admin = new UserDocument()
                .setName("ADMIN")
                .setSurname("TEST")
                .setEmail("admin@admin.it")
                .setProvider(AuthProvider.AMUSIC)
                .setCreateDate(Timestamp.now())
                .setEnabled(true)
                .setDisplayName("ADMIN")
                .setLastLogin(Timestamp.now())
                .setAuthorities(Collections.singletonList(roleRepository.findByAuthority(RoleDocument.RoleEnum.ADMIN)))
                .setEmailVerified(false);


        final UserDocument save = userRepository.save(userDocument);
        final UserDocument save1 = userRepository.save(userDocument1);
        final UserDocument save3 = userRepository.save(admin);

        Assertions.assertNotNull(save.getId());
        Assertions.assertNotNull(save1.getId());
        Assertions.assertNotNull(save3.getId());
    }

    @Test
    void addFriendTest() {
        contextLoads();
        final List<Friend> friends = userBusinessService.addFriend("JMapVlHllNOFuE6CrqmG");
        final boolean isPresent = friends.stream().anyMatch(friend -> friend.getId().equals("JMapVlHllNOFuE6CrqmG"));
        Assertions.assertTrue(isPresent, "Non é possibile non trovare un amico appena aggiunto");
    }

    @Test
    void removeFirendTest() {
        contextLoads();
        final List<Friend> friends = userBusinessService.removeFriend("JMapVlHllNOFuE6CrqmG");
        final boolean isNotPresent = friends.stream().noneMatch(friend -> friend.getId().equals("JMapVlHllNOFuE6CrqmG"));
        Assertions.assertTrue(isNotPresent, "Non é possibile  trovare un amico appena rimosso");
    }


    @Test
    void attendEvent() {
        contextLoads();
        final Event event = userBusinessService.attendAnEvent("puLxmw6ozrb7X7IuVWkr", "DlDXMobm0uoGrsV4uqXg", true);
        final boolean isPresent = event.getPartecipants().stream().anyMatch(partecipant -> partecipant.getId().equals("puLxmw6ozrb7X7IuVWkr"));
        Assertions.assertTrue(isPresent, "Non é possibile non trovare un partecipante appena aggiunto");
    }


    @Test
    void getUserEventHistory() {
        contextLoads();
        attendEvent();
        final boolean isPresent = eventBusinessService
                .getUserEventHistory()
                .stream()
                .anyMatch(event -> event.getId().equals("DlDXMobm0uoGrsV4uqXg"));
        Assertions.assertTrue(isPresent, "Non é possibile non trovare nella coronologia un evento appena aggiunto");
    }

    @Test
    void suggestedFriend() {
        contextLoads();
        final List<Friend> friends = userBusinessService.suggestedFriends();
        Assertions.assertTrue(friends.size() <= 6, "La funzione deve tornare almeno 5 amici");
    }

    @Test
    void eventNearMe() {
        contextLoads();
        final List<Event> byGeoPointNearMe = eventBusinessService.findByGeoPointNearMe(new GeoPoint(41.90182786258616, 12.4942199057119), 15);
        byGeoPointNearMe.forEach(System.out::println);
        Assertions.assertTrue(byGeoPointNearMe.size() != 0);
    }

}
