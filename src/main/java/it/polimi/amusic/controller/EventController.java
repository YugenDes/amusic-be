package it.polimi.amusic.controller;

import com.google.cloud.firestore.GeoPoint;
import it.polimi.amusic.model.dto.Event;
import it.polimi.amusic.model.response.AMusicResponse;
import it.polimi.amusic.service.EventBusinessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestController
@CrossOrigin
@RequiredArgsConstructor
@Slf4j
public class EventController {

    private final EventBusinessService eventBusinessService;

    @GetMapping(value = "/private/events/all")
    public AMusicResponse<List<Event>> getAllEvents() {
        log.info("New request to /events/all");
        final List<Event> events = eventBusinessService.findAll();
        return AMusicResponse.<List<Event>>builder().body(events).build();
    }


    @GetMapping(value = "/private/events")
    public AMusicResponse<List<Event>> getTodayEvents(@RequestParam(required = false)
                                                              @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate date,
                                                              @RequestParam(required = false)
                                                              @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate dateEnd) {
        log.info("New request to /events?date={}", date);
        List<Event> events = new ArrayList<>();
        if (Objects.isNull(date) && Objects.isNull(dateEnd)) {
            events = eventBusinessService.findByEventDate(LocalDate.now());
        } else if ((Objects.nonNull(date) && Objects.isNull(dateEnd))){
            events = eventBusinessService.findByEventDate(date);
        }else if(Objects.nonNull(date)){
            events = eventBusinessService.findByEventDateBetween(date, dateEnd);
        }
        return AMusicResponse.<List<Event>>builder().body(events).build();
    }


    @GetMapping(value = "/private/events/near")
    public AMusicResponse<List<Event>> getEvents(@RequestParam("lat") Double lat,
                                                 @RequestParam("lon") Double lon,
                                                 @RequestParam(value = "dist", required = false) Double distance) {
        log.info("New request to /events/near?lat={}&lon={}&distance={}", lat, lon, distance);
        if (Objects.isNull(distance)) {
            distance = 1d;
        }
        final List<Event> byGeoPointNearMe = eventBusinessService.findByGeoPointNearMe(new GeoPoint(lat, lon), distance);
        return AMusicResponse.<List<Event>>builder().body(byGeoPointNearMe).build();
    }

    @GetMapping(value = "/private/events/history")
    public AMusicResponse<List<Event>> getEvents() {
        log.info("New request to /events/history");
        final List<Event> userEventHistory = eventBusinessService.getUserEventHistory();
        return AMusicResponse.<List<Event>>builder().body(userEventHistory).build();
    }


}
