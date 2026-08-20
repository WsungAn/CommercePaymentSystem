package com.example.commercepaymentsystem.domain.order.facade;

import com.example.commercepaymentsystem.common.exception.BusinessException;
import com.example.commercepaymentsystem.common.exception.ErrorCode;

import com.example.commercepaymentsystem.domain.cart.entity.CartItem;
import com.example.commercepaymentsystem.domain.cart.service.CartService;
import com.example.commercepaymentsystem.domain.order.dto.OrderPreviewResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)

//장바구니에 있는 정보를 조회해서 주문서 페이지를 만드는것
//orderfacade는 카트보다 오더 도메인 쪽에 있는게 맞다
public class OrderFacade {
    private final CartService cartService;

    public OrderPreviewResponse getCheckout(Long memberId, List<Long> cartItemIds) {
//주문서 미리보기 -> 재고 차감/ 주문 생성 없는 읽기 전용
        //cartItemIds 가 비어있으면 null -> 전체 장바구니를 의미 , 값이 있으면 선택된 아이템만 주문서에 담는다

        List<CartItem> cartItems =getValidateCartitems(
                memberId, cartItemIds != null ? cartItemIds : List.of()  //null 세이프 하게 짠 코드
        );
        //장바구니 아이템에서 상품 가격과 장바구니 수량을 곱해서 각 이ㅏ이템의 총액을 구한다.

        List<OrderPreviewResponse.OrderPreviewItemResponse> items = cartItems.stream()
                .map(cartItem -> {  //map -> cartItem 타입을 OrderPreviewItemResponse 타입으로 바꾸기 위해서
                    int price = cartItem.getProduct().getPrice();
                    int subtotal = price * cartItem.getQuantity();
                    return new OrderPreviewResponse.OrderPreviewItemResponse(
                            cartItem.getId(),
                            cartItem.getProduct().getName(),
                            price,
                            cartItem.getQuantity(),
                            subtotal
                    );
                })
                .toList();

        //장바구니 주문 총액을 구한다.OrderPreviewResponse.OrderPreviewItemResponse의 subtotal을 모두 더한다.
        int totalPrice = items.stream()
                .mapToInt(OrderPreviewResponse.OrderPreviewItemResponse::subtotal)
                .sum();
        return new OrderPreviewResponse(items, totalPrice);
    }

    private List<CartItem> getValidateCartitems(Long memberId, List<Long> cartItemIds) {
        //cartItemIds 가 비어있으면 -> 전체 장바구니 , 아니면 선택된 아이템만  조회
        List<CartItem> cartItems = cartItemIds.isEmpty()
                ? cartService.findCartEntities(memebrId)   //member는 소유권을 위해 던져주기 /장바구니 전체를 다 찾아서
                : cartService.findCartEntitiesByIds(memebrId, cartItemIds); //선택된 장바구니 아이템까지 다 던져주기

        //1차 검증 -> 주문할 아이템이 하나도 없으면 X
        //전체 조회 : 빈 장바구니 / 선택 조회 : 넘긴 ID가 전부 남의 것 or 없는 것일 때도 여기로 떨어진다.

        if (cartItems.isEmpty()) {
            throw new BusinessException(ErrorCode.CART_EMPTY);
        }

        //2차 검증 -> 요천한 ID 개수와 조회된 개수가 다르다 -> 일부가 남의 것 또는 존재하지 않는 ID
        //->일부만 주문되는 상황을 막고, 명시적으로 에러를 던진다.
        if (!cartItemIds.isEmpty() && cartItems.size() != cartItemIds.size()) {
            throw new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND);
        }
        return cartItems;
    }
}
