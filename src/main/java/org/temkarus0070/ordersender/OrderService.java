package org.temkarus0070.ordersender;


import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.temkarus0070.models.Order;
import org.temkarus0070.models.Status;

@Service
public class OrderService {
    @Value("${order.server}")
    private String orderGeneratorServer;

    @Value("${spring.kafka.template.default-topic}")
    private String topicName;

    private KafkaTemplate<Long, Order> kafkaProducer;

    private RestTemplate restTemplate;

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Autowired
    public void setKafkaProducer(KafkaTemplate<Long, Order> kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }

    public OrderService() {
    }

    @Scheduled(fixedDelayString = "${order.delay}")
    public void getOrder() {
        ResponseEntity<Order> order = restTemplate.getForEntity(orderGeneratorServer + "/generate", Order.class);
        sendToQueue(order.getBody());
    }

    public void sendToQueue(Order order) {
        order.setStatus(Status.NEW);
        ProducerRecord<Long, Order> producerRecord = new ProducerRecord<>(topicName, order.getOrderNum(), order);
        kafkaProducer.send(producerRecord);
    }
}
