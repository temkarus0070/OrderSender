package org.temkarus0070.ordersender;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.temkarus0070.models.Order;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @org.springframework.beans.factory.annotation.Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    @Bean
    public Map<String, Object> properties(){
        HashMap<String,Object> properties = new HashMap<>();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ProducerConfig.CLIENT_ID_CONFIG, "orderSenderService");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class.getName());
        properties.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, String.valueOf(false));
        return properties;
    }



    @Bean
    public ProducerFactory<Long, Order> producerFactory() {
        return new DefaultKafkaProducerFactory<>(properties());
    }


    @Bean
    public KafkaTemplate<Long, Order> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
