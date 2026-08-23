-- 개발/로컬 테스트 전용 더미 데이터
-- application.yml의 ddl-auto: create + defer-datasource-initialization: true 환경을 전제로 합니다.
-- 테스트 로그인 계정의 비밀번호는 모두 1234 입니다.
-- BCrypt 해시이므로 평문 1234를 그대로 저장하지 않습니다.

-- 1) 회원
INSERT INTO members (id, name, email, phone_number, password, created_at, updated_at)
VALUES
    (1, '테스트회원1', 'test1@example.com', '010-1111-1111',
     '$2a$10$xAu1J73Yl8kY5mQJRU9xkewWhXLxBFxRFBtEOIWYi29h43EkGsmJq',
     '2026-08-21 10:00:00', '2026-08-21 10:00:00'),
    (2, '테스트회원2', 'test2@example.com', '010-2222-2222',
     '$2a$10$xAu1J73Yl8kY5mQJRU9xkewWhXLxBFxRFBtEOIWYi29h43EkGsmJq',
     '2026-08-21 10:01:00', '2026-08-21 10:01:00');

-- 2) 상품
-- 주문 1에서 p1 x2 + p2 x1 = 25,000원
-- 주문 2에서 p3 x3 = 30,000원
-- 주문 3에서 p4 x1 = 20,000원
-- stock은 '주문 생성 시 재고 차감이 이미 일어난 상태'를 가정
INSERT INTO products (id, name, price, stock, description, category, created_at, updated_at)
VALUES
    (1, '테스트 노트북', 10000, 98, '결제 성공 테스트용 상품', 'ELECTRONICS',
     '2026-08-21 10:10:00', '2026-08-21 10:10:00'),
    (2, '테스트 마우스', 5000, 49, '결제 성공 테스트용 상품', 'ELECTRONICS',
     '2026-08-21 10:11:00', '2026-08-21 10:11:00'),
    (3, '테스트 키보드', 10000, 27, '결제 실패 테스트용 상품', 'ELECTRONICS',
     '2026-08-21 10:12:00', '2026-08-21 10:12:00'),
    (4, '테스트 헤드셋', 20000, 19, '주문 취소/결제 단건조회 테스트용 상품', 'ELECTRONICS',
     '2026-08-21 10:13:00', '2026-08-21 10:13:00');

-- 3) 장바구니
-- CartService.getCart(memberId)가 cartRepository.findById(memberId)를 사용하므로
-- 테스트 편의를 위해 cart id와 member id를 동일하게 맞춥니다.
INSERT INTO carts (id, member_id, created_at, updated_at)
VALUES
    (1, 1, '2026-08-21 10:20:00', '2026-08-21 10:20:00'),
    (2, 2, '2026-08-21 10:21:00', '2026-08-21 10:21:00');

-- 4) 장바구니 상품
INSERT INTO cartitems (id, cart_id, product_id, quantity, created_at, updated_at)
VALUES
    (1, 1, 1, 2, '2026-08-21 10:22:00', '2026-08-21 10:22:00'),
    (2, 1, 2, 1, '2026-08-21 10:23:00', '2026-08-21 10:23:00'),
    (3, 2, 4, 1, '2026-08-21 10:24:00', '2026-08-21 10:24:00');

-- 5) 주문
-- 주문 1: 결제 성공 테스트용 -> PENDING_PAYMENT + IN_PROGRESS
-- 주문 2: 결제 실패 테스트용 -> PENDING_PAYMENT + IN_PROGRESS
-- 주문 3: 결제 완료/주문 확정 상태 -> 결제 단건조회 및 주문 취소 테스트용
INSERT INTO orders
    (id, order_number, member_id, total_price, status, cancellation_reason, cancelled_at, created_at, updated_at)
VALUES
    (1, 'ORD-TEST-0001', 1, 25000, 'PENDING_PAYMENT', NULL, NULL,
     '2026-08-21 11:00:00', '2026-08-21 11:00:00'),
    (2, 'ORD-TEST-0002', 1, 30000, 'PENDING_PAYMENT', NULL, NULL,
     '2026-08-21 11:01:00', '2026-08-21 11:01:00'),
    (3, 'ORD-TEST-0003', 2, 20000, 'CONFIRMED', NULL, NULL,
     '2026-08-21 11:02:00', '2026-08-21 11:02:00');

-- 6) 주문 상품
INSERT INTO order_items
    (id, order_id, product_id, product_name, unit_price, quantity)
VALUES
    (1, 1, 1, '테스트 노트북', 10000, 2),
    (2, 1, 2, '테스트 마우스', 5000, 1),
    (3, 2, 3, '테스트 키보드', 10000, 3),
    (4, 3, 4, '테스트 헤드셋', 20000, 1);

-- 7) 결제
-- paid_at은 PAID인 결제만 값을 넣습니다.
INSERT INTO payments
    (id, order_id, member_id, amount, status, paid_at, created_at, updated_at)
VALUES
    (1, 1, 1, 25000, 'IN_PROGRESS', NULL,
     '2026-08-21 11:00:00', '2026-08-21 11:00:00'),
    (2, 2, 1, 30000, 'IN_PROGRESS', NULL,
     '2026-08-21 11:01:00', '2026-08-21 11:01:00'),
    (3, 3, 2, 20000, 'PAID', '2026-08-21 11:03:00',
     '2026-08-21 11:02:00', '2026-08-21 11:03:00');
