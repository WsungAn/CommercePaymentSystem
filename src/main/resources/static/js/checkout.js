// 주문서. 장바구니 내용을 /api/orders/checkout 으로 미리 보고, 그대로 /api/orders 에 주문한다.
document.addEventListener('DOMContentLoaded', function () {
    const loading = document.getElementById('checkout-loading');
    const errorBox = document.getElementById('checkout-error');
    const empty = document.getElementById('checkout-empty');
    const list = document.getElementById('checkout-list');
    const summary = document.getElementById('checkout-summary');
    const actions = document.getElementById('checkout-actions');
    const total = document.getElementById('checkout-total');
    const submit = document.getElementById('checkout-submit');
    const rowTemplate = document.getElementById('checkout-row-template');

    function hideAll() {
        loading.hidden = true;
        errorBox.hidden = true;
        empty.hidden = true;
        list.hidden = true;
        summary.hidden = true;
        actions.hidden = true;
    }

    function showError(message) {
        hideAll();
        errorBox.textContent = message;
        errorBox.hidden = false;
    }

    function requireLogin() {
        hideAll();
        renderLoginRequired(errorBox);
    }

    function render(preview) {
        hideAll();

        if (preview.items.length === 0) {
            empty.hidden = false;
            return;
        }

        list.textContent = '';
        preview.items.forEach(function (item) {
            const row = rowTemplate.content.cloneNode(true);
            const name = row.querySelector('.cart-name');
            name.textContent = item.productName;
            name.href = '/products/' + item.productId;
            row.querySelector('.unit').textContent = item.price.toLocaleString();
            row.querySelector('.qty').textContent = item.quantity;
            row.querySelector('.cart-subtotal span').textContent = item.subtotal.toLocaleString();
            list.append(row);
        });
        total.textContent = preview.totalPrice.toLocaleString();

        list.hidden = false;
        summary.hidden = false;
        actions.hidden = false;
    }

    async function load() {
        try {
            const response = await apiFetch('/api/orders/checkout', {});
            if (!response) {
                requireLogin();
                return;
            }
            render((await response.json()).data);
        } catch (e) {
            showError(e.message);
        }
    }

    submit.addEventListener('click', async function () {
        submit.disabled = true;
        try {
            // cartItemIds 를 비워 보내면 장바구니 전체를 주문한다
            const response = await apiFetch('/api/orders', {
                method: 'POST',
                body: JSON.stringify({cartItemIds: []})
            });
            if (!response) {
                requireLogin();
                return;
            }
            const order = (await response.json()).data;
            location.href = '/orders/' + order.orderId;
        } catch (e) {
            showError(e.message);
        } finally {
            submit.disabled = false;
        }
    });

    if (!getAuth()) {
        requireLogin();
    } else {
        load();
    }
});
