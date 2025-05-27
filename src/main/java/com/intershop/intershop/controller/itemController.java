package com.intershop.intershop.controller;

import com.intershop.intershop.service.CartItemService;
import com.intershop.intershop.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping("intershop")
public class itemController {
    private final ProductService productService;
    private final CartItemService cartItemService;

    public itemController(ProductService productService, CartItemService cartItemService) {
        this.productService = productService;
        this.cartItemService = cartItemService;
    }

    @GetMapping("/item/{id}")
    public Mono<String> getItem(@PathVariable Long id, Model model) {
        return productService.getProduct(id)
                .flatMap(product -> cartItemService.getQuantityByProductId(id)
                        .map(quantity -> {
                            model.addAttribute("item", product);
                            model.addAttribute("quantity", quantity);
                            return "item";
                        }));
    }
}
