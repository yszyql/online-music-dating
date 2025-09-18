package org.x.backend.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.x.backend.pojo.*;
import org.x.backend.service.OmdAdminService;
import org.x.backend.service.OmdUserService;
import org.x.backend.utils.HelperUtil;

import java.util.Date;

@Service
@Slf4j
@Transactional(rollbackFor = Exception.class) // 统一管理事务
public class TransactionAdminService {

    @Autowired
    private OmdAdminService omdAdminService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private OmdUserService omdUserService;

    @Autowired
    private HelperUtil helperUtil;


    /**
     * 更新用户状态
     * @param omdUserId           用户ID
     * @param omdUserRemark       冻结备注
     * @param omdUserStatus       用户状态
     * @param omdUserFreezeType   冻结类型
     * @param omdUserFreezeEndTime 冻结结束时间
     * @param omdAdminId          当前管理员ID
     */
    public void updateUserStatus(Long omdUserId, String omdUserRemark, Integer omdUserStatus,
                                 Integer omdUserFreezeType, Date omdUserFreezeEndTime, Long omdAdminId) {
        // 1. 检测该用户是否同时是歌手
        OmdSinger singer = omdAdminService.getSingerByUserId(omdUserId);
        boolean isSinger = singer != null;

        // 2. 更新用户状态（仅需调用一次）
        if (!omdAdminService.updateUserStatus(omdUserId, omdUserRemark, omdUserStatus,
                omdUserFreezeType, omdUserFreezeEndTime)) {
            throw new RuntimeException("更新用户状态失败");
        }

        // 3. 冻结/解冻的差异化处理
        if (omdUserStatus == 0) {
            // 冻结逻辑：同步冻结歌手身份+下架作品
            if (isSinger) {
                singer.setOmdSingerStatus(2); // 冻结歌手
                omdAdminService.updateSingerStatus(omdUserId, singer.getOmdSingerStatus());
                omdAdminService.updateMusicStatusBySingerId(singer.getOmdSingerId(), 2, "账号被冻结"); // 下架作品
            }
        } else {
            // 解冻逻辑：仅恢复用户账号，歌手身份仍保持冻结
            if (isSinger) {
                // 记录日志提示需单独处理歌手身份
                log.warn("用户 {} 为歌手，已解冻用户账号，但歌手身份及作品仍保持冻结状态，需单独操作恢复", omdUserId);
            }
        }

        // 4. 操作日志记录
        OmdOperationLog log = new OmdOperationLog();
        log.setOmdAdminId(omdAdminId);
        log.setOmdOperationLogType(omdUserStatus == 1 ? "user:unfreeze" : "user:freeze");
        log.setOmdOperationLogTargetId(omdUserId);
        log.setOmdOperationLogTargetType("user");

        if (isSinger) {
            if (omdUserStatus == 1) {
                // 解冻用户时的日志（提示歌手身份未恢复）
                log.setOmdOperationLogDesc("解冻用户账号（ID：" + omdUserId + "），该用户为歌手，歌手身份及作品仍保持冻结，需单独恢复");
            } else {
                // 冻结用户时的日志（同步处理了歌手）
                log.setOmdOperationLogDesc("冻结用户账号（ID：" + omdUserId + "），该用户为歌手，已同步冻结歌手身份及下架所有作品");
            }
        } else {
            log.setOmdOperationLogDesc(omdUserStatus == 1 ? "解冻用户账号" : "冻结用户账号");
        }
        omdAdminService.addOmdOperationLog(log);
    }

    /**
     * 更新音乐信息状态
     * @param omdMusicInfoId      音乐信息ID
     * @param omdMusicInfoStatus  音乐信息状态
     * @param omdMusicInfoRemark  音乐信息备注
     * @param omdAdminId       当前用户ID
     */
    public void updateMusicInfoStatus(Long omdMusicInfoId, Integer omdMusicInfoStatus, String omdMusicInfoRemark, Long omdAdminId) {

        // 更新音乐信息状态
        if (!omdAdminService.updateMusicInfoStatus(omdMusicInfoId, omdMusicInfoStatus, omdMusicInfoRemark)) {
            throw new RuntimeException("更新音乐信息状态失败");
        }
        // 新增管理员操作表
        if (omdMusicInfoStatus == 1){
            if(!omdAdminService.addOmdOperationLog(helperUtil.setOmdOperationLog(omdAdminId,"review:music:pass","审核音乐信息通过",omdMusicInfoId,"music"))){
                throw new RuntimeException("新增管理员操作表失败");
            }
        }else {
            if (!omdAdminService.addOmdOperationLog(helperUtil.setOmdOperationLog(omdAdminId,"review:music:fail","审核音乐信息失败",omdMusicInfoId,"music"))){
                throw new RuntimeException("新增管理员操作表失败");
            }
        }

    }

    /**
     * 更新申请信息状态
     * @param omdApplicationsId      申请信息ID
     * @param omdApplicationsStatus  申请信息状态
     * @param omdApplicationsReviewRemark  申请信息备注
     * @param omdAdminId       当前用户ID
     */
    public void updateApplicationsStatus(Long omdApplicationsId, Integer omdApplicationsStatus,
                                         String omdApplicationsReviewRemark, Long omdAdminId) {
        // 更新申请表
        OmdApplications omdApplications = omdAdminService.getApplicationsById(omdApplicationsId);
        omdApplications.setOmdApplicationsStatus(omdApplicationsStatus);
        omdApplications.setOmdApplicationsReviewRemark(omdApplicationsReviewRemark);
        omdApplications.setOmdAdminName(omdAdminService.getAdminInfo(omdAdminId).getOmdAdminName());
        if (!omdAdminService.updateApplications(omdApplications)) {
            throw new RuntimeException("更新申请表失败");
        }

        // 通过审核，新增歌手信息
        if (omdApplicationsStatus == 1){
            OmdSinger omdSinger = new OmdSinger();
            omdSinger.setOmdSingerName(omdApplications.getOmdSingerName());
            omdSinger.setOmdUserId(omdApplications.getOmdUserId());
            omdSinger.setOmdSingerIntroduction(omdApplications.getOmdApplicationsIntroduction());
            omdSinger.setOmdSingerGenre(omdApplications.getOmdApplicationsGenre());

            // 新增歌手信息
            if (!omdAdminService.insertSinger(omdSinger)) {
                throw new RuntimeException("新增歌手信息失败");
            }

            // 修改用户权限
            if (!omdAdminService.updateUserRole(omdApplications.getOmdUserId(), "ROLE_SINGER")) {
                throw new RuntimeException("修改用户权限失败");
            }
        }

        // 新增管理员操作表
        if (omdApplicationsStatus == 1){
            if (!omdAdminService.addOmdOperationLog(helperUtil.setOmdOperationLog(omdAdminId,"review:applications:pass","审核申请信息通过",omdApplicationsId,"applications"))){
                throw new RuntimeException("新增管理员操作表失败");
            }
        }else {
            if (!omdAdminService.addOmdOperationLog(helperUtil.setOmdOperationLog(omdAdminId,"review:applications:fail","审核申请信息失败",omdApplicationsId,"applications"))){
                throw new RuntimeException("新增管理员操作表失败");
            }
        }
    }

    /**
     * 更新评论举报状态
     * @param omdMusicCommentId      被举报的评论ID
     * @param omdCommentReportStatus  评论举报状态
     * @param omdCommentReportRemark  评论举报备注
     * @param omdAdminId       当前用户ID
     */
    public void updateCommentReportStatus(Long omdMusicCommentId, Integer omdCommentReportStatus,
                                          String omdCommentReportRemark, Long omdAdminId) {


        // 查询评论举报表
        if (!omdAdminService.findMusicCommentById(omdMusicCommentId)){
            throw new RuntimeException("评论区中该评论已被用户删除");
        }

        // 更新评论举报表
        OmdCommentReport omdCommentReport = new OmdCommentReport();
        omdCommentReport.setOmdMusicCommentId(omdMusicCommentId);
        omdCommentReport.setOmdCommentReportStatus(omdCommentReportStatus);
        omdCommentReport.setOmdCommentReportRemark(omdCommentReportRemark);
        omdCommentReport.setOmdAdminName(omdAdminService.getAdminInfo(omdAdminId).getOmdAdminName());
        if (!omdAdminService.updateCommentReport(omdCommentReport)) {
            throw new RuntimeException("更新评论举报表失败");
        }

        if (omdCommentReportStatus == 1){
            // 通过审核，将被举报的评论删除
            if (!omdAdminService.deleteMusicComment(omdMusicCommentId)) {
                throw new RuntimeException("删除评论失败");
            }
        }

        // 新增管理员操作表
        if (omdCommentReportStatus == 1){
            if (!omdAdminService.addOmdOperationLog(helperUtil.setOmdOperationLog(omdAdminId,"review:comment:pass","审核评论举报通过",omdMusicCommentId,"comment"))){
                throw new RuntimeException("新增管理员操作表失败");
            }
        }else {
            if (!omdAdminService.addOmdOperationLog(helperUtil.setOmdOperationLog(omdAdminId,"review:comment:fail","审核评论举报失败",omdMusicCommentId,"comment"))){
                throw new RuntimeException("新增管理员操作表失败");
            }
        }
    }

    /**
     * 更新用户反馈状态
     * @param omdUserFeedbackId      反馈ID
     * @param omdUserFeedbackResponse  反馈回复
     * @param omdAdminId       当前用户ID
     */
    public void updateFeedbackStatus(Long omdUserFeedbackId, String omdUserFeedbackResponse, Long omdAdminId) {

        OmdUserFeedback omdUserFeedback = omdAdminService.findUserFeedbackById(omdUserFeedbackId);

        // 查询用户反馈表
        if (omdUserFeedback == null) {
            throw new RuntimeException("用户反馈中该反馈已被用户删除");
        }

        // 更新用户反馈表
        omdUserFeedback.setOmdUserFeedbackResponse(omdUserFeedbackResponse);
        omdUserFeedback.setOmdUserFeedbackStatus(1);
        if (!omdAdminService.updateUserFeedback(omdUserFeedback)) {
            throw new RuntimeException("更新用户反馈表失败");
        }
        // 新增管理员操作表
        if (!omdAdminService.addOmdOperationLog(helperUtil.setOmdOperationLog(omdAdminId,"review:feedback","审核反馈信息",omdUserFeedbackId,"feedback"))){
            throw new RuntimeException("新增管理员操作表失败");
        }
    }

    /**
     * 新增管理员信息
     * @param omdAdmin 管理员信息
     * @param omdAdminId 当前用户ID
     */
    public void insertAdmin(OmdAdmin omdAdmin, Long omdAdminId) {
        OmdAdmin omdSuperAdmin = omdAdminService.getAdminInfo(omdAdminId);
        if (omdSuperAdmin.getOmdAdminRole() != 1) {
            throw new RuntimeException("只有超级管理员可以新增管理员");
        }

        // 密码加密
        String encodedPassword = passwordEncoder.encode(omdAdmin.getOmdAdminPassword());
        omdAdmin.setOmdAdminPassword(encodedPassword);

        // 新增用户表，方便登录
        OmdUser omdUser = new OmdUser();
        omdUser.setOmdUserName(omdAdmin.getOmdAdminName());
        omdUser.setOmdUserPassword(omdAdmin.getOmdAdminPassword());
        omdUser.setOmdUserEmail(omdAdmin.getOmdAdminEmail());
        omdUser.setOmdUserPhone(omdAdmin.getOmdAdminPhone());
        if (!omdAdminService.insertUser(omdUser)) {
            throw new RuntimeException("新增用户表失败");
        }

        // 设置ID
        omdAdmin.setOmdAdminId(omdUser.getOmdUserId());
        // 新增管理员信息
        if (!omdAdminService.insertAdmin(omdAdmin)) {
            throw new RuntimeException("新增管理员信息失败");
        }

        // 新增用户权限表
        OmdUserRole omdUserRole = new OmdUserRole();
        omdUserRole.setOmdUserId(omdUser.getOmdUserId());
        omdUserRole.setOmdRoleCode("ROLE_ADMIN");
        if (!omdAdminService.insertUserRole(omdUserRole)) {
            throw new RuntimeException("新增用户权限表失败");
        }

        // 新增管理员操作表
        if (!omdAdminService.addOmdOperationLog(helperUtil.setOmdOperationLog(omdAdminId,"admin:insert","新增管理员信息",omdAdmin.getOmdAdminId(),"admin"))){
            throw new RuntimeException("新增管理员操作表失败");
        }
    }

    /**
     * 更新管理员信息
     * @param omdAdminId 被更新的管理员ID
     * @param currentUserId 当前用户ID
     * @param omdAdminStatus 管理员状态
     * @return 更新结果
     */
    public void updateAdminStatus(Long omdAdminId,Integer omdAdminStatus, Long currentUserId) {
        OmdAdmin omdSuperAdmin = omdAdminService.getAdminInfo(currentUserId);
        if (omdSuperAdmin.getOmdAdminRole() != 1) {
            throw new RuntimeException("只有超级管理员可以新增管理员");
        }
        if (!omdAdminService.updateAdminStatus(omdAdminId,omdAdminStatus)) {
            throw new RuntimeException("更新管理员信息失败");
        }
        // 新增管理员操作表
        if (omdAdminStatus == 1){
            if (!omdAdminService.addOmdOperationLog(helperUtil.setOmdOperationLog(currentUserId,"admin:unfreeze","解冻管理员账号",omdAdminId,"admin"))){
                throw new RuntimeException("新增管理员操作表失败");
            }
        }else {
            if (!omdAdminService.addOmdOperationLog(helperUtil.setOmdOperationLog(currentUserId,"admin:freeze","冻结管理员账号",omdAdminId,"admin"))){
                throw new RuntimeException("新增管理员操作表失败");
            }
        }
    }

    /**
     * 更新用户举报状态
     * @param omdUserReportId      举报ID
     * @param omdUserReportStatus  举报状态
     * @param omdUserReportHandleRemark  举报处理备注
     * @param omdAdminId 当前用户ID
     */
    public void updateUserReportStatus(Long omdUserReportId, Integer omdUserReportStatus, String omdUserReportHandleRemark, Long omdAdminId) {
        OmdUserReport omdUserReport = omdAdminService.findUserReportById(omdUserReportId);
        if(omdUserReport == null){
            throw new RuntimeException("查询不到该举报");
        }
        // 更新用户举报表
        omdUserReport.setOmdUserReportStatus(omdUserReportStatus);
        omdUserReport.setOmdUserReportHandleRemark(omdUserReportHandleRemark);
        omdUserReport.setOmdAdminId(omdAdminId);
        if (!omdAdminService.updateUserReport(omdUserReport)) {
            throw new RuntimeException("更新用户举报表失败");
        }
        // 新增管理员操作表
        if (omdUserReportStatus == 1){
            if (!omdAdminService.addOmdOperationLog(helperUtil.setOmdOperationLog(omdAdminId,"review:user:report:pass","审核用户举报通过",omdUserReportId,"userReport"))){
                throw new RuntimeException("新增管理员操作表失败");
            }
        }else {
            if (!omdAdminService.addOmdOperationLog(helperUtil.setOmdOperationLog(omdAdminId,"review:user:report:fail","审核用户举报失败",omdUserReportId,"userReport"))){
                throw new RuntimeException("新增管理员操作表失败");
            }
        }
    }

    /**
     * 更新音乐举报状态
     * @param omdMusicReportId      举报ID
     * @param omdMusicReportStatus  举报状态
     * @param omdMusicReportHandleRemark  举报处理备注
     * @param omdAdminId 当前用户ID
     */
    public void updateMusicReportStatus(Long omdMusicReportId, Integer omdMusicReportStatus, String omdMusicReportHandleRemark, Long omdAdminId) {
        OmdMusicReport omdMusicReport = omdAdminService.findMusicReportById(omdMusicReportId);
        if(omdMusicReport == null){
            throw new RuntimeException("查询不到该举报");
        }
        // 更新音乐举报表
        omdMusicReport.setOmdMusicReportStatus(omdMusicReportStatus);
        omdMusicReport.setOmdMusicReportHandleRemark(omdMusicReportHandleRemark);
        omdMusicReport.setOmdAdminId(omdAdminId);
        if (!omdAdminService.updateMusicReport(omdMusicReport)) {
            throw new RuntimeException("更新音乐举报表失败");
        }
        // 新增管理员操作表
        if (omdMusicReportStatus == 1){
            if (!omdAdminService.addOmdOperationLog(helperUtil.setOmdOperationLog(omdAdminId,"review:music:report:pass","审核音乐举报通过",omdMusicReportId,"musicReport"))){
                throw new RuntimeException("新增管理员操作表失败");
            }

            // 下架音乐
            OmdMusicInfo omdMusicInfo = omdAdminService.findMusicInfoByMusicId(omdMusicReport.getOmdMusicInfoId());
            omdMusicInfo.setOmdMusicInfoStatus(2);
            omdMusicInfo.setOmdMusicInfoRemark("举报被管理员处理，下架音乐");
            if (!omdAdminService.updateMusicInfoStatus(omdMusicInfo.getOmdMusicInfoId(), omdMusicInfo.getOmdMusicInfoStatus(), omdMusicInfo.getOmdMusicInfoRemark())) {
                throw new RuntimeException("更新音乐信息状态失败");
            }

            // 新增管理员操作表
            if (!omdAdminService.addOmdOperationLog(helperUtil.setOmdOperationLog(omdAdminId,"review:music:fail","下架音乐", omdMusicInfo.getOmdMusicInfoId(),"music"))){
                throw new RuntimeException("新增管理员操作表失败");
            }

        }else {
            if (!omdAdminService.addOmdOperationLog(helperUtil.setOmdOperationLog(omdAdminId,"review:music:report:fail","审核音乐举报失败",omdMusicReportId,"musicReport"))){
                throw new RuntimeException("新增管理员操作表失败");
            }
        }
    }

    /**
     * 更新用户申诉状态
     * @param omdUserAppealId      申诉ID
     * @param omdUserAppealStatus  申诉状态
     * @param omdUserAppealResult  申诉结果
     * @param omdAdminId 当前用户ID
     */
    public void updateUserAppealStatus(Long omdUserAppealId, Integer omdUserAppealStatus, String omdUserAppealResult, Long omdAdminId) {
        OmdUserAppeal omdUserAppeal = omdAdminService.findUserAppealById(omdUserAppealId);
        if(omdUserAppeal == null){
            throw new RuntimeException("查询不到该申诉");
        }
        // 更新用户申诉表
        omdUserAppeal.setOmdUserAppealStatus(omdUserAppealStatus);
        omdUserAppeal.setOmdUserAppealResult(omdUserAppealResult);
        omdUserAppeal.setOmdAdminId(omdAdminId);
        if (!omdAdminService.updateUserAppeal(omdUserAppeal)) {
            throw new RuntimeException("更新用户申诉表失败");
        }
        // 新增管理员操作表
        if (omdUserAppealStatus == 1){
            if (!omdAdminService.addOmdOperationLog(helperUtil.setOmdOperationLog(omdAdminId,"user:appeal:pass","审核用户申诉通过",omdUserAppealId,"userAppeal"))){
                throw new RuntimeException("新增管理员操作表失败");
            }

            // 申诉通过之后，解冻用户
            OmdUser omdUser = omdUserService.getUserInfoByOmdUserId(omdUserAppeal.getOmdUserId());
            omdUser.setOmdUserStatus(1);
            omdUser.setOmdUserFreezeType(0);
            omdUser.setOmdUserFreezeEndTime(null);
            omdUser.setOmdUserRemark("申诉通过，解冻用户");
            if (!omdAdminService.updateUserStatus(omdUser.getOmdUserId(), omdUser.getOmdUserRemark(), omdUser.getOmdUserStatus(),
                    omdUser.getOmdUserFreezeType(), omdUser.getOmdUserFreezeEndTime())) {
                throw new RuntimeException("更新用户状态失败");
            }
            // 新增管理员操作表
            if (!omdAdminService.addOmdOperationLog(helperUtil.setOmdOperationLog(omdAdminId,"user:unfreeze","解冻用户账号", omdUser.getOmdUserId(),"user"))){
                throw new RuntimeException("新增管理员操作表失败");
            }
        }else {
            if (!omdAdminService.addOmdOperationLog(helperUtil.setOmdOperationLog(omdAdminId,"user:appeal:fail","审核用户申诉失败",omdUserAppealId,"userAppeal"))){
                throw new RuntimeException("新增管理员操作表失败");
            }
        }
    }

    /**
     * 更新音乐申诉状态
     * @param omdMusicAppealId      申诉ID
     * @param omdMusicAppealStatus  申诉状态
     * @param omdMusicAppealHandleRemark  申诉处理备注
     * @param omdAdminId 当前用户ID
     */
    public void updateMusicAppealStatus(Long omdMusicAppealId, Integer omdMusicAppealStatus, String omdMusicAppealHandleRemark, Long omdAdminId) {
        OmdMusicAppeal omdMusicAppeal = omdAdminService.findMusicAppealById(omdMusicAppealId);
        if(omdMusicAppeal == null){
            throw new RuntimeException("查询不到该申诉");
        }
        // 更新音乐申诉表
        omdMusicAppeal.setOmdMusicAppealStatus(omdMusicAppealStatus);
        omdMusicAppeal.setOmdMusicAppealHandleRemark(omdMusicAppealHandleRemark);
        omdMusicAppeal.setOmdAdminId(omdAdminId);
        if (!omdAdminService.updateMusicAppeal(omdMusicAppeal)) {
            throw new RuntimeException("更新音乐申诉表失败");
        }
        // 新增管理员操作表
        if (omdMusicAppealStatus == 1){

            if (!omdAdminService.addOmdOperationLog( helperUtil.setOmdOperationLog(omdAdminId,"music:appeal:pass","审核音乐申诉通过",omdMusicAppealId,"musicAppeal"))){
                throw new RuntimeException("新增管理员操作表失败");
            }

            // 申诉通过后，上架音乐
            OmdMusicInfo omdMusicInfo = omdAdminService.findMusicInfoByMusicId(omdMusicAppeal.getOmdMusicInfoId());
            omdMusicInfo.setOmdMusicInfoStatus(1);
            omdMusicInfo.setOmdMusicInfoRemark("申诉通过，上架音乐");
            if (!omdAdminService.updateMusicInfoStatus(omdMusicInfo.getOmdMusicInfoId(), omdMusicInfo.getOmdMusicInfoStatus(), omdMusicInfo.getOmdMusicInfoRemark())) {
                throw new RuntimeException("更新音乐信息状态失败");
            }

            // 新增管理员操作表
            if (!omdAdminService.addOmdOperationLog( helperUtil.setOmdOperationLog(omdAdminId,"review:music:pass","上架音乐",omdMusicInfo.getOmdMusicInfoId(),"music"))){
                throw new RuntimeException("新增管理员操作表失败");
            }

        }else {
            if (!omdAdminService.addOmdOperationLog( helperUtil.setOmdOperationLog(omdAdminId,"music:appeal:fail","审核音乐申诉失败",omdMusicAppealId,"musicAppeal"))){
                throw new RuntimeException("新增管理员操作表失败");
            }
        }
    }
}
