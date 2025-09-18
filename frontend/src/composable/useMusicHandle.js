import { ref } from 'vue';
import { ElMessage } from "element-plus";

// 导入状态管理
import { useAuthStore } from "@/stores/auth.js";

// 导入服务器API
import { getMusicLikeInfoListService } from "@/api/public.js";
import { deleteLikeMusicInfoService, insertLikeMusicInfoService } from "@/api/music.js";

// 全局响应式点赞信息映射表
const musicLikeInfoMap = ref({});

// 本地存储键名
const LOCAL_STORAGE_KEY = 'music_like_info_cache';

export function useMusicHandle() {

    // 创建状态管理实例
    const authStore = useAuthStore();

    // 新增：点赞状态变化的回调（用于通知组件刷新）
    const onLikeStatusChange = ref(null);

    // 从本地存储加载点赞状态
    const loadLikeInfoFromLocalStorage = () => {
        // 如果是游客，直接返回
        if (authStore.isGuest) {
            return
        }
        try {
            const cachedData = localStorage.getItem(LOCAL_STORAGE_KEY);
            if (cachedData) {
                musicLikeInfoMap.value = JSON.parse(cachedData);
            }
        } catch (error) {
            console.error('本地存储加载失败:', error);
            localStorage.removeItem(LOCAL_STORAGE_KEY);
        }
    };

    // 保存点赞状态到本地存储
    const saveLikeInfoToLocalStorage = () => {
        // 如果是游客，直接返回
        if (authStore.isGuest) {
            return
        }
        try {
            localStorage.setItem(LOCAL_STORAGE_KEY, JSON.stringify(musicLikeInfoMap.value));
        } catch (error) {
            console.error('本地存储保存失败:', error);
        }
    };

    // 更新点赞信息（统一更新方法）
    const updateLikeInfo = (musicId, newInfo) => {
        // 如果是游客，直接返回
        if (authStore.isGuest) {
            return
        }
        musicLikeInfoMap.value = {
            ...musicLikeInfoMap.value,
            [musicId]: newInfo
        };
        saveLikeInfoToLocalStorage();
    };

    // 批量获取音乐点赞信息
    const fetchMusicLikeInfo = async (omdMusicInfoIdList) => {
        // 如果是游客，直接返回
        if (authStore.isGuest) {
            return
        }
        if (!omdMusicInfoIdList || omdMusicInfoIdList.length === 0) return;
        try {
            const { data } = await getMusicLikeInfoListService(omdMusicInfoIdList);

            // 处理API响应
            data.forEach(item => {
                updateLikeInfo(item.omdMusicInfoId, {
                    likeCount: item.omdMusicLikeCacheCount,
                    isUserLiked: item.isUserLiked
                });
            });

            return data;
        } catch (error) {
            console.error('获取点赞信息失败:', error);
            ElMessage.error('获取点赞信息失败，请稍后再试');
        }
    };

    // 点赞/取消点赞
    const toggleLike = async (omdMusicInfoId) => {
        // 如果是游客，直接返回
        if (authStore.isGuest) {
            return
        }
        // 获取当前状态
        const currentLikeInfo = getMusicLikeInfo(omdMusicInfoId);

        // 计算新状态
        const newLikeInfo = {
            likeCount: currentLikeInfo.likeCount + (currentLikeInfo.isUserLiked ? -1 : 1),
            isUserLiked: !currentLikeInfo.isUserLiked
        };

        try {
            // 乐观更新：先更新UI
            updateLikeInfo(omdMusicInfoId, newLikeInfo);

            // 发送API请求
            if (newLikeInfo.isUserLiked) {
                await insertLikeMusicInfoService(omdMusicInfoId);

                // 新增：通知可能需要刷新歌单列表
                if (onLikeStatusChange.value) {
                    onLikeStatusChange.value(omdMusicInfoId, true);
                }
            } else {
                await deleteLikeMusicInfoService(omdMusicInfoId);

                // 新增：通知可能需要刷新歌单列表
                if (onLikeStatusChange.value) {
                    onLikeStatusChange.value(omdMusicInfoId, false);
                }
            }

            ElMessage.success(newLikeInfo.isUserLiked ? '点赞成功' : '取消点赞成功');
        } catch (error) {
            console.error('点赞操作失败:', error);

            // 失败时回滚状态
            updateLikeInfo(omdMusicInfoId, currentLikeInfo);

            ElMessage.error('操作失败，请重试');
        }
    };

    // 获取单首音乐的点赞信息
    const getMusicLikeInfo = (omdMusicInfoId) => {
        // 如果是游客，直接返回
        if (authStore.isGuest) {
            return
        }
        return musicLikeInfoMap.value[omdMusicInfoId] || {
            likeCount: 0,
            isUserLiked: false
        };
    };

    // 初始化点赞信息
    const initMusicLikeInfo = async (omdMusicInfoIdList) => {
        // 如果是游客，直接返回
        if (authStore.isGuest) {
            return
        }
        loadLikeInfoFromLocalStorage();

        // 过滤需要从服务器获取的ID
        const needFetchIds = omdMusicInfoIdList.filter(id => {
            return !musicLikeInfoMap.value[id];
        });

        if (needFetchIds.length > 0) {
            await fetchMusicLikeInfo(needFetchIds);
        }
    };

    return {
        fetchMusicLikeInfo,
        getMusicLikeInfo,
        toggleLike,
        initMusicLikeInfo,
        updateLikeInfo,
        onLikeStatusChange
    };
}