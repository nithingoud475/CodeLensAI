const API_BASE = '/api';

function getToken() {
    return sessionStorage.getItem('jwt');
}

function setToken(token) {
    sessionStorage.setItem('jwt', token);
}

function clearToken() {
    sessionStorage.removeItem('jwt');
}

function isAuthenticated() {
    return !!getToken();
}

async function apiRequest(endpoint, options = {}) {
    const token = getToken();
    const headers = {
        'Content-Type': 'application/json',
        ...options.headers
    };

    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(`${API_BASE}${endpoint}`, {
        ...options,
        headers
    });

    if (response.status === 401) {
        clearToken();
        window.location.href = '/index.html';
        throw new Error('Unauthorized');
    }

    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
        const data = await response.json();
        if (!response.ok) {
            throw new Error(data.message || data.error || 'API Error');
        }
        return data;
    } else {
        const text = await response.text();
        if (!response.ok) {
            throw new Error(text || 'API Error');
        }
        return text;
    }
}

function logout() {
    clearToken();
    window.location.href = '/index.html';
}

function checkAuthAndRedirect() {
    const path = window.location.pathname;
    if (!isAuthenticated() && path !== '/index.html' && path !== '/') {
        window.location.href = '/index.html';
    } else if (isAuthenticated() && (path === '/index.html' || path === '/')) {
        window.location.href = '/dashboard.html';
    }
}

document.addEventListener('DOMContentLoaded', () => {
    checkAuthAndRedirect();
    
    const logoutBtn = document.getElementById('logout-btn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', (e) => {
            e.preventDefault();
            logout();
        });
    }
});
