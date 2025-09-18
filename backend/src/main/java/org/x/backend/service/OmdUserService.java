package org.x.backend.service;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import org.x.backend.pojo.*;

import java.util.List;
import java.util.Map;

public interface OmdUserService {

    /**
     * 根据用户ID查询用户信息
     * @param omdUserId 用户ID
     * @return 用户信息
     */
    OmdUser findByUserId(Long omdUserId);

    /**
     * 根据用户名查询用户信息
     * @param omdUserName 用户名
     * @return 用户信息
     */
    OmdUser findByUsername(@NotEmpty @Pattern(regexp = "^[\\u4e00-\\u9fa5a-zA-Z0-9_]{2,16}$",
            message = "用户名只能包含中文、英文、数字和下划线，长度2-16") String omdUserName);

    /**
     * 根据手机号查询用户信息
     * @param omdUserPhone 手机号
     * @return 用户信息
     */
    OmdUser findByPhone(@NotEmpty @Pattern(regexp = "^1[3-9]\\d{9}$") String omdUserPhone);

    /**
     * 注册用户
     * @param omdUser 用户信息
     */
    void register(OmdUser omdUser);

    /**
     * 根据用户名或手机号查询用户信息
     * @param identifier 用户名或手机号
     * @return 用户信息
     */
    OmdUser findByUsernameOrPhone(String identifier);

    /**
     * 根据用户ID查询用户信息及其角色
     * @param omdUserId 当前用户ID
     * @return 用户信息及其角色
     */
    OmdUser findOmdUserByIdWithRoles(Long omdUserId);

    /**
     * 修改用户信息
     * 这里不允许修改密码，与此同时用户名和手机号是不可修改的
     * @param omdUser 用户信息
     */
    void updateUserInfo(OmdUser omdUser);

    /**
     * 更新用户密码
     * @param omdUserId 用户ID
     * @param newPassword 新密码
     * @return 是否更新成功
     */
    boolean updatePassword(Long omdUserId, String newPassword);

    /**
     * 根据用户ID查询申请信息
     * @param omdUserId 用户ID
     * @return 申请信息
     */
    OmdApplications findApplicationByOmdUserId(Long omdUserId);

    /**
     * 申请成为歌手
     * @param omdApplications 申请信息
     */
    void applicationSinger(OmdApplications omdApplications);

    /**
     * 根据音乐评论的ID查询音乐评论
     * @param omdMusicCommentId 音乐评论ID
     * @return 音乐评论
     */
    OmdMusicComment findCommentByOmdMusicCommentId(Long omdMusicCommentId);

    /**
     * 插入音乐评论
     * @param omdMusicComment 音乐评论
     * @return 是否插入成功
     */
    boolean insertMusicComment(OmdMusicComment omdMusicComment);

    /**
     * 根据用户ID和评论ID删除音乐评论
     * @param omdMusicCommentId 音乐评论ID
     * @param omdUserId 当前用户ID
     * @return 音乐评论列表
     */
    boolean deleteMusicComment(Long omdMusicCommentId, Long omdUserId);

    /**
     * 根据音乐ID查询音乐评论列表
     * @param omdMusicInfoId 音乐ID
     * @param omdUserId 当前用户ID
     * @return 音乐评论列表
     */
    List<OmdMusicComment> findCommentListByOmdMusicInfoIdWithDynamicSort(Long omdMusicInfoId, Long omdUserId);

    /**
     * 点赞音乐评论
     * @param omdMusicCommentId 音乐评论ID
     * @return 是否点赞成功
     */
    boolean likeMusicComment(Long omdMusicCommentId);

    /**
     * 举报评论
     * @param omdCommentReport 举报信息
     * @return 是否举报成功
     */
    boolean insertOmdCommentReport(OmdCommentReport omdCommentReport);

    /**
     * 检查用户是否已经点赞过该评论
     * @param omdMusicCommentIdList 音乐评论ID
     * @param omdUserId 当前用户ID
     * @return 是否已经点赞过
     */
    boolean hasLikedMusicComment(Long omdMusicCommentIdList, Long omdUserId);

    /**
     * 取消点赞
     * @param omdMusicCommentId 音乐评论ID
     * @return 是否取消点赞成功
     */
    boolean unlikeMusicComment(Long omdMusicCommentId);

    /**
     * 删除评论点赞记录
     * @param omdMusicCommentId 音乐评论ID
     * @param omdUserId 当前用户ID
     * @return 是否删除成功
     */
    boolean deleteCommentLike(Long omdMusicCommentId, Long omdUserId);

    /**
     * 插入评论点赞记录
     * @param omdMusicCommentId 音乐评论ID
     * @param omdUserId 当前用户ID
     * @return 是否插入成功
     */
    boolean insertCommentLike(Long omdMusicCommentId, Long omdUserId);

    /**
     * 插入用户反馈
     * @param omdUserFeedback 用户反馈
     * @return 是否插入成功
     */
    boolean insertOmdFeedback(OmdUserFeedback omdUserFeedback);


    /**
     * 根据父评论ID更新子评论数量
     * @param omdMusicCommentParentId 父评论ID
     * @param delta 数量变化值（+1 或 -1）
     */
    boolean updateReplyCountByParentId(Long omdMusicCommentParentId,int delta);

    /**
     * 根据评论ID列表删除音乐评论
     * @param commentIds 评论ID列表
     * @return 成功删除的评论数量
     */
    int deleteMusicCommentByIdList(List<Long> commentIds);

    /**
     * 递归收集所有子评论ID
     * @param omdMusicCommentParentId 父评论ID
     */
    List<Long> selectChildCommentIds(Long omdMusicCommentParentId);

    /**
     * 根据OmdUserId获取OmdPlaylist
     * @param omdUserId OmdUserId
     * @return OmdPlaylist
     */
    List<OmdPlaylist> getPlaylistListByOmdUserId(Long omdUserId);

    /**
     * 根据OmdPlaylistId获取OmdPlaylistMusic
     * @param omdPlaylistId OmdPlaylistId
     * @return OmdPlaylistMusic
     */
    List<OmdPlaylistMusic> getPlaylistMusicByOmdPlaylistId(Long omdPlaylistId);

    /**
     * 根据omdMusicCommentIdList获取是否是本人评论
     * @param omdMusicCommentIdList 评论列表
     * @param omdUserId 用户ID
     * @return 状态列表
     */
    List<Map<String, Object>> isOwnCommentList(List<Long> omdMusicCommentIdList, Long omdUserId);

    /**
     * 根据omdMusicCommentIdList获取是否被用户点赞
     * @param omdMusicCommentIdList 评论列表
     * @param omdUserId 用户ID
     * @return 状态列表
     */
    List<Map<String, Object>> hasLikedMusicCommentList(List<Long> omdMusicCommentIdList, Long omdUserId);

    /**
     * 根据omdMusicCommentIdList获取是否被用户举报
     * @param omdMusicCommentIdList 评论列表
     * @param omdUserId 当前用户ID
     * @return 状态列表
     */
    List<Map<String, Object>> hasReportedMusicCommentList(List<Long> omdMusicCommentIdList, Long omdUserId);

    /**
     * 获取所有公开的歌单
     * @param omdUserId 用户ID
     * @return 歌单列表
     */
    List<OmdPlaylist> getPlaylistPublicList(Long omdUserId);

    /**
     * 根据用户ID获取其他用户信息
     * @param omdUserId 用户ID
     * @return 用户信息
     */
    OmdUser getUserInfoByOmdUserId(Long omdUserId);

    /**
     * 根据用户ID获取用户的公开歌单
     * @param omdUserId 用户ID
     * @return 歌单列表
     */
    List<OmdPlaylist> getOmdUserPlaylistPublic(Long omdUserId);

    /**
     * 获取其他用户的身份
     * @param omdUserId 用户ID
     * @return 歌单列表
     */
    List<OmdUserRole> getOmdUserRoleList(Long omdUserId);

    /**
     * 获取用户的举报列表
     * @param omdCommentReportStatus 举报状态
     * @param omdUserId 用户ID
     * @return 举报列表
     */
    List<OmdCommentReport> getOmdCommentReportList(Long omdUserId, Integer omdCommentReportStatus);

    /**
     * 获取用户的反馈列表
     * @param omdUserFeedbackStatus 反馈状态
     * @param omdUserId 当前用户ID
     * @return 反馈列表
     */
    List<OmdUserFeedback> getOmdUserFeedbackList(Long omdUserId, Integer omdUserFeedbackStatus);

    /**
     * 更新用户状态
     * @param updateUser 用户信息
     * @return 是否更新成功
     * */
    void updateUserStatus(OmdUser updateUser);

    /**
     * 获取被临时冻结且到时间的用户列表
     * @return 用户列表
     */
    List<OmdUser> selectExpiredFreezeUsers();

    /**
     * 根据申请ID获取申请信息
     * @param targetId 申请ID
     * @return 申请信息
     */
    OmdApplications getApplicationsByApplicationsId(Long targetId);

    /**
     * 根据反馈ID获取反馈信息
     * @param targetId 反馈ID
     * @return 反馈信息
     */
    OmdUserFeedback getFeedbackByFeedbackId(Long targetId);

    /**
     * 插入举报信息
     * @param omdUserReport 举报
     * @return 举报信息
     */
    boolean insertUserReport(OmdUserReport omdUserReport);

    /**
     * 检查用户是否已经举报过该用户
     * @param omdUserReportedUserId 被举报用户ID
     * @param omdUserId 当前用户ID
     * @return 是否已经举报过
     */
    boolean checkUserReport(Long omdUserReportedUserId, Long omdUserId);

    /**
     * 检查用户是否已经举报过该音乐
     * @param omdMusicInfoId 音乐ID
     * @param omdUserId 当前用户ID
     * @return 是否已经举报过
     */
    boolean checkMusicReport(Long omdMusicInfoId, Long omdUserId);

    /**
     * 插入音乐举报信息
     * @param omdMusicReport 举报信息
     * @return 是否插入成功
     * */
    boolean insertMusicReport(OmdMusicReport omdMusicReport);

    /**
     * 获取用户举报用户的举报列表
     * @param omdUserReportStatus 举报状态
     * @param omdUserId 当前用户ID
     * @return 举报列表
     */
    List<OmdUserReport> getOmdUserReportList(Long omdUserId, Integer omdUserReportStatus);

    /**
     * 获取用户举报音乐的举报列表
     * @param omdMusicReportStatus 举报状态
     * @param omdUserId 当前用户ID
     * @return 举报列表
     */
    List<OmdMusicReport> getOmdMusicReportList(Long omdUserId, Integer omdMusicReportStatus);
}

