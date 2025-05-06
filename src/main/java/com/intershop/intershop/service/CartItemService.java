package com.intershop.intershop.service;

import com.intershop.intershop.model.CartItem;
import com.intershop.intershop.repository.CartItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class CartItemService {

    @Autowired
    CartItemRepository cartItemRepository;

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
}
