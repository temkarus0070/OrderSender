package org.temkarus0070.ordersender;


import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.temkarus0070.models.Order;
import org.temkarus0070.models.Status;

import javax.annotation.PostConstruct;
import java.util.Properties;
import java.util.logging.Logger;

@Service
public class OrderService {


    @Value("${order.server}")
    private String orderGeneratorServer;

    @Value("${spring.kafka.template.default-topic}")
    private String topicName;


    @Autowired
    private KafkaTemplate<Long, Order> kafkaProducer;

    public OrderService() {
    }





    @Scheduled(fixedDelayString = "${order.delay}")
    public void getOrder() {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Order> order = restTemplate.getForEntity(orderGeneratorServer + "/generate", Order.class);
        sendToQueue(order.getBody());
    }


    public void sendToQueue(Order order) {
        order.setStatus(Status.NEW);
        ProducerRecord<Long, Order> producerRecord = new ProducerRecord<>(topicName, order.getOrderNum(), order);
        kafkaProducer.send(producerRecord);
        kafkaProducer.flush();
    }
}
