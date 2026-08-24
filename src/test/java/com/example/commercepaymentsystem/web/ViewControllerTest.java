package com.example.commercepaymentsystem.web;

import com.example.commercepaymentsystem.common.exception.BusinessException;
import com.example.commercepaymentsystem.common.exception.ErrorCode;
import com.example.commercepaymentsystem.common.jwt.JwtProvider;
import com.example.commercepaymentsystem.common.response.PageResponse;
import com.example.commercepaymentsystem.config.SecurityConfig;
import com.example.commercepaymentsystem.domain.product.dto.ProductResponse;
import com.example.commercepaymentsystem.domain.product.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

// @WebMvcTest 슬라이스는 @ControllerAdvice 를 모두 올린다.
// 즉 GlobalExceptionHandler(JSON) 와 ViewExceptionHandler(HTML) 가 함께 등록된 상태에서
// 화면 요청이 어느 쪽으로 가는지를 아래 두 테스트가 검증한다.
@WebMvcTest(ViewController.class)
@Import(SecurityConfig.class)
class ViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private JwtProvider jwtProvider;


    @Test
    @DisplayName("홈 화면은 상품과 페이지 링크를 렌더링한다")
    void 홈_화면_렌더링() throws Exception {
        ProductResponse product = new ProductResponse(1L, "무선 키보드", 89000, 3, "설명", "전자기기");
        given(productService.findAll(any(), any()))
                .willReturn(new PageResponse<>(List.of(product), 0, 10, 25, 3));

        MvcResult result = mockMvc.perform(get("/").param("category", "전자기기"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andReturn();

        String html = result.getResponse().getContentAsString();
        assertTrue(html.contains("무선 키보드"), "상품명이 렌더링되어야 한다");
        assertTrue(html.contains("89,000"), "가격이 천단위로 포맷되어야 한다");
        assertTrue(html.contains("재고 3개"), "재고가 렌더링되어야 한다");
        assertTrue(html.contains("value=\"전자기기\""), "검색 조건이 필터 폼에 유지되어야 한다");
        assertTrue(html.contains("category=%EC%A0%84%EC%9E%90%EA%B8%B0%EA%B8%B0"), "페이지 링크가 검색 조건을 유지해야 한다");
    }

    @Test
    @DisplayName("상품이 없으면 빈 목록 문구를 보여준다")
    void 홈_화면_빈_결과() throws Exception {
        given(productService.findAll(any(), any()))
                .willReturn(new PageResponse<>(List.of(), 0, 10, 0, 0));

        MvcResult result = mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andReturn();

        assertTrue(result.getResponse().getContentAsString().contains("조건에 맞는 상품이 없습니다"));
    }

    @Test
    @DisplayName("상세 페이지는 단건 상품을 렌더링한다")
    void 상세_페이지_렌더링() throws Exception {
        given(productService.findById(1L))
                .willReturn(new ProductResponse(1L, "무선 마우스", 32000, 0, "가벼운 마우스", "전자기기"));

        MvcResult result = mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("product/detail"))
                .andReturn();

        String html = result.getResponse().getContentAsString();
        assertTrue(html.contains("무선 마우스"));
        assertTrue(html.contains("32,000"));
        assertTrue(html.contains("품절"), "재고 0이면 품절로 표시되어야 한다");
        assertTrue(!html.contains("id=\"cart-add-box\""), "품절이면 담기 폼이 없어야 한다");
    }

    @Test
    @DisplayName("재고가 있으면 상세 페이지에 장바구니 담기 폼을 렌더링한다")
    void 상세_페이지_장바구니_담기_폼() throws Exception {
        given(productService.findById(1L))
                .willReturn(new ProductResponse(1L, "무선 마우스", 32000, 7, "가벼운 마우스", "전자기기"));

        String html = mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertTrue(html.contains("id=\"cart-add-box\""), "담기 폼이 있어야 한다");
        assertTrue(html.contains("data-product-id=\"1\""), "담기 요청에 쓸 상품 id 가 있어야 한다");
        assertTrue(html.contains("max=\"7\""), "수량 상한이 재고와 같아야 한다");
    }

    @Test
    @DisplayName("없는 상품은 JSON이 아니라 오류 페이지로 응답한다")
    void 없는_상품_오류_페이지() throws Exception {
        given(productService.findById(eq(999L)))
                .willThrow(new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        MvcResult result = mockMvc.perform(get("/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/business"))
                .andReturn();

        String html = result.getResponse().getContentAsString();
        assertTrue(html.contains("PRODUCT_001"));
        assertTrue(html.contains("상품을 찾을 수 없습니다"));
        assertEquals("text/html;charset=UTF-8", result.getResponse().getContentType());
    }

    @Test
    @DisplayName("예상 못한 예외도 화면에서는 오류 페이지로 응답한다")
    void 예상하지_못한_예외_오류_페이지() throws Exception {
        given(productService.findById(eq(1L))).willThrow(new IllegalStateException("boom"));

        MvcResult result = mockMvc.perform(get("/products/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(view().name("error/business"))
                .andReturn();

        assertTrue(result.getResponse().getContentAsString().contains("COMMON_002"));
        assertEquals("text/html;charset=UTF-8", result.getResponse().getContentType());
    }

    @Test
    @DisplayName("인증이 필요한 화면 요청은 로그인 페이지로 리다이렉트한다")
    void 화면_인증실패_로그인_리다이렉트() throws Exception {
        mockMvc.perform(get("/nope"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("인증이 필요한 API 요청은 JSON 401 을 그대로 돌려준다")
    void API_인증실패_JSON() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/orders"))
                .andExpect(status().isUnauthorized())
                .andReturn();

        assertTrue(result.getResponse().getContentAsString().contains("AUTH_001"));
        assertEquals("application/json;charset=UTF-8", result.getResponse().getContentType());
    }

    @Test
    @DisplayName("로그인 화면은 폼과 데모 계정 버튼을 렌더링한다")
    void 로그인_화면_렌더링() throws Exception {
        MvcResult result = mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andReturn();

        String html = result.getResponse().getContentAsString();
        assertTrue(html.contains("id=\"login-form\""), "로그인 폼이 있어야 한다");
        assertTrue(html.contains("id=\"demo-fill\""), "데모 계정 자동 입력 버튼이 있어야 한다");
    }

    @Test
    @DisplayName("회원가입 화면은 인증 없이 열리고 폼을 렌더링한다")
    void 회원가입_화면_렌더링() throws Exception {
        MvcResult result = mockMvc.perform(get("/signup"))
                .andExpect(status().isOk())
                .andExpect(view().name("signup"))
                .andReturn();

        assertTrue(result.getResponse().getContentAsString().contains("id=\"signup-form\""),
                "회원가입 폼이 있어야 한다");
    }

    // 장바구니 내용은 토큰이 있어야 읽을 수 있어서 화면 자체는 누구나 열 수 있어야 한다.
    // 여기가 막히면 로그인 안내조차 못 보여주고 /login 리다이렉트로 튕긴다.
    @Test
    @DisplayName("장바구니 화면은 인증 없이 열리고 목록 자리를 렌더링한다")
    void 장바구니_화면_렌더링() throws Exception {
        MvcResult result = mockMvc.perform(get("/cart"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andReturn();

        String html = result.getResponse().getContentAsString();
        assertTrue(html.contains("id=\"cart-list\""), "목록이 들어갈 자리가 있어야 한다");
        assertTrue(html.contains("id=\"cart-row-template\""), "행 템플릿이 있어야 한다");
    }

    // 장바구니와 같은 이유로 주문 화면들도 껍데기는 누구나 열려야 한다.
    // 셋을 한 테스트에 묶은 건 검증 내용이 "뷰 이름 + JS 가 붙을 자리"로 똑같기 때문이다.
    @Test
    @DisplayName("주문서·주문 내역·주문 상세 화면이 인증 없이 열린다")
    void 주문_화면_렌더링() throws Exception {
        assertTrue(renderView("/checkout", "order/checkout").contains("id=\"checkout-list\""));
        assertTrue(renderView("/orders", "order/list").contains("id=\"order-list\""));
        assertTrue(renderView("/orders/1", "order/detail").contains("id=\"detail-items\""));
    }

    // 어떤 주문을 불러올지 JS 가 이 값으로만 안다. 비면 /api/orders/undefined 를 부른다.
    @Test
    @DisplayName("주문 상세 화면은 경로의 orderId 를 화면에 실어준다")
    void 주문_상세_화면_orderId_전달() throws Exception {
        assertTrue(renderView("/orders/42", "order/detail").contains("data-order-id=\"42\""));
    }

    // JS 가 찾는 엘리먼트 id 와 템플릿이 그리는 id 는 한 쌍이다.
    // 한쪽 이름만 바뀌면 화면은 200 으로 멀쩡히 뜨는데 JS 가 null 을 만져
    // 결제·취소 버튼이나 내 정보가 조용히 죽는다. 이 화면들은 내용을 전부 JS 가 채워서
    // 서버에서 검증할 수 있는 계약이 이것뿐이다.
    @Test
    @DisplayName("JS 로 내용을 채우는 화면들이 스크립트가 찾는 엘리먼트를 모두 가지고 있다")
    void 화면_엘리먼트가_스크립트와_한_쌍이다() throws Exception {
        assertElementsMatchScript("/orders/7", "order/detail", "static/js/order-detail.js");
        assertElementsMatchScript("/mypage", "mypage", "static/js/mypage.js");
    }

    private void assertElementsMatchScript(String path, String viewName, String scriptPath) throws Exception {
        String html = renderView(path, viewName);
        String js = new String(new ClassPathResource(scriptPath)
                .getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        Matcher lookup = Pattern.compile("getElementById\\('([^']+)'\\)").matcher(js);
        int checked = 0;
        while (lookup.find()) {
            String id = lookup.group(1);
            assertTrue(html.contains("id=\"" + id + "\""),
                    scriptPath + " 가 찾는 id=\"" + id + "\" 엘리먼트가 " + path + " 에 있어야 한다");
            checked++;
        }
        assertTrue(checked > 0, scriptPath + " 에서 조회하는 id 를 하나도 찾지 못했다");
    }

    private String renderView(String path, String viewName) throws Exception {
        return mockMvc.perform(get(path))
                .andExpect(status().isOk())
                .andExpect(view().name(viewName))
                .andReturn().getResponse().getContentAsString();
    }

    // 버튼이 채워 넣는 값과 data.sql 이 넣는 계정은 한 쌍이다.
    // 한쪽만 바뀌면 화면은 멀쩡한데 로그인만 실패하므로 여기서 묶어둔다.
    @Test
    @DisplayName("데모 계정 버튼의 이메일/비밀번호로 data.sql 의 회원에 로그인할 수 있다")
    void 데모_계정_버튼_값이_data_sql_과_일치한다() throws Exception {
        String html = mockMvc.perform(get("/login"))
                .andReturn().getResponse().getContentAsString();
        String buttonEmail = attribute(html, "data-email");
        String buttonPassword = attribute(html, "data-password");

        // data.sql 의 members INSERT: (id?, name, email, phone_number, password)
        // id 컬럼은 있을 수도 없을 수도 있어 앞의 숫자 컬럼을 선택적으로 건너뛴다
        String sql = new String(new ClassPathResource("data.sql").getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        Matcher member = Pattern.compile("INSERT INTO members[^;]*?\\((?:\\s*\\d+\\s*,)?\\s*'[^']*',\\s*'([^']+)',\\s*'[^']*',\\s*'([^']+)'").matcher(sql);
        assertTrue(member.find(), "data.sql 에 회원 INSERT 가 있어야 한다");

        assertEquals(member.group(1), buttonEmail, "버튼의 이메일이 data.sql 회원과 같아야 한다");
        assertTrue(new BCryptPasswordEncoder().matches(buttonPassword, member.group(2)),
                "버튼의 비밀번호가 data.sql 의 BCrypt 해시와 맞아야 한다");
    }

    private String attribute(String html, String name) {
        Matcher matcher = Pattern.compile(name + "=\"([^\"]+)\"").matcher(html);
        assertTrue(matcher.find(), name + " 속성이 있어야 한다");
        return matcher.group(1);
    }
}
