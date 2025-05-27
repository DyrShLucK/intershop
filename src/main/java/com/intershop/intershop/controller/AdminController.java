package com.intershop.intershop.controller;

import com.intershop.intershop.exception.MissingParamException;
import com.intershop.intershop.model.Product;
import com.intershop.intershop.service.MultipartService;
import com.intershop.intershop.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ProductService productService;
    private final MultipartService  multipartService;

    @Autowired
    public AdminController(ProductService productService, MultipartService multipartService) {
        this.productService = productService;
        this.multipartService = multipartService;
    }

    @GetMapping("/add-product")
    public String showAddProductForm(Model model) {
        model.addAttribute("product", new Product());
        return "add-product";
    }

    @PostMapping("/add-product")
    public Mono<String> saveProduct(ServerWebExchange exchange) {
        return exchange.getMultipartData()
                .flatMap(multipartData -> {
                    String name = multipartService.getFormField(multipartData, "name").orElseThrow(() -> new MissingParamException("name"));

                    String description = multipartService.getFormField(multipartData, "description").orElseThrow(() -> new MissingParamException("description"));

                    String priceStr = multipartService.getFormField(multipartData, "price").orElseThrow(() -> new MissingParamException("price"));

                    BigDecimal price = new BigDecimal(priceStr);
                    Part imagePart = multipartData.getFirst("image");
                    FilePart image = (FilePart) imagePart;

                    return multipartService.extractImageBytes(image)
                            .flatMap(imageBytes -> {
                                Product product = new Product();
                                product.setName(name);
                                product.setDescription(description);
                                product.setPrice(price);
                                product.setImage(imageBytes);
                                return productService.save(product);
                            });
                }).thenReturn("redirect:/intershop");
    }
}