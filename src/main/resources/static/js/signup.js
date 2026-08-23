// 회원가입 폼. /api/auth/signup 은 201 만 돌려주고 본문이 없어서
// 성공하면 로그인 화면으로 보내고, 실패는 ApiResponse 봉투의 message 를 그대로 보여준다.
document.addEventListener('DOMContentLoaded', function () {
    const form = document.getElementById('signup-form');
    const nameInput = document.getElementById('name');
    const emailInput = document.getElementById('email');
    const passwordInput = document.getElementById('password');
    const passwordConfirmInput = document.getElementById('password-confirm');
    const phoneInput = document.getElementById('phone-number');
    const errorBox = document.getElementById('signup-error');
    const submitButton = document.getElementById('signup-submit');

    function showError(message) {
        errorBox.textContent = message;
        errorBox.hidden = false;
    }

    function hideError() {
        errorBox.hidden = true;
    }

    form.addEventListener('submit', async function (event) {
        event.preventDefault();
        hideError();

        const payload = {
            name: nameInput.value.trim(),
            email: emailInput.value.trim(),
            password: passwordInput.value,
            phoneNumber: phoneInput.value.trim()
        };

        if (!payload.name || !payload.email || !payload.password || !payload.phoneNumber) {
            showError('모든 항목을 입력하세요');
            return;
        }
        // 서버에는 확인용 필드가 없으므로 일치 여부는 화면에서만 본다
        if (payload.password !== passwordConfirmInput.value) {
            showError('비밀번호가 서로 다릅니다');
            return;
        }

        submitButton.disabled = true;
        try {
            const response = await fetch('/api/auth/signup', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(payload)
            });

            if (!response.ok) {
                const body = await response.json().catch(function () {
                    return null;
                });
                showError(body && body.message ? body.message : '회원가입에 실패했습니다');
                return;
            }

            location.href = '/login?signup=success';
        } catch (e) {
            showError('서버에 연결할 수 없습니다');
        } finally {
            submitButton.disabled = false;
        }
    });
});
