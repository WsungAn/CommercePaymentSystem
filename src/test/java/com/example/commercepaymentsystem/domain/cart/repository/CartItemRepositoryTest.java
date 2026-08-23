package com.example.commercepaymentsystem.domain.cart.repository;


import com.example.commercepaymentsystem.domain.TestJpaConfig;
import com.example.commercepaymentsystem.domain.cart.entity.Cart;
import com.example.commercepaymentsystem.domain.cart.entity.CartItem;
import com.example.commercepaymentsystem.domain.fixture.CartFixture;
import com.example.commercepaymentsystem.domain.fixture.CartItemFixture;
import com.example.commercepaymentsystem.domain.fixture.MemberFixture;
import com.example.commercepaymentsystem.domain.fixture.ProductFixture;
import com.example.commercepaymentsystem.domain.member.entity.Member;
import com.example.commercepaymentsystem.domain.member.repository.MemberRepository;
import com.example.commercepaymentsystem.domain.product.entity.Product;
import com.example.commercepaymentsystem.domain.product.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;


@DataJpaTest
@ActiveProfiles("test")
@Import(TestJpaConfig.class)
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CartItemRepositoryTest {

    // JPQL과 같은 메서드를 실행하는 쿼리 담당, save()로 영속성 컨텍스트에 등록
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private MemberRepository memberRepository;
    // 엔티티를 DB에 반영(Flush 쿼리반영, 커밋인 안됨)하고 비우기(clear 영속성에서 비움 )
    @Autowired
    private TestEntityManager testEntityManager;

    @Test
    @DisplayName("장바구니에 담긴 상품의 수량이 정상적으로 잘 카운팅 된다.")
    void sumQuantityByCartAndProduct_returnsCorrectSum() {

        // Given
        Member member = memberRepository.save(MemberFixture.createMemberWithoutId());
        Cart cart = cartRepository.save(CartFixture.createCartWithoutId(member));
        Product product = productRepository.save(ProductFixture.createProductWithoutId());
        CartItem cartItem = cartItemRepository.save(CartItemFixture.createCartItemWithoutId(cart, product,5));
        cartItemRepository.save(cartItem);
        testEntityManager.flush();
        testEntityManager.clear();

        // When
        Integer count = cartItemRepository.sumQuantityByCartAndProduct(cart, product);

        // Then
        assertThat(count).isNotNull();
        assertThat(count).isEqualTo(5);
    }

    @Test
    @DisplayName("Cart와 Product조합의 CartItem 이 있으면 CartItem을 리턴한다.")
    void findByCartAndProduct_returnsCorrectCartItem() {
        // Given
        Member member = memberRepository.save(MemberFixture.createMemberWithoutId());
        Cart cart = cartRepository.save(CartFixture.createCartWithoutId(member));
        Product product = productRepository.save(ProductFixture.createProductWithoutId());
        CartItem cartItem = cartItemRepository.save(CartItemFixture.createCartItemWithoutId(cart, product,5));
        cartItemRepository.save(cartItem);
        testEntityManager.flush();
        testEntityManager.clear();

        // when
        Optional<CartItem> getCartItem = cartItemRepository.findByCartAndProduct(cart, product);

        // then
        assertThat(getCartItem).isNotNull();
        assertThat(getCartItem.get().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Cart와 Product조합의 CartItem 이 없으면 null을 리턴한다.")
    void findByCartAndProduct_returnNullCartItem() {

        // Given
        Member member = memberRepository.save(MemberFixture.createMemberWithoutId());
        Cart cart = cartRepository.save(CartFixture.createCartWithoutId(member));
        Product product = productRepository.save(ProductFixture.createProductWithoutId());

        // when
        Optional<CartItem> getCartItem = cartItemRepository.findByCartAndProduct(cart, product);

        // then
        assertThat(getCartItem).isEmpty();
    }

    @Test
    @DisplayName("장바구니에 담긴 상품들을 리턴한다.")
    void findByCart_returnCartItems() {

        // Given
        Member member1 =
                memberRepository.save(new Member("test1", "test1@test.com", "010-1234-5678", "123456"));

        Cart cart1 = cartRepository.save(new Cart(member1));

        Product product1 =
                productRepository.save(new Product("test1", 10_000, 10, "test1", "test1"));
        Product product2 =
                productRepository.save(new Product("test2", 20_000, 10, "test2", "test2"));

        cartItemRepository.save(new CartItem(cart1, product1, 5));
        cartItemRepository.save(new CartItem(cart1, product2, 5));
        testEntityManager.flush();
        testEntityManager.clear();

        // when
        List<CartItem> cartItems = cartItemRepository.findByCart(cart1);

        // then
        assertThat(cartItems).isNotNull();
        assertThat(cartItems.size()).isEqualTo(2);
        assertThat(cartItems.get(0).getId()).isEqualTo(1L);
        assertThat(cartItems.get(0).getProduct().getName()).isEqualTo("test1");
        assertThat(cartItems.get(1).getProduct().getName()).isEqualTo("test2");
    }

    @Test
    @DisplayName("장바구니에 담긴 상품들이 없을경우 빈 배열을 리턴한다.")
    void findByCart_returnNull() {
        // Given
        Member member = memberRepository.save(MemberFixture.createMemberWithoutId());
        Cart cart = cartRepository.save(CartFixture.createCartWithoutId(member));
        testEntityManager.flush();
        testEntityManager.clear();

        // when
        List<CartItem> cartItems = cartItemRepository.findByCart(cart);

        // then
        assertThat(cartItems).isEmpty();
    }

    @Test
    @DisplayName("CartId와 cartItemId가 모두 일치하면 정상적으로 CartItem 을 리턴한다.")
    void findByIdAndCartId_returnCartItem() {
        // Given
        Member member = memberRepository.save(MemberFixture.createMemberWithoutId());
        Cart cart = cartRepository.save(CartFixture.createCartWithoutId(member));
        Product product = productRepository.save(ProductFixture.createProductWithoutId());
        CartItem cartItem = cartItemRepository.save(CartItemFixture.createCartItemWithoutId(cart, product,5));
        cartItemRepository.save(cartItem);
        testEntityManager.flush();
        testEntityManager.clear();

        // when
        Optional<CartItem> getCartItem = cartItemRepository.findByIdAndCartId(cartItem.getId(), cart);

        // then
        assertThat(getCartItem).isNotNull();
        assertThat(getCartItem.get().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("CartId와 cartItemId 가 일치하지 않으면 null을 리턴한다.")
    void findByIdAndCartId_returnNull() {
        // Given
        Member member1 =
                memberRepository.save(new Member("test1", "test1@test.com", "010-1234-5678", "123456"));

        Member member2 =
                memberRepository.save(new Member("test2", "test2@test.com", "010-1234-5679", "123456"));
        Cart cart1 = cartRepository.save(new Cart(member1));
        Cart cart2 = cartRepository.save(new Cart(member2));
        Product product = productRepository.save(ProductFixture.createProductWithoutId());
        CartItem cartItem1 = cartItemRepository.save(CartItemFixture.createCartItemWithoutId(cart1, product,5));
        CartItem cartItem2 = cartItemRepository.save(CartItemFixture.createCartItemWithoutId(cart2, product,5));

        cartItemRepository.save(cartItem1);
        testEntityManager.flush();
        testEntityManager.clear();

        // when ( member1은 cartItem1 소유이고 cart2는 member2 소유  서로 다름)
        Optional<CartItem> getCartItem = cartItemRepository.findByIdAndCartId(cartItem1.getId(), cart2);

        // then
        assertThat(getCartItem).isEmpty();
    }

    @Test
    @DisplayName("선택한 장바구니에 있는 cartItems가 정상적으로 지워진다.")
    void findByCartId_deleteAll() {

        // Given
        Member member1 =
                memberRepository.save(new Member("test1", "test1@test.com", "010-1234-5678", "123456"));

        Member member2 =
                memberRepository.save(new Member("test2", "test2@test.com", "010-1234-5679", "123456"));
        Cart cart1 = cartRepository.save(new Cart(member1));
        Cart cart2 = cartRepository.save(new Cart(member2));
        Product product = productRepository.save(ProductFixture.createProductWithoutId());
        CartItem cartItem1 = cartItemRepository.save(CartItemFixture.createCartItemWithoutId(cart1, product,5));
        CartItem cartItem2 = cartItemRepository.save(CartItemFixture.createCartItemWithoutId(cart2, product, 5));

        cartItemRepository.save(cartItem1);
        testEntityManager.flush();
        testEntityManager.clear();


        // when (cart1 장바구니 삭제)
        cartItemRepository.deleteAllByCart(cart1);

        // then
        List<CartItem> cartItems = cartItemRepository.findByCart(cart1);
        assertThat(cartItems).isEmpty();

        List<CartItem> cartItems2 = cartItemRepository.findByCart(cart2);
        assertThat(cartItems2).isNotEmpty();
        assertThat(cartItems2).hasSize(1);

        // cartItem가 없어도 삭제시 문제가 발생하지 않음
        cartItemRepository.deleteAllByCart(cart1);
    }

    @Test
    @DisplayName("선택된 ProductId들만 List로 전달 한다.")
    void findSelectedForOrder_fetchesOnlyItemsBelongToCart() {

        // Given
        Member member1 =
                memberRepository.save(new Member("test1", "test1@test.com", "010-1234-5678", "123456"));

        Member member2 =
                memberRepository.save(new Member("test2", "test2@test.com", "010-1234-5679", "123456"));
        Cart cart1 = cartRepository.save(new Cart(member1));
        Cart cart2 = cartRepository.save(new Cart(member2));

        Product product1 =
                productRepository.save(new Product("test1", 10_000, 10, "test1", "test1"));
        Product product2 =
                productRepository.save(new Product("test2", 20_000, 10, "test2", "test2"));

         CartItem cartItem1 = cartItemRepository.save(new CartItem(cart1, product1, 5));
         CartItem cartItem2 = cartItemRepository.save(new CartItem(cart1, product2, 5));
         CartItem cartItem3 = cartItemRepository.save(new CartItem(cart2, product1, 5));
         List<Long> cartItemIds = List.of(cartItem1.getId(), cartItem2.getId(), cartItem3.getId());
         testEntityManager.flush();
         testEntityManager.clear();

        // When (cartItem을 3개 주긴 했지만 cart도 조건으로 함께 보기 때문에 2개가 나와여 됨
        List<CartItem> cartItems = cartItemRepository.findSelectedForOrder(cart1, cartItemIds);

        // Then
        assertThat(cartItems).isNotNull();
        assertThat(cartItems).isNotEmpty();
        assertThat(cartItems).hasSize(2);
        assertThat(cartItems.get(0).getProduct()).isNotNull();
        assertThat(cartItems.get(1).getProduct()).isNotNull();
        assertThat(cartItems.get(0).getProduct().getName()).isNotNull();
        assertThat(cartItems.get(1).getProduct().getName()).isNotNull();
        assertThat(cartItems.get(0).getCart().getId()).isEqualTo(cart1.getId());
        assertThat(cartItems.get(1).getCart().getId()).isEqualTo(cart1.getId());
    }

}