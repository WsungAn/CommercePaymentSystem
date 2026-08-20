package com.example.commercepaymentsystem.domain.product.repository;

import com.example.commercepaymentsystem.config.JpaConfig;
import com.example.commercepaymentsystem.domain.product.dto.ProductSearchCondition;
import com.example.commercepaymentsystem.domain.product.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 필터·페이징을 실제 DB에 데이터를 넣고 검증한다.
 * 조건이 어떻게 조립되는지가 아니라, 조립된 조건이 실제로 올바른 결과를 거르는지를 본다.
 *
 * <p>인메모리 H2 를 쓰므로 Docker 도, 사전 준비도 필요 없다.
 * 엔티티에 int UNSIGNED 같은 MySQL 전용 columnDefinition 이 있어 MODE=MySQL 이 필요하다.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaConfig.class) // 감사(auditing)를 켜야 created_at(NOT NULL)이 채워진다
// 개발용 commerce_db 를 건드리지 않도록 인메모리 H2 로 격리한다.
// application.yml 의 MySQL 접속 정보와 MySQLDialect 를 모두 덮어써야 한다.
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:productdb;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@DisplayName("상품 필터·페이징 (H2)")
class ProductRepositoryFilterPagingTest {

    @Autowired
    private ProductRepository productRepository;

    // 가격 오름차순: 마우스15,000 < 티셔츠19,000 < 자바책32,000 < 스프링책38,000
    //             < 이어폰45,000 < 키보드89,000 < 자켓120,000 < 모니터250,000
    @BeforeEach
    void setUp() {
        productRepository.saveAll(List.of(
                new Product("무선 마우스", 15_000, 10, "저소음", "전자기기"),
                new Product("기계식 키보드", 89_000, 5, "청축", "전자기기"),
                new Product("4K 모니터", 250_000, 2, "27인치", "전자기기"),
                new Product("무선 이어폰", 45_000, 8, "노이즈 캔슬링", "전자기기"),
                new Product("반팔 티셔츠", 19_000, 30, "면 100%", "의류"),
                new Product("경량 자켓", 120_000, 7, "방풍", "의류"),
                new Product("자바의 정석", 32_000, 15, "기초", "도서"),
                new Product("스프링 인 액션", 38_000, 12, "실전", "도서")
        ));
        productRepository.flush();
    }

    private Page<Product> search(ProductSearchCondition condition, Pageable pageable) {
        Specification<Product> spec = ProductSpecification.search(condition);
        return productRepository.findAll(spec, pageable);
    }

    private static Pageable byPriceAsc(int page, int size) {
        return PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "price"));
    }

    private static ProductSearchCondition condition(String category, Integer min, Integer max) {
        return new ProductSearchCondition(category, min, max);
    }

    @Nested
    @DisplayName("필터")
    class Filter {

        @Test
        @DisplayName("조건이 없으면 전체 상품이 조회된다")
        void 조건_없음() {
            Page<Product> result = search(condition(null, null, null), byPriceAsc(0, 20));

            assertThat(result.getTotalElements()).isEqualTo(8);
        }

        @Test
        @DisplayName("카테고리로 필터하면 해당 카테고리만 조회된다")
        void 카테고리_필터() {
            Page<Product> result = search(condition("전자기기", null, null), byPriceAsc(0, 20));

            assertThat(result.getTotalElements()).isEqualTo(4);
            assertThat(result.getContent())
                    .extracting(Product::getCategory)
                    .containsOnly("전자기기");
            assertThat(result.getContent())
                    .extracting(Product::getName)
                    .containsExactly("무선 마우스", "무선 이어폰", "기계식 키보드", "4K 모니터");
        }

        @Test
        @DisplayName("일치하는 상품이 없으면 빈 결과가 조회된다")
        void 매칭_없음() {
            Page<Product> result = search(condition("가전", null, null), byPriceAsc(0, 20));

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
            assertThat(result.getTotalPages()).isZero();
        }

        @Test
        @DisplayName("최소 가격은 경계값을 포함한다 (price >= minPrice)")
        void 최소가격_경계포함() {
            Page<Product> result = search(condition(null, 45_000, null), byPriceAsc(0, 20));

            assertThat(result.getContent())
                    .extracting(Product::getName)
                    .containsExactly("무선 이어폰", "기계식 키보드", "경량 자켓", "4K 모니터");
            assertThat(result.getContent())
                    .allSatisfy(product -> assertThat(product.getPrice()).isGreaterThanOrEqualTo(45_000));
        }

        @Test
        @DisplayName("최대 가격은 경계값을 포함한다 (price <= maxPrice)")
        void 최대가격_경계포함() {
            Page<Product> result = search(condition(null, null, 32_000), byPriceAsc(0, 20));

            assertThat(result.getContent())
                    .extracting(Product::getName)
                    .containsExactly("무선 마우스", "반팔 티셔츠", "자바의 정석");
            assertThat(result.getContent())
                    .allSatisfy(product -> assertThat(product.getPrice()).isLessThanOrEqualTo(32_000));
        }

        @Test
        @DisplayName("최소·최대 가격을 함께 주면 범위 안의 상품만 조회된다")
        void 가격_범위() {
            Page<Product> result = search(condition(null, 19_000, 89_000), byPriceAsc(0, 20));

            assertThat(result.getContent())
                    .extracting(Product::getName)
                    .containsExactly("반팔 티셔츠", "자바의 정석", "스프링 인 액션", "무선 이어폰", "기계식 키보드");
        }

        @Test
        @DisplayName("카테고리와 가격 조건은 AND로 결합된다")
        void 카테고리_가격_조합() {
            Page<Product> result = search(condition("전자기기", null, 50_000), byPriceAsc(0, 20));

            assertThat(result.getContent())
                    .extracting(Product::getName)
                    .containsExactly("무선 마우스", "무선 이어폰");
        }
    }

    @Nested
    @DisplayName("페이징")
    class Paging {

        @Test
        @DisplayName("첫 페이지는 요청한 크기만큼 자르고 전체 개수·페이지 수를 함께 반환한다")
        void 첫_페이지() {
            Page<Product> result = search(condition(null, null, null), byPriceAsc(0, 3));

            assertThat(result.getContent())
                    .extracting(Product::getName)
                    .containsExactly("무선 마우스", "반팔 티셔츠", "자바의 정석");
            assertThat(result.getNumber()).isZero();
            assertThat(result.getSize()).isEqualTo(3);
            assertThat(result.getTotalElements()).isEqualTo(8);
            assertThat(result.getTotalPages()).isEqualTo(3); // 3 + 3 + 2
            assertThat(result.isFirst()).isTrue();
            assertThat(result.isLast()).isFalse();
        }

        @Test
        @DisplayName("마지막 페이지는 남은 개수만 반환한다")
        void 마지막_페이지() {
            Page<Product> result = search(condition(null, null, null), byPriceAsc(2, 3));

            assertThat(result.getContent())
                    .extracting(Product::getName)
                    .containsExactly("경량 자켓", "4K 모니터");
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.isLast()).isTrue();
        }

        @Test
        @DisplayName("범위를 벗어난 페이지는 빈 목록이지만 전체 개수는 그대로다")
        void 범위_밖_페이지() {
            Page<Product> result = search(condition(null, null, null), byPriceAsc(5, 3));

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(8);
            assertThat(result.getTotalPages()).isEqualTo(3);
        }

        @Test
        @DisplayName("정렬 방향을 바꾸면 순서가 뒤집힌다")
        void 정렬_내림차순() {
            Page<Product> result = productRepository.findAll(
                    ProductSpecification.search(condition(null, null, null)),
                    PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "price")));

            assertThat(result.getContent())
                    .extracting(Product::getName)
                    .containsExactly("4K 모니터", "경량 자켓", "기계식 키보드");
        }

        @Test
        @DisplayName("필터와 페이징을 함께 쓰면 전체 개수가 필터된 개수를 따른다")
        void 필터_페이징_조합() {
            Page<Product> result = search(condition("전자기기", null, null), byPriceAsc(0, 2));

            // 전체 8개가 아니라 필터된 4개를 기준으로 계산돼야 한다
            assertThat(result.getTotalElements()).isEqualTo(4);
            assertThat(result.getTotalPages()).isEqualTo(2);
            assertThat(result.getContent())
                    .extracting(Product::getName)
                    .containsExactly("무선 마우스", "무선 이어폰");
        }
    }
}
