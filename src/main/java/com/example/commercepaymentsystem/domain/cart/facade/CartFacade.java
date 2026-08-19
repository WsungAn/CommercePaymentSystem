package com.example.commercepaymentsystem.domain.cart.facade;

import com.example.commercepaymentsystem.common.exception.BusinessException;
import com.example.commercepaymentsystem.common.exception.ErrorCode;
import com.example.commercepaymentsystem.domain.cart.dto.CartResponse;
import com.example.commercepaymentsystem.domain.cart.entity.Cart;
import com.example.commercepaymentsystem.domain.cart.entity.CartItem;
import com.example.commercepaymentsystem.domain.cart.service.CartItemService;
import com.example.commercepaymentsystem.domain.cart.service.CartService;
import com.example.commercepaymentsystem.domain.member.entity.Member;
import com.example.commercepaymentsystem.domain.product.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CartFacade {

    // 상품 담기를 시도한 사용자가 유효한지, 장바구니를 가지고 있는지  등 검증을 위해 사용
    private final MemberService memberService;
    // 담았던 상품이 유효한지, 장바구니에 담은 수량아 현재 상품 재고 수량보다 많지 않는지 검증을 위해 사용
    private final ProductService productService;
    // 사용자가 가진 장바구니 확인 및 장바구니 생성을 위해 사용
    private final CartService cartService;
    // 사용자가 담은 상품과 수량을 확인하기 위해 사용
    private final CartItemService cartItemService;

    // 상품 담기 요청, 요청한 수량만큼 상품을 담는다
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

        // 장바구니에 넣은 수량이 현재 상품의 재고보다 많음
        if (productService.validateStock(totalQuantity, product )){
            throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK);
        }

        // cart_id + product_id 조합으로 장바구니 수량 증감
        cartItemService.addCartItem(cart, product, quantity);

    }

    // 장바구니 조회 요청 상품정보(id,name,price)와 수량, 총 가격을 전달한다.
    public CartResponse getCart(Long memberId) {

        // 유저의 장바구니를 꺼내옴
        Optional<Cart> cart = cartService.getCart(memberId);

        // 장바구니가 없는 사용자라면 그냥 빈 상품과 총금액 0원으로 전달한다.
        if (cart.isEmpty()) {
            // 빈 List가 아닌 null로 전달하면 프론트에서 한번 더 로직을 타야되서 힘듦
            return new CartResponse(List.of(),0);
        }

        // 사용자의 장바구니에 있는 상품 정보를 dto로 변환
        return cartItemService.getCartItems(cart.get()) ;
    }

    // 장바구니에 들어있는 상품의 수령을 변경한다.
    public void changeProductToCart(Long memberId,  Long cartItemId, int changeQuantity )
    {

        // 요청한 사용자가 소지하고 있는 장바구니를 꺼내옴
        Optional<Cart> cart = cartService.getCart(memberId);

        // 해당 사용자의 장바구니가 존재하지 않는다면 예외
        if (cart.isEmpty()) {
            throw new BusinessException(ErrorCode.CART_EMPTY);
        }

        // 변경 요청한 cartItemId이 요청한 사용자 소유인지 확인 후 가져다줌
        CartItem cartItem = cartItemService.checkCartItemAuthor(cartItemId, cart.get());

        // 변경하려는 수량이 현재 상품의 재고보다 많음
        if (productService.validateStock(changeQuantity, cartItem.getProduct() )){
            throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK);
        }

        // 본인 소유의 cartItem이 맞고 수량도 일치하다면 변경 진행
        cartItemService.changeCartItem(cartItem, changeQuantity);

    }

    // 장바구니에 들어있는 상품을 삭제한다.
    public void deleteProductToCart(Long memberId, Long cartItemId) {

        // 요청한 사용자가 소지하고 있는 장바구니를 꺼내옴
        Optional<Cart> cart = cartService.getCart(memberId);

        // 해당 사용자의 장바구니가 존재하지 않는다면 예외
        if (cart.isEmpty()) {
            throw new BusinessException(ErrorCode.CART_EMPTY);
        }

        // 변경 요청한 cartItemId이 요청한 사용자 소유인지 확인 후 가져다줌
        CartItem cartItem = cartItemService.checkCartItemAuthor(cartItemId, cart.get());

        cartItemService.deleteCartItem(cartItem);
    }

    // 장바구니에 들어있는 "모든" 상품을 삭제한다.
    public void deleteAllProductToCart(Long memberId) {

        // 요청한 사용자가 소지하고 있는 장바구니를 꺼내옴
        Optional<Cart> cart = cartService.getCart(memberId);

        // 해당 사용자의 장바구니가 존재하지 않는다면 예외
        if (cart.isEmpty()) {
            throw new BusinessException(ErrorCode.CART_EMPTY);
        }

        cartItemService.deleteCartItems(cart.get());
    }
}
