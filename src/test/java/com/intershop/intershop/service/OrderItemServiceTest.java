package com.intershop.intershop.service;

import com.intershop.intershop.model.OrderItem;
import com.intershop.intershop.repository.OrderItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class OrderItemServiceTest {

    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private OrderItemService orderItemService;

    @Test
    void save_DelegatesToRepository() {
        OrderItem item = new OrderItem();
        orderItemService.save(item);
        verify(orderItemRepository, times(1)).save(item);
    }

    @Test
    void saveAll_DelegatesToRepository() {
        OrderItem item = new OrderItem();
        orderItemService.saveAll(List.of(item));
        verify(orderItemRepository, times(1)).saveAll(List.of(item));
    }
}