package com.cinx.enrollment.mapper;

import com.cinx.enrollment.dto.response.OrderAggregate;
import com.cinx.enrollment.dto.response.OrderDetailResponse;
import com.cinx.enrollment.dto.response.OrderResponse;
import com.cinx.enrollment.messaging.event.OrderEvent;
import com.cinx.enrollment.model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "id", source = "order.id")
    @Mapping(target = "totalPrice", source = "order.totalPrice")
    @Mapping(target = "discounted", source = "order.discounted")
    @Mapping(target = "orderDate", source = "order.orderDate")
    @Mapping(target = "items", source = "order.items")
    @Mapping(target = "payment", source = "payment")
    OrderDetailResponse toDetailDto(OrderAggregate aggregate);

    OrderResponse toDto(Order order);
    OrderEvent toEvent(Order order);
}
