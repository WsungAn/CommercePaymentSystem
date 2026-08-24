// 주문 상세. 어떤 주문인지는 화면이 data-order-id 로 알려주고, 내용은 여기서 가져온다.
document.addEventListener('DOMContentLoaded', function () {
    const page = document.getElementById('order-detail-page');
    const orderId = page.dataset.orderId;

    const loading = document.getElementById('detail-loading');
    const errorBox = document.getElementById('detail-error');
    const body = document.getElementById('detail-body');
    const items = document.getElementById('detail-items');
    const cancelledBox = document.getElementById('detail-cancelled');
    const cancelButton = document.getElementById('detail-cancel');
    const payBox = document.getElementById('detail-pay');
    const paySuccess = document.getElementById('detail-pay-success');
    const payFail = document.getElementById('detail-pay-fail');
    const cancelForm = document.getElementById('detail-cancel-form');
    const cancelInput = document.getElementById('detail-cancel-input');
    const cancelConfirm = document.getElementById('detail-cancel-confirm');
    const cancelClose = document.getElementById('detail-cancel-close');
    const itemTemplate = document.getElementById('detail-item-template');

    // 결제 요청은 주문 금액을 실어 보내야 해서 마지막으로 그린 주문을 들고 있는다
    let currentOrder = null;

    function showError(message) {
        loading.hidden = true;
        body.hidden = true;
        errorBox.textContent = message;
        errorBox.hidden = false;
    }

    function requireLogin() {
        loading.hidden = true;
        body.hidden = true;
        renderLoginRequired(errorBox);
    }

    function render(order) {
        loading.hidden = true;
        errorBox.hidden = true;
        currentOrder = order;

        document.getElementById('detail-number').textContent = order.orderNumber;
        document.getElementById('detail-ordered-at').textContent = formatDateTime(order.orderedAt);

        const status = document.getElementById('detail-status');
        status.textContent = statusLabel(ORDER_STATUS_LABEL, order.orderStatus);
        status.className = 'order-status status-' + order.orderStatus.toLowerCase().replace('_', '-');

        const payment = document.getElementById('detail-payment');
        payment.textContent = statusLabel(PAYMENT_STATUS_LABEL, order.paymentStatus);
        payment.className = 'order-status status-' + order.paymentStatus.toLowerCase().replace('_', '-');

        const cancelled = order.orderStatus === 'CANCELLED';
        cancelledBox.hidden = !cancelled;
        if (cancelled) {
            document.getElementById('detail-cancelled-at').textContent =
                '취소 일시: ' + formatDateTime(order.cancelledAt);
            document.getElementById('detail-cancel-reason').textContent =
                '취소 사유: ' + (order.cancellationReason || '(없음)');
        }
        cancelButton.hidden = cancelled;
        // 상태가 바뀌어 다시 그릴 때는 열려 있던 취소 폼을 닫는다
        cancelForm.hidden = true;

        // 서버 tryPayment 가 통과시키는 조합과 같은 조건으로만 결제 버튼을 연다.
        // 화면에서 먼저 걸러도 최종 판단은 서버가 하므로 둘이 어긋나면 서버 쪽이 이긴다.
        payBox.hidden = !(order.orderStatus === 'PENDING_PAYMENT' && order.paymentStatus === 'IN_PROGRESS');

        items.textContent = '';
        order.items.forEach(function (item) {
            const row = itemTemplate.content.cloneNode(true);
            const name = row.querySelector('.cart-name');
            // 주문 당시 이름 스냅샷을 보여주되, 링크는 현재 상품으로 건다
            name.textContent = item.productName;
            name.href = '/products/' + item.productId;
            row.querySelector('.unit').textContent = item.unitPrice.toLocaleString();
            row.querySelector('.qty').textContent = item.quantity;
            row.querySelector('.cart-subtotal span').textContent = item.lineTotal.toLocaleString();
            items.append(row);
        });

        document.getElementById('detail-total').textContent = order.totalPrice.toLocaleString();
        body.hidden = false;
    }

    async function load() {
        try {
            const response = await apiFetch('/api/orders/' + orderId, {});
            if (!response) {
                requireLogin();
                return;
            }
            render((await response.json()).data);
        } catch (e) {
            showError(e.message);
        }
    }

    // "주문 취소" 는 사유 입력 폼을 여는 것까지만 한다 - 실제 취소는 폼의 확정 버튼이 보낸다
    cancelButton.addEventListener('click', function () {
        cancelForm.hidden = false;
        cancelInput.value = '';
        cancelInput.focus();
    });

    cancelClose.addEventListener('click', function () {
        cancelForm.hidden = true;
    });

    cancelConfirm.addEventListener('click', async function () {
        cancelConfirm.disabled = true;
        try {
            const response = await apiFetch('/api/orders/' + orderId + '/cancel', {
                method: 'POST',
                // 사유는 선택이라 비워 보내도 된다 - 서버는 500자 제한만 본다
                body: JSON.stringify({reason: cancelInput.value})
            });
            if (!response) {
                requireLogin();
                return;
            }
            // 취소되면 주문·결제 상태가 함께 바뀌므로 상세를 다시 읽는다
            await load();
        } catch (e) {
            showError(e.message);
        } finally {
            cancelConfirm.disabled = false;
        }
    });

    // 사유 입력 중 엔터로도 확정할 수 있게 한다
    cancelInput.addEventListener('keydown', function (event) {
        if (event.key === 'Enter') {
            cancelConfirm.click();
        }
    });

    /**
     * 모의 PG 결제. result 만 바꿔 성공/실패 두 경로를 태운다.
     * 금액은 화면이 지어내지 않고 서버가 내려준 주문 금액을 그대로 돌려보낸다
     * - 서버가 결제 금액과 대조해서 다르면 PAYMENT_AMOUNT_MISMATCH 로 막는다.
     */
    async function pay(result) {
        if (!currentOrder) {
            return;
        }
        paySuccess.disabled = true;
        payFail.disabled = true;
        try {
            const response = await apiFetch('/api/payments/confirm', {
                method: 'POST',
                body: JSON.stringify({
                    orderId: currentOrder.orderId,
                    result: result,
                    amount: currentOrder.totalPrice
                })
            });
            if (!response) {
                requireLogin();
                return;
            }
            // 결제 결과에 따라 주문/결제 상태가 함께 바뀌므로 상세를 다시 읽는다
            await load();
        } catch (e) {
            showError(e.message);
        } finally {
            paySuccess.disabled = false;
            payFail.disabled = false;
        }
    }

    paySuccess.addEventListener('click', function () {
        pay('SUCCESS');
    });

    payFail.addEventListener('click', function () {
        pay('FAIL');
    });

    if (!getAuth()) {
        requireLogin();
    } else {
        load();
    }
});
