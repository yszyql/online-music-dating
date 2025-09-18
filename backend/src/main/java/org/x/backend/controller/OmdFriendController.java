package org.x.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.x.backend.pojo.*;
import org.x.backend.service.impl.TransactionFriendService;
import org.x.backend.service.OmdFriendService;
import org.x.backend.utils.HelperUtil;
import org.x.backend.utils.RedisUtil;

import java.util.List;

/**
 * 好友管理控制器
 */
@RestController
@RequestMapping("/friend")
@Validated
@Slf4j
public class OmdFriendController {

    // 好友服务层
    @Autowired
    private OmdFriendService omdFriendService;

    // 事务服务层
    @Autowired
    private TransactionFriendService transactionFriendService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // helper工具类
    @Autowired
    private HelperUtil helperUtil;

    // redis工具类
    @Autowired
    private RedisUtil redisUtil;

    /**
     * 获取好友状态
     * @param omdFriendUserId 要查询的好友ID
     * @return 好友关系
     */
    @GetMapping("/getFriendStatus")
    public Result<OmdFriend> getFriendStatus(@RequestParam("omdFriendUserId") Long omdFriendUserId) {
        OmdFriend omdFriend = omdFriendService.findFriendByOmdUserId(helperUtil.getCurrentUserId(), omdFriendUserId);
        return Result.success(omdFriend);
    }

    /**
     * 添加好友
     * @param omdFriendUserId 要添加的好友ID
     * @return 添加结果
     */
    @PostMapping("/insertFriend")
    public Result<String> insertFriend(@RequestParam("omdFriendUserId") Long omdFriendUserId) {

        // 校验朋友ID是否为当前用户ID
        if (helperUtil.getCurrentUserId().equals(omdFriendUserId)) {
            return Result.error("不能添加自己为好友");
        }

        // 校验朋友ID是否已经是好友或是否已经申请过添加好友
        OmdFriend omdFriend = omdFriendService.findFriendByOmdUserId(helperUtil.getCurrentUserId(), omdFriendUserId);
        if (omdFriend != null) {
            if (omdFriend.getOmdFriendStatus() == 1 || omdFriend.getOmdFriendStatus() == 0) {
                return Result.error("该用户已经是好友或已经申请过添加好友");
            }
        }

        // 校验是否被用户申请添加
        OmdFriend omdApplicationFriend = omdFriendService.findFriendByOmdUserId(omdFriendUserId, helperUtil.getCurrentUserId());
        if (omdApplicationFriend != null) {
            if (omdApplicationFriend.getOmdFriendStatus() == 1 || omdApplicationFriend.getOmdFriendStatus() == 0) {
                return Result.error("该用户已经是好友或已经申请过添加好友");
            }
        }

        // 添加好友
        if (!omdFriendService.insertFriend(helperUtil.getCurrentUserId(), omdFriendUserId, 0)){
            return Result.error("添加好友失败");
        }

        return Result.success("添加好友成功，请等待对方同意");
    }

    /**
     * 根据用户ID查询好友列表
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param omdFriendStatus 好友状态 0:未通过 1:通过 2:删除
     * @return 好友列表
     */
    @GetMapping("/getFriendListByOmdUserId")
    public Result<PageBean<OmdFriend>> getFriendListByOmdUserId(@RequestParam("pageNum") Integer pageNum,
                                                                @RequestParam("pageSize") Integer pageSize,
                                                                @RequestParam("omdFriendStatus") Integer omdFriendStatus) {
        // 执行分页查询
        PageBean<OmdFriend> FriendListByOmdUserId = helperUtil.executePageQuery(
                pageNum,
                pageSize,
                () -> omdFriendService.getFriendListByOmdUserId(helperUtil.getCurrentUserId(),omdFriendStatus)
        );

        // 对每个好友添加未读消息计数
        FriendListByOmdUserId.getItems().forEach(friend -> {
            Long unreadCount = omdFriendService.getMessageUnreadCountByOmdFriendId(
                    helperUtil.getCurrentUserId(),
                    friend.getOmdFriendId()
            );
            friend.setUnreadMessageCount(unreadCount != null ? unreadCount : 0);

            // 获取最后一条消息
            OmdMessage lastMessage = omdFriendService.getLastMessageByFriend(
                    helperUtil.getCurrentUserId(),
                    friend.getOmdFriendId()
            );
            friend.setLastMessage(lastMessage);
        });


        // 如果查询结果为空，返回空列表
        return Result.success(FriendListByOmdUserId);
    }

    /**
     * 根据用户ID查询好友申请列表
     * @return 申请列表
     */
    @GetMapping("/getFriendApplicationListByOmdUserId")
    public Result<PageBean<OmdFriend>> getFriendApplicationListByOmdUserId(@RequestParam("pageNum") Integer pageNum,
                                                                           @RequestParam("pageSize") Integer pageSize) {

        // 执行分页查询
        PageBean<OmdFriend> FriendListByOmdUserId = helperUtil.executePageQuery(
                pageNum,
                pageSize,
                () -> omdFriendService.getFriendApplicationListByOmdUserId(helperUtil.getCurrentUserId())
        );
        return Result.success(FriendListByOmdUserId);
    }

    /**
     * 同意添加好友
     * @param omdFriendUserId 要添加的好友ID
     * @return 添加结果
     */
    @PostMapping("/agreeAddFriend")
    public Result<String> agreeAddFriend(@RequestParam("omdFriendUserId") Long omdFriendUserId) {
        // 调用事务服务层的方法
        transactionFriendService.agreeAddFriend(helperUtil.getCurrentUserId(),omdFriendUserId);
        return Result.success("同意添加好友成功");
    }

    /**
     * 拒绝添加好友
     * @param omdFriendUserId 要添加的好友ID
     * @return 添加结果
     */
    @PostMapping("/refuseAddFriend")
    public Result<String> refuseAddFriend(@RequestParam("omdFriendUserId") Long omdFriendUserId) {
        // 修改好友状态
        if (!omdFriendService.updateFriend(helperUtil.getCurrentUserId(),omdFriendUserId,2)){
            return Result.error("拒绝添加好友失败");
        }
        return Result.success("拒绝添加好友成功");
    }

    /**
     * 重新添加好友
     * @param omdFriendUserId 要添加的好友ID
     * @return 添加结果
     */
    @PostMapping("/anewAddFriend")
    public Result<String> anewAddFriend(@RequestParam("omdFriendUserId") Long omdFriendUserId) {
        // 先查询是否被对方拉黑了
        OmdFriend omdFriend = omdFriendService.findFriendByOmdUserId(omdFriendUserId, helperUtil.getCurrentUserId());
        if (omdFriend != null && omdFriend.getOmdFriendStatus() == 3) {
            return Result.error("该用户已拉黑您，请不要重复添加");
        }
        // 修改好友状态
        if (!omdFriendService.updateFriend(omdFriendUserId, helperUtil.getCurrentUserId(), 0)){
            return Result.error("重新添加好友失败");
        }
        return Result.success("重新添加好友成功");
    }

    /**
     * 拉黑用户
     * @param omdFriendUserId 用户ID
     * @return 结果
     */
    @PostMapping("/insertBlackUser")
    public Result<String> insertBlackUser(@RequestParam("omdFriendUserId") Long omdFriendUserId) {
        transactionFriendService.insertBlackUser(helperUtil.getCurrentUserId(), omdFriendUserId);
        return Result.success("拉黑成功");
    }

    /**
     * 删除黑名单
     * @param omdFriendUserId 用户ID
     * @return 结果
     */
    @PostMapping("/deleteBlackUser")
    public Result<String> deleteBlackUser(@RequestParam("omdFriendUserId") Long omdFriendUserId){
        // 查询是否在黑名单中，在的话直接删除黑名单，不需要考虑是否为好友关系
        OmdFriend omdFriend = omdFriendService.findFriendByOmdUserId(helperUtil.getCurrentUserId(), omdFriendUserId);
        if (omdFriend != null && omdFriend.getOmdFriendStatus() == 3) {
            if (!omdFriendService.deleteFriend(helperUtil.getCurrentUserId(), omdFriendUserId)){
                return Result.error("删除黑名单失败");
            }
        }
        return Result.error("该用户不是黑名单");
    }

    /**
     * 删除好友
     * @param omdFriendUserId 要删除的好友ID
     * @return 删除结果
     */
    @PostMapping("/deleteFriend")
    public Result<String> deleteFriend(@RequestParam("omdFriendUserId") Long omdFriendUserId) {
        // 调用事务服务层的方法
        transactionFriendService.deleteFriend(helperUtil.getCurrentUserId(),omdFriendUserId);
        return Result.success("删除好友成功");
    }

    /**
     * 发送消息
     * @param omdMessage 要发送消息
     * @return 发送结果
     */
    @MessageMapping("/sendMessage")
    public Result<String> sendMessage(@Payload OmdMessage omdMessage) {
        log.info("接收到的消息对象: {}", omdMessage);

        // 打印接收到的原始JSON
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            log.info("接收到的原始JSON: {}", objectMapper.writeValueAsString(omdMessage));
        } catch (Exception e) {
            log.error("JSON转换异常", e);
        }
        // 校验朋友ID是否为当前用户好友
        OmdFriend omdFriend = omdFriendService.findFriendByOmdUserId(omdMessage.getOmdUserId(), omdMessage.getOmdFriendUserId());
        log.info("查询到的好友关系: {}", omdFriend);
        if (omdFriend == null || omdFriend.getOmdFriendStatus() == 2 || omdFriend.getOmdFriendStatus() == 0) {
            return Result.error("该用户不是您的好友或未通过好友申请");
        }
        // 校验消息内容是否为空
        if (StringUtils.isBlank(omdMessage.getOmdMessageContent())) {
            return Result.error("消息内容不能为空");
        }

        log.info("设置了用户ID的消息对象: {}", omdMessage);
        if (!omdFriendService.sendMessage(omdMessage)){
            return Result.error("发送消息失败");
        }

        // 发送消息
        messagingTemplate.convertAndSendToUser(String.valueOf(omdMessage.getOmdFriendUserId()), "/queue/friend", omdMessage);
        // 发送消息，消息回显给自己
        messagingTemplate.convertAndSendToUser(String.valueOf(omdMessage.getOmdUserId()), "/queue/friend", omdMessage);

        return Result.success("发送成功：" + omdMessage.getOmdFriendUserId() + " " + omdMessage);
    }

    /**
     * 获取消息
     * @param omdFriendUserId 消息接收者ID
     * @return 消息列表
     */
    @GetMapping("/messages/{omdFriendUserId}")
    public Result<List<OmdMessage>> getChatMessages(@PathVariable Long omdFriendUserId,
                                                    @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                                    @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize) {
        // 获取聊天消息（按时间倒序，新消息在前）
        PageHelper.startPage(pageNum, pageSize);
        List<OmdMessage> messages = omdFriendService.selectChatRecordsByOmdUserIdAndOmdFriendUserId(
                helperUtil.getCurrentUserId(),
                omdFriendUserId
        );

        // 将消息标记为已读
        omdFriendService.updateMessageReadStatus(
                helperUtil.getCurrentUserId(),
                omdFriendUserId
        );

        return Result.success(messages);
    }

    /**
     * 获取消息未读数量
     * @return 消息未读数量
     */
    @GetMapping("/getAllMessageUnreadCount")
    public Result<Long> getAllMessageUnreadCount() {
        Long omdMessageUnreadCount = omdFriendService.getAllMessageUnreadCount(helperUtil.getCurrentUserId());
        return Result.success(omdMessageUnreadCount);
    }

    /**
     * 获取好友是否在线
     * @param omdFriendUserId 要查询的好友ID
     * @return 好友是否在线
     */
    @GetMapping("/getFriendIsOnline")
    public Result<Boolean> getFriendIsOnline(@RequestParam("omdFriendUserId") Long omdFriendUserId) {
        return Result.success(redisUtil.isUserOnline(omdFriendUserId));
    }

}
