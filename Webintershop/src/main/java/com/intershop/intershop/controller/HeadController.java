package com.intershop.intershop.controller;

import com.intershop.intershop.DTO.ProductPageDTO;
import com.intershop.intershop.service.CartItemService;
import com.intershop.intershop.service.ProductService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.Map;


@Controller
@RequestMapping({"/", "/intershop"})
public class HeadController {
    private final ProductService productService;
    private final CartItemService cartItemService;

    public HeadController(ProductService productService, CartItemService cartItemService) {
        this.productService = productService;
        this.cartItemService = cartItemService;
    }

    @GetMapping
    public Mono<String> mainPage(
            @RequestParam(name = "search", required = false, defaultValue = "") String search,
            @RequestParam(name = "sort", defaultValue = "id") String sortBy,
            @RequestParam(name = "sortDir", defaultValue = "DESC") String sortDir,
            @RequestParam(name = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(name = "pageNumber", defaultValue = "0") int pageNumber,
            Model model, Principal principal) {


        Pageable pageable = PageRequest.of(pageNumber, pageSize,
                Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        boolean isAdmin;
        boolean exist = principal != null;
        if (exist) {
            Authentication authentication = (Authentication) principal;
            isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"));
        } else {
            isAdmin = false;
        }
        return Mono.zip(
                productService.getProductsWithPaginationAndSort(search, pageable),
                cartItemService.getCartQuantitiesMap(principal)
        ).map(tuple -> {
            ProductPageDTO productPage = tuple.getT1();
            Map<Long, Integer> quantities = tuple.getT2();

            model.addAttribute("items", productPage.getContent());
            model.addAttribute("paging", productPage);
            model.addAttribute("search", search);
            model.addAttribute("sort", sortBy);
            model.addAttribute("sortDir", sortDir);
            model.addAttribute("pageSize", pageSize);
            model.addAttribute("currentPage", pageNumber);
            model.addAttribute("productQuantities", quantities);
            model.addAttribute("isAdmin", isAdmin);

            return "main";
        });
    }

    @GetMapping("/products/{id}/image")
    public Mono<ResponseEntity<byte[]>> getProductImage(@PathVariable Long id) {
        return productService.getProduct(id)
                .map(product -> ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(product.getImage()));
    }


}
