package com.example.commercepaymentsystem.domain.product.controller;

import com.example.commercepaymentsystem.common.exception.BusinessException;
import com.example.commercepaymentsystem.common.jwt.JwtProvider;
import com.example.commercepaymentsystem.config.SecurityConfig;
import com.example.commercepaymentsystem.common.exception.ErrorCode;
import com.example.commercepaymentsystem.common.response.PageResponse;
import com.example.commercepaymentsystem.domain.product.dto.ProductResponse;
import com.example.commercepaymentsystem.domain.product.dto.ProductSearchCondition;
import com.example.commercepaymentsystem.domain.product.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// @WebMvcTest 슬라이스는 앱의 SecurityConfig 대신 Security 자동설정의 기본 체인(전부 인증 요구)을
// 적용한다. 그대로 두면 모든 요청이 401이 되므로 실제 SecurityConfig를 import 한다.
// 필터를 끄는 대신 import 하는 이유: /products/** 가 정말 공개 경로인지도 함께 검증된다.
@WebMvcTest(ProductController.class)
@Import(SecurityConfig.class)
@DisplayName("ProductController")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    // 슬라이스는 Filter 빈(JwtAuthFilter)을 포함하지만 그 의존성인 JwtProvider는 포함하지 않는다.
    // 채워주지 않으면 컨텍스트 로딩이 실패한다.
    @MockitoBean
    private JwtProvider jwtProvider;

    private static ProductResponse response(Long id, String name, int price, int stock, String category) {
        return new ProductResponse(id, name, price, stock, "설명", category);
    }

    @Test
    @DisplayName("GET /products: 상품 목록을 ApiResponse + PageResponse 형태로 반환한다")
    void 목록_조회() throws Exception {
        PageResponse<ProductResponse> page = new PageResponse<>(
                List.of(response(1L, "무선 마우스", 15_000, 10, "전자기기")), 0, 10, 1, 1);
        given(productService.findAll(any(ProductSearchCondition.class), any(Pageable.class))).willReturn(page);

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").doesNotExist())
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].name").value("무선 마우스"))
                .andExpect(jsonPath("$.data.content[0].price").value(15_000))
                .andExpect(jsonPath("$.data.content[0].stock").value(10))
                .andExpect(jsonPath("$.data.content[0].category").value("전자기기"))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(10))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.totalPages").value(1));
    }

    @Test
    @DisplayName("GET /products: 검색 조건 쿼리 파라미터가 ProductSearchCondition으로 바인딩된다")
    void 검색조건_바인딩() throws Exception {
        given(productService.findAll(any(ProductSearchCondition.class), any(Pageable.class)))
                .willReturn(new PageResponse<>(List.of(), 0, 10, 0, 0));

        mockMvc.perform(get("/products")
                        .param("category", "전자기기")
                        .param("minPrice", "10000")
                        .param("maxPrice", "50000"))
                .andExpect(status().isOk());

        ArgumentCaptor<ProductSearchCondition> conditionCaptor =
                ArgumentCaptor.forClass(ProductSearchCondition.class);
        verify(productService).findAll(conditionCaptor.capture(), any(Pageable.class));

        ProductSearchCondition condition = conditionCaptor.getValue();
        assertThat(condition.category()).isEqualTo("전자기기");
        assertThat(condition.minPrice()).isEqualTo(10_000);
        assertThat(condition.maxPrice()).isEqualTo(50_000);
    }

    @Test
    @DisplayName("GET /products: 페이징 파라미터가 없으면 size 10, createdAt DESC 기본값이 적용된다")
    void 페이징_기본값() throws Exception {
        given(productService.findAll(any(ProductSearchCondition.class), any(Pageable.class)))
                .willReturn(new PageResponse<>(List.of(), 0, 10, 0, 0));

        mockMvc.perform(get("/products")).andExpect(status().isOk());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(productService).findAll(any(ProductSearchCondition.class), pageableCaptor.capture());

        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(10);
        Sort.Order order = pageable.getSort().getOrderFor("createdAt");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    @DisplayName("GET /products/{id}: 상품이 없으면 404와 PRODUCT_001 에러 코드를 반환한다")
    void 단건_조회_없음() throws Exception {
        given(productService.findById(eq(999L)))
                .willThrow(new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        mockMvc.perform(get("/products/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("PRODUCT_001"))
                .andExpect(jsonPath("$.message").value(ErrorCode.PRODUCT_NOT_FOUND.getMessage()))
                .andExpect(jsonPath("$.data").doesNotExist());
    }
}
