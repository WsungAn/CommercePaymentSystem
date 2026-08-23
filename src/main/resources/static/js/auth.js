// 로그인 상태 보관소.
// JWT 는 Authorization 헤더로 보내야 하는데 <a> 링크로 화면을 이동할 때는 헤더를 실을 수 없다.
// 그래서 토큰은 브라우저가 들고 있다가, 인증이 필요한 데이터를 fetch 로 가져올 때만 붙인다.
const AUTH_STORAGE_KEY = 'commerce.auth';

function saveAuth(auth) {
    localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(auth));
}

function getAuth() {
    const raw = localStorage.getItem(AUTH_STORAGE_KEY);
    if (!raw) {
        return null;
    }
    try {
        return JSON.parse(raw);
    } catch (e) {
        localStorage.removeItem(AUTH_STORAGE_KEY); // 깨진 값은 버린다
        return null;
    }
}

function clearAuth() {
    localStorage.removeItem(AUTH_STORAGE_KEY);
}

/** 인증이 필요한 API 를 호출할 때 쓸 헤더 */
function authHeaders() {
    const auth = getAuth();
    return auth ? {Authorization: 'Bearer ' + auth.token} : {};
}

/** 헤더에 들어갈 링크 하나 */
function navLink(href, text) {
    const link = document.createElement('a');
    link.href = href;
    link.textContent = text;
    return link;
}

/** 헤더 우측 영역을 현재 로그인 상태에 맞게 다시 그린다 */
function renderAuthState() {
    const box = document.getElementById('auth-state');
    if (!box) {
        return;
    }

    const auth = getAuth();
    if (!auth || !auth.member) {
        return; // 비로그인 - 템플릿에 이미 들어있는 "로그인" 링크를 그대로 둔다
    }

    // 이름을 누르면 내 정보로 간다
    const name = document.createElement('a');
    name.className = 'auth-name';
    name.href = '/mypage';
    name.textContent = auth.member.name + '님';

    const logout = document.createElement('button');
    logout.type = 'button';
    logout.className = 'btn-link';
    logout.textContent = '로그아웃';
    logout.addEventListener('click', function () {
        clearAuth();
        location.href = '/';
    });

    box.textContent = '';
    box.append(name, navLink('/cart', '장바구니'), navLink('/orders', '주문내역'), logout);
}

/**
 * 인증이 필요한 API 호출을 한 곳으로 모은다.
 * - 401 이면 저장된 토큰을 지우고 null 을 돌려준다 (호출부는 로그인 안내로 바꾸면 된다)
 * - 그 밖의 실패는 ApiResponse 봉투의 message 를 담아 던진다
 */
async function apiFetch(path, options) {
    const request = Object.assign({}, options);
    request.headers = Object.assign({'Content-Type': 'application/json'}, authHeaders(), request.headers);

    const response = await fetch(path, request);
    if (response.status === 401) {
        clearAuth();
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

/** 로그인 링크가 달린 안내를 지정한 요소에 그린다 */
function renderLoginRequired(element) {
    element.textContent = '로그인이 필요합니다. ';
    const link = document.createElement('a');
    link.href = '/login';
    link.textContent = '로그인하러 가기';
    element.append(link);
    element.hidden = false;
}

document.addEventListener('DOMContentLoaded', renderAuthState);
