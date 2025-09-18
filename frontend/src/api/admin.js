// 导入request.js
import request from '@/utils/request.js'

// 获取管理员信息
export const getAdminInfoService = () => {
    return request.get('/admin/getAdminInfo')
}

// 编辑管理员信息
export const updateAdminInfoService = (omdAdmin) => {
    return request.post('/admin/updateAdminInfo', omdAdmin)
}

// 获取所有用户
export const getAllUserService = (pageNum,pageSize) => {
    // 构建请求参数对象
    const params = {
        pageNum : pageNum,
        pageSize : pageSize
    };
    return request.get('/admin/getAllUsers', {
        params
    });
}

// 根据用户名搜索用户
export const getUserByUsernameService = (omdUserName,pageNum,pageSize) => {
    // 构建请求参数对象
    const params = {
        omdUserName : omdUserName,
        pageNum : pageNum,
        pageSize : pageSize
    };
    return request.get('/admin/getUserByUsername', {
        params
    })
}

// 更新用户状态
export const updateUserStatusService = (omdUserId,omdUserRemark,omdUserStatus,omdUserFreezeType,omdUserFreezeEndTime) => {
    const params = new URLSearchParams();
    params.append('omdUserId', omdUserId);
    params.append('omdUserRemark', omdUserRemark);
    params.append('omdUserStatus', omdUserStatus);
    params.append('omdUserFreezeType', omdUserFreezeType);
    // 只在freezeEndTime有值时添加该参数
    if (omdUserFreezeEndTime !== null && omdUserFreezeEndTime !== undefined && omdUserFreezeEndTime !== "null") {
        params.append('omdUserFreezeEndTime', omdUserFreezeEndTime);
    }
    return request.post('/admin/updateUserStatus', null, { params });
}

// 获取所有音乐
export const getAllMusicInfoListService = (pageNum,pageSize) => {
    // 构建请求参数对象
    const params = {
        pageNum : pageNum,
        pageSize : pageSize
    };
    return request.get('/admin/getAllMusicInfoList', {
        params
    });
}

// 获取待审核的上传音乐
export const getMusicInfoListByStatusService = (pageNum,pageSize) => {
    // 构建请求参数对象
    const params = {
        pageNum : pageNum,
        pageSize : pageSize
    };
    return request.get('/admin/getMusicInfoListByStatus', {
        params
    });
}

// 审核音乐
export const updateMusicInfoStatusService = (omdMusicInfoId,omdMusicInfoStatus,omdMusicInfoRemark) => {
    const params = new URLSearchParams();
    params.append('omdMusicInfoId', omdMusicInfoId);
    params.append('omdMusicInfoStatus', omdMusicInfoStatus);
    params.append('omdMusicInfoRemark', omdMusicInfoRemark);
    return request.post('/admin/updateMusicInfoStatus',  params)
}

// 获取待审核的歌手申请
export const getApplicationsListByStatusService = (pageNum,pageSize) => {
    // 构建请求参数对象
    const params = {
        pageNum : pageNum,
        pageSize : pageSize
    };
    return request.get('/admin/getApplicationsListByStatus', {
        params
    });
}

// 审核歌手
export const updateApplicationsStatusService = (omdApplicationsId,omdApplicationsStatus,omdApplicationsReviewRemark) => {
    const params = new URLSearchParams();
    params.append('omdApplicationsId', omdApplicationsId);
    params.append('omdApplicationsStatus', omdApplicationsStatus);
    params.append('omdApplicationsReviewRemark', omdApplicationsReviewRemark);
    return request.post('/admin/updateApplicationsStatus',  params)
}

// 获取待审核的举报评论
export const getCommentReportListByStatusService = (pageNum,pageSize) => {
    // 构建请求参数对象
    const params = {
        pageNum : pageNum,
        pageSize : pageSize
    };
    return request.get('/admin/getCommentReportListByStatus', {
        params
    })
}

// 审核举报评论
export const updateCommentReportStatusService = (omdMusicCommentId,omdCommentReportStatus,omdCommentReportRemark) => {
    const params = new URLSearchParams();
    params.append('omdMusicCommentId', omdMusicCommentId);
    params.append('omdCommentReportStatus', omdCommentReportStatus);
    params.append('omdCommentReportRemark', omdCommentReportRemark);
    return request.post('/admin/updateCommentReportStatus',  params)
}

// 获取用户反馈列表
export const getFeedbackListService = (pageNum,pageSize) => {
    // 构建请求参数对象
    const params = {
        pageNum : pageNum,
        pageSize : pageSize
    };
    return request.get('/admin/getFeedbackList', {
        params
    })
}

// 审核用户反馈
export const updateFeedbackStatusService = (omdUserFeedbackId,omdUserFeedbackResponse) => {
    const params = new URLSearchParams();
    params.append('omdUserFeedbackId', omdUserFeedbackId);
    params.append('omdUserFeedbackResponse', omdUserFeedbackResponse);
    return request.post('/admin/updateFeedbackStatus',  params)
}

// 获取所有管理员信息
export const getAllAdminListService = (pageNum,pageSize) => {
    // 构建请求参数对象
    const params = {
        pageNum : pageNum,
        pageSize : pageSize
    };
    return request.get('/admin/getAllAdminList', {
        params
    })
}

// 新增管理员
export const insertAdminService = (omdAdmin) => {
    return request.post('/admin/insertAdmin',  omdAdmin)
}

// 冻结管理员
export const updateAdminStatusService = (omdAdminId,omdAdminStatus) => {
    const params = new URLSearchParams();
    params.append('omdAdminId', omdAdminId);
    params.append('omdAdminStatus', omdAdminStatus);
    return request.post('/admin/updateAdminStatus',  params)
}

// 获取用户举报用户列表
export const getUserReportListService = (pageNum,pageSize) => {
    // 构建请求参数对象
    const params = {
        pageNum : pageNum,
        pageSize : pageSize
    };
    return request.get('/admin/getUserReportList', {
        params
    })
}

// 审核用户举报用户
export const updateUserReportStatusService = (omdUserReportId,omdUserReportStatus,omdUserReportHandleRemark) => {
    const params = new URLSearchParams();
    params.append('omdUserReportId', omdUserReportId);
    params.append('omdUserReportStatus', omdUserReportStatus);
    params.append('omdUserReportHandleRemark', omdUserReportHandleRemark);
    return request.post('/admin/updateUserReportStatus',  params)
}

// 获取音乐举报列表
export const getMusicReportListService = (pageNum,pageSize) => {
    // 构建请求参数对象
    const params = {
        pageNum : pageNum,
        pageSize : pageSize
    };
    return request.get('/admin/getMusicReportList', {
        params
    })
}

// 审核音乐举报
export const updateMusicReportStatusService = (omdMusicReportId,omdMusicReportStatus,omdMusicReportHandleRemark) => {
    const params = new URLSearchParams();
    params.append('omdMusicReportId', omdMusicReportId);
    params.append('omdMusicReportStatus', omdMusicReportStatus);
    params.append('omdMusicReportHandleRemark', omdMusicReportHandleRemark);
    return request.post('/admin/updateMusicReportStatus',  params)
}

// 获取用户申诉列表
export const getUserAppealListService = (pageNum,pageSize) => {
    // 构建请求参数对象
    const params = {
        pageNum : pageNum,
        pageSize : pageSize
    };
    return request.get('/admin/getUserAppealList', {
        params
    })
}

// 审核用户申诉
export const updateUserAppealStatusService = (omdUserAppealId,omdUserAppealStatus,omdUserAppealResult) => {
    const params = new URLSearchParams();
    params.append('omdUserAppealId', omdUserAppealId);
    params.append('omdUserAppealStatus', omdUserAppealStatus);
    params.append('omdUserAppealResult', omdUserAppealResult);
    return request.post('/admin/updateUserAppealStatus',  params)
}

// 获取音乐申诉列表
export const getMusicAppealListService = (pageNum,pageSize) => {
    // 构建请求参数对象
    const params = {
        pageNum : pageNum,
        pageSize : pageSize
    };
    return request.get('/admin/getMusicAppealList', {
        params
    })
}

// 审核音乐申诉
export const updateMusicAppealStatusService = (omdMusicAppealId,omdMusicAppealStatus,omdMusicAppealHandleRemark) => {
    const params = new URLSearchParams();
    params.append('omdMusicAppealId', omdMusicAppealId);
    params.append('omdMusicAppealStatus', omdMusicAppealStatus);
    params.append('omdMusicAppealHandleRemark', omdMusicAppealHandleRemark);
    return request.post('/admin/updateMusicAppealStatus',  params)
}

// 获取操作日志
export const getOperationLogListService = (pageNum,pageSize,targetTypes) => {
    // 构建请求参数对象
    const params = {
        pageNum : pageNum,
        pageSize : pageSize,
        targetTypes : targetTypes
    };
    return request.get('/admin/getOperationLogList', {
        params
    })
}