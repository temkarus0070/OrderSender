package org.temkarus0070.ordersender;


import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.temkarus0070.models.Order;

import javax.annotation.PostConstruct;
import java.util.Properties;

@Component
public class OrderService {
    @Value("${spring.kafka.bootstrap-servers}")
    private String kafkaServer;

    @Value("${order.server}")
    private String orderGeneratorServer;

    @Value("${spring.kafka.template.default-topic}")
    private String topicName;

    @Value("${order.delay}")
    private int delay;

    private KafkaProducer<Long,Order>kafkaProducer;
    public OrderService(){}

    @PostConstruct
    public void init(){
        Properties properties=new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,kafkaServer);
        properties.setProperty(ProducerConfig.CLIENT_ID_CONFIG,"orderSenderService");
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class.getName());
        properties.put("serializer.class", "kafka.serializer.DefaultEncoder");
        kafkaProducer=new KafkaProducer<>(properties);
    }

    @Scheduled(fixedDelayString = "${order.delay}")
    public void getOrder(){
        RestTemplate restTemplate=new RestTemplate();
        ResponseEntity<Order> order=restTemplate.getForEntity(orderGeneratorServer+"/generateOrder",Order.class);
        sendToQueue(order.getBody());
    }


    public void sendToQueue(Order order){
        ProducerRecord<Long,Order> producerRecord=new ProducerRecord<>(topicName,order.getOrderNum(),order);
        if(kafkaProducer.send(producerRecord).isDone()){
            System.out.println("done");
        }
    }
}
