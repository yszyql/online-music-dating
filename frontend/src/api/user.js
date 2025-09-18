// 导入request.js
import request from '@/utils/request.js'

// 退出登录
export const logoutService = ()=>{
    return request.post('/user/logout');
}

// 普通重置密码
export const updatePasswordService = (updateData)=>{
    // 借助于urlSearchParams对象，将对象转换为url参数
    const params = new URLSearchParams();
    for(let key in updateData) {
        params.append(key,updateData[key])
    }
    return request.post('/user/updatePassword',params);
}

//获取个人信息
export const getUserInfoService = ()=>{
    return request.get('/user/getUserInfo');
}

// 编辑个人信息
export const userInfoUpdateService = (userInfo)=>{
    return request.post('/user/updateUserInfo',userInfo);
}

// 获取用户创建的所有歌单
export const getPlaylistListByUserIdService = (pageNum,pageSize)=>{
    // 构建请求参数对象
    const params = {
        pageNum : pageNum,
        pageSize : pageSize
    };
    return request.get('/user/getPlaylistListByOmdUserId',{
        params
    })
}

// 获取评论区列表
export const getCommentListWithDynamicSortService = (pageNum,pageSize,omdMusicInfoId)=>{
    // 构建请求参数对象
    const params = {
        pageNum : pageNum,
        pageSize : pageSize,
        omdMusicInfoId : omdMusicInfoId,
    };
    return request.get('/user/getCommentListWithDynamicSort',{
        params
    })
}

// 发表或回复评论
export const commentMusicService = (newCommentData)=>{
    return request.post('/user/commentMusic',newCommentData);
}

// 删除评论
export const deleteMusicCommentService = (omdMusicCommentId)=>{
    // 借助于urlSearchParams对象，将对象转换为url参数
    const params = new URLSearchParams();
    params.append('omdMusicCommentId', omdMusicCommentId);
    return request.post('/user/deleteMusicComment', params)
}

// 点赞或不点赞评论
export const likeMusicCommentOrNotService = (omdMusicCommentId) => {
    const params = new URLSearchParams();
    params.append('omdMusicCommentId', omdMusicCommentId);

    return request.post('/user/likeMusicCommentOrNot', params);
};

// 获取用户本人对评论的情况
export const getMusicCommentListStatusByUserIdService = (omdMusicCommentIdList)=>{
    return request.get('/user/getMusicCommentListStatusByUserId',{
        params: {
            omdMusicCommentIdList: omdMusicCommentIdList.join(',')
        },
        headers: {
            'Cache-Control': 'no-cache, no-store, must-revalidate'
        }
    });
}

// 根据用户ID查询歌手信息
export const getOmdSingerInfoByOmdUserIdService = (omdUserId) => {
    const params = {
        omdUserId: omdUserId
    }
    return request.get('/user/getOmdSingerInfoByOmdUserId',{
        params: params
    });
}

// 根据歌手ID查询歌曲信息
export const getMusicInfoBySingerIdService = (pageNum,pageSize,omdSingerId) => {
    const params = {
        pageNum : pageNum,
        pageSize : pageSize,
        omdSingerId: omdSingerId
    };
    return request.get('/user/getMusicInfoBySingerId',{
        params: params
    })
}


// 举报评论
export const reportMusicCommentService = (omdCommentReport)=>{
    return request.post('/user/reportMusicComment', omdCommentReport)
}

// 反馈信息
export const feedbackToAdminService = (omdUserFeedback)=>{
    return request.post('/user/feedbackToAdmin',omdUserFeedback);
}

// 获取用户是否完善了个人信息
export const getIsUserInfoCompleteService = ()=>{
    return request.get('/user/getIsUserInfoComplete');
}

// 获取用户申请歌手的信息
export const findApplicationByOmdUserIdService = ()=>{
    return request.get('/user/findApplicationByOmdUserId')
}

// 申请成为歌手
export const applicationSingerService = (omdApplications)=>{
    console.log(omdApplications);
    return request.post('/user/applicationSinger',omdApplications);
}

// 获取所有用户创建的公共歌单，随机获取二十条歌单
export const getPlaylistPublicListService = (pageNum,pageSize)=>{
    // 构建请求参数对象
    const params = {
        pageNum : pageNum,
        pageSize : pageSize
    };
    return request.get('/user/getPlaylistPublicList', {
        params
    });
}

// 获取用户身份
export const getOmdUserRoleListService = (omdUserId)=>{
    return request.get('/user/getOmdUserRoleList',{
        params: {
            omdUserId: omdUserId
        }
    })
}

// 获取其他用户的基本信息
export const getUserInfoByOmdUserIdService = (omdUserId)=>{
    return request.get('/user/getUserInfoByOmdUserId',{
        params: {
            omdUserId: omdUserId
        }
    })
}

// 获取选择用户的公开歌单
export const getOmdUserPlaylistPublicService = (pageNum,pageSize,omdUserId)=>{
    // 构建请求参数对象
    const params = {
        pageNum : pageNum,
        pageSize : pageSize,
        omdUserId: omdUserId
    };
    return request.get('/user/getOmdUserPlaylistPublic',{
        params
    })
}

// 获取用户的举报列表
export const getOmdCommentReportListService = (pageNum,pageSize,omdCommentReportStatus)=>{
    // 构建请求参数对象
    const params = {
        pageNum : pageNum,
        pageSize : pageSize,
        omdCommentReportStatus: omdCommentReportStatus
    };
    return request.get('/user/getOmdCommentReportList',{
        params
    })
}

// 获取用户的反馈列表
export const getUserFeedbackListService = (pageNum,pageSize,omdUserFeedbackStatus)=>{
    // 构建请求参数对象
    const params = {
        pageNum : pageNum,
        pageSize : pageSize,
        omdUserFeedbackStatus: omdUserFeedbackStatus
    };
    return request.get('/user/getOmdUserFeedbackList',{
        params
    })
}

// 检查用户是否举报过该用户
export const getUserReportStatusService = (omdUserId)=>{
    return request.get('/user/getUserReportStatus',{
        params: {
            omdUserId: omdUserId
        }
    })
}

// 举报用户
export const insertUserReportService = (omdUserReport)=>{
    return request.post('/user/insertUserReport',omdUserReport);
}

// 获取用户举报用户的列表
export const getUserReportListService = (pageNum,pageSize,omdUserReportStatus)=>{
    // 构建请求参数对象
    const params = {
        pageNum : pageNum,
        pageSize : pageSize,
        omdUserReportStatus: omdUserReportStatus
    };
    return request.get('/user/getOmdUserReportList',{
        params
    })
}

// 检查用户是否举报过音乐
export const getMusicReportStatusService = (omdMusicInfoId)=>{
    return request.get('/user/getMusicReportStatus',{
        params: {
            omdMusicInfoId: omdMusicInfoId
        }
    })
}

// 举报音乐
export const insertMusicReportService = (omdMusicReport)=>{
    return request.post('/user/insertMusicReport',omdMusicReport);
}

// 获取用户举报音乐的列表
export const getOmdMusicReportListService = (pageNum,pageSize,omdMusicReportStatus)=>{
    const params = {
        pageNum : pageNum,
        pageSize : pageSize,
        omdMusicReportStatus: omdMusicReportStatus
    };
    return request.get('/user/getOmdMusicReportList',{
        params})
}
