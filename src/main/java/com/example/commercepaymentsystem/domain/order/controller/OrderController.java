package com.example.commercepaymentsystem.domain.order.controller;

import com.example.commercepaymentsystem.common.response.ApiResponse;
import com.example.commercepaymentsystem.common.response.PageResponse;
import com.example.commercepaymentsystem.domain.order.dto.OrderCreateRequest;
import com.example.commercepaymentsystem.domain.order.dto.OrderCreateResponse;
import com.example.commercepaymentsystem.domain.order.dto.OrderPreviewResponse;
import com.example.commercepaymentsystem.domain.order.dto.OrderSummaryResponse;
import com.example.commercepaymentsystem.domain.order.facade.OrderFacade;
import com.example.commercepaymentsystem.domain.order.service.OrderService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
//주문 생성 , 목록 , 상세 , 취소 담당
    private final OrderService orderService;
//장바구니 이용 -> 주문서 미리보기 담당
    private final OrderFacade orderFacade;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderCreateResponse>> createOrder(
            @AuthenticationPrincipal Long memberId,
            @RequestBody(required = false) OrderCreateRequest request
    ) {
        OrderCreateRequest safeRequest =
                request == null ? new OrderCreateRequest(null) : request;

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(orderService.createOrder(memberId, safeRequest)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<OrderSummaryResponse>>> getMyOrders(
            @AuthenticationPrincipal Long memberId,
            @RequestParam(defaultValue = "0") @Min(0)
            int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100)
            int size
    ) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getMyOrders(memberId, page, size)));
    }

    @GetMapping("/checkout")
    public ResponseEntity<ApiResponse<OrderPreviewResponse>> checkout(
            @AuthenticationPrincipal Long memberId, //멤버 토큰 꺼내오기
            @RequestParam(required = false)List<Long> cartItemIds
            ) {
        return ResponseEntity.ok(ApiResponse.ok(orderFacade.getCheckout(memberId, cartItemIds)));
    }
}
