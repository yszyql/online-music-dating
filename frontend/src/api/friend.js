// 导入request.js
import request from '@/utils/request.js'

// 获取好友状态
export const getFriendStatusService = (omdFriendUserId) => {
    const params = new URLSearchParams();
    params.append('omdFriendUserId', omdFriendUserId);
    return request.get('/friend/getFriendStatus', { params });
}

// 添加好友
export const insertFriendService = (omdFriendUserId) => {
    const params = new URLSearchParams();
    params.append('omdFriendUserId', omdFriendUserId);
    return request.post('/friend/insertFriend', params);
}

// 获取好友列表
export const getFriendListByOmdUserIdService = (pageNum,pageSize,omdFriendStatus) => {
    // 构建请求参数对象
    const params = {
        pageNum : pageNum,
        pageSize : pageSize,
        omdFriendStatus : omdFriendStatus
    };
    return request.get('/friend/getFriendListByOmdUserId', {
        params
    });
}

// 获取用户与好友的聊天信息
export const getChatMessagesService = (pageNum,pageSize,omdFriendUserId) => {
    // 构建请求参数对象
    const params = {
        pageNum : pageNum,
        pageSize : pageSize
    };
    return request.get(`/friend/messages/${omdFriendUserId}`, { params });
}

// 获取好友申请列表
export const getFriendApplicationListByOmdUserIdService = (pageNum,pageSize) => {
    // 构建请求参数对象
    const params = {
        pageNum : pageNum,
        pageSize : pageSize
    };
    return request.get('/friend/getFriendApplicationListByOmdUserId', {
        params
    });
}

// 同意好友申请
export const agreeAddFriendService = (omdFriendUserId) => {
    const params = new URLSearchParams();
    params.append('omdFriendUserId', omdFriendUserId);
    return request.post('/friend/agreeAddFriend', params);
}

// 拒绝好友申请
export const refuseAddFriendService = (omdFriendUserId) => {
    const params = new URLSearchParams();
    params.append('omdFriendUserId', omdFriendUserId);
    return request.post('/friend/refuseAddFriend', params);
}

// 重新添加好友
export const anewAddFriendService = (omdFriendUserId) => {
    const params = new URLSearchParams();
    params.append('omdFriendUserId', omdFriendUserId);
    return request.post('/friend/anewAddFriend', params);
}

// 获取好友是否在线
export const getFriendIsOnlineService = (omdFriendUserId) => {
    const params = new URLSearchParams();
    params.append('omdFriendUserId', omdFriendUserId);
    return request.get('/friend/getFriendIsOnline', { params });
}

// 拉黑好友
export const blackFriendService = (omdFriendUserId) => {
    const params = new URLSearchParams();
    params.append('omdFriendUserId', omdFriendUserId);
    return request.post('/friend/insertBlackUser', params);
}

// 取消拉黑
export const deleteBlackUserService = (omdFriendUserId) => {
    const params = new URLSearchParams();
    params.append('omdFriendUserId', omdFriendUserId);
    return request.post('/friend/deleteBlackUser', params);
}

// 删除好友
export const deleteFriendService = (omdFriendUserId) => {
    const params = new URLSearchParams();
    params.append('omdFriendUserId', omdFriendUserId);
    return request.post('/friend/deleteFriend', params);
}