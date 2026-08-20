package com.example.commercepaymentsystem.domain.cart.service;

import com.example.commercepaymentsystem.common.exception.BusinessException;
import com.example.commercepaymentsystem.common.exception.ErrorCode;
import com.example.commercepaymentsystem.domain.cart.dto.CartItemResponse;
import com.example.commercepaymentsystem.domain.cart.dto.CartResponse;
import com.example.commercepaymentsystem.domain.cart.entity.Cart;
import com.example.commercepaymentsystem.domain.cart.entity.CartItem;
import com.example.commercepaymentsystem.domain.cart.repository.CartItemRepository;
import com.example.commercepaymentsystem.domain.product.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Transactional
public class CartItemService {

    private final CartItemRepository cartItemRepository;

    // cart_id + product_id 조합으로 장바구니 수량 증감
    public void addCartItem(Cart cart, Product product, int quantity) {

        // Cart와 Product조합의 CartItem 이 있는지 확인
        Optional<CartItem> foundCartItem = cartItemRepository.findByCartAndProduct(cart, product);

        // Cart와 Product조합이 없을 경우 생성
        if (foundCartItem.isEmpty()) {
            CartItem cartItem = new CartItem(cart, product, quantity);
            cartItemRepository.save(cartItem);
            return;
        }

        // Cart와 Product조합이 있을 경우 수량을 더한다.
        foundCartItem.get().addQuantity(quantity);
        cartItemRepository.save(foundCartItem.get());

    }

    // 장바구니에 담긴 상품이 몇개 담겨있는지 조회
    @Transactional(readOnly = true)
    public int getExistingQuantity(Cart cart, Product product) {

        return cartItemRepository.countQuantityByCartAndProduct(cart, product );
    }

    // 장바구니에 담긴 상품들을 조회
    @Transactional(readOnly = true)
    public CartResponse getCartItems(Cart cart) {
        // 카트에 담긴 cartItem들을 가져온다.
        List<CartItem> cartItems = cartItemRepository.findByCart(cart);

        // 장바구니는 존재하지만 안에 상품이 하나도 없을 경우
        if (cartItems.isEmpty()) {
            return new CartResponse(List.of(), 0);
        }

        // 장바구니에 들어있는 상품들의 총 가격을 구함
        int cartTotalPrice = cartItems.stream()
                // CartItem에 있는 상품을 꺼냄
                .map(CartItem::getProduct)
                // 상품의 가격을 꺼냄
                .mapToInt(Product::getPrice)
                // 모두 더함
                .sum();

        // CartItem 타입을 Return 타입으로 변환
        List<CartItemResponse> cartItemResponses = cartItems.stream()
                .map(CartItemResponse::from)
                .toList();

        return new CartResponse(cartItemResponses, cartTotalPrice);
    }

    // 수량변경을 요청한 사용자의 CartId와 변경 대상 cartItemId가 모두 일치하는지 확인
    @Transactional(readOnly = true)
    public CartItem checkCartItemAuthor(Long id, Cart cart) {

        Optional<CartItem> cartItem = cartItemRepository
                .findByIdAndCartId(id,cart );

        //  존재하지 않는다면 그건 본인 소유의 cartId가 아님
        if (cartItem.isEmpty()) {
            throw new BusinessException(ErrorCode.NO_AUTHORITY);
        }

        return cartItem.get();
    }

    // cartItem의 수량을 변경한다.
    public void changeCartItem(CartItem cartItem, int changeQuantity) {
        cartItem.changeQuantity(changeQuantity);
        cartItemRepository.save(cartItem);
    }

    // 지정한 cartItem을 삭제한다.
    public void deleteCartItem(CartItem cartItem) {
        cartItemRepository.delete(cartItem);
    }

    // 장바구니의 모든 cartItem을 삭제한다.
    public void deleteCartItems(Cart cart) {
        cartItemRepository.deleteAllByCart(cart);
    }

}
