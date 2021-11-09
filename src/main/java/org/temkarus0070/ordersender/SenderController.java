package org.temkarus0070.ordersender;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.temkarus0070.models.Order;
import org.temkarus0070.models.Status;

@RestController
public class SenderController {
    private OrderService orderService;

    @Autowired
    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping(path = "/send")
    public void sendOrder(@RequestBody Order order){
        order.setStatus(Status.NEW);
        orderService.sendToQueue(order);


    }
}
