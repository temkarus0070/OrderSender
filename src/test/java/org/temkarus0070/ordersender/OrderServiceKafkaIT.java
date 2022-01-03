package org.temkarus0070.ordersender;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.internals.Sender;
import org.apache.kafka.common.serialization.LongDeserializer;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.rule.EmbeddedKafkaRule;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import org.temkarus0070.models.Order;
import org.temkarus0070.models.Status;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.kafka.test.assertj.KafkaConditions.key;
import static org.springframework.kafka.test.hamcrest.KafkaMatchers.*;


@SpringBootTest
@ExtendWith(SpringExtension.class)
@EmbeddedKafka(topics = "orders",
        bootstrapServersProperty = "spring.kafka.bootstrap-servers",partitions = 1)
public class OrderServiceKafkaIT {

    @Autowired
    private OrderService orderService;


    private static String SENDER_TOPIC = "orders";



    private KafkaMessageListenerContainer<Long, Order> container;

    private BlockingQueue<ConsumerRecord<Long, Order>> records;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @BeforeEach
    public void setUp() throws Exception {
        // set up the Kafka consumer properties
        Map<String, Object> consumerProperties =
                KafkaTestUtils.consumerProps("sender", "false",
                        embeddedKafka);
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,LongDeserializer.class.getName());
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,JsonDeserializer.class.getName());
        consumerProperties.put("spring.json.value.default.type",Order.class.getName());

        // create a Kafka consumer factory
        DefaultKafkaConsumerFactory<Long, Order>consumerFactory =
                new DefaultKafkaConsumerFactory<Long, Order>(
                        consumerProperties);

        // set the topic that needs to be consumed
        ContainerProperties containerProperties =
                new ContainerProperties(SENDER_TOPIC);


        // create a Kafka MessageListenerContainer
        container = new KafkaMessageListenerContainer<>(consumerFactory,
                containerProperties);

        // create a thread safe queue to store the received message
        records = new LinkedBlockingQueue<>();

        // setup a Kafka message listener
        container
                .setupMessageListener(new MessageListener<Long, Order>() {
                    @Override
                    public void onMessage(
                            ConsumerRecord<Long, Order> record) {

                        records.add(record);
                    }
                });

        // start the container and underlying message listener
        container.start();

        // wait until the container has the required number of assigned partitions
        ContainerTestUtils.waitForAssignment(container,
                embeddedKafka.getPartitionsPerTopic());
    }

    @AfterEach
    public void tearDown() {
        // stop the container
        container.stop();
    }

    @Test
    public void testSend() throws InterruptedException {
        Random random=new Random(new Date().getTime());
        Order order=new Order("pupkin",random.nextLong(),new ArrayList<>(),Status.NEW);
        orderService.sendToQueue(order);
        ConsumerRecord<Long, Order> received =
                records.poll(10000, TimeUnit.MILLISECONDS);
        Assertions.assertEquals(received.value(), order);

    }

}
