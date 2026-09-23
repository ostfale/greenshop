package de.ostfale.greenshop.adapter.in.web;

import de.ostfale.greenshop.application.port.in.ShowPurchase;
import de.ostfale.greenshop.application.port.in.StartPurchase;
import de.ostfale.greenshop.application.port.out.PaymentUnavailable;
import de.ostfale.greenshop.application.port.out.ProductNotForSale;
import de.ostfale.greenshop.config.ReturnAddresses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;
import java.util.UUID;

@Controller
class CheckoutController {

    private static final Logger log = LoggerFactory.getLogger(CheckoutController.class);

    private final StartPurchase startPurchase;
    private final ShowPurchase showPurchase;

    CheckoutController(StartPurchase startPurchase, ShowPurchase showPurchase) {
        this.startPurchase = startPurchase;
        this.showPurchase = showPurchase;
    }

    /**
     * 303, not 302: the browser follows a POST with a GET to the payment page, and a reload
     * there does not send the form again.
     */
    @PostMapping("/checkout")
    ResponseEntity<Void> buy(@RequestParam String productId, @RequestParam UUID attempt) {
        var paymentPage = startPurchase.start(productId, attempt);
        return ResponseEntity.status(HttpStatus.SEE_OTHER).location(paymentPage).build();
    }

    @GetMapping(ReturnAddresses.SUCCESS)
    ModelAndView success(@RequestParam(ReturnAddresses.REFERENCE) String reference) {
        return showPurchase.purchase(reference)
                .map(purchase -> new ModelAndView("checkout/success", Map.of("purchase", purchase)))
                .orElseGet(() -> problem("unknown", HttpStatus.NOT_FOUND));
    }

    /**
     * Back in the catalog, which says that nothing was charged. The notice travels as a flash
     * attribute, so a reload of the catalog does not show it again.
     */
    @GetMapping(ReturnAddresses.CANCEL)
    String cancel(RedirectAttributes redirect) {
        redirect.addFlashAttribute("cancelled", true);
        return "redirect:/";
    }

    @ExceptionHandler(ProductNotForSale.class)
    ModelAndView notForSale(ProductNotForSale e) {
        log.info("CheckoutController :: {}", e.getMessage());
        return problem("notForSale", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(PaymentUnavailable.class)
    ModelAndView paymentUnavailable(PaymentUnavailable e) {
        log.warn("CheckoutController :: payment unavailable: {}", e.getMessage());
        return problem("unavailable", HttpStatus.SERVICE_UNAVAILABLE);
    }

    private static ModelAndView problem(String reason, HttpStatus status) {
        return new ModelAndView("checkout/problem", Map.of("reason", reason), status);
    }
}
