package de.ostfale.greenshop.adapter.in.web;

import de.ostfale.greenshop.application.port.in.ShowCatalog;
import de.ostfale.greenshop.application.port.out.CatalogUnavailable;
import de.ostfale.greenshop.domain.products.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.stream.Collectors.toMap;

@Controller
class CatalogController {

    private static final Logger log = LoggerFactory.getLogger(CatalogController.class);

    private final ShowCatalog showCatalog;

    CatalogController(ShowCatalog showCatalog) {
        this.showCatalog = showCatalog;
    }

    /**
     * Every buy button carries an attempt of its own, drawn when the page is built. Two clicks
     * on the same button send the same attempt, and Stripe answers the second call with the
     * checkout of the first instead of opening another one.
     */
    @GetMapping("/")
    String catalog(Model model) {
        var products = showCatalog.productsForSale();
        model.addAttribute("products", products);
        model.addAttribute("attempts", products.stream()
                .collect(toMap(Product::id, product -> UUID.randomUUID())));
        return "catalog";
    }

    /**
     * The page stays, with a notice instead of the list: the shop is down, not broken.
     */
    @ExceptionHandler(CatalogUnavailable.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    String catalogUnavailable(CatalogUnavailable e, Model model) {
        log.warn("CatalogController :: catalog unavailable: {}", e.getMessage());
        model.addAttribute("products", List.of());
        model.addAttribute("attempts", Map.of());
        model.addAttribute("unavailable", true);
        return "catalog";
    }
}
