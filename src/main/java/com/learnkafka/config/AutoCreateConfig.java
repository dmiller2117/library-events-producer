package com.learnkafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.TopicBuilder;

/**
 * KafkaAdmin
 * <ul>
 * <li>Create topics programmatically</li>
 * <li>Part of the SpringKafka</li>
 * <li>How to create a topic from code?</li>
 *  <ul>
 *  <li>Create a bean of type KafkaAdmin in Spring configuration</li>
 *  <li>Create a bean of type NewTopic in Spring configuration</li>
 *  </ul>
 * </ul>
 */
@Configuration
@Profile("local")
public class AutoCreateConfig {

    @Value("${spring.kafka.topic}")
    String topicName;

    @Bean
    public NewTopic libraryEvents() {
        return TopicBuilder
                .name(topicName)
                .partitions(3)
                .replicas(3)
                .build();
    }

}