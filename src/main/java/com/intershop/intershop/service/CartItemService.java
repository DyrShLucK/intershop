package com.intershop.intershop.service;

import com.intershop.intershop.model.CartItem;
import com.intershop.intershop.model.Product;
import com.intershop.intershop.repository.CartItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CartItemService {

    @Autowired
    CartItemRepository cartItemRepository;
    @Autowired
    ProductService productService;

    public List<CartItem> getCart(){
        return cartItemRepository.findAll();
    }
    public CartItem save(CartItem cartItem){
        return cartItemRepository.save(cartItem);
    }
    public void delete(Long id){
        cartItemRepository.deleteById(id);
    }
    public void deleteAll(){
        cartItemRepository.deleteAll();
    }
    public int getQuantityByProductId(Long productId) {
        CartItem item = cartItemRepository.findByProduct_Id(productId);
        return (item != null) ? item.getQuantity() : 0;
    }

    public Map<Long, Integer> getCartQuantitiesMap() {
        Map<Long, Integer> quantities = new HashMap<>();
        List<CartItem> cartItems = cartItemRepository.findAll();

        for (CartItem item : cartItems) {
            quantities.put(item.getProduct().getId(), item.getQuantity());
        }

        return quantities;
    }

    @Transactional
    public void updateCartItem(Long productId, String action) {
        Product product = productService.getProduct(productId); // Добавьте ProductService
        if (product == null) {
            throw new IllegalArgumentException("Продукт не найден");
        }

        CartItem cartItem = cartItemRepository.findByProduct_Id(productId);
        if (cartItem == null) {
            cartItem = new CartItem();
            cartItem.setProduct(product);
            cartItem.setQuantity(0);
        }

        if ("plus".equals(action)) {
            cartItem.setQuantity(cartItem.getQuantity() + 1);
        } else if ("minus".equals(action)) {
            if (cartItem.getQuantity() > 0) {
                cartItem.setQuantity(cartItem.getQuantity() - 1);
            }
        }

        if (cartItem.getQuantity() == 0 && cartItem.getId() != null) {
            cartItemRepository.deleteById(cartItem.getId());
        } else {
            cartItemRepository.save(cartItem);
        }
    }

    public List<Product> getProductsInCart() {
        List<CartItem> cartItems = cartItemRepository.findAllWithProductSortedById();
        return cartItems.stream()
                .map(CartItem::getProduct)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public BigDecimal getTotal(){
        return cartItemRepository.calculateTotalPrice();
    }

}
