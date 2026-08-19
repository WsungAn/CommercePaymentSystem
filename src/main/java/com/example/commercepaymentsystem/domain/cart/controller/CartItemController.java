package com.example.commercepaymentsystem.domain.cart.controller;

import com.example.commercepaymentsystem.common.response.ApiResponse;
import com.example.commercepaymentsystem.domain.cart.dto.AddCartItemRequest;
import com.example.commercepaymentsystem.domain.cart.dto.CartResponse;
import com.example.commercepaymentsystem.domain.cart.facade.CartFacade;
import com.example.commercepaymentsystem.domain.cart.service.CartItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController("/api/carts")
public class CartItemController {

    private final CartFacade cartFacade;

    // 상품을 장바구니에 담는다.
    @PostMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> addCartItem(
            // JWT에서 어떤걸 저장하내에 따라 변경 필요
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long productId,
            @RequestBody AddCartItemRequest request
    ){
        cartFacade.addProductToCart(memberId, productId, request.quantity());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok());
    }

    // 장바구니 정보(상품, 수량, 가격)를 전달한다.
    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            // JWT에서 어떤걸 저장하내에 따라 변경 필요
            @AuthenticationPrincipal Long memberId
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.ok(cartFacade.getCart(memberId)));
    }

}
