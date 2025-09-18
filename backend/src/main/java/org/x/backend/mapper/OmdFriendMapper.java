package org.x.backend.mapper;

import org.apache.ibatis.annotations.*;
import org.springframework.web.bind.annotation.RequestParam;
import org.x.backend.pojo.OmdFriend;
import org.x.backend.pojo.OmdMessage;

import java.util.List;

@Mapper
public interface OmdFriendMapper {


    /**
     * 根据用户ID和朋友ID查询好友关系
     * @param omdUserId 用户ID
     * @param omdFriendUserId 朋友ID
     * @return 好友关系
     */
    @Select("select * from tb_omd_friend where omd_user_id = #{omdUserId} and omd_friend_user_id = #{omdFriendUserId}")
    OmdFriend findFriendByOmdUserId(Long omdUserId, Long omdFriendUserId );

    /**
     * 添加好友
     * @param omdUserId 用户ID
     * @param omdFriendUserId 朋友ID
     * @param friendStatus 好友状态
     * @return 是否添加成功
     */
    @Insert("insert into tb_omd_friend(omd_user_id, omd_friend_user_id, omd_friend_status) " +
            "values (#{omdUserId}, #{omdFriendUserId}, #{friendStatus})")
    boolean insertFriend(Long omdUserId, Long omdFriendUserId, Integer friendStatus);

    /**
     * 根据用户ID查询好友列表
     * @param omdUserId 用户ID
     * @param omdFriendStatus 好友状态 0:未通过 1:通过 2:删除
     * @return 好友列表
     */
    List<OmdFriend> getFriendListByOmdUserId(Long omdUserId, Integer omdFriendStatus);

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
    @Update("update tb_omd_friend set omd_friend_status = #{omdFriendStatus}, omd_friend_verify_time = now()" +
            " where omd_user_id = #{omdFriendUserId} and omd_friend_user_id = #{omdUserId}")
    int updateFriend(Long omdUserId,Long omdFriendUserId,Integer omdFriendStatus);

    /**
     * 删除好友关系
     * @param omdFriendUserId 好友ID
     * @param omdUserId 当前用户ID
     * @return 是否删除成功
     */
    @Delete("delete from tb_omd_friend where omd_user_id = #{omdUserId} and omd_friend_user_id = #{omdFriendUserId}")
    int deleteFriend(Long omdFriendUserId ,Long omdUserId);

    /**
     * 发送消息
     * @param omdMessage 消息对象
     * @return 是否发送成功
     */
    @Insert("insert into tb_omd_message(omd_user_id, omd_friend_user_id, omd_message_content) " +
            "values (#{omdUserId}, #{omdFriendUserId}, #{omdMessageContent})")
    @Options(useGeneratedKeys = true, keyProperty = "omdMessageId")
    int sendMessage(OmdMessage omdMessage);

    /**
     * 根据用户ID和朋友ID查询聊天记录
     * @param omdUserId 用户ID
     * @param omdFriendUserId 朋友ID
     * @return 聊天记录列表
     */
    @Select("select * from tb_omd_message where (omd_user_id = #{omdUserId} and omd_friend_user_id = #{omdFriendUserId}) " +
            "or (omd_user_id = #{omdFriendUserId} and omd_friend_user_id = #{omdUserId}) order by omd_message_send_time")
    List<OmdMessage> selectChatRecordsByOmdUserIdAndOmdFriendUserId(Long omdUserId, Long omdFriendUserId);

    /**
     * 根据用户ID查询未读消息数量
     * @param omdUserId 用户ID
     * @return 未读消息数量
     */
    @Select("select count(*) from tb_omd_message where omd_friend_user_id = #{omdUserId} and omd_message_read_status = 0")
    Long getAllMessageUnreadCount(Long omdUserId);

    /**
     * 根据用户ID和朋友ID查询未读消息数量
     * @param omdUserId 用户ID
     * @param omdFriendUserId 朋友ID
     * @return 未读消息数量
     */
    @Select("select count(*) from tb_omd_message where omd_user_id = #{omdUserId}" +
            " and omd_friend_user_id = #{omdFriendUserId} and omd_message_read_status = 0")
    Long getMessageUnreadCountByOmdFriendId(Long omdUserId, Long omdFriendUserId);

    /**
     * 更新消息为已读
     * @param omdUserId 用户ID
     * @param omdFriendUserId 朋友ID
     * @return 是否更新成功
     */
    @Update("update tb_omd_message set omd_message_read_status = 1 where omd_user_id = #{omdFriendUserId} " +
            "and omd_friend_user_id = #{omdUserId}")
    int updateMessageReadStatus(Long omdUserId, Long omdFriendUserId);

    /**
     * 获取用户与好友的最后一条最新的信息
     * @param omdUserId 当前用户ID
     * @param omdFriendUserId 朋友ID
     */
    @Select("select * from tb_omd_message where (omd_user_id = #{omdUserId} and omd_friend_user_id = #{omdFriendUserId}) " +
            "or (omd_user_id = #{omdFriendUserId} and omd_friend_user_id = #{omdUserId}) order by omd_message_send_time desc limit 1")
    OmdMessage getLastMessageByFriend(Long omdUserId, Long omdFriendUserId);

    /**
     * 插入黑名单
     * @param omdFriendUserId 朋友ID
     * @param omdUserId 当前用户ID
     * @return 是否添加成功
     */
    @Insert("insert into tb_omd_friend(omd_user_id, omd_friend_user_id,omd_friend_status) " +
            "values (#{omdUserId}, #{omdFriendUserId},3 )")
    int insertBlackUser(Long omdUserId, Long omdFriendUserId);

    /**
     * 查询是否在黑名单中
     * @param omdFriendUserId 朋友ID
     * @param omdUserId 当前用户ID
     * @return 是否在黑名单中
     */
    int findBlackUser(Long omdFriendUserId, Long omdUserId);
}
