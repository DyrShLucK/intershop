package com.intershop.intershop.repository;

import com.intershop.intershop.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    CartItem findByProduct_Id(Long productId);
    @Query("SELECT SUM(ci.quantity * ci.product.price) FROM CartItem ci")
    BigDecimal calculateTotalPrice();
    @Query("SELECT c FROM CartItem c JOIN FETCH c.product p ORDER BY p.id ASC")
    List<CartItem> findAllWithProductSortedById();
}