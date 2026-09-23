package de.ostfale.greenshop.adapter.in.web;

import de.ostfale.greenshop.application.port.in.ShowOrders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
class OrdersController {

    private final ShowOrders showOrders;

    OrdersController(ShowOrders showOrders) {
        this.showOrders = showOrders;
    }

    @GetMapping("/orders")
    String orders(Model model) {
        model.addAttribute("orders", showOrders.orders());
        return "orders";
    }
}
