package com.intershop.intershop.controller;

import com.intershop.intershop.exception.OrderNotFoundException;
import com.intershop.intershop.model.Order;
import com.intershop.intershop.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@WebMvcTest(OrderController.class)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    private Order createTestOrder(Long id) {
        Order order = new Order();
        order.setId(id);
        order.setOrderDate(LocalDateTime.now());
        order.setTotalAmount(BigDecimal.valueOf(200.00));
        return order;
    }

    @Test
    public void viewOrder_ShouldReturnOrderTemplateWithOrder() throws Exception {
        Order testOrder = createTestOrder(1L);

        when(orderService.findOrderById(1L)).thenReturn(testOrder);

        mockMvc.perform(get("/intershop/orders/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("order"))
                .andExpect(model().attribute("order", testOrder));

        verify(orderService, times(1)).findOrderById(1L);
    }

    @Test
    public void listOrders_ShouldReturnOrdersTemplateWithList() throws Exception {
        Order order1 = createTestOrder(1L);
        Order order2 = createTestOrder(2L);

        when(orderService.findAll()).thenReturn(List.of(order1, order2));

        mockMvc.perform(get("/intershop/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders"))
                .andExpect(model().attribute("orders", List.of(order1, order2)));

        verify(orderService, times(1)).findAll();
    }
}