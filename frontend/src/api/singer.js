// 导入request.js
import request from '@/utils/request.js'

// 获取歌手信息
export const getOmdSingerInfoService = () => {
    return request.get('/singer/getOmdSingerInfo');
}

// 更新歌手信息
export const updateOmdSingerService = (singerData) => {
    return request.post('/singer/updateOmdSinger', singerData);
}

// 上传歌曲
export const insertSongAndLyricService = (requestMap) => {
    return request.post('/singer/insertSongAndLyric', requestMap);
}

// 获取登录歌手的音乐列表
export const getMusicInfoListBySingerIdService = (pageNum,pageSize,omdMusicInfoStatus) => {
    const params = {
        pageNum : pageNum,
        pageSize : pageSize
    };
    // 仅当状态是有效的数字（0/1/2）时，才添加该参数
    if (omdMusicInfoStatus !== '' && omdMusicInfoStatus !== undefined && !isNaN(omdMusicInfoStatus)) {
        params.omdMusicInfoStatus = omdMusicInfoStatus;
    }
    return request.get('/singer/getMusicInfoListBySingerId', {
        params
    });
}

// 重新上传歌曲
export const updateMusicInfoStatusService = (omdMusicInfoId) => {
    const params = new URLSearchParams();
    params.append('omdMusicInfoId', omdMusicInfoId);
    return request.post('/singer/updateMusicInfoStatus', params);
}

// 删除歌曲
export const deleteMusicInfoService = (omdMusicInfoId) => {
    const params = new URLSearchParams();
    params.append('omdMusicInfoId', omdMusicInfoId);
    return request.post('/singer/deleteMusicInfo', params);
}

// 查询歌曲是否因被举报而下架
export const findMusicReportByIdService = (omdMusicInfoId) => {
    return request.get('/singer/findMusicReportById', {
        params: {
            omdMusicInfoId: omdMusicInfoId
        }
    });
}

// 获取被举报的歌曲列表
export const getMusicReportListBySingerIdService = (pageNum,pageSize) => {
    const params = {
        pageNum : pageNum,
        pageSize : pageSize
    };
    return request.get('/singer/getMusicReportListBySingerId', {
        params
    })
}

// 查询歌曲是否被申诉
export const getMusicAppealStatusByMusicService = (omdMusicInfoId) => {
    return request.get('/singer/getMusicAppealStatusByMusic', {
        params: {
            omdMusicInfoId: omdMusicInfoId
        }
    });
}

// 申诉歌曲
export const insertMusicAppealService = (omdMusicAppeal) => {
    return request.post('/singer/insertMusicAppeal',omdMusicAppeal)
}

// 获取登录歌手的音乐申诉列表
export const getMusicAppealListBySingerIdService = (pageNum,pageSize,omdMusicAppealStatus) => {
    const params = {
        pageNum : pageNum,
        pageSize : pageSize,
        omdMusicAppealStatus: omdMusicAppealStatus
    };
    return request.get('/singer/getMusicAppealListBySingerId', {
        params
    });
}

// 重新申诉
export const updateMusicAppealService = (omdMusicAppeal) => {
    // 借助于urlSearchParams对象，将对象转换为url参数
    const params = new URLSearchParams();
    for(let key in omdMusicAppeal) {
        params.append(key,omdMusicAppeal[key])
    }
    return request.post('/singer/updateMusicAppeal',params)
}