// 주문/결제 상태 enum 을 화면 문구로 바꾼다. 서버가 상수 이름 그대로 내려주기 때문에 필요하다.
const ORDER_STATUS_LABEL = {
    PENDING_PAYMENT: '결제 대기',
    CONFIRMED: '주문 확정',
    CANCELLED: '주문 취소'
};

const PAYMENT_STATUS_LABEL = {
    IN_PROGRESS: '결제 진행 중',
    PAID: '결제 완료',
    FAILED: '결제 실패',
    CANCELLED: '결제 취소'
};

/** 모르는 값이 오면 원래 문자열을 그대로 보여준다 - 조용히 비는 것보다 낫다 */
function statusLabel(labels, status) {
    return labels[status] || status;
}

/** LocalDateTime 문자열(2026-08-21T13:05:12)을 사람이 읽는 형태로 */
function formatDateTime(value) {
    if (!value) {
        return '';
    }
    const date = new Date(value);
    return isNaN(date.getTime()) ? value : date.toLocaleString('ko-KR');
}
