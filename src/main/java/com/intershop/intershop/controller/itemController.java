package com.intershop.intershop.controller;

import com.intershop.intershop.model.Product;
import com.intershop.intershop.service.CartItemService;
import com.intershop.intershop.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("intershop")
public class itemController {
    @Autowired
    ProductService productService;
    @Autowired
    CartItemService cartItemService;

    @GetMapping("/item/{id}")
    public String itemView(@PathVariable("id") Long id, Model model){
        Product item = productService.getProduct(id);
        model.addAttribute("item", item);
        int quantity = cartItemService.getQuantityByProductId(id);
        model.addAttribute("quantity", quantity);
        return "item";
    }
}
