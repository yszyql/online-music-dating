package org.x.backend.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.x.backend.pojo.OmdFriend;
import org.x.backend.pojo.Result;
import org.x.backend.service.OmdFriendService;

@Service
@Transactional(rollbackFor = Exception.class)
public class TransactionFriendService {

    @Autowired
    private OmdFriendService omdFriendService;

    /**
     * 同意添加好友
     * @param omdUserId 当前用户ID
     * @param omdFriendUserId 好友ID
     */
    public void agreeAddFriend(Long omdUserId, Long omdFriendUserId) {
        // 修改好友状态
        if (!omdFriendService.updateFriend(omdUserId,omdFriendUserId,1) ||
                !omdFriendService.insertFriend(omdUserId ,omdFriendUserId, 1)){
            throw  new RuntimeException("同意好友申请失败");
        }
    }

    /**
     * 删除好友
     * @param omdUserId 当前用户ID
     * @param omdFriendUserId 好友ID
     */
    public void deleteFriend(Long omdUserId, Long omdFriendUserId) {
        // 删除好友
        if (!omdFriendService.deleteFriend(omdFriendUserId,omdUserId) ||
                !omdFriendService.deleteFriend(omdUserId,omdFriendUserId)){
            throw  new RuntimeException("删除好友失败");
        }
    }

    /**
     * 拉黑用户
     * @param omdUserId 当前用户ID
     * @param omdFriendUserId 好友ID
     */
    public void insertBlackUser(Long omdUserId, Long omdFriendUserId) {

        // 先校验是否已经拉黑
        OmdFriend omdFriend = omdFriendService.findFriendByOmdUserId(omdUserId, omdFriendUserId);
        if (omdFriend != null && omdFriend.getOmdFriendStatus() == 3) {
           throw new RuntimeException("用户已被拉黑");
        }

        // 查询双向好友关系
        OmdFriend omdApplicationFriend = omdFriendService.findFriendByOmdUserId(omdFriendUserId, omdUserId);

        // 处理双向好友关系（避免双方同时有拉黑状态）
        if (omdFriend != null && omdFriend.getOmdFriendStatus() == 1
                && omdApplicationFriend != null && omdApplicationFriend.getOmdFriendStatus() == 1) {
            // 先删除好友关系
            deleteFriend(omdUserId, omdFriendUserId);
            // 再拉黑用户
            if (!omdFriendService.insertBlackUser(omdUserId, omdFriendUserId)) {
                throw new RuntimeException("拉黑用户失败");
            }
        }

        // 处理单向好友关系（当前用户是发起方）
        else if (omdFriend != null && omdApplicationFriend == null) {
            omdFriendService.updateFriend(omdUserId, omdFriendUserId, 3);
        }

        // 处理单向好友关系（对方是发起方），修改状态为拒绝状态，并拉黑当前用户
        else if (omdFriend == null && omdApplicationFriend != null) {
            if (omdFriendService.updateFriend(omdFriendUserId, omdUserId, 2)){
                omdFriendService.insertBlackUser(omdUserId, omdFriendUserId);
            }
        }

        // 没有好友关系，直接插入黑名单
        else {
            omdFriendService.insertBlackUser(omdUserId, omdFriendUserId);
        }

    }
}
