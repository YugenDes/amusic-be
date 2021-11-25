package it.polimi.amusic;

import com.firebase.geofire.core.GeoHash;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.GeoPoint;
import it.polimi.amusic.model.document.EventDocument;
import it.polimi.amusic.model.document.UserDocument;
import it.polimi.amusic.service.business.UserBusinessService;
import it.polimi.amusic.service.persistance.EventService;
import it.polimi.amusic.external.gcs.FileService;
import it.polimi.amusic.service.persistance.UserService;
import it.polimi.amusic.utils.GeoUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

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


    @Test
    void saveNewEvent() {
        Map<String,Boolean> partecipanti = new HashMap<>();
        partecipanti.put("0v9hk14jhvCaltWyfkW0",true);
        eventService.save(new EventDocument()
                .setEventName("Evento 1")
                .setEventDate(LocalDateTime.now())
                .setDescription("Non si sa mai")
                .setGeoPoint(new GeoPoint(41.91045717842842,12.529327513568498))
                .setPartecipants(partecipanti));
    }

    @Test
    void saveNewEventWithPartecipants() {
        userService.findReferenceByEmail("andrea.messina@soft.it")
                .map(documentReference -> {
                    return eventService.save(new EventDocument()
                            .setEventName("Monti Lol")
                            .setEventDate(LocalDateTime.now())
                            .setPartecipants(new HashMap<>() {{
                                put(documentReference.getId(), true);
                            }}));
                });
    }

//    @Test
//    void findEvent() {
//        final EventDocument byId = eventService.findById("8iwwd7QRAnsJRhgu9wYB").orElseThrow();
//        System.out.println(byId);
//    }

    @Test
    void findEventByPartecipantsEmail() {
        final List<EventDocument> eventDocuments = userService.findByEmail("andrea.messina@soft.it")
                .map(userDocument -> eventService.findByPartecipant(userDocument.getId()))
                .orElseThrow();

        eventDocuments.forEach(System.out::println);
    }

    @Test
    void findPartecipantUser() {
        eventService.findById("Xymi8cgmxAzydGMdYyXE")
                .orElseThrow()
                .getPartecipants()
                .entrySet()
                .stream()
                .map(documentReference -> userService.findById(documentReference.getKey()).orElseThrow())
                .forEach(System.out::println);
    }

    @Test
    void findEventByLocalDate() {
        eventService.findByEventDate(LocalDate.of(2021, 10, 23)).stream().forEach(System.out::println);
    }


//    @Test
//    void saveGeoEventTest() {
//
//        EventDocument tiburtina = new EventDocument()
//                .setEventName("tiburtina")
//                .setGeoPoint(new GeoPoint(41.91045717842842, 12.529327513568498))
//                .setGeoHash(new GeoHash(41.91045717842842, 12.529327513568498).getGeoHashString());
//
//        EventDocument montiTiburtina = new EventDocument()
//                .setEventName("montiTiburtina")
//                .setGeoPoint(new GeoPoint(41.917124357591405, 12.54287495380747))
//                .setGeoHash(new GeoHash(41.917124357591405, 12.54287495380747).getGeoHashString());
//
//        EventDocument bologna = new EventDocument()
//                .setEventName("bologna")
//                .setGeoPoint(new GeoPoint(41.913555515039356, 12.5207066642881))
//                .setGeoHash(new GeoHash(41.913555515039356, 12.5207066642881).getGeoHashString());
//
//        eventService.save(bologna);
//        eventService.save(montiTiburtina);
//        eventService.save(tiburtina);
//    }

    @SneakyThrows
    @Test
    void findGeoPoint() {
        double distance = 1d;
        final GeoPoint casa = new GeoPoint(41.908761427826434, 12.545459410032102);

        final GeoPoint min = GeoUtils.boundingGeoPoints(casa, distance).get(0);
        final GeoPoint max = GeoUtils.boundingGeoPoints(casa, distance).get(1);

        String minGeoHashString = new GeoHash(min.getLatitude(), min.getLongitude()).getGeoHashString();
        String maxGeoHashString = new GeoHash(max.getLatitude(), max.getLongitude()).getGeoHashString();

        firestore.collection("events")
                .orderBy("geoHash")
                .startAt(minGeoHashString)
                .endAt(maxGeoHashString)
                .get()
                .get()
                .toObjects(EventDocument.class)
                .stream()
                .filter(eventDocument -> {
                    final double distance1 = GeoUtils.distance(casa, eventDocument.getGeoPoint());
                    System.out.println(eventDocument.getEventName() + " " + eventDocument.getGeoPoint() + " distance :" + distance1);
                    return distance1 <= distance;
                }).forEach(System.out::println);
    }


//    @Test
//    void saveUserTest() {
//        UserDocument userDocument = new UserDocument().setDisplayName("Andreaa").setEmail("andrea@test.it");
//        final UserDocument save = userService.save(userDocument);
//        System.out.println(save);
//    }


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
//    void prepareSuggestedFriend() {
//
//        List<UserDocument> users = new ArrayList<>();
//
//        for (int i = 0; i < 20; i++) {
//            users.add(new UserDocument()
//                    .setEmail("test" + i)
//                    .setDisplayName("test" + i));
//        }
//
//        final List<UserDocument> collect = users
//                .stream()
//                .map(userDocument -> userService.save(userDocument))
//                .collect(Collectors.toList());
//
//
//        final EventDocument giga = new EventDocument().setEventDate(LocalDateTime.now()).setEventName("Giga");
//
//        collect.forEach(userDocument -> giga.addIfAbsent(userDocument.getId()));
//
//        eventService.save(giga);
//
//        collect.stream().forEach(userDocument -> userDocument.addEventIfAbsent(giga.getId()));
//
//        int max = 19;
//        int min = 1;
//        int range = max - min + 1;
//
//        for (int i = 0; i < 40; i++) {
//            int user = (int) (Math.random() * range) + min;
//            int firend = (int) (Math.random() * range) + min;
//
//            collect.get(user).addFriendIfAbsent(collect.get(firend).getId());
//        }
//
//        collect.forEach(userDocument -> userService.save(userDocument));
//
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

     @Test
    void saveFirend(){
         final UserDocument userDocument = userService.findById("0v9hk14jhvCaltWyfkW0").orElseThrow();
         userDocument.addFriendIfAbsent("ArgzLAmRGiDPPe80DHEz");
         userDocument.addFriendIfAbsent("FrQCT2YTcx8OX6QiZ7NG");
         userService.save(userDocument);
     }


}
