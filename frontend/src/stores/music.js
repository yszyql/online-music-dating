import { defineStore } from 'pinia';

export const useMusicStore = defineStore('music', {
    state: () => ({
        playlist: [],
        currentPlaylist: [],
        currentMusic: {},
        currentIndex: 0,
        currentTime: 0,  // 新增：当前播放时间（秒）
        isPlaying: false,
        isFirstLoad: true, // 新增：首次加载标志
        playMode: '', // 新增：播放模式
        userInteracted: false, // 新增：用户是否已交互
        progressPercent: 0, // 新增：进度百分比
        bufferPercent: 0,   // 新增：缓冲百分比
        duration: 0,        // 新增：总时长
        isDragging: false,   // 新增：拖拽状态
        volume: 70, // 新增：音量百分比
        isMuted: false // 新增：静音状态
    }),
    actions: {

        // 标记首次加载完成
        markFirstLoadComplete() {
            this.isFirstLoad = false;
        },

        // 设置当前播放歌曲
        setCurrentMusic(song) {
            this.currentMusic = song;
            if (song) {
                this.currentTime = 0; // 重置播放时间状态
            }
        },
        // 新增：更新播放进度
        updateCurrentTime(time) {
            this.currentTime = time;
        },
        seekTo(time){
            this.currentTime = time;
        },
        // 新增：设置播放模式
        setPlayMode(mode) {
            this.playMode = mode;
        },
        setUserInteracted(interacted) {
            this.userInteracted = interacted;
        },
        // 新增：更新进度状态
        updateProgressState(progress, buffer, duration) {
            if (!this.isDragging) {
                this.progressPercent = progress;
                this.bufferPercent = buffer;
                this.duration = duration;
            }
        },
        // 新增：设置音量
        setVolume(volume) {
            this.volume = Math.max(0, Math.min(100, volume)); // 确保在 0-100 范围内
        },

        // 新增：切换静音
        toggleMute() {
            this.isMuted = !this.isMuted;
        },

        // 清空持久化数据
        clearPersistentData() {
            this.$reset(); // 重置状态到初始值
            localStorage.removeItem('music-store'); // 移除本地存储
        }

    },
    // 新增：持久化配置
    persist: {
        key: 'music-store', // 存储键名，可自定义
        storage: localStorage, // 存储位置，可选 sessionStorage
        paths: [ // 指定需要持久化的字段
            'playlist',
            'currentPlaylist',
            'userInteracted',
            'currentMusic',
            'currentIndex',
            'currentTime',  // 新增：持久化播放进度
            'isPlaying',
            'playMode', // 新增：持久化播放模式
            'progressPercent',
            'bufferPercent',
            'duration',
            'isDragging',
            'volume', // 新增：持久化音量
            'isMuted' // 新增：持久化静音状态
        ]
    }
});