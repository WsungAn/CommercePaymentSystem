// 내 정보. 껍데기는 누구나 열리고 내용은 토큰이 있어야 읽을 수 있다.
document.addEventListener('DOMContentLoaded', function () {
    const loading = document.getElementById('member-loading');
    const errorBox = document.getElementById('member-error');
    const body = document.getElementById('member-body');

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

    function render(member) {
        loading.hidden = true;
        errorBox.hidden = true;

        document.getElementById('member-name').textContent = member.name;
        document.getElementById('member-email').textContent = member.email;
        document.getElementById('member-phone').textContent = member.phoneNumber;
        document.getElementById('member-created-at').textContent = formatDateTime(member.createdAt);

        body.hidden = false;
    }

    async function load() {
        try {
            const response = await apiFetch('/api/members/me', {});
            if (!response) {
                requireLogin();
                return;
            }
            // /api/members/me 는 로그인과 마찬가지로 ApiResponse 봉투 없이 바로 내려온다
            render(await response.json());
        } catch (e) {
            showError(e.message);
        }
    }

    if (!getAuth()) {
        requireLogin();
    } else {
        load();
    }
});
