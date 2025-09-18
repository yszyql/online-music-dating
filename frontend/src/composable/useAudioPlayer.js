import {computed, nextTick, onMounted, onUnmounted, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';

// 导入状态管理
import {useMusicStore} from '@/stores/music';
import {useAuthStore} from '@/stores/auth.js'

// 导入服务
import {recordMusicPlayStatService} from '@/api/public.js';

// 全局音频实例
let globalAudio = null;

export function useAudioPlayer() {
    // 状态管理
    const musicStore = useMusicStore();
    const authStore = useAuthStore();

    // 响应式状态
    // 播放器状态
    const audio = ref(null);
    const bufferPercent = ref(0);
    const duration = ref(0);
    const playlistVisible = ref(false);
    const playPromise = ref(null); // 保存播放承诺
    const shuffledPlaylist = ref([]); // 存储随机播放列表

    // 播放模式枚举
    const PlayMode = {
        SEQUENTIAL: 'sequential', // 顺序播放
        LOOP: 'loop',             // 列表循环
        RANDOM: 'random',         // 随机播放
        SINGLE: 'single'          // 单曲循环
    };

    const playMode = ref(musicStore.playMode || PlayMode.SEQUENTIAL); // 播放模式


    // 方法

    // 获取游客 UUID
    const getGuestUuid = () => {
        if (!authStore.isGuest) return;
        let uuid = localStorage.getItem('guest_uuid');
        if (!uuid) {
            uuid = crypto.randomUUID();
            localStorage.setItem('guest_uuid', uuid);
            console.log('生成游客 UUID:', uuid);
        }
        return uuid;
    };

    /** 记录播放行为 */
    const recordPlay = async (omdMusicInfoId) => {
        try {
            const isGuestMode = authStore.isGuest;

            const playData = {
                omdMusicInfoId: omdMusicInfoId,
                isGuest: isGuestMode ? 1 : 0,
                guestUuid: isGuestMode ? getGuestUuid() : null,
            };

            await recordMusicPlayStatService(playData);
        } catch (error) {
            console.error('上报播放记录失败:', error);
        }
    };

    /** 设置音频源并加载 */
    const setupAudio = async (music) => {
        if (!music.omdMusicInfoSongUrl) {
            ElMessage.error('暂无播放地址');
            return;
        }

        // 若当前音频实例已加载同一首歌，直接恢复状态
        if (audio.value && audio.value.src === music.omdMusicInfoSongUrl) {
            audio.value.currentTime = musicStore.currentTime; // 恢复进度
            if (musicStore.isPlaying) {
                await playAudio(); // 继续播放
            }
            return;
        }

        await pauseAudio();

        try {
            await recordPlay(music.omdMusicInfoId);
            audio.value.src = music.omdMusicInfoSongUrl;
            musicStore.currentMusic = music;
            musicStore.currentTime = 0;

            // 加载新资源
            await new Promise((resolve) => {
                const onLoaded = () => {
                    audio.value.removeEventListener('loadedmetadata', onLoaded);
                    resolve();
                };

                audio.value.addEventListener('loadedmetadata', onLoaded);
                audio.value.load();
            });

            if (musicStore.currentTime > 0) {
                audio.value.currentTime = musicStore.currentTime;
            }

            // 如果是用户选择的新歌，尝试播放
            if (musicStore.userInteracted) {
                await smartPlay();
            }
        } catch (error) {
            console.error('设置音频源失败:', error);
            ElMessage.error('加载歌曲失败');
        }
    };

    /** 播放/暂停切换 */
    const togglePlay = async () => {
        if (!musicStore.currentMusic) {
            ElMessage.error('没有当前播放的音乐');
            return;
        }

        if (authStore.isGuest) {
            ElMessage.warning('游客模式仅支持试听');
        }

        // 标记用户已交互
        musicStore.setUserInteracted(true);
        musicStore.markFirstLoadComplete();

        musicStore.isPlaying ? await pauseAudio() : await smartPlay();
    };

    /** 智能播放（处理自动播放限制） */
    const smartPlay = async () => {
        if (musicStore.isFirstLoad) {
            return;
        }

        try {
            // 确保音频已加载元数据
            if (audio.value.readyState < HTMLMediaElement.HAVE_METADATA) {
                await new Promise((resolve) => {
                    const onLoaded = () => {
                        audio.value.removeEventListener('loadedmetadata', onLoaded);
                        resolve();
                    };
                    audio.value.addEventListener('loadedmetadata', onLoaded);
                });
            }

            await playAudio();
        } catch (err) {
            if (err.name === 'NotAllowedError') {
                return;
            } else if (err.name === 'AbortError') {
                console.log('播放请求被中断，重试播放');
                // 重试播放
                setTimeout(async () => {
                    await smartPlay();
                }, 100);
            } else {
                console.error('播放失败:', err);
                ElMessage.error('播放失败，请点击重试');
            }
        }
    };

    /** 播放全部歌曲 */
    const playAll = async (currentPlaylist, playlist) => {
        if (!currentPlaylist || currentPlaylist.length === 0) {
            ElMessage.warning('播放列表为空');
            return;
        }

        try {
            // 设置播放列表
            musicStore.playlist = playlist;
            musicStore.currentPlaylist = currentPlaylist;
            musicStore.currentIndex = 0;
            musicStore.setCurrentMusic(currentPlaylist[0]);

            // 标记用户已交互
            musicStore.setUserInteracted(true);
            musicStore.markFirstLoadComplete();

            // 确保播放器设置完成
            await nextTick();

            // 立即播放
            await smartPlay();
        } catch (error) {
            console.error('播放全部失败:', error);
            ElMessage.error('播放失败');
        }
    };

    /** 随机播放歌曲 */
    const shufflePlay = async (currentPlaylist, playlist) => {
        if (!currentPlaylist || currentPlaylist.length === 0) {
            ElMessage.warning('播放列表为空');
            return;
        }

        try {
            // 创建播放列表的副本
            const shuffledList = [...currentPlaylist];

            // Fisher-Yates 洗牌算法
            fisherYatesShuffle(shuffledList);

            // 设置播放列表
            musicStore.playlist = playlist;
            musicStore.currentPlaylist = shuffledList;
            musicStore.currentIndex = 0;
            musicStore.setCurrentMusic(shuffledList[0]);

            // 标记用户已交互
            musicStore.setUserInteracted(true);
            musicStore.markFirstLoadComplete();

            // 确保播放器设置完成
            await nextTick();

            // 立即播放
            await smartPlay();
        } catch (error) {
            console.error('随机播放失败:', error);
            ElMessage.error('播放失败');
        }
    };

    /** 播放音频（处理自动播放限制） */
    const playAudio = async () => {
        if (!audio.value) return;

        // 清除之前的播放承诺
        if (playPromise.value) {
            try {
                await playPromise.value;
            } catch (ignore) {
                // 忽略中断错误
            } finally {
                playPromise.value = null;
            }
        }

        try {
            // 确保音频已加载元数据
            if (audio.value.readyState < HTMLMediaElement.HAVE_METADATA) {
                await new Promise((resolve) => {
                    const onLoaded = () => {
                        audio.value.removeEventListener('loadedmetadata', onLoaded);
                        resolve();
                    };
                    audio.value.addEventListener('loadedmetadata', onLoaded);
                });
            }

            playPromise.value = audio.value.play();
            await playPromise.value;

            musicStore.isPlaying = true;
        } catch (err) {
            if (err.name === 'AbortError') {
                console.log('播放请求被中断');
                throw err; // 重新抛出，让上层处理
            } else if (err.name === 'NotAllowedError') {
                console.log('自动播放被阻止');
                throw err; // 抛出错误让上层处理
            } else {
                console.error('播放失败:', err);
                musicStore.isPlaying = false;
                ElMessage.error('播放失败，请点击重试');
                throw err;
            }
        } finally {
            playPromise.value = null;
        }
    };

    /** 暂停音频 */
    const pauseAudio = async () => {
        if (!audio.value) return;

        if (playPromise.value) {
            playPromise.value.catch(ignore => {});
            playPromise.value = null;
        }

        try {
            audio.value.pause();
            musicStore.isPlaying = false;
        } catch (err) {
            console.error('暂停失败:', err);
        }
    };

    /** 播放下一首（增强支持多种模式） */
    const playNext = async () => {
        // 先暂停当前播放
        await pauseAudio();

        let nextIndex;
        let playlist = musicStore.currentPlaylist;

        // 根据播放模式选择播放列表
        if (playMode.value === PlayMode.RANDOM && shuffledPlaylist.value.length > 0) {
            playlist = shuffledPlaylist.value;
        }

        // 根据模式计算下一首索引
        switch (playMode.value) {
            case PlayMode.RANDOM:
            case PlayMode.LOOP:
                nextIndex = (musicStore.currentIndex + 1) % playlist.length;
                break;

            case PlayMode.SINGLE:
                // 单曲循环模式下，按照原始播放列表顺序切换
                nextIndex = musicStore.currentIndex + 1;
                if (nextIndex >= playlist.length) {
                    // 如果是最后一首，回到第一首
                    nextIndex = 0;
                }
                break;

            default: // SEQUENTIAL
                nextIndex = musicStore.currentIndex + 1;
                if (nextIndex >= playlist.length) {
                    musicStore.isPlaying = false;
                    return;
                }
        }

        // 更新当前播放歌曲
        const nextMusic = playlist[nextIndex];
        if (nextMusic) {
            // 确保设置新歌曲前暂停当前播放
            musicStore.setCurrentMusic(nextMusic);
            musicStore.currentIndex = nextIndex;

            // 确保设置完成后播放
            await nextTick();

            // 设置用户交互标志
            musicStore.setUserInteracted(true);
            musicStore.markFirstLoadComplete();

            // 使用setTimeout避免播放冲突
            setTimeout(async () => {
                await smartPlay();
            }, 100);
        }
    };

    /** 播放上一首 */
    const playPrev = async () => {
        // 先暂停当前播放
        await pauseAudio();

        let prevIndex;
        let playlist = musicStore.currentPlaylist;

        if (playMode.value === PlayMode.RANDOM && shuffledPlaylist.value.length > 0) {
            playlist = shuffledPlaylist.value;
        }

        switch (playMode.value) {
            case PlayMode.RANDOM:
            case PlayMode.LOOP:
                prevIndex = (musicStore.currentIndex - 1 + playlist.length) % playlist.length;
                break;

            case PlayMode.SINGLE:
                // 单曲循环模式下，按照原始播放列表顺序切换
                prevIndex = musicStore.currentIndex - 1;
                if (prevIndex < 0) {
                    // 如果是第一首，跳到最后一首
                    prevIndex = playlist.length - 1;
                }
                break;

            default: // SEQUENTIAL
                prevIndex = musicStore.currentIndex - 1;
                if (prevIndex < 0) {
                    return;
                }
        }

        const prevMusic = playlist[prevIndex];
        if (prevMusic) {
            // 确保设置新歌曲前暂停当前播放
            musicStore.setCurrentMusic(prevMusic);
            musicStore.currentIndex = prevIndex;

            // 确保设置完成后播放
            await nextTick();

            // 设置用户交互标志
            musicStore.setUserInteracted(true);
            musicStore.markFirstLoadComplete();

            // 使用setTimeout避免播放冲突
            setTimeout(async () => {
                await smartPlay();
            }, 100);
        }
    };

    /** 从播放列表选歌播放 */
    const playMusicFromPlaylist = async (music, index, playlist,currentPlaylist) => {
        try {
            musicStore.setUserInteracted(true);
            musicStore.markFirstLoadComplete();

            // 更新随机播放列表
            if (playMode.value === PlayMode.RANDOM) {
                shuffledPlaylist.value = [...currentPlaylist];
                // Fisher-Yates 洗牌算法
                fisherYatesShuffle(shuffledPlaylist.value);
                // 找到在新列表中的位置
                index = shuffledPlaylist.value.findIndex(
                    item => item.omdMusicInfoId === music.omdMusicInfoId
                );
            }

            musicStore.currentMusic = music;
            musicStore.currentIndex = index;
            musicStore.currentPlaylist = currentPlaylist;
            musicStore.playlist = playlist;

            await nextTick();
            await smartPlay();
        } catch (error) {
            console.error('播放列表选歌失败:', error);
            ElMessage.error('播放失败');
        }
    };

    // 切换播放模式时重置播放列表
    const togglePlayMode = () => {
        const modes = [
            PlayMode.SEQUENTIAL,
            PlayMode.SINGLE,
            PlayMode.LOOP,
            PlayMode.RANDOM
        ];

        const currentIndex = modes.indexOf(playMode.value);
        const newMode = modes[(currentIndex + 1) % modes.length];

        playMode.value = newMode;
        musicStore.setPlayMode(newMode);

        // 随机模式时创建洗牌列表
        if (newMode === PlayMode.RANDOM && musicStore.currentPlaylist.length > 0) {
            shuffledPlaylist.value = [...musicStore.currentPlaylist];
            // Fisher-Yates 洗牌算法
            fisherYatesShuffle(shuffledPlaylist.value);
        }

        ElMessage.success(`已切换到${getPlayModeName(newMode)}模式`);
    };

    /** Fisher-Yates 洗牌算法 */
    const fisherYatesShuffle = (array) => {
        for (let i = array.length - 1; i > 0; i--) {
            const j = Math.floor(Math.random() * (i + 1));
            [array[i], array[j]] = [array[j], array[i]];
        }
        return array;
    };

    /** 获取播放模式名称 */
    const getPlayModeName = (mode) => {
        switch (mode) {
            case PlayMode.SEQUENTIAL: return '顺序';
            case PlayMode.LOOP: return '列表循环';
            case PlayMode.RANDOM: return '随机';
            case PlayMode.SINGLE: return '单曲循环';
            default: return '未知';
        }
    };

    /** 音频结束处理 - 支持单曲循环 */
    const handleAudioEnded = async () => {
        musicStore.isPlaying = false;

        // 单曲循环模式特殊处理
        if (playMode.value === PlayMode.SINGLE) {
            audio.value.currentTime = 0;
            await smartPlay();
        } else {
            await playNext(); // 自动切下一首
        }
    };

    // 更新进度百分比
    const updateProgressPercent = () => {
        if (musicStore.duration > 0 && audio.value) {
            const percent = (audio.value.currentTime / musicStore.duration) * 100;
            musicStore.updateProgressState(
                percent,
                musicStore.bufferPercent,
                musicStore.duration
            );
        }
    };

    // 格式化时间
    const formatTime = (seconds) => {
        if (isNaN(seconds) || seconds === 0) return '00:00';

        const minutes = Math.floor(seconds / 60);
        const secs = Math.floor(seconds % 60);
        return `${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
    };

    // 处理时间更新
    const handleTimeUpdate = () => {
        if (!musicStore.isDragging) {
            musicStore.updateCurrentTime(audio.value.currentTime);
            updateProgressPercent();
        }
    };

    // 处理元数据加载完成
    const handleMetadataLoaded = () => {
        musicStore.duration = audio.value.duration;
    };

    // 处理缓冲进度
    const handleProgress = () => {
        if (audio.value.buffered.length > 0) {
            const bufferedEnd = audio.value.buffered.end(audio.value.buffered.length - 1);
            if (musicStore.duration > 0) {
                musicStore.bufferPercent = (bufferedEnd / musicStore.duration) * 100;
            }
        }
    };

    // 进度条点击跳转
    const seekByClick = (event) => {
        if (!audio.value || musicStore.duration <= 0) return;

        const progressBar = event.currentTarget;
        const rect = progressBar.getBoundingClientRect();
        const clickX = event.clientX - rect.left;
        const seekTime = (clickX / rect.width) * musicStore.duration;

        audio.value.currentTime = seekTime;
        musicStore.seekTo(seekTime);
    };

    // 开始拖拽进度条
    const startDrag = (event) => {
        if (!audio.value || duration.value <= 0) return;

        musicStore.isDragging = true;
        document.addEventListener('mousemove', handleDrag);
        document.addEventListener('mouseup', endDrag);

        // 初始拖拽位置
        const progressBar = event.currentTarget;
        const rect = progressBar.getBoundingClientRect();
        musicStore.dragPosition = Math.max(0, Math.min(100, ((event.clientX - rect.left) / rect.width) * 100));
        updateProgressFromDrag();
    };

    // 处理拖拽
    const handleDrag = (event) => {
        if (!musicStore.isDragging) return;

        const progressBar = document.querySelector('.progress-bar');
        if (!progressBar || !audio.value || duration.value <= 0) {
            endDrag(); // 确保在异常情况下也能结束拖拽
            return;
        }

        const rect = progressBar.getBoundingClientRect();
        musicStore.dragPosition = Math.max(0, Math.min(100, ((event.clientX - rect.left) / rect.width) * 100));
        updateProgressFromDrag();
    };

    // 结束拖拽
    const endDrag = () => {
        // 无论什么情况都先移除事件监听器
        document.removeEventListener('mousemove', handleDrag);
        document.removeEventListener('mouseup', endDrag);

        if (!musicStore.isDragging || !audio.value || duration.value <= 0) {
            musicStore.isDragging = false;
            return;
        }

        const seekTime = (musicStore.dragPosition / 100) * duration.value;
        audio.value.currentTime = seekTime;
        musicStore.seekTo(seekTime);

        musicStore.isDragging = false;
    };

    // 从拖拽位置更新进度
    const updateProgressFromDrag = () => {
        musicStore.progressPercent = musicStore.dragPosition;
    };

    // 设置音量
    const setVolume = (value) => {
        if (!audio.value) return;

        // 转换为音频所需的 0-1 范围
        audio.value.volume = value / 100;

        // 更新 Pinia 状态
        musicStore.setVolume(value);

        // 转换为音频所需的 0-1 范围
        audio.value.volume = value / 100;

        // 如果设置为0则静音，否则取消静音
        if (value === 0) {
            musicStore.isMuted = true;
        } else if (musicStore.isMuted) {
            musicStore.isMuted = false;
        }
    };

    // 切换静音
    const toggleMute = () => {
        if (!audio.value) return;

        if (!audio.value) return;

        musicStore.toggleMute();

        if (musicStore.isMuted) {
            // 静音时记录当前音量
            audio.value.volume = 0;
        } else {
            // 取消静音时恢复音量
            audio.value.volume = musicStore.volume / 100;
        }
    };

    // 切换播放列表显示
    const togglePlaylist = () => {
        playlistVisible.value = !playlistVisible.value;
    };


    // 清空播放列表
    const clearPlaylist = () => {
        musicStore.playlist = [];
        musicStore.currentPlaylist = [];
        musicStore.setCurrentMusic({});
        playlistVisible.value = false;
    };

    // 生命周期钩子
    onMounted(() => {
        // 确保全局只有一个音频实例
        if (!globalAudio) {
            globalAudio = new Audio();
            audio.value = globalAudio;
        } else {
            audio.value = globalAudio;
        }

        // 设置初始音量
        audio.value.volume = musicStore.volume / 100;

        // 设置静音状态
        if (musicStore.isMuted) {
            audio.value.volume = 0;
        }

        // 添加事件监听
        audio.value.addEventListener('ended', handleAudioEnded);
        audio.value.addEventListener('timeupdate', handleTimeUpdate);
        audio.value.addEventListener('loadedmetadata', handleMetadataLoaded);
        audio.value.addEventListener('progress', handleProgress);

        // 监听当前音乐变化
        // 修改 currentMusic 的 watch 逻辑
        watch(
            () => musicStore.currentMusic,
            async (newMusic, oldMusic) => {
                // 若新歌曲与旧歌曲ID相同，不重新加载，直接恢复状态
                if (newMusic?.omdMusicInfoId === oldMusic?.omdMusicInfoId && newMusic) {
                    if (audio.value) {
                        audio.value.currentTime = musicStore.currentTime; // 恢复进度
                        if (musicStore.isPlaying) {
                            await playAudio(); // 继续播放
                        }
                    }
                    return;
                }
                // 不同歌曲时才重新加载
                if (newMusic) {
                    await setupAudio(newMusic);
                }
            },
            { deep: true, immediate: true }
        );

        // 监听音量变化（使用 Pinia 状态）
        watch(() => musicStore.volume, (newVolume) => {
            if (audio.value) {
                // 如果当前是静音状态且设置了，则取消静音
                if (musicStore.isMuted && newVolume > 0) {
                    musicStore.isMuted = false;
                }
                audio.value.volume = newVolume / 100;
            }
        });

        // 监听静音状态变化
        watch(() => musicStore.isMuted, (isMuted) => {
            if (audio.value) {
                if (isMuted) {
                    audio.value.volume = 0;
                } else {
                    audio.value.volume = musicStore.volume / 100;
                }
            }
        });

        // 对播放模式的监听
        watch(
            () => playMode.value,
            (newMode) => {
                musicStore.setPlayMode(newMode);
            }
        );
    });

    onUnmounted(() => {
        // 只清理组件局部的拖拽事件（非全局音频事件）
        document.removeEventListener('mousemove', handleDrag);
        document.removeEventListener('mouseup', endDrag);

        // 保存当前进度
        if (audio.value) {
            musicStore.updateCurrentTime(audio.value.currentTime);
            musicStore.isPlaying = !audio.value.paused; // 保存播放状态
        }

        // 游客模式下清理UUID
        if (authStore.isGuest) {
            localStorage.removeItem('guest_uuid');
        }
    });

    // 返回可复用的状态和方法
    return {
        // 状态
        audio,
        bufferPercent,
        duration,
        playlistVisible,

        // 方法
        smartPlay,
        setupAudio,
        togglePlay,
        playNext,
        playPrev,
        playMusicFromPlaylist,
        seekByClick,
        startDrag,
        setVolume,
        toggleMute,
        formatTime,
        togglePlaylist,
        clearPlaylist,
        playAll,
        shufflePlay,
        togglePlayMode,
        PlayMode,
        playMode: computed(() => playMode.value),
        playModeName: computed(() => getPlayModeName(playMode.value))
    };
}