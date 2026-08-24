// 장바구니 화면. 내용은 토큰이 필요해서 화면이 뜬 뒤 /api/carts 로 따로 가져온다.
document.addEventListener('DOMContentLoaded', function () {
    const loading = document.getElementById('cart-loading');
    const errorBox = document.getElementById('cart-error');
    const empty = document.getElementById('cart-empty');
    const list = document.getElementById('cart-list');
    const summary = document.getElementById('cart-summary');
    const total = document.getElementById('cart-total');
    const clearButton = document.getElementById('cart-clear');
    const checkout = document.getElementById('cart-checkout');
    const rowTemplate = document.getElementById('cart-row-template');

    function hideAll() {
        loading.hidden = true;
        errorBox.hidden = true;
        empty.hidden = true;
        list.hidden = true;
        summary.hidden = true;
        checkout.hidden = true;
    }

    function showError(message) {
        hideAll();
        errorBox.textContent = message;
        errorBox.hidden = false;
    }

    /** 비로그인/토큰 만료 - 로그인 링크를 같이 준다 */
    function showLoginRequired() {
        hideAll();
        clearAuth();
        errorBox.textContent = '로그인이 필요합니다. ';
        const link = document.createElement('a');
        link.href = '/login';
        link.textContent = '로그인하러 가기';
        errorBox.append(link);
        errorBox.hidden = false;
    }

    /**
     * 인증이 필요한 장바구니 API 호출을 한 곳으로 모은다.
     * 401 이면 null 을 돌려주고 로그인 안내로 바꾼다.
     */
    async function callCartApi(path, options) {
        const request = Object.assign({}, options);
        request.headers = Object.assign({'Content-Type': 'application/json'}, authHeaders(), request.headers);

        const response = await fetch(path, request);
        if (response.status === 401) {
            showLoginRequired();
            return null;
        }
        if (!response.ok) {
            const body = await response.json().catch(function () {
                return null;
            });
            throw new Error(body && body.message ? body.message : '요청을 처리하지 못했습니다');
        }
        return response;
    }

    function render(cart) {
        hideAll();

        if (cart.cartItems.length === 0) {
            empty.hidden = false;
            return;
        }

        list.textContent = '';
        cart.cartItems.forEach(function (item) {
            list.append(buildRow(item));
        });
        total.textContent = cart.totalPrice.toLocaleString();

        list.hidden = false;
        summary.hidden = false;
        checkout.hidden = false;
    }

    function buildRow(item) {
        const row = rowTemplate.content.cloneNode(true);

        const name = row.querySelector('.cart-name');
        name.textContent = item.productName;
        name.href = '/products/' + item.productId;

        row.querySelector('.cart-unit-price span').textContent = item.price.toLocaleString();
        row.querySelector('.cart-subtotal span').textContent = (item.price * item.quantity).toLocaleString();

        const quantityInput = row.querySelector('.cart-quantity');
        quantityInput.value = item.quantity;

        row.querySelector('.cart-update').addEventListener('click', function () {
            changeQuantity(item.id, Number(quantityInput.value));
        });
        row.querySelector('.cart-delete').addEventListener('click', function () {
            removeItem(item.id);
        });

        return row;
    }

    async function load() {
        try {
            const response = await callCartApi('/api/carts', {});
            if (!response) {
                return;
            }
            render((await response.json()).data);
        } catch (e) {
            showError(e.message);
        }
    }

    async function changeQuantity(cartItemId, quantity) {
        if (!Number.isInteger(quantity) || quantity < 1) {
            showError('수량은 1개 이상이어야 합니다');
            return;
        }
        try {
            const response = await callCartApi('/api/carts/cartItems/' + cartItemId, {
                method: 'PATCH',
                body: JSON.stringify({quantity: quantity})
            });
            if (response) {
                await load();
            }
        } catch (e) {
            showError(e.message);
        }
    }

    async function removeItem(cartItemId) {
        try {
            const response = await callCartApi('/api/carts/cartItems/' + cartItemId, {method: 'DELETE'});
            if (response) {
                await load();
            }
        } catch (e) {
            showError(e.message);
        }
    }

    clearButton.addEventListener('click', async function () {
        if (!confirm('장바구니를 전부 비울까요?')) {
            return;
        }
        try {
            const response = await callCartApi('/api/carts/cartItems', {method: 'DELETE'});
            if (response) {
                await load();
            }
        } catch (e) {
            showError(e.message);
        }
    });

    if (!getAuth()) {
        showLoginRequired();
    } else {
        load();
    }
});
