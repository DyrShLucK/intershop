package com.intershop.intershop.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Table(name = "cart")
public class Cart {
    @Id
    private Long id;
    @Column("total_amount")
    private BigDecimal totalAmount;
    @Transient
    private List<CartItem> cartItems;
    @Column("user_name")
    private String userName;
}
