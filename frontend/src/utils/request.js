// utils/request.js
import axios from 'axios';
import { ElMessage } from 'element-plus';
import router from '@/router';
import { useAuthStore } from '@/stores/auth.js'

const request = axios.create({
    baseURL: '/api',
    timeout: 50000
});

// 请求拦截器
request.interceptors.request.use(
    config => {
        // 初始化meta对象，避免undefined
        config.meta = config.meta || {};

        const authStore = useAuthStore()
        const token = authStore.token;
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`;
        }
        return config;
    },
    error => {
        return Promise.reject(error);
    }
);

// 响应拦截器
request.interceptors.response.use(
    response => {
        const res = response.data;
        if (res.code !== 0) {
            ElMessage({
                message: res.message || '请求失败',
                type: 'error',
                duration: 3000
            });
            return Promise.reject(new Error(res.message || '请求失败'));
        } else {
            return res;
        }
    },
    error => {
        console.error('请求错误', error);
        let message = error.message;

        if (error.response) {
            const { status, data } = error.response;
            message = data.message || message;

            // 处理401未授权错误
            if (status === 401) {
                const { needLogin = true } = error.config.meta;

                if (needLogin) {
                    // 需要跳转登录页
                    ElMessage.error('登录状态失效，请重新登录');
                    router.push('/login');
                } else {
                    // 只提示，不跳转
                    ElMessage.warning('您没有权限访问此资源');
                }
            }

            // 其他状态码处理
            if (status === 403) {
                ElMessage.error('禁止访问，您没有足够权限');
            } else if (status === 500) {
                ElMessage.error('服务器内部错误');
            }else if (status === 400) {
                ElMessage.error(message);
            }
        } else {
            ElMessage.error('网络错误，请稍后再试');
        }

        return Promise.reject(error);
    }
);

export default request;