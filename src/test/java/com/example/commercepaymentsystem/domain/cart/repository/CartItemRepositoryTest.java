package com.example.commercepaymentsystem.domain.cart.repository;


import com.example.commercepaymentsystem.config.JpaConfig;
import com.example.commercepaymentsystem.domain.cart.entity.Cart;
import com.example.commercepaymentsystem.domain.cart.entity.CartItem;
import com.example.commercepaymentsystem.domain.fixture.CartFixture;
import com.example.commercepaymentsystem.domain.fixture.CartItemFixture;
import com.example.commercepaymentsystem.domain.fixture.ProductFixture;
import com.example.commercepaymentsystem.domain.member.entity.Member;
import com.example.commercepaymentsystem.domain.member.repository.MemberRepository;
import com.example.commercepaymentsystem.domain.product.entity.Product;
import com.example.commercepaymentsystem.domain.product.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.*;


@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaConfig.class)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:productdb;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.sql.init.mode=never"
})
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

    // 테스트가 한 번에 여러 개 돌려도 email/phone 충돌 나지 않게 유니크 보장
    private static final AtomicLong SEQ = new AtomicLong(0);

    private String uniqueEmail(String prefix) {
        return prefix + "+" + SEQ.incrementAndGet() + "@test.com";
    }

    private String uniquePhone(String prefix) {
        // 전화번호 형식/길이가 엔티티 제약과 맞는지 확인해 주세요.
        return prefix + "-" + SEQ.incrementAndGet();
    }

    private Member saveMember(String name, String emailPrefix, String phonePrefix) {
        Member member = new Member(
                name,
                uniqueEmail(emailPrefix),
                "123456",                 // password 위치
                uniquePhone(phonePrefix) // phoneNumber 위치
        );
        return memberRepository.save(member);
    }

    @Test
    @DisplayName("장바구니에 담긴 상품의 수량이 정상적으로 잘 카운팅 된다.")
    void sumQuantityByCartAndProduct_returnsCorrectSum() {

        // Given
        Member member = saveMember("memberA", "memberAA", "010-1111");
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
        Member member = saveMember("memberA", "memberAB", "010-1112");
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
        assertThat(getCartItem.get().getId()).isEqualTo(cartItem.getId());
    }

    @Test
    @DisplayName("Cart와 Product조합의 CartItem 이 없으면 null을 리턴한다.")
    void findByCartAndProduct_returnNullCartItem() {

        // Given
        Member member = saveMember("memberA", "memberAC", "010-1113");
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
        Member member1 = saveMember("memberA", "memberAD", "010-1114");
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
        assertThat(cartItems.get(0).getId()).isEqualTo(cart1.getId());
        assertThat(cartItems.get(0).getProduct().getName()).isEqualTo("test1");
        assertThat(cartItems.get(1).getProduct().getName()).isEqualTo("test2");
    }

    @Test
    @DisplayName("장바구니에 담긴 상품들이 없을경우 빈 배열을 리턴한다.")
    void findByCart_returnNull() {
        // Given
        Member member = saveMember("memberA", "memberAE", "010-1115");
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
        Member member = saveMember("memberA", "memberAF", "010-1116");
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
        assertThat(getCartItem.get().getId()).isEqualTo(cartItem.getId());
    }

    @Test
    @DisplayName("CartId와 cartItemId 가 일치하지 않으면 null을 리턴한다.")
    void findByIdAndCartId_returnNull() {
        // Given
        Member member1 = saveMember("memberA", "memberAG", "010-1116");

        Member member2 = saveMember("memberB", "memberBA", "010-1117");
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
        Member member1 = saveMember("memberA", "memberAH", "010-1118");
        Member member2 = saveMember("memberB", "memberBB", "010-1119");

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
        Member member1 = saveMember("memberA", "memberAI", "010-1120");
        Member member2 = saveMember("memberB", "memberBC", "010-1121");
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