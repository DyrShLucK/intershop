package com.intershop.intershop.model;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;


@Table(name = "cart_items")
@Data
public class CartItem {
    @Id
    private Long id;
    @Column("product_id")
    private Long productId;
    private int quantity;
    @Column("cart_id")
    private Long cartId;


    public CartItem(Long id, Long productId, int quantity, Long cartId) {
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
        this.cartId = cartId;
    }

    public CartItem() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}