package org.x.backend.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.x.backend.pojo.OmdFriend;
import org.x.backend.pojo.OmdMusicComment;
import org.x.backend.pojo.OmdUserReport;
import org.x.backend.pojo.Result;
import org.x.backend.service.OmdAdminService;
import org.x.backend.service.OmdFriendService;
import org.x.backend.service.OmdUserService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@Transactional(rollbackFor = Exception.class) // 统一管理事务
public class TransactionUserService {

    @Autowired
    private OmdUserService omdUserService;

    @Autowired
    private OmdAdminService omdAdminService;

    @Autowired
    private OmdFriendService omdFriendService;

    /**
     * 插入音乐评论
     * @param omdMusicComment 音乐评论对象
     */
    public void insertMusicComment(OmdMusicComment omdMusicComment) {
        // 1. 校验参数
        if (omdMusicComment == null || omdMusicComment.getOmdMusicInfoId() == null ||
                omdMusicComment.getOmdUserId() == null || omdMusicComment.getOmdMusicCommentContent() == null) {
            throw new RuntimeException("参数错误");
        }

        // 4. 如果是子评论，更新父评论回复数
        if (omdMusicComment.getOmdMusicCommentParentId() != null && omdMusicComment.getOmdMusicCommentParentId() > 0) {
            if (!omdUserService.updateReplyCountByParentId(omdMusicComment.getOmdMusicCommentParentId() , + 1)){
                throw new RuntimeException("更新父评论回复数失败");
            }

        }

        if (!omdUserService.insertMusicComment(omdMusicComment)) {
            throw new RuntimeException("插入音乐评论失败");
        }

    }

    /**
     * 是否点赞评论
     * @param omdMusicCommentId 音乐评论ID
     * @param omdUserId 当前用户ID
     */
    /**
     * 检查取消点赞的数据库操作是否执行
     */
    public void likeMusicCommentOrNot(Long omdMusicCommentId, Long omdUserId) {
        try {
            // 判断用户是否点赞过
            boolean hasLiked = omdUserService.hasLikedMusicComment(omdMusicCommentId, omdUserId);

            if (hasLiked) {
                // 如果已点赞，则取消点赞
                boolean updateResult = omdUserService.unlikeMusicComment(omdMusicCommentId);
                boolean deleteResult = omdUserService.deleteCommentLike(omdMusicCommentId, omdUserId);


                if (!updateResult || !deleteResult) {
                    throw new RuntimeException("取消点赞失败");
                }
            } else {
                // 如果未点赞，则执行点赞
                boolean updateResult = omdUserService.likeMusicComment(omdMusicCommentId);
                boolean insertResult = omdUserService.insertCommentLike(omdMusicCommentId, omdUserId);

                if (!updateResult || !insertResult) {
                    throw new RuntimeException("点赞失败");
                }
            }
        } catch (Exception e) {
            log.error("点赞/取消点赞操作异常", e);
            throw e;
        }
    }

    /**
     * 删除评论
     * @param omdMusicCommentId 评论ID
     * @param omdUserId 当前用户ID
     */
    public void deleteMusicComment(Long omdMusicCommentId, Long omdUserId) {
        // 1. 查询评论
        OmdMusicComment comment = omdUserService.findCommentByOmdMusicCommentId(omdMusicCommentId);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }

        // 2. 校验权限（当前用户是否为评论作者或管理员）
        if (!comment.getOmdUserId().equals(omdUserId) && omdAdminService.getAdminInfo(omdUserId) != null) {
            throw new RuntimeException("没有权限删除评论");
        }

        // 3. 检查是否为子评论
        if (comment.getOmdMusicCommentParentId() > 0) {
            // 如果是子评论，更新父评论回复数，同时删除该评论
            if (!omdUserService.deleteMusicComment(omdMusicCommentId, omdUserId)) {
                throw new RuntimeException("删除评论失败");
            }
            if (!omdUserService.updateReplyCountByParentId(comment.getOmdMusicCommentParentId(), -1)) {
                throw new RuntimeException("更新父评论回复数失败");
            }
        }else {

            // 3. 收集所有子评论ID（包括当前评论）
            List<Long> commentIds = new ArrayList<>();
            collectChildComments(omdMusicCommentId, commentIds);

            if(!commentIds.isEmpty()){
                // 4. 批量删除评论
                int result = omdUserService.deleteMusicCommentByIdList(commentIds);

                if (comment.getOmdMusicCommentParentId() > 0){

                    // 5. 更新各级父评论的回复数
                    if (result > 0) {
                        updateParentReplyCounts(commentIds);
                    }
                }

            }
        }


    }

    /**
     * 递归收集所有子评论ID
     */
    private void collectChildComments(Long omdMusicCommentParentId, List<Long> commentIds) {
        commentIds.add(omdMusicCommentParentId);
        List<Long> childIds = omdUserService.selectChildCommentIds(omdMusicCommentParentId);
        for (Long childId : childIds) {
            collectChildComments(childId, commentIds);
        }
    }

    /**
     * 逆序更新父评论回复数
     */
    private void updateParentReplyCounts(List<Long> commentIds) {
        // 按评论深度排序（子评论在前，父评论在后）
        Map<Long, OmdMusicComment> commentMap = new HashMap<>();
        for (Long id : commentIds) {
            OmdMusicComment comment = omdUserService.findCommentByOmdMusicCommentId(id);
            if (comment != null) {
                commentMap.put(id, comment);
            }
        }

        // 按父评论ID分组，确保先更新子评论的父评论
        commentIds.sort((a, b) -> {
            OmdMusicComment ca = commentMap.get(a);
            OmdMusicComment cb = commentMap.get(b);
            return Integer.compare(ca.getOmdMusicCommentParentId() == 0 ? 1 : 0,
                    cb.getOmdMusicCommentParentId() == 0 ? 1 : 0);
        });

        // 更新父评论回复数
        for (Long id : commentIds) {
            OmdMusicComment comment = commentMap.get(id);
            if (comment.getOmdMusicCommentParentId() > 0) {
                omdUserService.updateReplyCountByParentId(comment.getOmdMusicCommentParentId(), -1);
            }
        }
    }

    /**
     * 插入用户举报
     * @param omdUserReport 用户举报对象
     */
    public void insertUserReport(OmdUserReport omdUserReport, Long omdUserId) {
        // 1. 校验参数
        if (omdUserReport == null || omdUserReport.getOmdUserReportedUserId() == null ||
                omdUserReport.getOmdUserReportReason() == null) {
            throw new RuntimeException("参数错误");
        }
        // 2. 插入举报
        if (!omdUserService.insertUserReport(omdUserReport)) {
            throw new RuntimeException("插入用户举报失败");
        }
        // 3. 拉黑用户
        // 先校验是否为朋友，如果是朋友则修改状态为拉黑状态
        OmdFriend omdFriend = omdFriendService.findFriendByOmdUserId(omdUserId, omdUserReport.getOmdUserReportedUserId());
        if (omdFriend != null){
            if (omdFriend.getOmdFriendStatus() == 1 || omdFriend.getOmdFriendStatus() == 0){
                if (!omdFriendService.updateFriend(omdUserId, omdUserReport.getOmdUserReportedUserId(),3)){
                    throw new RuntimeException("更新用户状态失败");
                }
                throw new RuntimeException("用户已被拉黑");
            }
        }
        // 校验是否已经拉黑
        if (omdFriend != null && omdFriend.getOmdFriendStatus() == 3) {
            throw new RuntimeException("用户已被拉黑");
        }
        // 插入黑名单
        if (!omdFriendService.insertBlackUser(omdUserId, omdUserReport.getOmdUserReportedUserId())){
            throw new RuntimeException("插入黑名单失败");
        }
    }
}
