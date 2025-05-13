package com.intershop.intershop.controller;

import com.intershop.intershop.model.CartItem;
import com.intershop.intershop.model.Product;
import com.intershop.intershop.service.CartItemService;
import com.intershop.intershop.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
@RequestMapping("intershop")
public class HeadController {
    @Autowired
    ProductService productService;
    @Autowired
    CartItemService cartItemService;

    @GetMapping
    public String mainPage(@RequestParam(name = "search", required = false, defaultValue = "") String search,
                           @RequestParam(name = "sort", defaultValue = "id") String sortBy,
                           @RequestParam(name = "sortDir", defaultValue = "ASC") String sortDir,
                           @RequestParam(name = "pageSize", defaultValue = "10") int pageSize,
                           @RequestParam(name = "pageNumber", defaultValue = "0") int pageNumber,
                           Model model){

        Page<Product> productPage = productService.getProductsWithPaginationAndSort(
                search, search, pageNumber, pageSize, sortBy, sortDir);


        model.addAttribute("items", productPage.getContent());
        model.addAttribute("paging", productPage);
        model.addAttribute("search", search != null ? search : "");
        model.addAttribute("sort", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("currentPage", pageNumber);

        Map<Long, Integer> productQuantities = cartItemService.getCartQuantitiesMap();
        model.addAttribute("productQuantities", productQuantities);

        return "main";
    }

    @GetMapping("/products/{id}/image")
    public ResponseEntity<byte[]> getProductImage(@PathVariable Long id) {
        Product product = productService.getProduct(id);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(product.getImage());
    }
}
