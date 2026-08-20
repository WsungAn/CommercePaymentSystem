package com.example.commercepaymentsystem.domain.product.service;

import com.example.commercepaymentsystem.common.exception.BusinessException;
import com.example.commercepaymentsystem.common.exception.ErrorCode;
import com.example.commercepaymentsystem.common.response.PageResponse;
import com.example.commercepaymentsystem.domain.product.dto.ProductResponse;
import com.example.commercepaymentsystem.domain.product.dto.ProductSearchCondition;
import com.example.commercepaymentsystem.domain.product.entity.Product;
import com.example.commercepaymentsystem.domain.product.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private static Product product(Long id, String name, int price, int stock, String category) {
        Product product = new Product(name, price, stock, "설명", category);
        ReflectionTestUtils.setField(product, "id", id);
        return product;
    }

    @Test
    @DisplayName("findAll: 조회 결과를 PageResponse로 변환하고 페이지 정보를 함께 담는다")
    void findAll_변환() {
        Pageable pageable = PageRequest.of(0, 2);
        List<Product> products = List.of(
                product(1L, "무선 마우스", 15_000, 10, "전자기기"),
                product(2L, "기계식 키보드", 89_000, 3, "전자기기")
        );
        given(productRepository.findAll(any(Specification.class), eq(pageable)))
                .willReturn(new PageImpl<>(products, pageable, 5));

        PageResponse<ProductResponse> result =
                productService.findAll(new ProductSearchCondition("전자기기", null, null), pageable);

        assertThat(result.content()).hasSize(2);
        assertThat(result.content().get(0).id()).isEqualTo(1L);
        assertThat(result.content().get(0).name()).isEqualTo("무선 마우스");
        assertThat(result.content().get(0).price()).isEqualTo(15_000);
        assertThat(result.content().get(0).stock()).isEqualTo(10);
        assertThat(result.content().get(0).category()).isEqualTo("전자기기");
        assertThat(result.content().get(1).name()).isEqualTo("기계식 키보드");
        assertThat(result.page()).isZero();
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.totalElements()).isEqualTo(5);
        assertThat(result.totalPages()).isEqualTo(3);
    }

    @Test
    @DisplayName("findById: 상품을 찾아 ProductResponse로 변환한다")
    void findById_성공() {
        given(productRepository.findById(1L))
                .willReturn(Optional.of(product(1L, "무선 마우스", 15_000, 10, "전자기기")));

        ProductResponse result = productService.findById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("무선 마우스");
        assertThat(result.price()).isEqualTo(15_000);
        assertThat(result.stock()).isEqualTo(10);
        assertThat(result.description()).isEqualTo("설명");
        assertThat(result.category()).isEqualTo("전자기기");
    }

    @Test
    @DisplayName("findById: 상품이 없으면 PRODUCT_NOT_FOUND 예외가 발생한다")
    void findById_없음() {
        given(productRepository.findById(999L)).willReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> productService.findById(999L));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
    }

    @Test
    @DisplayName("validateStock: 재고가 충분하면 true, 부족하면 false를 반환한다")
    void validateStock() {
        Product product = product(1L, "무선 마우스", 15_000, 10, "전자기기");

        assertThat(productService.validateStock(9, product)).isTrue();
        assertThat(productService.validateStock(10, product)).isTrue();
        assertThat(productService.validateStock(11, product)).isFalse();
    }
}
