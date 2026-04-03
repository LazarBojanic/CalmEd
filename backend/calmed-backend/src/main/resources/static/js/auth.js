const API_BASE_URL = '';

const Auth = {
    _config: null,
    getConfig: async () => {
        if (Auth._config) return Auth._config;
        const response = await fetch(`${API_BASE_URL}/config`);
        Auth._config = await response.json();
        return Auth._config;
    },
    getToken: () => localStorage.getItem('accessToken'),
    setTokens: (tokens) => {
        localStorage.setItem('accessToken', tokens.accessToken);
        localStorage.setItem('refreshToken', tokens.refreshToken);
    },
    logout: () => {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        window.location.href = '/login';
    },
    isLoggedIn: () => !!localStorage.getItem('accessToken'),
    getUser: async () => {
        const token = Auth.getToken();
        if (!token) return null;
        try {
            // Add a timestamp parameter to prevent browser caching
            const response = await fetch(`${API_BASE_URL}/user/me?t=${new Date().getTime()}`, {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            if (response.status === 401) {
                Auth.logout();
                return null;
            }
            const user = await response.json();
            console.log('[DEBUG_LOG] Auth.getUser success. isPaid:', user.isPaid);
            return user;
        } catch (e) {
            console.error('Error fetching user:', e);
            return null;
        }
    },

    register: async (email, username, password, confirmPassword) => {
        const response = await fetch(`${API_BASE_URL}/auth/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, username, password, confirmPassword })
        });
        const result = await response.json();
        if (response.ok) {
            // Check if we got real tokens or placeholders
            if (result.access && result.access !== "") {
                Auth.setTokens({ accessToken: result.access, refreshToken: result.refresh });
                return { success: true, loggedIn: true };
            }
            return { success: true, loggedIn: false, message: 'Registration successful! Please check your email to verify your account before logging in.' };
        }
        return { success: false, message: result.message || 'Registration failed' };
    },

    login: async (email, password) => {
        const response = await fetch(`${API_BASE_URL}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        });
        const result = await response.json();
        if (response.ok) {
            Auth.setTokens({ accessToken: result.access, refreshToken: result.refresh });
            return { success: true };
        }
        return { success: false, message: result.message || 'Login failed' };
    },

    loginWithGoogle: async (idToken) => {
        const response = await fetch(`${API_BASE_URL}/auth/google`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ idToken })
        });
        const result = await response.json();
        if (response.ok) {
            Auth.setTokens({ accessToken: result.access, refreshToken: result.refresh });
            return { success: true };
        }
        return { success: false, message: result.message || 'Google login failed' };
    },

    loginWithApple: async (identityToken) => {
        const response = await fetch(`${API_BASE_URL}/auth/apple`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ identityToken })
        });
        const result = await response.json();
        if (response.ok) {
            Auth.setTokens({ accessToken: result.access, refreshToken: result.refresh });
            return { success: true };
        }
        return { success: false, message: result.message || 'Apple login failed' };
    },

    updateNavbar: async () => {
        // Find the rightmost side of the nav container
        const navContainer = document.querySelector('nav .flex.justify-between');
        if (!navContainer) return;

        // Ensure nav-items container (Home, About) is always present and correctly styled
        let navItems = document.getElementById('nav-items');
        if (!navItems) {
            const flexContainer = navContainer.querySelector('.flex');
            if (flexContainer) {
                navItems = document.createElement('div');
                navItems.id = 'nav-items';
                navItems.className = 'hidden sm:ml-6 sm:flex sm:space-x-8';
                flexContainer.appendChild(navItems);
            }
        }

        if (navItems) {
            const currentPath = window.location.pathname;
            const homeClass = (currentPath === '/' || currentPath === '/home')
                ? 'border-blue-500 text-gray-900 inline-flex items-center px-1 pt-1 border-b-2 text-sm font-medium'
                : 'border-transparent text-gray-500 hover:border-gray-300 hover:text-gray-700 inline-flex items-center px-1 pt-1 border-b-2 text-sm font-medium';
            const aboutClass = (currentPath === '/about')
                ? 'border-blue-500 text-gray-900 inline-flex items-center px-1 pt-1 border-b-2 text-sm font-medium'
                : 'border-transparent text-gray-500 hover:border-gray-300 hover:text-gray-700 inline-flex items-center px-1 pt-1 border-b-2 text-sm font-medium';
            
            navItems.innerHTML = `
                <a href="/" class="${homeClass}">Home</a>
                <a href="/about" class="${aboutClass}">About</a>
            `;
        }

        let authSection = document.getElementById('auth-section');
        if (authSection) authSection.remove();

        authSection = document.createElement('div');
        authSection.id = 'auth-section';
        authSection.className = 'flex items-center space-x-4';
        
        const currentPath = window.location.pathname;

        if (Auth.isLoggedIn()) {
            const user = await Auth.getUser();
            const premiumBadge = (user && user.isPaid) ? '<span class="bg-green-100 text-green-800 text-xs px-2 py-1 rounded-full font-bold mr-2">Premium</span>' : '';
            const accountLinkClass = (currentPath === '/account' || currentPath.startsWith('/account/')) 
                ? 'border-blue-500 text-gray-900 inline-flex items-center px-1 pt-1 border-b-2 text-sm font-medium'
                : 'text-gray-600 hover:text-blue-600 px-3 py-2 rounded-md text-sm font-medium';
            
            authSection.innerHTML = `
                ${premiumBadge}
                <a href="/account" class="${accountLinkClass}">Account</a>
                <button onclick="Auth.logout()" class="bg-blue-600 text-white px-4 py-2 rounded-md text-sm font-medium hover:bg-blue-700 transition ml-4">Logout</button>
            `;
        } else {
            authSection.innerHTML = `
                <a href="/login" class="text-gray-600 hover:text-blue-600 px-3 py-2 rounded-md text-sm font-medium">Login</a>
                <a href="/register" class="bg-blue-600 text-white px-4 py-2 rounded-md text-sm font-medium hover:bg-blue-700 transition ml-4">Register</a>
            `;
        }
        navContainer.appendChild(authSection);
    },

    initGoogleLogin: async () => {
        try {
            const config = await Auth.getConfig();
            console.log('[DEBUG_LOG] Initializing Google Login');
            if (!config.googleClientId || config.googleClientId === 'placeholder') {
                console.error('[DEBUG_LOG] Google Client ID is not configured. Google login will not work.');
                return;
            }
            console.log('[DEBUG_LOG] Using Google Client ID:', config.googleClientId);
            
            if (typeof google === 'undefined') {
                console.error('[DEBUG_LOG] Google Identity Services SDK not loaded. Retrying in 1s...');
                setTimeout(Auth.initGoogleLogin, 1000);
                return;
            }

            google.accounts.id.initialize({
                client_id: config.googleClientId,
                auto_select: false,
                ux_mode: 'popup',
                callback: async (response) => {
                    console.log('[DEBUG_LOG] Google login response received');
                    if (response.error) {
                        console.error('[DEBUG_LOG] Google login error:', response.error);
                        alert('Google login error: ' + response.error);
                        return;
                    }
                    const result = await Auth.loginWithGoogle(response.credential);
                    if (result.success) {
                        console.log('[DEBUG_LOG] Google login successful, redirecting to account');
                        window.location.href = '/account';
                    } else {
                        console.error('[DEBUG_LOG] Google login failed:', result.message);
                        alert('Google login failed: ' + result.message);
                    }
                }
            });

            const btn = document.getElementById('google-login-btn');
            if (btn) {
                google.accounts.id.renderButton(btn, { 
                    theme: 'outline', 
                    size: 'large', 
                    width: btn.offsetWidth > 0 ? btn.offsetWidth : 300,
                    type: 'standard',
                    shape: 'rectangular',
                    text: 'signin_with',
                    logo_alignment: 'left'
                });
            }
        } catch (error) {
            console.error('[DEBUG_LOG] Error initializing Google login:', error);
        }
    },

    initAppleLogin: async () => {
        try {
            const config = await Auth.getConfig();
            console.log('[DEBUG_LOG] Initializing Apple Login');
            if (!config.appleClientId || config.appleClientId === 'placeholder') {
                console.error('[DEBUG_LOG] Apple Client ID is not configured. Apple login will not work.');
                return;
            }

            if (typeof AppleID === 'undefined') {
                console.error('[DEBUG_LOG] Apple SDK not loaded. Retrying in 1s...');
                setTimeout(Auth.initAppleLogin, 1000);
                return;
            }

            const urlParams = new URLSearchParams(window.location.search);
            
            // Handle error from URL
            if (urlParams.get('status') === 'error') {
                const error = urlParams.get('error');
                console.error('[DEBUG_LOG] Apple login error from URL:', error);
                alert('Apple login failed: ' + error);
                // Clean up URL
                window.history.replaceState({}, document.title, window.location.pathname);
            }

            // Check for id_token in URL (redirect flow)
            const idTokenFromUrl = urlParams.get('id_token');
            if (idTokenFromUrl) {
                console.log('[DEBUG_LOG] id_token found in URL, performing Apple login');
                const result = await Auth.loginWithApple(idTokenFromUrl);
                if (result.success) {
                    window.location.href = '/account';
                    return;
                } else {
                    console.error('[DEBUG_LOG] Apple login from URL token failed:', result.message);
                    alert('Apple login failed: ' + result.message);
                    window.history.replaceState({}, document.title, window.location.pathname);
                }
            }

            console.log('[DEBUG_LOG] Apple Client ID:', config.appleClientId);
            console.log('[DEBUG_LOG] Apple Redirect URI:', config.appleRedirectUriWeb);

            try {
                AppleID.auth.init({
                    clientId: config.appleClientId,
                    scope: 'name email',
                    redirectURI: config.appleRedirectUriWeb,
                    state: 'web',
                    usePopup: false
                });
                console.log('[DEBUG_LOG] AppleID.auth.init successful (Redirect flow)');
            } catch (e) {
                console.error('[DEBUG_LOG] AppleID.auth.init failed:', e);
            }

            const btn = document.getElementById('apple-login-btn');
            if (btn) {
                const newBtn = btn.cloneNode(true);
                btn.parentNode.replaceChild(newBtn, btn);
                
                newBtn.onclick = async (e) => {
                    e.preventDefault();
                    console.log('[DEBUG_LOG] Apple login button clicked (Redirect flow)');
                    try {
                        AppleID.auth.signIn();
                    } catch (error) {
                        console.error('[DEBUG_LOG] Apple sign-in error:', error);
                    }
                };
            }
        } catch (error) {
            console.error('[DEBUG_LOG] Error initializing Apple login:', error);
        }
    }
};

document.addEventListener('DOMContentLoaded', () => {
    Auth.updateNavbar();
    if (document.getElementById('google-login-btn')) Auth.initGoogleLogin();
    if (document.getElementById('apple-login-btn')) Auth.initAppleLogin();
});
