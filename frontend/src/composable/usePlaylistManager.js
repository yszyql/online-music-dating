import {computed, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';

// 导入服务器请求
import {getPlaylistListByUserIdService} from "@/api/user.js";
import {
    deleteLikeMusicInfoService,
    deleteMusicFromPlaylistService, deleteMusicListFromPlaylistService,
    deletePlaylistService, insertLikeMusicInfoService, insertMusicListToPlaylistService,
    insertMusicToPlaylistService,
    insertPlaylistService,
    updatePlaylistService
} from "@/api/music.js";

export function usePlaylistManager() {

    // 用户歌单列表
    const playlistList = ref([]);
    const loadingPlaylistList = ref(false);

    // 弹窗可见性
    const showPlaylistAddToDialog = ref(false);
    const showCreatePlaylistDialog = ref(false);
    const showEditPlaylistDialog = ref(false);

    // 批量操作状态
    const batchMode = ref(false); // 是否处于批量选择模式
    const selectedSongs = ref([]); // 选中的歌曲ID数组

    // 是否有更多数据
    const hasMore = ref(true);

    // 表单加载状态
    const formLoading = ref(false);


    // 分页相关状态
    const pagination = ref({
        pageNum: 1,
        pageSize: 10,
        total: 0
    });

    // 表单数据
    const formData = ref({
        omdPlaylistName: '',
        omdPlaylistDescription: '',
        omdPlaylistCover: '',
        omdPlaylistPublic: 0 // 默认私有
    });

    // 表单验证规则
    const formRules = ref({
        omdPlaylistName: [
            { required: true, message: '请输入歌单名称', trigger: 'blur' },
            { min: 1, max: 20, message: '长度在 1 到 20 个字符', trigger: 'blur' }
        ]
    });


    // 获取用户歌单
    const fetchUserPlaylistsData = async (loadMore = false) => {

        try {
            loadingPlaylistList.value = true;

            // 如果是加载更多，增加页码；否则重置为第一页
            if (!loadMore) {
                pagination.value.pageNum = 1;
                playlistList.value = [];
            }

            const response = await getPlaylistListByUserIdService(
                pagination.value.pageNum,
                pagination.value.pageSize
            );

            // 更新分页信息
            pagination.value.total = response.data.total;

            // 合并歌单列表
            playlistList.value = [
                ...playlistList.value,
                ...response.data.items
            ];

            // 检查是否还有更多数据
            hasMore.value = playlistList.value.length < pagination.value.total;

            // 增加页码为下次加载准备
            if (hasMore.value) {
                pagination.value.pageNum++;
            }
        } catch (error) {
            console.error('获取用户歌单失败:', error);
            ElMessage.error('获取歌单失败');
        } finally {
            loadingPlaylistList.value = false;
        }
    };

    // 打开添加歌曲弹窗
    const openAddToPlaylistDialog = async (song, songIds = []) => {
        selectedSongs.value = Array.isArray(songIds) ? songIds : [songIds];
        showPlaylistAddToDialog.value = true;

        /// 获取用户歌单数据（初始加载）
        await fetchUserPlaylistsData();
    };

    // 加载更多歌单
    const loadMorePlaylists = async () => {
        if (!loadingPlaylistList.value && hasMore.value) {
            await fetchUserPlaylistsData(true);
        }
    };

    // 切换批量选择模式
    const toggleBatchMode = () => {
        batchMode.value = !batchMode.value;
        if (!batchMode.value) {
            // 退出批量模式时清除选择
            selectedSongs.value = [];
        }
    };

    // 切换单支歌曲选择状态
    const toggleSongSelection = (songId) => {
        const index = selectedSongs.value.indexOf(songId);
        if (index === -1) {
            selectedSongs.value.push(songId);
        } else {
            selectedSongs.value.splice(index, 1);
        }
    };


    // 全选/取消全选
    const toggleSelectAll = (playlistMusic) => {
        // 检查 playlistMusic 是否有效
        if (!playlistMusic || playlistMusic.length === 0) return;

        // 计算当前是否全选
        const isCurrentlyAllSelected = selectedSongs.value.length === playlistMusic.length;

        if (isCurrentlyAllSelected) {
            // 取消全选
            selectedSongs.value = [];
        } else {
            // 全选
            selectedSongs.value = playlistMusic.map(
                item => item.omdMusicInfo.omdMusicInfoId
            );
        }
    };

    // 添加歌曲到歌单
    const addSongToPlaylistHandler = async (omdPlaylistName) => {

        try {
            if (selectedSongs.value.length === 0) {
                ElMessage.warning('请选择要添加的歌曲');
                return;
            }

            if (selectedSongs.value.length === 1) {

                console.log(selectedSongs.value[0]);

                if (omdPlaylistName === '我喜欢的音乐') {
                    // 添加到我喜欢的音乐
                    await insertLikeMusicInfoService(selectedSongs.value[0]);
                    ElMessage.success('歌曲已添加到我喜欢的音乐');
                }else{

                    // 单曲添加
                    await insertMusicToPlaylistService(
                        omdPlaylistName,
                        selectedSongs.value[0]
                    );
                    ElMessage.success('歌曲已添加到歌单');
                }

            } else {
                // 批量添加
                const requestData = {
                    omdPlaylistName: omdPlaylistName,
                    omdMusicInfoIdList: selectedSongs.value,
                };
                await insertMusicListToPlaylistService(requestData);
                ElMessage.success(`成功添加 ${selectedSongs.value.length} 首歌曲到歌单`);
            }

            await fetchUserPlaylistsData(false);
            showPlaylistAddToDialog.value = false;
            selectedSongs.value = [];
            batchMode.value = false;
        } catch (error) {
            console.error('添加歌曲失败:', error);
            ElMessage.warning(error.message);
        }

    };

    // 整合删除歌曲到歌单的方法
    const deleteMusicFromPlaylistHandler = async (omdPlaylistName,songIds = []) => {
        if (!omdPlaylistName) {
            ElMessage.warning('请选择要操作的歌单');
            return;
        }

        // 获取要删除的歌曲ID列表
        selectedSongs.value = Array.isArray(songIds) ? songIds : [songIds];

        if (selectedSongs.value.length === 0) {
            ElMessage.warning('请选择要移除的歌曲');
            return;
        }

        try {
            // 确认对话框
            const confirmMessage =
                selectedSongs.value.length === 1
                    ? '确定要从歌单中移除这首歌曲吗？'
                    : `确定要从歌单中移除这 ${selectedSongs.value.length} 首歌曲吗？`;

            await ElMessageBox.confirm(confirmMessage, '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            });

            if (selectedSongs.value.length === 1) {

                if (omdPlaylistName === '我喜欢的音乐') {
                    // 从我喜欢的音乐中移除
                    await deleteLikeMusicInfoService(selectedSongs.value[0]);
                    ElMessage.success('歌曲已从我喜欢的音乐中移除');
                }else {
                    // 单个删除
                    await deleteMusicFromPlaylistService(omdPlaylistName, selectedSongs.value[0]);
                    ElMessage.success('歌曲已从歌单移除');
                }
            } else {
                // 批量删除
                const requestData = {
                    omdPlaylistName,
                    omdMusicInfoIdList: selectedSongs.value
                };
                await deleteMusicListFromPlaylistService(requestData);
                ElMessage.success(`成功移除 ${selectedSongs.value.length} 首歌曲`);
            }

            // 操作成功后重置状态
            await fetchUserPlaylistsData(false);
            selectedSongs.value = [];
            batchMode.value = false;

            return true;
        } catch (error) {
            if (error !== 'cancel') {
                console.error('移除歌曲失败:', error);
                ElMessage.error('移除歌曲失败: ' + (error.response?.data?.message || error.message));
            }
            return false;
        }
    };

    // 通用下载函数（核心逻辑）
    const downloadSong = (music) => {
        // 校验歌曲信息
        if (!music) {
            ElMessage.warning('未找到歌曲信息');
            return false;
        }
        if (!music.omdMusicInfoSongUrl) {
            ElMessage.warning(`《${music.omdMusicInfoName || '未知歌曲'}》无有效下载地址`);
            return false;
        }

        // 执行下载
        try {
            const link = document.createElement('a');
            link.href = music.omdMusicInfoSongUrl;
            link.download = `${music.omdMusicInfoName || '未知歌曲'}.mp3`;
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            return true;
        } catch (error) {
            console.error(`下载《${music.omdMusicInfoName}》失败:`, error);
            ElMessage.error(`《${music.omdMusicInfoName || '未知歌曲'}》下载失败`);
            return false;
        }
    };

    // 批量下载或单曲下载
    const handleBatchDownload = async (song,playlistMusic) => {

        // 处理下载逻辑
        if (selectedSongs.value.length === 0) {
            // 单首歌曲下载
            ElMessage.success(`开始单曲下载：共1首`);
            downloadSong(song);
        }else {
            if (selectedSongs.value.length === 0) {
                ElMessage.error('未找到选中的歌曲信息');
                return;
            }
            // 从 playlistList 中筛选选中的歌曲对象
            const selectedMusicList = playlistMusic.map(item => item.omdMusicInfo)
                .filter(item => selectedSongs.value.includes(item.omdMusicInfoId));
            console.log(playlistMusic)
            console.log(selectedMusicList);
            // 批量下载（带延迟避免浏览器拦截）
            let successCount = 0;
            ElMessage.success(`开始批量下载：共${selectedSongs.value.length}首`);
            selectedMusicList.forEach((music, index) => {
                setTimeout(() => {
                    if (downloadSong(music)) {
                        successCount++;
                    }
                    // 最后一首下载完成后提示
                    if (index === selectedMusicList.length - 1) {
                        setTimeout(() => {
                        }, 1000);
                    }
                    ElMessage.success(`批量下载完成：共${selectedMusicList.length}首，成功${successCount}首`);
                }, index * 500); // 每首间隔500ms
            });
        }
    };

    // 打开创建歌单弹窗
    const openCreateDialog = () => {
        showCreatePlaylistDialog.value = true;
        // 重置表单
        formData.value = {
            omdPlaylistName: '',
            omdPlaylistDescription: '',
            omdPlaylistCover: '',
            omdPlaylistPublic: 0
        };
    };

    // 打开编辑歌单弹窗
    const openEditDialog = (playlist) => {
        showEditPlaylistDialog.value = true;
        // 重置表单
        formData.value = {
            omdPlaylistName: playlist.omdPlaylistName,
            omdPlaylistDescription: playlist.omdPlaylistDescription,
            omdPlaylistCover: playlist.omdPlaylistCover,
            omdPlaylistPublic: playlist.omdPlaylistPublic
        };
        console.log(playlist);
        console.log(formData.value);
    }

    // 提交表单
    const submitForm = async (isEditMode,omdPlaylistId) => {
        try {
            formLoading.value = true;

            if (isEditMode === false) {
                // 新建歌单

                // 添加创建时间（服务端会自动设置，这里可选）
                const playlistData = {
                    ...formData.value
                };

                await insertPlaylistService(playlistData);
                ElMessage.success('歌单创建成功');
                showCreatePlaylistDialog.value = false;
            }else {
                // 编辑歌单
                const playlistData = {
                    ...formData.value,
                    omdPlaylistId: omdPlaylistId
                };

                await updatePlaylistService(playlistData);
                ElMessage.success('歌单修改成功');
                showEditPlaylistDialog.value = false;
            }

            // 创建成功后刷新歌单列表
            await fetchUserPlaylistsData(false);

        } catch (error) {
            console.error('创建歌单失败:', error);
            ElMessage.error('创建歌单失败: ' + (error.response?.data?.message || error.message));
        } finally {
            formLoading.value = false;
            formData.value = {
                omdPlaylistName: '',
                omdPlaylistDescription: '',
                omdPlaylistCover: '',
                omdPlaylistPublic: 0
            }
        }
    };

    // 删除播放列表
    const deletePlaylist = (omdPlaylistName, callback) => {
        ElMessageBox.confirm('你确定要删除这个播放列表吗？', '提示')
            .then(async () => {
                try {
                    await deletePlaylistService(omdPlaylistName);
                    await fetchUserPlaylistsData(false);
                    ElMessage.success('播放列表删除成功');

                    // 执行回调函数
                    if (callback && typeof callback === 'function') {
                        callback();
                    }
                } catch (error) {
                    console.error('删除播放列表失败:', error);
                }
            })
            .catch(() => {
                // catch error
            })
    };

    return {
        // 状态
        playlistList,
        loadingPlaylistList,
        showPlaylistAddToDialog,
        hasMore,
        showCreatePlaylistDialog,
        showEditPlaylistDialog,
        batchMode,
        selectedSongs,

        // 表单
        formData,
        formRules,
        formLoading,

        // 方法
        openAddToPlaylistDialog,
        addSongToPlaylistHandler,
        loadMorePlaylists,
        openCreateDialog,
        openEditDialog,
        submitForm,
        deletePlaylist,
        toggleBatchMode,
        toggleSongSelection,
        toggleSelectAll,
        deleteMusicFromPlaylistHandler,
        downloadSong,
        handleBatchDownload
    };
}