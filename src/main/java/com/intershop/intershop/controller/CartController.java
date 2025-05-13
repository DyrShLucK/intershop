package com.intershop.intershop.controller;

import com.intershop.intershop.model.Product;
import com.intershop.intershop.service.CartItemService;
import com.intershop.intershop.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@RequestMapping("intershop/cart")
@Controller
public class CartController {

    @Autowired
    CartItemService cartItemService;
    @Autowired
    private OrderService orderService;

    @GetMapping
    public String cartView(Model model){

        List<Product> productList = cartItemService.getProductsInCart();
        model.addAttribute("items", productList);

        Map<Long, Integer> productQuantities = cartItemService.getCartQuantitiesMap();
        model.addAttribute("productQuantities", productQuantities);

        model.addAttribute("total", cartItemService.getTotal());
        return "cart";
    }
    @PostMapping("/{productId}")
    public String updateCartItem(@PathVariable Long productId,
                                 @RequestParam String action,
                                 @RequestParam(required = false) String redirectUrl) {
        cartItemService.updateCartItem(productId, action);
        return "redirect:" + (redirectUrl != null ? redirectUrl : "/intershop");
    }

    @PostMapping("/buy")
    public String createOrderAndRedirect(RedirectAttributes redirectAttributes) {
        try {
            var order = orderService.createOrderFromCart();
            return "redirect:/intershop/orders/" + order.getId();
        } catch (Exception e) {
            return "redirect:/intershop/cart";
        }
    }
}
