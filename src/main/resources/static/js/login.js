// 로그인 폼. /api/auth/login 에 JSON 으로 보내고 받은 토큰을 저장한 뒤 홈으로 이동한다.
document.addEventListener('DOMContentLoaded', function () {
    const form = document.getElementById('login-form');
    const emailInput = document.getElementById('email');
    const passwordInput = document.getElementById('password');
    const errorBox = document.getElementById('login-error');
    const submitButton = document.getElementById('login-submit');
    const demoButton = document.getElementById('demo-fill');

    function showError(message) {
        errorBox.textContent = message;
        errorBox.hidden = false;
    }

    function hideError() {
        errorBox.hidden = true;
    }

    // 데모 계정 자동 입력. 계정 값은 버튼의 data-* 에만 두고 여기서는 읽기만 한다
    demoButton.addEventListener('click', function () {
        emailInput.value = demoButton.dataset.email;
        passwordInput.value = demoButton.dataset.password;
        hideError();
        submitButton.focus();
    });

    form.addEventListener('submit', async function (event) {
        event.preventDefault();
        hideError();

        const email = emailInput.value.trim();
        const password = passwordInput.value;
        if (!email || !password) {
            showError('이메일과 비밀번호를 입력하세요');
            return;
        }

        submitButton.disabled = true;
        try {
            const response = await fetch('/api/auth/login', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({email: email, password: password})
            });

            // 실패 응답은 ApiResponse 봉투({success,code,message,data})로 온다
            const body = await response.json().catch(function () {
                return null;
            });

            if (!response.ok) {
                showError(body && body.message ? body.message : '로그인에 실패했습니다');
                return;
            }

            saveAuth(body);
            location.href = '/';
        } catch (e) {
            showError('서버에 연결할 수 없습니다');
        } finally {
            submitButton.disabled = false;
        }
    });
});
