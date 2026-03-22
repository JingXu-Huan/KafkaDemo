package com.jingxu.kafkademo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerService.class);

    @KafkaListener(topics = "${app.kafka.topic:demo-topic}", 
    groupId = "${spring.kafka.consumer.group-id:demo-group}")
    public void consume(String message) {
        log.info("Received message: {}", message);
    }
}