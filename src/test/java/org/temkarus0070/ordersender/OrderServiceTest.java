package org.temkarus0070.ordersender;


import org.apache.kafka.clients.producer.ProducerRecord;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.temkarus0070.models.Order;
import org.temkarus0070.models.Status;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@DirtiesContext
public class OrderServiceTest {
    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private KafkaTemplate<Long, Order> kafkaProducer;

    @SpyBean
    private OrderService orderService;

    @Value("${order.delay}")
    private String delay;

    @Value("${order.server}")
    private String orderGeneratorServer;

    @Test
    public void testScheduleMethodCall() {
        Order order = new Order("pupkin", 111l, new ArrayList<>(), Status.NEW);
        Mockito.when(restTemplate.getForEntity(anyString(), any())).thenReturn(new ResponseEntity<>(order, HttpStatus.OK));
        Mockito.when(kafkaProducer.send(new ProducerRecord<>("orders", order.getOrderNum(), order))).then(invocationOnMock -> null);
        int delay = Integer.valueOf(this.delay) * 3;
        Awaitility.await().atMost(delay, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    Mockito.when(restTemplate.getForEntity(anyString(), any())).thenReturn(new ResponseEntity<>(order, HttpStatus.OK));
                    verify(orderService, times(2)).getOrder();
                });
    }

    @Test
    public void testRestTemplateWork() {
        ArrayList<Order> orders = new ArrayList<>();
        Order order = new Order("pupkin", 111l, new ArrayList<>(), Status.NEW);
        Mockito.when(restTemplate.getForEntity(orderGeneratorServer + "/generate", Order.class)).thenReturn(new ResponseEntity<>(order, HttpStatus.OK));
        Mockito.when(kafkaProducer.send(new ProducerRecord<>("orders", order.getOrderNum(), order))).then(invocationOnMock -> {
            final ProducerRecord<Long, Order> argument = invocationOnMock.getArgument(0, ProducerRecord.class);
            orders.add(argument.value());
            return null;
        });
        orderService.getOrder();
        Assertions.assertEquals(1, orders.size());
        Assertions.assertEquals(orders.get(0), order);
    }

    @Test
    public void testForSystemCrash() {
        Order order = new Order("pupkin", 111l, new ArrayList<>(), Status.NEW);
        Mockito.when(restTemplate.getForEntity(anyString(), any())).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
        try {
            orderService.getOrder();
        } catch (Exception e) {
            e.printStackTrace();
        }
        verify(orderService, times(0)).sendToQueue(order);
    }
}
