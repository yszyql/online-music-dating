package org.x.backend.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.x.backend.mapper.OmdAdminMapper;
import org.x.backend.pojo.*;
import org.x.backend.service.OmdAdminService;

import java.util.Date;
import java.util.List;

@Service
public class OmdAdminServiceImpl implements OmdAdminService {

    @Autowired
    private OmdAdminMapper omdAdminMapper;

    /**
     * 获取管理员信息
     * @param omdAdminId 当前用户ID
     * @return 管理员信息
     */
    @Override
    public OmdAdmin getAdminInfo(Long omdAdminId) {
        return omdAdminMapper.getAdminInfo(omdAdminId);
    }

    /**
     * 更新管理员信息
     * @param omdAdmin 管理员信息
     * @return 是否更新成功
     */
    @Override
    public boolean updateAdminInfo(OmdAdmin omdAdmin) {
        return omdAdminMapper.updateAdminInfo(omdAdmin) > 0;
    }

    /**
     * 获取待审核的音乐信息
     * @return 音乐信息列表
     */
    @Override
    public List<OmdMusicInfo> getMusicInfoListByStatus() {
        return omdAdminMapper.getMusicInfoListByStatus();
    }

    /**
     * 获取待审核的申请信息
     * @return 申请信息列表
     */
    @Override
    public List<OmdApplications> getApplicationsListByStatus() {
        return omdAdminMapper.getApplicationsListByStatus();
    }

    /**
     * 审核音乐信息状态
     * @param omdMusicInfoId 音乐信息ID
     * @param omdMusicInfoStatus 音乐信息状态
     * @param omdMusicInfoRemark 音乐信息备注
     * @return 是否更新成功
     */
    @Override
    public boolean updateMusicInfoStatus(Long omdMusicInfoId, Integer omdMusicInfoStatus, String omdMusicInfoRemark) {
        return omdAdminMapper.updateMusicInfoStatus(omdMusicInfoId, omdMusicInfoStatus,omdMusicInfoRemark) > 0;
    }

    /**
     * 新增管理员操作表
     * @param omdOperationLog 管理员操作表
     * @return 是否新增成功
     */
    @Override
    public boolean addOmdOperationLog(OmdOperationLog omdOperationLog) {
        return omdAdminMapper.addOmdOperationLog(omdOperationLog) > 0;
    }

    /**
     * 获取申请信息详情
     * @param omdApplicationsId 歌手申请ID
     * @return 申请信息详情
     */
    @Override
    public OmdApplications getApplicationsById(Long omdApplicationsId) {
        return omdAdminMapper.getApplicationsById(omdApplicationsId);
    }

    /**
     * 更新申请信息状态
     * @param omdApplications 申请信息
     * @return 是否更新成功
     */
    @Override
    public boolean updateApplications(OmdApplications omdApplications) {
        return omdAdminMapper.updateApplications(omdApplications) > 0;
    }

    /**
     * 获取评论举报信息列表
     * @return 评论举报信息列表
     */
    @Override
    public List<OmdCommentReport> getCommentReportListByStatus() {
        return omdAdminMapper.getCommentReportListByStatus();
    }

    /**
     * 更新评论举报信息状态
     * @param omdCommentReport 评论举报信息
     * @return 是否更新成功
     */
    @Override
    public boolean updateCommentReport(OmdCommentReport omdCommentReport) {
        return omdAdminMapper.updateCommentReport(omdCommentReport) > 0;
    }

    /**
     * 根据评论ID获取音乐评论信息
     * @return 音乐评论信息
     */
    @Override
    public boolean findMusicCommentById(Long omdMusicCommentId) {
        return omdAdminMapper.findMusicCommentById(omdMusicCommentId) > 0;
    }

    /**
     * 获取用户反馈列表
     * @return 用户反馈列表
     */
    @Override
    public List<OmdUserFeedback> getFeedbackListByStatus() {
        return omdAdminMapper.getFeedbackListByStatus();
    }

    /**
     * 根据用户反馈ID获取用户反馈信息
     * @param omdUserFeedbackId 用户反馈ID
     * @return 用户反馈信息
     */
    @Override
    public OmdUserFeedback findUserFeedbackById(Long omdUserFeedbackId) {
        return omdAdminMapper.findUserFeedbackById(omdUserFeedbackId);
    }

    /**
     * 更新用户反馈信息状态
     * @param omdUserFeedback 用户反馈信息
     * @return 是否更新成功
     */
    @Override
    public boolean updateUserFeedback(OmdUserFeedback omdUserFeedback) {
        return omdAdminMapper.updateUserFeedback(omdUserFeedback) > 0;
    }

    /**
     * 获取所有用户信息
     * @return 用户信息列表
     */
    @Override
    public List<OmdUser> getAllUsers() {
        return omdAdminMapper.getAllUsers();
    }

    /**
     * 更新用户状态
     * @param omdUserId           用户ID
     * @param omdUserRemark       冻结备注
     * @param omdUserStatus       用户状态
     * @param omdUserFreezeType   冻结类型
     * @param omdUserFreezeEndTime 冻结结束时间
     * @return 是否更新成功
     * */
    @Override
    public boolean updateUserStatus(Long omdUserId, String omdUserRemark, Integer omdUserStatus, Integer omdUserFreezeType, Date omdUserFreezeEndTime) {
        return omdAdminMapper.updateUserStatus(omdUserId,omdUserRemark,omdUserStatus,omdUserFreezeType,omdUserFreezeEndTime) > 0;
    }

    /**
     * 获取操作日志列表
     * @return 操作日志列表
     */
    @Override
    public List<OmdOperationLog> getOperationLogList() {
        return omdAdminMapper.getOperationLogList();
    }

    /**
     * 根据用户名获取用户信息
     * @param omdUserName 用户名
     * @return 用户信息列表
     */
    @Override
    public List<OmdUser> getUserByUsername(String omdUserName) {
        return omdAdminMapper.getUserByUsername(omdUserName);
    }

    /**
     * 获取所有管理员信息
     * @return 管理员信息列表
     */
    @Override
    public List<OmdAdmin> getAllAdminList() {
        return omdAdminMapper.getAllAdminList();
    }

    /**
     * 新增管理员信息
     * @param omdAdmin 管理员信息
     * @return 是否新增成功
     */
    @Override
    public boolean insertAdmin(OmdAdmin omdAdmin) {
        return omdAdminMapper.insertAdmin(omdAdmin) > 0;
    }

    /**
     * 更新管理员信息
     * @param omdAdminId 管理员ID
     * @param omdAdminStatus 管理员状态
     * @return 是否更新成功
     */
    @Override
    public boolean updateAdminStatus(Long omdAdminId,Integer omdAdminStatus) {
        return omdAdminMapper.updateAdminStatus(omdAdminId,omdAdminStatus) > 0;
    }

    /**
     * 新增用户
     * @param omdUser 用户信息
     * @return 是否新增成功
     */
    @Override
    public boolean insertUser(OmdUser omdUser) {
        return omdAdminMapper.insertUser(omdUser) > 0;
    }

    /**
     * 删除评论
     * @param omdMusicCommentId 评论ID
     * @return 是否删除成功
     */
    @Override
    public boolean deleteMusicComment(Long omdMusicCommentId) {
        return omdAdminMapper.deleteMusicComment(omdMusicCommentId) > 0;
    }

    /**
     * 更新用户角色
     * @param omdUserId 用户ID
     * @param roleSinger 角色
     * @return 是否更新成功
     */
    @Override
    public boolean updateUserRole(long omdUserId, String roleSinger) {
        return omdAdminMapper.updateUserRole(omdUserId, roleSinger) > 0;
    }

    /**
     * 新增歌手
     * @return 歌手信息列表
     */
    @Override
    public boolean insertSinger(OmdSinger omdSinger) {
        return omdAdminMapper.insertSinger(omdSinger) > 0;
    }

    /**
     * 根据用户ID获取歌手信息
     * @param omdUserId 用户ID
     * @return 歌手信息
     */
    @Override
    public OmdSinger getSingerByUserId(Long omdUserId) {
        return omdAdminMapper.getSingerByUserId(omdUserId);
    }

    /**
     * 更新歌手状态
     * @param omdUserId 用户ID
     * @param omdSingerStatus 歌手状态
     * @return 是否更新成功
     */
    @Override
    public void updateSingerStatus(Long omdUserId, Integer omdSingerStatus) {
        omdAdminMapper.updateSingerStatus(omdUserId,omdSingerStatus);
    }

    /**
     * 更新音乐状态
     * @param omdUserId 用户ID
     * @param omdMusicInfoStatus 音乐状态
     * @param omdMusicInfoRemark 音乐状态备注
     * @return 是否更新成功
     */
    @Override
    public void updateMusicStatusBySingerId(Long omdUserId, Integer omdMusicInfoStatus,String omdMusicInfoRemark) {
        omdAdminMapper.updateMusicStatusBySingerId(omdUserId,omdMusicInfoStatus,omdMusicInfoRemark);
    }

    /**
     * 新增用户角色
     * @param omdUserRole 用户角色信息
     * @return 是否新增成功
     */
    @Override
    public boolean insertUserRole(OmdUserRole omdUserRole) {
        return omdAdminMapper.insertUserRole(omdUserRole) > 0;
    }

    /**
     * 根据类型获取操作日志列表
     * @param targetTypes 类型
     * @return 操作日志列表
     */
    @Override
    public List<OmdOperationLog> getOperationLogListByType(String targetTypes) {
        return omdAdminMapper.getOperationLogListByType(targetTypes);
    }

    /**
     * 获取用户举报信息列表
     * @return 用户举报信息列表
     */
    @Override
    public List<OmdUserReport> getUserReportList() {
        return omdAdminMapper.getUserReportList();
    }

    /**
     * 根据用户举报ID获取用户举报信息
     * @param omdUserReportId 用户举报ID
     * @return 用户举报信息
     */
    @Override
    public OmdUserReport findUserReportById(Long omdUserReportId) {
        return omdAdminMapper.findUserReportById(omdUserReportId);
    }

    /**
     * 更新用户举报信息状态
     * @param omdUserReport 用户举报信息
     * @return 是否更新成功
     */
    @Override
    public boolean updateUserReport(OmdUserReport omdUserReport) {
        return omdAdminMapper.updateUserReport(omdUserReport) > 0;
    }

    /**
     * 获取音乐举报信息列表
     * @return 音乐举报信息列表
     */
    @Override
    public List<OmdMusicReport> getMusicReportList() {
        return omdAdminMapper.getMusicReportList();
    }

    /**
     * 根据音乐举报ID获取音乐举报信息
     * @param omdMusicReportId 音乐举报ID
     * @return 音乐举报信息
     */
    @Override
    public OmdMusicReport findMusicReportById(Long omdMusicReportId) {
        return omdAdminMapper.findMusicReportById(omdMusicReportId);
    }

    /**
     * 更新音乐举报信息状态
     * @param omdMusicReport 音乐举报信息
     * @return 是否更新成功
     */
    @Override
    public boolean updateMusicReport(OmdMusicReport omdMusicReport) {
        return omdAdminMapper.updateMusicReport(omdMusicReport) > 0;
    }

    /**
     * 获取用户申诉信息列表
     * @return 用户申诉信息列表
     */
    @Override
    public List<OmdUserAppeal> getUserAppealList() {
        return omdAdminMapper.getUserAppealList();
    }

    /**
     * 根据用户申诉ID获取用户申诉信息
     * @param omdUserAppealId 用户申诉ID
     * @return 用户申诉信息
     */
    @Override
    public OmdUserAppeal findUserAppealById(Long omdUserAppealId) {
        return omdAdminMapper.findUserAppealById(omdUserAppealId);
    }

    /**
     * 更新用户申诉信息状态
     * @param omdUserAppeal 用户申诉信息
     * @return 是否更新成功
     */
    @Override
    public boolean updateUserAppeal(OmdUserAppeal omdUserAppeal) {
        return omdAdminMapper.updateUserAppeal(omdUserAppeal) > 0;
    }

    /**
     * 获取音乐申诉信息列表
     * @return 音乐申诉信息列表
     */
    @Override
    public List<OmdMusicAppeal> getMusicAppealList() {
        return omdAdminMapper.getMusicAppealList();
    }

    /**
     * 根据音乐申诉ID获取音乐申诉信息
     * @param omdMusicAppealId 音乐申诉ID
     * @return 音乐申诉信息
     */
    @Override
    public OmdMusicAppeal findMusicAppealById(Long omdMusicAppealId) {
        return omdAdminMapper.findMusicAppealById(omdMusicAppealId);
    }

    /**
     * 更新音乐申诉信息状态
     * @param omdMusicAppeal 音乐申诉信息
     * @return 是否更新成功
     */
    @Override
    public boolean updateMusicAppeal(OmdMusicAppeal omdMusicAppeal) {
        return omdAdminMapper.updateMusicAppeal(omdMusicAppeal) > 0;
    }

    /**
     * 根据音乐ID获取音乐信息
     * @param omdMusicInfoId 音乐ID
     * @return 音乐信息
     */
    @Override
    public OmdMusicInfo findMusicInfoByMusicId(Long omdMusicInfoId) {
        return omdAdminMapper.findMusicInfoByMusicId(omdMusicInfoId);
    }

    /**
     * 根据评论ID获取评论举报信息
     * @param targetId 评论ID
     * @return 评论举报信息
     */
    @Override
    public OmdCommentReport getCommentReportByCommentId(Long targetId) {
        return omdAdminMapper.getCommentReportByCommentId(targetId);
    }
}
