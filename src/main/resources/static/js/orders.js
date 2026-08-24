// 주문 내역. /api/orders 는 PageResponse 라 페이지 링크도 같이 그린다.
document.addEventListener('DOMContentLoaded', function () {
    const PAGE_SIZE = 10;

    const loading = document.getElementById('order-loading');
    const errorBox = document.getElementById('order-error');
    const empty = document.getElementById('order-empty');
    const list = document.getElementById('order-list');
    const pagination = document.getElementById('order-pagination');
    const rowTemplate = document.getElementById('order-row-template');

    function hideAll() {
        loading.hidden = true;
        errorBox.hidden = true;
        empty.hidden = true;
        list.hidden = true;
        pagination.hidden = true;
    }

    function showError(message) {
        hideAll();
        errorBox.textContent = message;
        errorBox.hidden = false;
    }

    function render(page) {
        hideAll();

        if (page.content.length === 0) {
            empty.hidden = false;
            return;
        }

        list.textContent = '';
        page.content.forEach(function (order) {
            const row = rowTemplate.content.cloneNode(true);
            const link = row.querySelector('.order-link');
            link.href = '/orders/' + order.orderId;
            row.querySelector('.order-number').textContent =
                order.orderNumber + ' · ' + formatDateTime(order.orderedAt);
            row.querySelector('.order-name').textContent = order.orderName;

            const status = row.querySelector('.order-status');
            status.textContent = statusLabel(ORDER_STATUS_LABEL, order.status);
            status.classList.add('status-' + order.status.toLowerCase().replace('_', '-'));

            row.querySelector('.order-price span').textContent = order.totalPrice.toLocaleString();
            list.append(row);
        });
        list.hidden = false;

        renderPagination(page);
    }

    function renderPagination(page) {
        pagination.textContent = '';
        if (page.totalPages <= 1) {
            return;
        }
        for (let i = 0; i < page.totalPages; i++) {
            const link = document.createElement('button');
            link.type = 'button';
            link.className = 'page-link';
            if (i === page.page) {
                link.classList.add('current');
            }
            link.textContent = i + 1;
            link.addEventListener('click', function () {
                load(i);
            });
            pagination.append(link);
        }
        pagination.hidden = false;
    }

    async function load(page) {
        try {
            const response = await apiFetch('/api/orders?page=' + page + '&size=' + PAGE_SIZE, {});
            if (!response) {
                hideAll();
                renderLoginRequired(errorBox);
                return;
            }
            render((await response.json()).data);
        } catch (e) {
            showError(e.message);
        }
    }

    if (!getAuth()) {
        hideAll();
        renderLoginRequired(errorBox);
    } else {
        load(0);
    }
});
