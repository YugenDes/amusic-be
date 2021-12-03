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
import it.polimi.amusic.security.model.AuthProvider;
import it.polimi.amusic.service.business.UserBusinessService;
import it.polimi.amusic.service.persistance.EventService;
import it.polimi.amusic.service.persistance.RoleService;
import it.polimi.amusic.service.persistance.UserService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@SpringBootTest
@Slf4j
class FirestoreServiceTest {

    @Autowired
    EventService eventService;

    @Autowired
    UserService userService;

    @Autowired
    UserBusinessService userBusinessService;

    @Autowired
    FileService fileService;

    @Autowired
    Firestore firestore;

    @Autowired
    RoleService roleService;


    void contextLoads() {
        final UserDocument userDocument = userService.findById("puLxmw6ozrb7X7IuVWkr").orElseThrow();
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDocument, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }


    @Test
    void saveNewEvent() {
        eventService.save(new EventDocument()
                .setEventName("Qube")
                .setEventDate(LocalDateTime.of(2021, 12, 13, 23, 00))
                .setEventDatePublished(LocalDateTime.now())
                .setDescription("Qube")
                .setGeoPoint(new GeoPoint(41.897845844351465, 12.540060246022259))
                .setGeoHash(new GeoHash(41.897845844351465, 12.540060246022259).getGeoHashString())
                .setMaxPartecipants(100)
                .setTicketPrice(12.5D));

        eventService.save(new EventDocument()
                .setEventName("Piper Club")
                .setEventDate(LocalDateTime.of(2021, 12, 14, 23, 00))
                .setEventDatePublished(LocalDateTime.now())
                .setDescription("Piper Club")
                .setGeoPoint(new GeoPoint(41.919022344359966, 12.501152542236955))
                .setGeoHash(new GeoHash(41.919022344359966, 12.501152542236955).getGeoHashString())
                .setMaxPartecipants(200)
                .setTicketPrice(20D));

        eventService.save(new EventDocument()
                .setEventName("Meeting Place Bar")
                .setEventDate(LocalDateTime.of(2021, 12, 13, 19, 00))
                .setEventDatePublished(LocalDateTime.now())
                .setDescription("Meeting Place Bar")
                .setGeoPoint(new GeoPoint(41.91382665676414, 12.52141626922569))
                .setGeoHash(new GeoHash(41.91382665676414, 12.52141626922569).getGeoHashString())
                .setMaxPartecipants(2)
                .setTicketPrice(9D));

        eventService.save(new EventDocument()
                .setEventName("T Bar Ostiense")
                .setEventDate(LocalDateTime.of(2021, 12, 15, 22, 30))
                .setEventDatePublished(LocalDateTime.now())
                .setDescription("T Bar Ostiense")
                .setGeoPoint(new GeoPoint(41.863255373433056, 12.478290459422745))
                .setGeoHash(new GeoHash(41.863255373433056, 12.478290459422745).getGeoHashString())
                .setMaxPartecipants(100)
                .setTicketPrice(18.5D));
    }


    @Test
    void findEvent() {
        final List<Event> byEventDate = eventService.findByEventDate(LocalDate.of(2021, 12, 15));
        Assert.isTrue(!byEventDate.isEmpty(), "Deve esserci un evento");
    }

    @Test
    void findEventByPartecipantsEmail() {
        final List<EventDocument> eventDocuments = userService.findByEmail("andrea.messina@soft.it")
                .map(userDocument -> eventService.findByParticipant(userDocument.getId()))
                .orElseThrow();

        eventDocuments.forEach(System.out::println);
    }

    @Test
    void findPartecipantUser() {
        eventService.findById("Xymi8cgmxAzydGMdYyXE")
                .orElseThrow()
                .getPartecipants()
                .stream()
                .map(documentReference -> userService.findById(documentReference.getId()).orElseThrow())
                .forEach(System.out::println);
    }

    @SneakyThrows
    @Test
    void findGeoPoint() {
        final GeoPoint casa = new GeoPoint(41.908761427826434, 12.545459410032102);
        final List<Event> byGeoPointNearMe = eventService.findByGeoPointNearMe(casa, 3);
        byGeoPointNearMe.forEach(System.out::println);
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
                .setAuthorities(Collections.singletonList(roleService.findByAuthority(RoleDocument.RoleEnum.USER)))
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
                .setAuthorities(Collections.singletonList(roleService.findByAuthority(RoleDocument.RoleEnum.USER)))
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
                .setAuthorities(Collections.singletonList(roleService.findByAuthority(RoleDocument.RoleEnum.ADMIN)))
                .setEmailVerified(false);


        final UserDocument save = userService.save(userDocument);
        final UserDocument save1 = userService.save(userDocument1);
        final UserDocument save3 = userService.save(admin);

        System.out.println(save);
        System.out.println(save1);
        System.out.println(save3);
    }

    @Test
    void addFriendTest() {
        final List<Friend> friends = userBusinessService.addFriend("JMapVlHllNOFuE6CrqmG");
        friends.forEach(System.out::println);
    }

    @Test
    void removeFirendTest() {
        final List<Friend> friends = userBusinessService.removeFriend("JMapVlHllNOFuE6CrqmG");
        friends.forEach(System.out::println);
    }

    @Test
    void attendEvent() {
        contextLoads();
        final Event event = userBusinessService.attendAnEvent("8Xq2gviMAwO1Pc3EjoAl", "Px1rphu8AiVS3aGnoSGj", true);
        final Event event1 = userBusinessService.attendAnEvent("8Xq2gviMAwO1Pc3EjoAl", "DlDXMobm0uoGrsV4uqXg", false);
        System.out.println(event);
        System.out.println(event1);
    }


    @Test
    void findEventByPartecipant() {
        final List<EventDocument> events = eventService.findByParticipant("8Xq2gviMAwO1Pc3EjoAl");
        events.forEach(System.out::println);
    }

    @Test
    void getFriends() {
        contextLoads();
        userBusinessService.getFriends().forEach(System.out::println);
    }


//    @Test
//    void eventUpdateTest() {
//        try (FileInputStream fileInputStream = new FileInputStream("C:\\Users\\andrea.messina\\Desktop\\logo.jpg")) {
//            final byte[] file = fileInputStream.readAllBytes();
//            final ByteArrayResource byteArrayReource = new ByteArrayResource(file, "logo.jpg");
//
//            final String mediaLink = fileService.uploadFile(byteArrayReource);
//            eventService.findByEventDate(LocalDate.of(2021, 10, 23))
//                    .stream()
//                    .forEach(eventDocument -> eventService
//                            .save(eventDocument.setEventDatePublished(LocalDateTime.now().minusDays(3)).setImageUrl(mediaLink)));
//
//        } catch (StorageException | NullPointerException | FileNotFoundException e) {
//            throw new GCPBucketException("Errore durante il caricamento del file {}", e.getLocalizedMessage());
//        } catch (IOException e) {
//            throw new GCPBucketException("Errore durante il caricamento del file {}", e.getLocalizedMessage());
//        }
//    }


//    @Test
//    void suggestedFriend() {
//        final UserDocument userDocument = userService.findById("aGe054mKQkH3psotr6vv").orElseThrow();
//        final String reduce = userDocument.getFirendList()
//                .stream()
//                .map(s -> userService.findById(s))
//                .map(userDocument1 -> userDocument1.get().getDisplayName())
//                .reduce("", (s, s1) -> s + " " + s1);
//        final String reduce1 = userBusinessService.suggestedFriends("aGe054mKQkH3psotr6vv")
//                .stream()
//                .map(userDocument1 -> userDocument1.getDisplayName())
//                .reduce("", (s, s2) -> s + " " + s2);
//        System.out.println(userDocument.getDisplayName());
//        System.out.println(reduce);
//        System.out.println(reduce1);
//    }

//     @Test
//    void saveFirend(){
//         final UserDocument userDocument = userService.findById("0v9hk14jhvCaltWyfkW0").orElseThrow();
//         userDocument.addFriendIfAbsent("ArgzLAmRGiDPPe80DHEz");
//         userDocument.addFriendIfAbsent("FrQCT2YTcx8OX6QiZ7NG");
//         userService.save(userDocument);
//     }


}
