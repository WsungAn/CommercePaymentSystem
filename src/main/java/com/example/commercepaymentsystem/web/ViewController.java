package com.example.commercepaymentsystem.web;

import com.example.commercepaymentsystem.common.response.PageResponse;
import com.example.commercepaymentsystem.domain.product.dto.ProductResponse;
import com.example.commercepaymentsystem.domain.product.dto.ProductSearchCondition;
import com.example.commercepaymentsystem.domain.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 화면(Thymeleaf) 컨트롤러.
 * 서비스는 그대로 재사용하고 JSON 대신 뷰 이름을 반환한다 - JSON 계약은 /api/** 가 담당한다.
 */
@Controller
@RequiredArgsConstructor
public class ViewController {

    private final ProductService productService;


    /** 홈 - 상품 목록. 검색 조건을 그대로 모델에 담아 필터 폼과 페이지 링크를 다시 구성한다 */
    @GetMapping("/")
    public String index(
            ProductSearchCondition condition,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Model model
    ) {
        PageResponse<ProductResponse> products = productService.findAll(condition, pageable);
        model.addAttribute("products", products);
        model.addAttribute("condition", condition);
        return "index";
    }

    /** 상품 단건 */
    @GetMapping("/products/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.findById(id));
        return "product/detail";
    }

    /**
     * 로그인 화면. 인증 자체는 /api/auth/login 이 처리하고
     * 이 화면은 폼만 내려준다 - 발급받은 JWT 는 브라우저가 보관한다.
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /**
     * 장바구니 화면. 내용은 토큰이 있어야 읽을 수 있어서 껍데기만 내려주고
     * 실제 목록은 cart-page.js 가 /api/carts 에서 가져온다.
     */
    @GetMapping("/cart")
    public String cart() {
        return "cart";
    }

    /** 주문서. 장바구니 내용을 /api/orders/checkout 으로 미리 보고 그대로 주문한다 */
    @GetMapping("/checkout")
    public String checkout() {
        return "order/checkout";
    }

    /** 주문 내역 */
    @GetMapping("/orders")
    public String orderList() {
        return "order/list";
    }

    /** 주문 상세. 어떤 주문인지만 화면에 실어주고 내용은 JS 가 가져온다 */
    @GetMapping("/orders/{orderId}")
    public String orderDetail(@PathVariable Long orderId, Model model) {
        model.addAttribute("orderId", orderId);
        return "order/detail";
    }

    /**
     * 내 정보. 헤더의 사용자 이름에서 들어온다.
     * 껍데기만 내려주고 내용은 mypage.js 가 /api/members/me 에서 가져온다.
     */
    @GetMapping("/mypage")
    public String mypage() {
        return "mypage";
    }

    /** 회원가입 화면. 가입 자체는 /api/auth/signup 이 처리하고 이 화면은 폼만 내려준다 */
    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }

}
