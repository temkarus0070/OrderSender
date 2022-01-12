package org.temkarus0070.ordersender;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.temkarus0070.models.Order;

import java.util.Map;

@TestConfiguration

public class TestConfig {
    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;
    private String SENDER_TOPIC = "orders";

    @Bean
    @Primary
    public KafkaTemplate<Long, Order> kafkaTemplateForTest() {
        Map<String, Object> config = KafkaTestUtils.producerProps(embeddedKafka);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class.getName());
        config.put("spring.json.value.default.type", Order.class.getName());
        KafkaTemplate<Long, Order> kafkaTemplate = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(config));
        kafkaTemplate.setDefaultTopic(SENDER_TOPIC);
        return kafkaTemplate;
    }
}