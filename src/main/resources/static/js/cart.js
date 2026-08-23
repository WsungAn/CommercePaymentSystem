// 상품 상세의 "장바구니 담기".
// 토큰은 헤더로만 보낼 수 있어서 화면 이동이 아니라 fetch 로 담는다 (auth.js 의 authHeaders 사용).
document.addEventListener('DOMContentLoaded', function () {
    const box = document.getElementById('cart-add-box');
    if (!box) {
        return; // 품절이면 담기 폼 자체가 렌더링되지 않는다
    }

    const button = document.getElementById('cart-add');
    const quantityInput = document.getElementById('cart-quantity');
    const notice = document.getElementById('cart-notice');
    const errorBox = document.getElementById('cart-error');
    const productId = box.dataset.productId;

    function showNotice(message) {
        notice.textContent = message;
        notice.hidden = false;
    }

    function showError(message) {
        errorBox.textContent = message;
        errorBox.hidden = false;
    }

    /** 비로그인/토큰 만료 - 로그인 링크를 같이 준다 */
    function showLoginRequired() {
        errorBox.textContent = '로그인이 필요합니다. ';
        const link = document.createElement('a');
        link.href = '/login';
        link.textContent = '로그인하러 가기';
        errorBox.append(link);
        errorBox.hidden = false;
    }

    function hideMessages() {
        notice.hidden = true;
        errorBox.hidden = true;
    }

    /** 담은 뒤 장바구니를 다시 읽어 현재 상태를 알려준다 */
    async function describeCart(added) {
        let message = added + '개를 장바구니에 담았습니다.';
        try {
            const response = await fetch('/api/carts', {headers: authHeaders()});
            if (response.ok) {
                const cart = (await response.json()).data;
                const count = cart.cartItems.reduce(function (sum, item) {
                    return sum + item.quantity;
                }, 0);
                message += ' 현재 장바구니에 ' + count + '개, 총 ' + cart.totalPrice.toLocaleString() + '원입니다.';
            }
        } catch (e) {
            // 요약은 부가 정보다. 실패해도 담기가 된 사실은 그대로 알린다
        }
        showNotice(message);
    }

    button.addEventListener('click', async function () {
        hideMessages();

        const quantity = Number(quantityInput.value);
        if (!Number.isInteger(quantity) || quantity < 1) {
            showError('수량은 1개 이상이어야 합니다');
            return;
        }
        if (!getAuth()) {
            showLoginRequired();
            return;
        }

        button.disabled = true;
        try {
            const response = await fetch('/api/carts/' + productId, {
                method: 'POST',
                headers: Object.assign({'Content-Type': 'application/json'}, authHeaders()),
                body: JSON.stringify({quantity: quantity})
            });

            // 토큰이 만료됐거나 잘못된 경우
            if (response.status === 401) {
                clearAuth();
                showLoginRequired();
                return;
            }

            if (!response.ok) {
                const body = await response.json().catch(function () {
                    return null;
                });
                showError(body && body.message ? body.message : '장바구니에 담지 못했습니다');
                return;
            }

            await describeCart(quantity);
        } catch (e) {
            showError('서버에 연결할 수 없습니다');
        } finally {
            button.disabled = false;
        }
    });
});
