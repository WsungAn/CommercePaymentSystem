package com.example.commercepaymentsystem.domain.order.controller;

import com.example.commercepaymentsystem.common.response.ApiResponse;
import com.example.commercepaymentsystem.domain.order.dto.OrderCreateRequest;
import com.example.commercepaymentsystem.domain.order.dto.OrderCreateResponse;
import com.example.commercepaymentsystem.domain.order.service.OrderService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderCreateResponse>> createOrder(
            @AuthenticationPrincipal Long memberId,
            @RequestBody(required = false) OrderCreateRequest request
    ) {
        OrderCreateRequest safeRequest = request == null ? new OrderCreateRequest(null) : request;
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(orderService.createOrder(memberId, safeRequest)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<OrderSummaryResponse>>> getMyOrders(
            @AuthenticationPrincipal Long memberId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getMyOrders(memberId, page, size)));
    }
}
