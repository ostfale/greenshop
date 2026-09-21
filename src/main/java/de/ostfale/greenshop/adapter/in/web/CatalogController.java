package de.ostfale.greenshop.adapter.in.web;

import de.ostfale.greenshop.application.port.in.ShowCatalog;
import de.ostfale.greenshop.application.port.out.CatalogUnavailable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@Controller
class CatalogController {

    private static final Logger log = LoggerFactory.getLogger(CatalogController.class);

    private final ShowCatalog showCatalog;

    CatalogController(ShowCatalog showCatalog) {
        this.showCatalog = showCatalog;
    }

    @GetMapping("/")
    String catalog(Model model) {
        model.addAttribute("products", showCatalog.productsForSale());
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
        model.addAttribute("unavailable", true);
        return "catalog";
    }
}
