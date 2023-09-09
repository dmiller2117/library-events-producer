package com.learnkafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnkafka.domain.LibraryEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@Slf4j
public class LibraryEventsProducer {

    @Value("${spring.kafka.topic}")
    String topic;

    private final KafkaTemplate<Integer, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public LibraryEventsProducer(KafkaTemplate<Integer, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Send a library event asynchronously.
     * The preferred approach.
     *
     * @param libraryEvent the library event
     * @throws JsonProcessingException
     */
    public void sendLibraryEvent(LibraryEvent libraryEvent) throws JsonProcessingException {
        var key = libraryEvent.libraryEventId();
        var value = objectMapper.writeValueAsString(libraryEvent);
        var completableFuture = kafkaTemplate.send(topic, key, value);

        // 1. blocking call - get metadata about the kafka cluster
        // 2. send message, returns the CompletableFuture
        completableFuture.whenComplete((sendResult, throwable) -> {
            if (throwable != null) {
                handleFailure(key, value, throwable);
            } else {
                handleSuccess(key, value, sendResult);
            }
        });
    }

    /**
     * Send a library event synchronously.
     *
     * @param libraryEvent the library event
     * @throws JsonProcessingException
     */
    public SendResult<Integer, String> sendLibraryEvent_approach2(LibraryEvent libraryEvent) throws JsonProcessingException, ExecutionException, InterruptedException, TimeoutException {
        var key = libraryEvent.libraryEventId();
        var value = objectMapper.writeValueAsString(libraryEvent);

        // 1. blocking call - get metadata about the kafka cluster
        // 2. block and wait until the message is sent to the Kafka queue,
        var sendResult = kafkaTemplate.send(topic, key, value)
                // .get()
                .get(3, TimeUnit.SECONDS);
        handleSuccess(key, value, sendResult);
        return sendResult;
    }

    private void handleSuccess(Integer key, String value, SendResult<Integer, String> sendResult) {
        log.info("Message sent successfully for the key: {} and the value: {} , partition is {} ",
                key, value, sendResult.getRecordMetadata().partition());
    }

    private void handleFailure(Integer key, String value, Throwable throwable) {
        log.error("Error sending the message and the exception is {}", throwable.getMessage(), throwable);
    }

}