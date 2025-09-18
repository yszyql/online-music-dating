// 导入request.js
import request from '@/utils/request.js'


// 给音乐点赞
export const insertLikeMusicInfoService = (omdMusicInfoId)=>{
    const params = new URLSearchParams();
    params.append('omdMusicInfoId', omdMusicInfoId);
    return request.post('/music/insertLikeMusicInfo',params);
}

// 取消点赞API
export const deleteLikeMusicInfoService = (omdMusicInfoId) => {
    const params = new URLSearchParams();
    params.append('omdMusicInfoId', omdMusicInfoId);
    return request.post('/music/deleteLikeMusicInfo',params);
};

// 获取用户创建歌单里面的音乐列表
export const getMusicListFromPlaylistService = (omdPlaylistId) => {
    return request.get('/music/getMusicListFromPlaylist',{
        params: {
            omdPlaylistId: omdPlaylistId
        }
    });
}

// 新建歌单
export const insertPlaylistService = (omdPlaylistData) => {
    return request.post('/music/insertPlaylist',omdPlaylistData);
}

// 修改歌单
export const updatePlaylistService = (omdPlaylistData) => {
    return request.post('/music/updatePlaylist',omdPlaylistData);
}

// 删除歌单
export const deletePlaylistService = (omdPlaylistName) => {
    const params = new URLSearchParams();
    params.append('omdPlaylistName', omdPlaylistName);
    return request.post('/music/deletePlaylist',params);
}

// 添加音乐到歌单
export const insertMusicToPlaylistService = (omdPlaylistName, omdMusicInfoId) => {
    const params = new URLSearchParams();
    params.append('omdPlaylistName', omdPlaylistName);
    params.append('omdMusicInfoId', omdMusicInfoId);
    return request.post('/music/insertMusicToPlaylist',params);
}

// 批量添加音乐到歌单
export const insertMusicListToPlaylistService = (requestData) => {
    return request.post('/music/insertMusicListToPlaylist',requestData);
}

// 删除歌单中的音乐
export const deleteMusicFromPlaylistService = (omdPlaylistName, omdMusicInfoId) => {
    const params = new URLSearchParams();
    params.append('omdPlaylistName', omdPlaylistName);
    params.append('omdMusicInfoId', omdMusicInfoId);
    return request.post('/music/deleteMusicFromPlaylist',params);
}

// 批量删除歌单中的音乐
export const deleteMusicListFromPlaylistService = (requestData) => {
    return request.post('/music/deleteMusicListFromPlaylist',requestData);
}