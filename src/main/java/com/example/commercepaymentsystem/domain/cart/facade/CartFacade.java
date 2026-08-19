package com.example.commercepaymentsystem.domain.cart.facade;

import com.example.commercepaymentsystem.domain.cart.dto.CartResponse;
import com.example.commercepaymentsystem.domain.cart.entity.Cart;
import com.example.commercepaymentsystem.domain.cart.service.CartItemService;
import com.example.commercepaymentsystem.domain.cart.service.CartService;
import com.example.commercepaymentsystem.domain.member.entity.Member;
import com.example.commercepaymentsystem.domain.product.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Transactional
public class CartFacade {

    // 상품 담기를 시도한 사용자가 유효한지, 장바구니를 가지고 있는지  등 검증을 위해 사용
    private final MemberService memberService;
    // 담았던 상품이 유효한지, 장바구니에 담은 수량아 현재 상품 재고 수량보다 많지 않는지 검증을 위해 사용
    private final ProductService productService;
    // 사용자가 가진 장바구니 확인 및 장바구니 생성을 위해 사용
    private final CartService cartService;
    // 사용자가 담은 상품과 수량을 확인하기 위해 사용
    private final CartItemService cartItemService;

    // 상품 담기 요청
    public void addProductToCart(
            Long memberId, Long productId, int quantity)
    {
        // 담으려는 상품이 0개 이하인지 확인
        if (quantity <= 0) {
            // INVALID_CART_QUANTITY 변경 필요
            throw new IllegalArgumentException("Quantity should be greater than 0");
        }

        // 요청한 멤버가 있는지 확인
        Member member = memberService.findMember(memberId);

        // 담으려는 상품이 있는지 확인
        Product product = productService.findProduct(productId);

        // 유저의 장바구니를 꺼내옴 없으면 장바구니 생성
        Cart cart = cartService.getOrCreateCart(member);

        // 장바구니에 담기 상품의 재고보다 장바구니에 담은 수량이 더 많은지 검증하는 로직
        int currentCartItemQuantity = cartItemService.getExistingQuantity(cart, product);
        int totalQuantity = currentCartItemQuantity + quantity;
        // 상품 구현이 완료되면 재고 검증을 하는 로직 사용
        productService.validateStock(totalQuantity);

        // cart_id + product_id 조합으로 장바구니 수량 증감
        cartItemService.addCartItem(cart, product, quantity);

    }

    public CartResponse getCart(Long memberId) {

        // 요청한 멤버가 있는지 확인
        memberService.findMember(memberId);

        // 유저의 장바구니를 꺼내옴 없으면 장바구니 생성


        // 장바구니에 있는 상품 정보들을 가져온현
        List<Long> productIds = new ArrayList<>();

        return null;
    }
}
