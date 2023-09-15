package com.learnkafka.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.learnkafka.domain.LibraryEvent;
import com.learnkafka.domain.LibraryEventType;
import com.learnkafka.producer.LibraryEventsProducer;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@RestController
@Slf4j
public class LibraryEventsController {

    private LibraryEventsProducer libraryEventsProducer;

    public LibraryEventsController(LibraryEventsProducer libraryEventsProducer) {
        this.libraryEventsProducer = libraryEventsProducer;
    }

    @PostMapping("/v1/libraryevent")
    public ResponseEntity<LibraryEvent> postLibraryEvent(@RequestBody @Valid LibraryEvent libraryEvent) throws JsonProcessingException, ExecutionException, InterruptedException, TimeoutException {
        log.info("libraryEvent :: {} ", libraryEvent);
        //libraryEventsProducer.sendLibraryEvent(libraryEvent);
        //libraryEventsProducer.sendLibraryEvent_approach2(libraryEvent);
        libraryEventsProducer.sendLibraryEvent_approach3(libraryEvent);
        log.info("After sending library event to Kafka");
        return ResponseEntity.status(HttpStatus.CREATED).body(libraryEvent);
    }

    @PutMapping("/v1/libraryevent")
    public ResponseEntity<?> putLibraryEvent(@RequestBody @Valid LibraryEvent libraryEvent) throws JsonProcessingException, ExecutionException, InterruptedException, TimeoutException {
        log.info("libraryEvent :: {} ", libraryEvent);

        ResponseEntity<String> BAD_REQUEST = validate(libraryEvent);
        if (nonNull(BAD_REQUEST)) return BAD_REQUEST;

        libraryEventsProducer.sendLibraryEvent_approach3(libraryEvent);
        return ResponseEntity.status(HttpStatus.OK).body(libraryEvent);
    }

    private static ResponseEntity<String> validate(LibraryEvent libraryEvent) {
        if (isNull(libraryEvent.libraryEventId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please pass the LibraryEventId");
        }
        if (LibraryEventType.UPDATE.compareTo(libraryEvent.libraryEventType()) != 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Only UPDATE event type is supported");
        }
        return null;
    }

}