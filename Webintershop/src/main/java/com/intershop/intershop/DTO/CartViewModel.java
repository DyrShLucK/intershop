package com.intershop.intershop.DTO;

import com.intershop.intershop.model.Product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class CartViewModel {
    private List<Product> items;
    private Map<Long, Integer> productQuantities;
    private BigDecimal total;
    private Float balance;
    private boolean hasSufficientBalance;
    private boolean paymentServiceAvailable;

    public CartViewModel(List<Product> items, Map<Long, Integer> productQuantities, BigDecimal total, Float balance, boolean hasSufficientBalance, boolean paymentServiceAvailable) {
        this.items = items;
        this.productQuantities = productQuantities;
        this.total = total;
        this.balance = balance;
        this.hasSufficientBalance = hasSufficientBalance;
        this.paymentServiceAvailable = paymentServiceAvailable;
    }

    public List<Product> getItems() {
        return items;
    }

    public void setItems(List<Product> items) {
        this.items = items;
    }

    public Map<Long, Integer> getProductQuantities() {
        return productQuantities;
    }

    public void setProductQuantities(Map<Long, Integer> productQuantities) {
        this.productQuantities = productQuantities;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public Float getBalance() {
        return balance;
    }

    public void setBalance(Float balance) {
        this.balance = balance;
    }

    public boolean isHasSufficientBalance() {
        return hasSufficientBalance;
    }

    public void setHasSufficientBalance(boolean hasSufficientBalance) {
        this.hasSufficientBalance = hasSufficientBalance;
    }

    public boolean isPaymentServiceAvailable() {
        return paymentServiceAvailable;
    }

    public void setPaymentServiceAvailable(boolean paymentServiceAvailable) {
        this.paymentServiceAvailable = paymentServiceAvailable;
    }
}
