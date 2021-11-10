package org.temkarus0070.ordersender;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OrderSenderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderSenderApplication.class, args);
    }

}
