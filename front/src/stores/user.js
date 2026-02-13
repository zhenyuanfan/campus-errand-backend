import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getLoginUser, userLogout as apiLogout } from '@/api/user'

export const useUserStore = defineStore('user', () => {
    const loginUser = ref(null)

    const isLoggedIn = computed(() => !!loginUser.value)
    const isAdmin = computed(() => loginUser.value?.userRole === 'admin')
    const isRunner = computed(() =>
        loginUser.value?.userRole === 'runner' || loginUser.value?.userRole === 'admin'
    )

    async function fetchLoginUser() {
        try {
            const data = await getLoginUser()
            loginUser.value = data
        } catch {
            loginUser.value = null
        }
    }

    async function logout() {
        try {
            await apiLogout()
        } finally {
            loginUser.value = null
        }
    }

    function setLoginUser(user) {
        loginUser.value = user
    }

    return { loginUser, isLoggedIn, isAdmin, isRunner, fetchLoginUser, logout, setLoginUser }
})
