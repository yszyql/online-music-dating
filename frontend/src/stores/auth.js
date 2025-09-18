import { defineStore } from "pinia"
import { ref, computed } from 'vue'

export const useAuthStore = defineStore('auth', () => {
    // 1. 登录状态
    const isLoggedIn = ref(false)

    // 2. Token 管理
    const token = ref('')

    // 定义设置 token 的方法
    const setToken = (newToken) => {
        token.value = newToken
        isLoggedIn.value = !!newToken // 有 token 即视为已登录
        console.log('设置 token:', newToken)
    }

    // 3. 用户信息
    const userInfo = ref({})

    // 定义设置用户信息的方法
    const setUserInfo = (userData) => {
        userInfo.value = userData
        console.log('设置用户信息:', userData)
    }

    // 4. 登录方法
    // 定义登录方法（整合设置逻辑）
    const login = (tokenVal, userData) => {
        setToken(tokenVal)
        setUserInfo(userData)
        isLoggedIn.value = true
    }

    // 5. 登出方法
    const logout = () => {
        isLoggedIn.value = false
        token.value = ''
        userInfo.value = {}
        console.log('用户已登出')
    }

    // 6. 便捷访问用户 ID
    const userId = computed(() => userInfo.value.omdUserId)

    // 7. 判断是否为游客
    const isGuest = computed(() => !isLoggedIn.value)


    // 计算属性：用户角色判断
    const userRoles = computed(() => {
        // 如果 omdUserRole 是数组，提取每个元素的 omdRoleCode；否则返回空数组
        return (userInfo.value.omdUserRole || []).map(item => item.omdRoleCode);
    });

    // 计算属性：是否为管理员
    const isAdmin = computed(() => {
        return isLoggedIn.value && userRoles.value.includes("ROLE_ADMIN");
    });

    // 计算属性：是否为歌手
    const isSinger = computed(() => {
        return isLoggedIn.value && userRoles.value.includes("ROLE_SINGER");
    });

    return {
        isAdmin,
        isSinger,
        isLoggedIn,
        token,
        setToken,
        setUserInfo,
        userInfo,
        userId,
        isGuest,
        login,
        logout
    }
}, {
    persist: {
        key: 'auth_state',
        paths: ['isLoggedIn', 'token', 'userInfo'] // 统一持久化相关字段
    }
})