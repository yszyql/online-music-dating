package org.x.backend.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.x.backend.mapper.OmdFriendMapper;
import org.x.backend.pojo.OmdFriend;
import org.x.backend.pojo.OmdMessage;
import org.x.backend.service.OmdFriendService;

import java.util.List;

@Service
public class OmdFriendServiceImpl implements OmdFriendService {

    @Autowired
    private OmdFriendMapper omdFriendMapper;

    /**
     * 根据用户ID和朋友ID查询好友关系
     * @param omdUserId 用户ID
     * @param omdFriendUserId 朋友ID
     * @return 好友关系
     */
    @Override
    public OmdFriend findFriendByOmdUserId(Long omdUserId, Long omdFriendUserId) {
        return omdFriendMapper.findFriendByOmdUserId(omdUserId, omdFriendUserId);
    }

    /**
     * 添加好友
     * @param omdUserId 用户ID
     * @param omdFriendUserId 朋友ID
     * @param friendStatus 好友状态
     * @return 是否添加成功
     */
    @Override
    public boolean insertFriend(Long omdUserId, Long omdFriendUserId, Integer friendStatus) {
        return omdFriendMapper.insertFriend(omdUserId, omdFriendUserId, friendStatus);
    }

    /**
     * 根据用户ID查询好友列表
     * @param omdUserId 用户ID
     * @param omdFriendStatus 好友状态 0:未通过 1:通过 2:删除
     * @return 好友列表
     */
    @Override
    public List<OmdFriend> getFriendListByOmdUserId(Long omdUserId, Integer omdFriendStatus) {
        return omdFriendMapper.getFriendListByOmdUserId(omdUserId,omdFriendStatus);
    }

    /**
     * 根据用户ID查询好友申请列表
     * @param omdUserId 当前用户ID
     * @return 好友申请列表
     */
    @Override
    public List<OmdFriend> getFriendApplicationListByOmdUserId(Long omdUserId) {
        return omdFriendMapper.getFriendApplicationListByOmdUserId(omdUserId);
    }

    /**
     * 更新好友关系
     * @param omdFriendUserId 朋友ID
     * @param omdUserId 当前用户ID
     * @param omdFriendStatus 好友状态 0:未通过 1:通过 2:删除
     * @return 是否更新成功
     */
    @Override
    public boolean updateFriend(Long omdUserId,Long omdFriendUserId,Integer omdFriendStatus) {
        return omdFriendMapper.updateFriend(omdUserId,omdFriendUserId,omdFriendStatus) > 0;
    }

    /**
     * 删除好友关系
     * @param omdFriendUserId 好友ID
     * @param omdUserId 当前用户ID
     * @return 是否删除成功
     */
    @Override
    public boolean deleteFriend(Long omdFriendUserId ,Long omdUserId) {
        return omdFriendMapper.deleteFriend(omdFriendUserId,omdUserId) > 0;
    }

    /**
     * 发送消息
     * @param omdMessage 消息对象
     * @return 是否发送成功
     */
    @Override
    public boolean sendMessage(OmdMessage omdMessage) {
        return omdFriendMapper.sendMessage(omdMessage) > 0;
    }

    /**
     * 根据用户ID查询未读消息数量
     * @param omdUserId 当前用户ID
     * @return 未读消息数量
     */
    @Override
    public Long getAllMessageUnreadCount(Long omdUserId) {
        return omdFriendMapper.getAllMessageUnreadCount(omdUserId);
    }

    /**
     * 根据用户ID和朋友ID查询聊天记录
     * @param omdUserId 用户ID
     * @param omdFriendUserId 朋友ID
     * @return 聊天记录列表
     */
    @Override
    public List<OmdMessage> selectChatRecordsByOmdUserIdAndOmdFriendUserId(Long omdUserId, Long omdFriendUserId) {
        return omdFriendMapper.selectChatRecordsByOmdUserIdAndOmdFriendUserId(omdUserId, omdFriendUserId);
    }

    /**
     * 根据用户ID和朋友ID查询未读消息数量
     * @param omdUserId 当前用户ID
     * @param omdFriendUserId 朋友ID
     * @return 未读消息数量
     */
    @Override
    public Long getMessageUnreadCountByOmdFriendId(Long omdUserId, Long omdFriendUserId) {
        return omdFriendMapper.getMessageUnreadCountByOmdFriendId(omdUserId, omdFriendUserId);
    }

    /**
     * 获取用户与好友的最后一条最新的信息
     * @param omdUserId 当前用户ID
     * @param omdFriendUserId 朋友ID
     */
    @Override
    public OmdMessage getLastMessageByFriend(Long omdUserId, Long omdFriendUserId) {
        return omdFriendMapper.getLastMessageByFriend(omdUserId, omdFriendUserId);
    }

    /**
     * 更新消息已读状态
     * @param omdUserId 用户ID
     * @param omdFriendUserId 朋友ID
     */
    @Override
    public void updateMessageReadStatus(Long omdUserId, Long omdFriendUserId) {
        omdFriendMapper.updateMessageReadStatus(omdUserId, omdFriendUserId);
    }

    /**
     * 插入黑名单
     * @param omdFriendUserId 朋友ID
     * @param omdUserId 当前用户ID
     * @return 是否添加成功
     */
    @Override
    public boolean insertBlackUser(Long omdFriendUserId, Long omdUserId) {
        return omdFriendMapper.insertBlackUser(omdFriendUserId, omdUserId) > 0;
    }

    /**
     * 查询是否在黑名单中
     * @param omdFriendUserId 朋友ID
     * @param omdUserId 当前用户ID
     * @return 是否在黑名单中
     */
    @Override
    public boolean findBlackUser(Long omdUserId,Long omdFriendUserId) {
        return omdFriendMapper.findBlackUser(omdUserId,omdFriendUserId) > 0;
    }
}
