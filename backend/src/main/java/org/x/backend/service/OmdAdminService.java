package org.x.backend.service;

import org.x.backend.pojo.*;

import java.util.Date;
import java.util.List;

public interface OmdAdminService {
    /**
     * 获取管理员信息
     * @param omdAdminId 当前用户ID
     * @return 管理员信息
     */
    OmdAdmin getAdminInfo(Long omdAdminId);

    /**
     * 更新管理员信息
     * @param omdAdmin 管理员信息
     * @return 是否更新成功
     */
    boolean updateAdminInfo(OmdAdmin omdAdmin);

    /**
     * 获取待审核的音乐信息
     * @return 音乐信息列表
     */
    List<OmdMusicInfo> getMusicInfoListByStatus();

    /**
     * 获取待审核的申请信息
     * @return 申请信息列表
     */
    List<OmdApplications> getApplicationsListByStatus();

    /**
     * 审核音乐信息状态
     * @param omdMusicInfoId 音乐信息ID
     * @param omdMusicInfoStatus 音乐信息状态
     * @param omdMusicInfoRemark 音乐信息备注
     * @return 是否更新成功
     */
    boolean updateMusicInfoStatus(Long omdMusicInfoId, Integer omdMusicInfoStatus, String omdMusicInfoRemark);

    /**
     * 新增管理员操作表
     * @param omdOperationLog 管理员操作表
     * @return 是否新增成功
     * */
    boolean addOmdOperationLog(OmdOperationLog omdOperationLog);

    /**
     * 获取申请信息详情
     * @param omdApplicationsId 歌手申请ID
     * @return 申请信息详情
     */
    OmdApplications getApplicationsById(Long omdApplicationsId);

    /**
     * 更新申请信息状态
     * @param omdApplications 申请信息
     * @return 是否更新成功
     */
    boolean updateApplications(OmdApplications omdApplications);

    /**
     * 获取评论举报信息列表
     * @return 评论举报信息列表
     */
    List<OmdCommentReport> getCommentReportListByStatus();

    /**
     * 更新评论举报信息状态
     * @param omdCommentReport 评论举报信息
     * @return 是否更新成功
     */
    boolean updateCommentReport(OmdCommentReport omdCommentReport);

    /**
     * 根据评论ID获取音乐评论信息
     * @return 音乐评论信息
     */
    boolean findMusicCommentById(Long omdMusicCommentId);

    /**
     * 获取用户反馈信息列表
     * @return 用户反馈信息列表
     */
    List<OmdUserFeedback> getFeedbackListByStatus();

    /**
     * 查询用户反馈信息详情
     * @param omdUserFeedbackId 用户反馈信息ID
     * @return 反馈信息详情
     */
    OmdUserFeedback findUserFeedbackById(Long omdUserFeedbackId);

    /**
     * 更新用户反馈信息状态
     * @param omdUserFeedback 用户反馈信息
     * @return 是否更新成功
     */
    boolean updateUserFeedback(OmdUserFeedback omdUserFeedback);

    /**
     * 获取所有用户信息
     * @return 用户信息列表
     */
    List<OmdUser> getAllUsers();

    /**
     * 更新用户状态
     * @param omdUserId           用户ID
     * @param omdUserRemark       冻结备注
     * @param omdUserStatus       用户状态
     * @param omdUserFreezeType   冻结类型
     * @param omdUserFreezeEndTime 冻结结束时间
     * @return 是否更新成功
     */
    boolean updateUserStatus(Long omdUserId, String omdUserRemark, Integer omdUserStatus, Integer omdUserFreezeType, Date omdUserFreezeEndTime);

    /**
     * 获取操作日志列表
     * @return 操作日志列表
     */
    List<OmdOperationLog> getOperationLogList();

    /**
     * 根据用户名获取用户信息
     * @param omdUserName 用户名
     * @return 用户信息列表
     */
    List<OmdUser> getUserByUsername(String omdUserName);

    /**
     * 获取所有管理员信息
     * @return 管理员信息列表
     */
    List<OmdAdmin> getAllAdminList();

    /**
     * 新增管理员
     * @param omdAdmin 管理员信息
     * @return 是否新增成功
     */
    boolean insertAdmin(OmdAdmin omdAdmin);

    /**
     * 更新管理员信息
     * @param omdAdminId 管理员ID
     * @param omdAdminStatus 管理员状态
     * @return 是否更新成功
     */
    boolean updateAdminStatus(Long omdAdminId,Integer omdAdminStatus);

    /**
     * 新增用户
     * @param omdUser 用户信息
     * @return 是否新增成功
     */
    boolean insertUser(OmdUser omdUser);

    /**
     * 删除评论
     * @param omdMusicCommentId 评论ID
     * @return 是否删除成功
     */
    boolean deleteMusicComment(Long omdMusicCommentId);

    /**
     * 更新用户角色
     * @param omdUserId 用户ID
     * @param roleSinger 角色
     * @return 是否更新成功
     */
    boolean updateUserRole(long omdUserId, String roleSinger);

    /**
     * 新增歌手
     * @return 歌手信息列表
     */
    boolean insertSinger(OmdSinger omdSinger);

    /**
     * 获取歌手信息
     * @param omdUserId 用户ID
     * @return 歌手信息列表
     */
    OmdSinger getSingerByUserId(Long omdUserId);

    /**
     * 更新歌手状态
     * @param omdUserId 用户ID
     * @param omdSingerStatus 歌手状态
     * @return 是否更新成功
     */
    void updateSingerStatus(Long omdUserId, Integer omdSingerStatus);

    /**
     * 更新音乐状态
     * @param omdUserId 用户ID
     * @param omdMusicInfoStatus 音乐状态
     * @param omdMusicInfoRemark 音乐状态备注
     * @return 是否更新成功
     */
    void updateMusicStatusBySingerId(Long omdUserId, Integer omdMusicInfoStatus,String omdMusicInfoRemark);

    /**
     * 新增用户角色
     * @param omdUserRole 用户角色信息
     * @return 是否新增成功
     */
    boolean insertUserRole(OmdUserRole omdUserRole);

    /**
     * 根据操作类型获取操作日志
     * @param targetTypes 操作类型
     * @return 操作日志
     */
    List<OmdOperationLog> getOperationLogListByType(String targetTypes);

    /**
     * 获取用户举报信息列表
     * @return 用户举报信息列表
     */
    List<OmdUserReport> getUserReportList();

    /**
     * 根据用户举报ID获取用户举报信息
     * @param omdUserReportId 用户举报ID
     * @return 用户举报信息
     */
    OmdUserReport findUserReportById(Long omdUserReportId);

    /**
     * 更新用户举报信息状态
     * @param omdUserReport 用户举报信息
     * @return 是否更新成功
     */
    boolean updateUserReport(OmdUserReport omdUserReport);

    /**
     * 获取音乐举报信息列表
     * @return 音乐举报信息列表
     */
    List<OmdMusicReport> getMusicReportList();

    /**
     * 根据音乐举报ID获取音乐举报信息
     * @param omdMusicReportId 音乐举报ID
     * @return 音乐举报信息
     */
    OmdMusicReport findMusicReportById(Long omdMusicReportId);

    /**
     * 更新音乐举报信息状态
     * @param omdMusicReport 音乐举报信息
     * @return 是否更新成功
     */
    boolean updateMusicReport(OmdMusicReport omdMusicReport);

    /**
     * 获取用户申诉信息列表
     * @return 用户申诉信息列表
     */
    List<OmdUserAppeal> getUserAppealList();

    /**
     * 根据用户申诉ID获取用户申诉信息
     * @param omdUserAppealId 用户申诉ID
     * @return 用户申诉信息
     */
    OmdUserAppeal findUserAppealById(Long omdUserAppealId);

    /**
     * 更新用户申诉信息状态
     * @param omdUserAppeal 用户申诉信息
     * @return 是否更新成功
     */
    boolean updateUserAppeal(OmdUserAppeal omdUserAppeal);

    /**
     * 获取音乐申诉信息列表
     * @return 音乐申诉信息列表
     */
    List<OmdMusicAppeal> getMusicAppealList();

    /**
     * 根据音乐申诉ID获取音乐申诉信息
     * @param omdMusicAppealId 音乐申诉ID
     * @return 音乐申诉信息
     */
    OmdMusicAppeal findMusicAppealById(Long omdMusicAppealId);

    /**
     * 更新音乐申诉信息状态
     * @param omdMusicAppeal 音乐申诉信息
     * @return 是否更新成功
     */
    boolean updateMusicAppeal(OmdMusicAppeal omdMusicAppeal);

    /**
     * 根据音乐ID获取音乐信息
     * @param omdMusicInfoId 音乐ID
     * @return 音乐信息
     */
    OmdMusicInfo findMusicInfoByMusicId(Long omdMusicInfoId);

    /**
     * 根据评论ID获取评论举报信息
     * @param targetId 评论ID
     * @return 评论举报信息
     */
    OmdCommentReport getCommentReportByCommentId(Long targetId);
}
