package de.ostfale.greenshop.adapter.in.web;

import de.ostfale.greenshop.application.port.in.RefundPurchase;
import de.ostfale.greenshop.application.port.in.ShowOrders;
import de.ostfale.greenshop.application.port.out.RefundRefused;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
class OrdersController {

    private static final Logger log = LoggerFactory.getLogger(OrdersController.class);

    private final ShowOrders showOrders;
    private final RefundPurchase refundPurchase;

    OrdersController(ShowOrders showOrders, RefundPurchase refundPurchase) {
        this.showOrders = showOrders;
        this.refundPurchase = refundPurchase;
    }

    @GetMapping("/orders")
    String orders(Model model) {
        model.addAttribute("orders", showOrders.orders());
        return "orders";
    }

    /**
     * Gives the whole order back. The notice travels as a flash attribute, so a reload of the
     * list does not show it again — and above all does not refund again.
     */
    @PostMapping("/orders/{reference}/refund")
    String refund(@PathVariable String reference, RedirectAttributes redirect) {
        try {
            var order = refundPurchase.refund(reference);
            redirect.addFlashAttribute("refunded", order.total());
        } catch (RefundRefused | IllegalStateException | IllegalArgumentException e) {
            log.warn("OrdersController :: refund of {} refused: {}", reference, e.getMessage());
            redirect.addFlashAttribute("refundRefused", true);
        }
        return "redirect:/orders";
    }
}
