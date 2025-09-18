package org.x.backend.service;

import org.x.backend.pojo.OmdFriend;
import org.x.backend.pojo.OmdMessage;

import java.util.List;

public interface OmdFriendService {

    /**
     * 根据用户ID和朋友ID查询好友关系
     * @param omdUserId 用户ID
     * @param omdFriendUserId 朋友ID
     * @return 好友关系
     */
    OmdFriend findFriendByOmdUserId(Long omdUserId, Long omdFriendUserId);

    /**
     * 添加好友
     * @param omdUserId 用户ID
     * @param omdFriendUserId 朋友ID
     * @param friendStatus 好友状态
     * @return 是否添加成功
     */
    boolean insertFriend(Long omdUserId, Long omdFriendUserId, Integer friendStatus);

    /**
     * 根据用户ID查询好友列表
     * @param omdUserId 用户ID
     * @param omdFriendStatus 好友状态 0:未通过 1:通过 2:删除
     * @return 好友列表
     */
    List<OmdFriend> getFriendListByOmdUserId(Long omdUserId ,Integer omdFriendStatus);

    /**
     * 根据用户ID查询好友申请列表
     * @param omdUserId 当前用户ID
     * @return 好友申请列表
     */
    List<OmdFriend> getFriendApplicationListByOmdUserId(Long omdUserId);

    /**
     * 更新好友关系
     * @param omdFriendUserId 朋友ID
     * @param omdUserId 当前用户ID
     * @param omdFriendStatus 好友状态 0:未通过 1:通过 2:删除
     * @return 是否更新成功
     */
    boolean updateFriend(Long omdUserId,Long omdFriendUserId,Integer omdFriendStatus);

    /**
     * 删除好友关系
     * @param omdFriendUserId 好友ID
     * @param omdUserId 当前用户ID
     * @return 是否删除成功
     */
    boolean deleteFriend(Long omdFriendUserId ,Long omdUserId);

    /**
     * 发送消息
     * @param omdMessage 消息对象
     * @return 是否发送成功
     */
    boolean sendMessage(OmdMessage omdMessage);

    /**
     * 根据用户ID查询未读消息数量
     * @param omdUserId 当前用户ID
     * @return 未读消息数量
     */
    Long getAllMessageUnreadCount(Long omdUserId);

    /**
     * 根据用户ID和朋友ID查询聊天记录
     * @param omdUserId 用户ID
     * @param omdFriendUserId 朋友ID
     * @return 聊天记录列表
     */
    List<OmdMessage> selectChatRecordsByOmdUserIdAndOmdFriendUserId(Long omdUserId, Long omdFriendUserId);


    /**
     * 根据用户ID和朋友ID查询未读消息数量
     * @param omdUserId 当前用户ID
     * @param omdFriendUserId 朋友ID
     * @return 未读消息数量
     */
    Long getMessageUnreadCountByOmdFriendId(Long omdUserId, Long omdFriendUserId);

    /**
     * 获取用户与好友的最后一条最新的信息
     * @param omdUserId 当前用户ID
     * @param omdFriendUserId 朋友ID
     */
    OmdMessage getLastMessageByFriend(Long omdUserId, Long omdFriendUserId);

    /**
     * 更新消息为已读
     * @param omdUserId 当前用户ID
     * @param omdFriendUserId 朋友ID
     * */
    void updateMessageReadStatus(Long omdUserId, Long omdFriendUserId);

    /**
     * 插入黑名单
     * @param omdFriendUserId 朋友ID
     * @param omdUserId 当前用户ID
     * @return 是否添加成功
     */
    boolean insertBlackUser(Long omdUserId,Long omdFriendUserId);

    /**
     * 查询是否在黑名单中
     * @param omdFriendUserId 朋友ID
     * @param omdUserId 当前用户ID
     * @return 是否在黑名单中
     */
    boolean findBlackUser(Long omdFriendUserId, Long omdUserId);
}
