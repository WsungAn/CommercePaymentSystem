package com.example.commercepaymentsystem.domain.order.controller;

import com.example.commercepaymentsystem.common.response.ApiResponse;
import com.example.commercepaymentsystem.common.response.PageResponse;
import com.example.commercepaymentsystem.domain.order.dto.*;
import com.example.commercepaymentsystem.domain.order.facade.OrderFacade;
import com.example.commercepaymentsystem.domain.order.service.OrderService;
import jakarta.validation.Valid;
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
                .body(ApiResponse.ok(orderFacade.createOrder(memberId,safeRequest)));
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

    // 주문하기 전 결제 미리보기 창을 보여줌 ( get이기 때문에 프론트에서 cartItemId를 body로 보내지 않고 쿼리 파라미터로 보냄)
    @GetMapping("/checkout")
    public ResponseEntity<ApiResponse<OrderPreviewResponse>> checkout(
            @AuthenticationPrincipal Long memberId, //멤버 토큰 꺼내오기
            @RequestParam(required = false)List<Long> cartItemIds
            ) {
        return ResponseEntity.ok(ApiResponse.ok(orderFacade.getCheckout(memberId, cartItemIds)));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderCancelResponse>> cancelOrder(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long orderId,
            @RequestBody(required = false)
            @Valid OrderCancelRequest request
    ) {
        String reason = request == null ? null : request.reason();

        return ResponseEntity.ok(
                ApiResponse.ok(
                        orderFacade.cancelOrder(
                                memberId,
                                orderId,
                                reason
                        )
                )
        );
    }
}
