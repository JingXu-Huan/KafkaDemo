package com.jingxu.kafkademo.service;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service

public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String topic;
    private final KafkaAdminClient kafkaAdminClient;

    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate,
            @Value("${app.kafka.topic:demo-topic}") String topic,
            KafkaAdminClient kafkaAdminClient) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
        this.kafkaAdminClient = kafkaAdminClient;
    }

    public void send(String message) {
        CompletableFuture<SendResult<String, String>> send = kafkaTemplate.send(topic, message);
        SendResult<String, String> sendResult = null;
        try {
            sendResult = send.get();
            sendResult.getProducerRecord().headers().forEach(header -> {
                System.out.println("Header: " + header.key() + " = " + new String(header.value()));
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        kafkaTemplate.flush(); // 确保消息立即发送

    }

    public void sendByOrder(String message, String orderId) {
        // 获取topic的分区数量
        int partitionCount = getPartitionCount("demo-topic");
        // 假设我们有十个商品,五个分区，消息上游可能有下单成功，发货成功，订单完成的消息
        // 按照订单的顺序发送到Kafka中 - 使用orderId作为分区键保证顺序
        int id = Integer.parseInt(orderId);
        int partition = Integer.parseInt(String.valueOf(id % partitionCount));
        kafkaTemplate.send(topic, String.valueOf(partition), message + orderId + "分区" + partition);
    }

    /**
     * 
     * 获取指定topic的分区数量
     */
    private int getPartitionCount(String topicName) {
        try {
            return kafkaAdminClient.describeTopics(Collections.singletonList(topicName))
                    .allTopicNames()
                    .get()
                    .get(topicName)
                    .partitions()
                    .size();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to get partition count for topic: " + topicName, e);
        }
    }
}