# API 명세서

## 1. 인증

### 1.1 회원가입

- **Method**: `POST`
- **URL**: `/auth/signup`
- **인증**: 불필요
- **담당**: 안우성

#### 설명

이메일, 비밀번호, 이름, 전화번호를 입력하여 회원가입합니다.

- 이메일 중복 시 오류 반환
- 비밀번호는 BCrypt를 사용하여 암호화 후 저장

#### Request

```
{
  "email": "test@example.com",
  "password": "password123!",
  "name": "홍길동",
  "phoneNumber": "010-1234-5678"
}
```

#### Response

```
{
  "success": true,
  "code": "SUCCESS",
  "message": "회원가입이 완료되었습니다.",
  "data": null
}
```
---

### 1.2 로그인

- **Method**: `POST`
- **URL**: `/auth/login`
- **인증**: 불필요
- **담당**: 안우성

#### 설명

회원 정보를 확인하고 로그인 후 Access Token을 발급합니다.

#### Request

```
{
  "email": "test@example.com",
  "password": "password123!"
}
```

#### Response

```
{
  "success": true,
  "code": "SUCCESS",
  "message": "로그인에 성공했습니다.",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9..."
  }
}
```
---

### 1.3 내 정보 조회

- **Method**: `GET`
- **URL**: `/auth/me`
- **인증**: Access Token 필요
- **담당**: 안우성

#### 설명

로그인한 사용자의 정보를 조회합니다.

#### Response

```
{
  "success": true,
  "code": "SUCCESS",
  "message": "회원 정보 조회에 성공했습니다.",
  "data": {
    "id": 1,
    "email": "test@example.com",
    "name": "홍길동",
    "phoneNumber": "010-1234-5678"
  }
}
```

## 2. 상품

### 2.1 상품 목록 조회

- **Method**: `GET`
- **URL**: `/api/products`
- **인증**: 불필요

#### 설명

전체 상품 목록을 조회합니다.

다음 조건으로 상품을 검색할 수 있습니다.

- 카테고리 필터
- 최소 가격 / 최대 가격 필터
- 페이지네이션
- 최신순 정렬

로그인하지 않은 사용자도 상품을 조회할 수 있습니다.

#### Query Parameter

- `category`: 상품 카테고리
- `minPrice`: 최소 가격
- `maxPrice`: 최대 가격
- `page`: 페이지 번호
- `size`: 페이지당 상품 수

#### Response

```
{
  "success": true,
  "code": "SUCCESS",
  "message": "상품 목록 조회에 성공했습니다.",
  "data": {
    "content": [
      {
        "id": 1,
        "name": "베이직 반팔 티셔츠",
        "price": 19000,
        "stock": 48,
        "description": "면 100% 소재의 데일리 반팔 티셔츠입니다.",
        "category": "의류"
      },
      {
        "id": 9,
        "name": "컬러 양말 5족 세트",
        "price": 9000,
        "stock": 0,
        "description": "다섯 가지 컬러로 구성된 데일리 양말 세트입니다.",
        "category": "의류"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 2,
    "totalPages": 1
  }
}
```

재고가 0인 상품은 품절 상태로 표시합니다.

---

### 2.2 상품 단건 조회

- **Method**: `GET`
- **URL**: `/api/products/{productId}`
- **인증**: 불필요

#### 설명

선택한 상품의 상세 정보를 조회합니다.

존재하지 않는 상품 ID로 요청할 경우 `404 Not Found`를 반환합니다.

#### Path Variable

- `productId`: 조회할 상품 ID

#### Response

```
{
  "success": true,
  "code": "SUCCESS",
  "message": "상품 조회에 성공했습니다.",
  "data": {
    "id": 1,
    "name": "베이직 반팔 티셔츠",
    "price": 19000,
    "stock": 48,
    "description": "면 100% 소재의 데일리 반팔 티셔츠입니다.",
    "category": "의류"
  }
}
```

## 3. 장바구니

### 3.1 장바구니 조회

- **Method**: `GET`
- **URL**: `/api/carts`
- **인증**: Access Token 필요
- **담당**: 신원열

#### 설명

로그인한 사용자의 장바구니에 담긴 상품 목록을 조회합니다.

각 상품의 수량과 금액 및 장바구니 전체 금액을 반환합니다.

#### Response

```
{
  "success": true,
  "code": "SUCCESS",
  "message": "장바구니 조회에 성공했습니다.",
  "data": {
    "cartItems": [
      {
        "id": 1,
        "productId": 1,
        "productName": "베이직 반팔 티셔츠",
        "price": 19000,
        "quantity": 2
      },
      {
        "id": 2,
        "productId": 2,
        "productName": "워싱 데님 팬츠",
        "price": 59000,
        "quantity": 1
      }
    ],
    "totalPrice": 97000
  }
}
```
---

### 3.2 장바구니 상품 담기

- **Method**: `POST`
- **URL**: `/api/carts/{productId}`
- **인증**: Access Token 필요
- **담당**: 신원열

#### 설명

지정한 상품을 장바구니에 담습니다.

- 동일한 상품을 다시 담으면 기존 수량에 추가
- 담으려는 수량이 재고를 초과하면 오류 반환

#### Path Variable

- `productId`: 장바구니에 담을 상품 ID

#### Request

```
{
  "quantity": 2
}
```

#### Response

장바구니 상품 추가 성공 시 `201 Created`를 반환합니다.

---

### 3.3 장바구니 상품 수량 변경

- **Method**: `PATCH`
- **URL**: `/api/carts/cartItems/{cartItemId}`
- **인증**: Access Token 필요
- **담당**: 신원열

#### 설명

장바구니에 담긴 상품의 수량을 변경합니다.

변경하려는 수량이 상품의 현재 재고를 초과하면 오류를 반환합니다.

#### Path Variable

- `cartItemId`: 변경할 장바구니 상품 ID

#### Request

```
{
"productId": 1,
"quantity": 5
}
```

#### Response

수량 변경 성공 시 `200 OK`를 반환합니다.

---

### 3.4 장바구니 상품 개별 삭제

- **Method**: `DELETE`
- **URL**: `/api/carts/cartItems/{cartItemId}`
- **인증**: Access Token 필요
- **담당**: 신원열

#### 설명

장바구니에 담긴 특정 상품을 삭제합니다.

#### Path Variable

- `cartItemId`: 삭제할 장바구니 상품 ID

#### Response

삭제 성공 시 `204 No Content`를 반환합니다.

---

### 3.5 장바구니 전체 비우기

- **Method**: `DELETE`
- **URL**: `/api/carts/cartItems`
- **인증**: Access Token 필요
- **담당**: 신원열

#### 설명

로그인한 사용자의 장바구니에 담긴 모든 상품을 삭제합니다.

#### Response

삭제 성공 시 `204 No Content`를 반환합니다.


## 4. 주문

### 4.1 주문 미리보기

- **Method**: `GET`
- **URL**: `/api/orders/checkout`
- **인증**: Access Token 필요
- **담당**: 송나영

#### 설명

결제하기 전 선택한 장바구니 상품의 정보를 확인합니다.

선택한 상품 목록과 총 가격을 반환합니다.

#### Query Parameter

- `cartItem`: 결제할 장바구니 상품 ID
- 여러 상품을 선택할 경우 여러 개의 `cartItem` 파라미터 사용

#### Response

```
{
  "success": true,
  "code": "SUCCESS",
  "message": "주문 미리보기에 성공했습니다.",
  "data": {
    "items": [
      {
        "cartItemId": 1,
        "productId": 1,
        "productName": "베이직 반팔 티셔츠",
        "price": 19000,
        "quantity": 2
      }
    ],
    "totalPrice": 38000
  }
}
```

---

### 4.2 주문 생성

- **Method**: `POST`
- **URL**: `/api/orders`
- **인증**: Access Token 필요
- **담당**: 송나영

#### 설명

장바구니 상품 ID 목록을 전달받아 주문을 생성합니다.

주문 생성 과정에서 상품 재고를 차감하고 주문 상품 정보를 저장합니다.

상품명과 판매가를 주문 상품에 스냅샷으로 저장하여 이후 상품 정보가 변경되어도 주문 당시 정보를 유지합니다.

#### Request

```
{
  "cartItemIds": [1, 2, 3]
}
```

#### Response

```
{
  "success": true,
  "code": "SUCCESS",
  "message": "주문이 생성되었습니다.",
  "data": {
    "orderId": 1,
    "orderNumber": "ORD-20260825-000001",
    "status": "PENDING_PAYMENT",
    "totalPrice": 97000
  }
}
```

주문 생성 후 결제 상태는 `PAYMENT_PENDING` 상태로 시작합니다.

---

### 4.3 내 주문 목록 조회

- **Method**: `GET`
- **URL**: `/api/orders?page=&size=`
- **인증**: Access Token 필요
- **담당**: 송나영

#### 설명

로그인한 사용자의 주문 목록을 최신순으로 조회합니다.

페이지네이션을 지원합니다.

#### Query Parameter

- `page`: 페이지 번호
- `size`: 페이지당 주문 수

#### Response

```
{
  "success": true,
  "code": "SUCCESS",
  "message": "주문 목록 조회에 성공했습니다.",
  "data": {
    "content": [
      {
        "orderId": 1,
        "orderNumber": "ORD-20260825-000001",
        "totalPrice": 97000,
        "status": "PENDING_PAYMENT",
        "createdAt": "2026-08-25T10:30:00"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

---

### 4.4 주문 상세 조회

- **Method**: `GET`
- **URL**: `/api/orders/{orderId}`
- **인증**: Access Token 필요
- **담당**: 송나영

#### 설명

특정 주문의 상세 정보를 조회합니다.

주문 정보와 주문 상품 목록 및 결제 상태를 함께 반환합니다.

#### Path Variable

- `orderId`: 조회할 주문 ID

#### Response

```
{
  "success": true,
  "code": "SUCCESS",
  "message": "주문 상세 조회에 성공했습니다.",
  "data": {
    "orderId": 1,
    "orderNumber": "ORD-20260825-000001",
    "totalPrice": 97000,
    "orderStatus": "PENDING_PAYMENT",
    "paymentStatus": "IN_PROGRESS",
    "items": [
      {
        "productId": 1,
        "productName": "베이직 반팔 티셔츠",
        "price": 19000,
        "quantity": 2
      }
    ]
  }
}
```

---

### 4.5 주문 취소

- **Method**: `POST`
- **URL**: `/api/orders/{orderId}/cancel`
- **인증**: Access Token 필요
- **담당**: 송나영

#### 설명

사용자가 생성한 주문을 취소합니다.

이미 완료된 주문은 취소할 수 없으며, 이미 취소된 주문도 중복 취소할 수 없습니다.

주문 취소 시 차감되었던 상품 재고를 복구합니다.

#### Path Variable

- `orderId`: 취소할 주문 ID

#### Response

```
{
  "success": true,
  "code": "SUCCESS",
  "message": "주문이 취소되었습니다.",
  "data": null
}
```

주문 취소 성공 시 주문 상태가 `CANCELLED`로 변경됩니다.


## 5. 결제

### 5.1 모의 결제 승인

- **Method**: `POST`
- **URL**: `/api/payments/confirm`
- **인증**: Access Token 필요
- **담당**: 강충만

#### 설명

실제 PG사 연동을 대신하여 결제 성공 및 실패 상황을 모의 결제로 처리합니다.

결제 요청 시 주문 정보와 결제 금액을 검증한 후 결제 결과에 따라 주문과 결제 상태를 변경합니다.

#### Request

```
{
  "orderId": 1,
  "amount": 97000,
  "result": "SUCCESS"
}
```

- `orderId`: 결제할 주문 ID
- `amount`: 결제 금액
- `result`: 결제 결과 (`SUCCESS` 또는 `FAIL`)

#### 결제 성공

결제 성공 시 다음 작업을 수행합니다.

- 결제 상태를 완료 상태로 변경
- 주문 상태를 주문 완료 상태로 변경
- 장바구니 상품 삭제

#### 결제 실패

결제 실패 시 다음 작업을 수행합니다.

- 결제 상태를 실패 상태로 변경
- 주문 상태를 주문 취소 상태로 변경
- 주문 생성 시 차감했던 상품 재고 복구
- 장바구니 상품은 그대로 유지

#### Response

```
{
  "success": true,
  "code": "SUCCESS",
  "message": "결제가 처리되었습니다.",
  "data": {
    "paymentStatus": "COMPLETED",
    "orderStatus": "COMPLETED"
  }
}
```

- `paymentStatus`: 결제 상태
- `orderStatus`: 주문 상태

---

### 5.2 결제 단건 조회

- **Method**: `GET`
- **URL**: `/api/payments/{orderId}`
- **인증**: Access Token 필요
- **담당**: 강충만

#### 설명

주문 ID를 기준으로 해당 주문의 결제 정보를 조회합니다.

주문이 존재하지 않거나 결제 정보가 존재하지 않는 경우 오류를 반환합니다.

#### Path Variable

- `orderId`: 결제 정보를 조회할 주문 ID

#### Response

```
{
  "success": true,
  "code": "SUCCESS",
  "message": "결제 정보 조회에 성공했습니다.",
  "data": {
    "paymentId": 1,
    "orderId": 1,
    "amount": 97000,
    "status": "COMPLETED",
    "paidAt": "2026-08-25T10:35:00",
    "createdAt": "2026-08-25T10:34:00",
    "updatedAt": "2026-08-25T10:35:00"
  }
}
```


## 6. 공통 응답

### 성공 응답

각 API의 HTTP 상태 코드에 따라 성공 여부를 반환합니다.

- `200 OK`: 조회 및 수정 성공
- `201 Created`: 생성 성공
- `204 No Content`: 삭제 성공

### 주요 오류 응답

- `400 Bad Request`: 잘못된 요청
- `401 Unauthorized`: 인증되지 않은 사용자
- `403 Forbidden`: 접근 권한이 없는 사용자
- `404 Not Found`: 존재하지 않는 리소스
- `409 Conflict`: 중복 또는 상태 충돌
- `500 Internal Server Error`: 서버 내부 오류