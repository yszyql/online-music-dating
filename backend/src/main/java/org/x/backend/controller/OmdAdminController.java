package org.x.backend.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.x.backend.pojo.*;
import org.x.backend.service.OmdAdminService;
import org.x.backend.service.OmdMusicService;
import org.x.backend.service.OmdPublicService;
import org.x.backend.service.OmdUserService;
import org.x.backend.service.impl.TransactionAdminService;
import org.x.backend.utils.HelperUtil;
import org.x.backend.utils.OperationTargetTypeUtil;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/admin")
@Validated
@Slf4j
public class OmdAdminController {

    @Autowired
    private OmdAdminService omdAdminService;

    @Autowired
    private OmdMusicService omdMusicService;

    @Autowired
    private TransactionAdminService transactionAdminService;

    @Autowired
    private HelperUtil helperUtil;
    @Autowired
    private OmdUserService omdUserService;
    @Autowired
    private OmdPublicService omdPublicService;

    /**
     * 获取管理员个人信息
     * @return 管理员信息
     */
    @GetMapping("getAdminInfo")
    public Result<OmdAdmin> getAdminInfo() {
        // 获取当前登录的用户信息
        return Result.success(omdAdminService.getAdminInfo(helperUtil.getCurrentUserId()));
    }

    /**
     * 更新管理员个人信息
     * @param omdAdmin 管理员信息
     * @return 是否更新成功
     */
    @PostMapping("updateAdminInfo")
    public Result<String> updateAdminInfo(@RequestBody @Validated OmdAdmin omdAdmin) {
        // 设置当前登录的用户ID
        omdAdmin.setOmdAdminId(helperUtil.getCurrentUserId());
        if(!omdAdminService.updateAdminInfo(omdAdmin)){
            return Result.error("更新失败");
        }
        return Result.success("更新成功");
    }

    /**
     * 获取所有用户信息
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 用户信息列表
     */
    @GetMapping("getAllUsers")
    public Result<PageBean<OmdUser>> getAllUsers(@RequestParam("pageNum") Integer pageNum,
                                                 @RequestParam("pageSize") Integer pageSize) {
        // 执行分页查询
        PageBean<OmdUser> omdUserInfoList = helperUtil.executePageQuery(
                pageNum,
                pageSize,
                () -> omdAdminService.getAllUsers()
        );
        // 返回正确的分页对象
        return Result.success(omdUserInfoList);
    }

    /**
     * 根据用户名获取用户信息
     * @param omdUserName 用户名
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 用户信息列表
     */
    @GetMapping("getUserByUsername")
    public Result<PageBean<OmdUser>> getUserByUsername(@RequestParam("omdUserName") String omdUserName,
                                                       @RequestParam("pageNum") Integer pageNum,
                                                       @RequestParam("pageSize") Integer pageSize) {
        // 执行分页查询
        PageBean<OmdUser> omdUserInfoList = helperUtil.executePageQuery(
                pageNum,
                pageSize,
                () -> omdAdminService.getUserByUsername(omdUserName)
        );
        return Result.success(omdUserInfoList);
    }

    /**
     * 更新用户状态
     * @param omdUserId 用户ID
     * @param omdUserRemark 用户备注
     * @param omdUserStatus 用户状态
     * @param omdUserFreezeType 用户冻结类型
     * @param omdUserFreezeEndTime 用户冻结结束时间
     * @return 是否更新成功
     */
    @PostMapping("updateUserStatus")
    public Result<String> updateUserStatus(@RequestParam("omdUserId") Long omdUserId,
                                           @RequestParam("omdUserFreezeType") Integer omdUserFreezeType,
                                           @RequestParam(value = "omdUserFreezeEndTime", required = false) Long omdUserFreezeEndTime,
                                           @RequestParam("omdUserRemark") String omdUserRemark,
                                           @RequestParam("omdUserStatus") Integer omdUserStatus) {
        Date endDate = omdUserFreezeEndTime != null ? new Date(omdUserFreezeEndTime) : null;
        transactionAdminService.updateUserStatus(omdUserId,omdUserRemark, omdUserStatus,omdUserFreezeType,endDate,helperUtil.getCurrentUserId());
        return Result.success("更新用户状态成功");
    }


    /**
     * 获取所有音乐信息
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 音乐信息
     */
    @GetMapping("/getAllMusicInfoList")
    public Result<PageBean<OmdMusicInfo>> getAllMusicInfoList(@RequestParam("pageNum") Integer pageNum,
                                                              @RequestParam("pageSize") Integer pageSize) {

        // 执行分页查询
        PageBean<OmdMusicInfo> musicInfoAllList = helperUtil.executePageQuery(
                pageNum,
                pageSize,
                () -> omdMusicService.getAllMusicInfoList()
        );

        // 返回正确的分页对象
        return Result.success(musicInfoAllList);
    }


    /**
     * 获取待审核的音乐信息
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 音乐信息列表
     */
    @GetMapping("getMusicInfoListByStatus")
    public Result<PageBean<OmdMusicInfo>> getMusicInfoListByStatus(@RequestParam("pageNum") Integer pageNum,
                                                                   @RequestParam("pageSize") Integer pageSize){
        // 执行分页查询
        PageBean<OmdMusicInfo> musicInfoStatusList = helperUtil.executePageQuery(
                pageNum,
                pageSize,
                () -> omdAdminService.getMusicInfoListByStatus()
        );
        // 返回正确的分页对象
        return Result.success(musicInfoStatusList);

    }

    /**
     * 审核音乐信息状态
     * @param omdMusicInfoId 音乐信息ID
     * @param omdMusicInfoStatus 音乐信息状态
     * @param omdMusicInfoRemark 音乐信息备注
     * @return 是否更新成功
     */
    @PostMapping("updateMusicInfoStatus")
    public Result<String> updateMusicInfoStatus(@RequestParam("omdMusicInfoId") Long omdMusicInfoId,
                                                @RequestParam("omdMusicInfoStatus") Integer omdMusicInfoStatus,
                                                @RequestParam("omdMusicInfoRemark") String omdMusicInfoRemark) {
        transactionAdminService.updateMusicInfoStatus(omdMusicInfoId, omdMusicInfoStatus,omdMusicInfoRemark, helperUtil.getCurrentUserId());

        return Result.success("审核音乐信息成功");
    }

    /**
     * 获取待审核的申请信息
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 申请信息列表
     */
    @GetMapping("getApplicationsListByStatus")
    public Result<PageBean<OmdApplications>> getApplicationsListByStatus(@RequestParam("pageNum") Integer pageNum,
                                                                         @RequestParam("pageSize") Integer pageSize){
        // 执行分页查询
        PageBean<OmdApplications> applicationsStatusList = helperUtil.executePageQuery(
                pageNum,
                pageSize,
                () -> omdAdminService.getApplicationsListByStatus()
        );
        // 返回正确的分页对象
        return Result.success(applicationsStatusList);
    }

    /**
     * 审核申请信息状态
     * @param omdApplicationsId 申请信息ID
     * @param omdApplicationsStatus 申请信息状态
     * @param omdApplicationsReviewRemark 申请信息备注
     * @return 是否更新成功
     */
    @PostMapping("updateApplicationsStatus")
    public Result<String> updateApplicationsStatus(@RequestParam("omdApplicationsId") Long omdApplicationsId,
                                                   @RequestParam("omdApplicationsStatus") Integer omdApplicationsStatus,
                                                   @RequestParam("omdApplicationsReviewRemark") String omdApplicationsReviewRemark) {
        transactionAdminService.updateApplicationsStatus(omdApplicationsId, omdApplicationsStatus, omdApplicationsReviewRemark, helperUtil.getCurrentUserId());
        return Result.success("审核歌手申请成功");
    }

    /**
     * 获取待审核的举报评论信息
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 评论信息列表
     */
    @GetMapping("getCommentReportListByStatus")
    public Result<PageBean<OmdCommentReport>> getCommentReportListByStatus(@RequestParam("pageNum") Integer pageNum,
                                                                           @RequestParam("pageSize") Integer pageSize){
        // 执行分页查询
        PageBean<OmdCommentReport> commentReportStatusList = helperUtil.executePageQuery(
                pageNum,
                pageSize,
                () -> omdAdminService.getCommentReportListByStatus()
        );

        // 返回正确的分页对象
        return Result.success(commentReportStatusList);
    }

    /**
     * 审核举报评论信息状态
     * @param omdMusicCommentId 被举报的评论ID
     * @param omdCommentReportStatus 举报评论信息状态
     * @param omdCommentReportRemark 举报评论信息备注
     * @return 是否更新成功
     */
    @PostMapping("updateCommentReportStatus")
    public Result<String> updateCommentReportStatus(@RequestParam("omdMusicCommentId") Long omdMusicCommentId,
                                                    @RequestParam("omdCommentReportStatus") Integer omdCommentReportStatus,
                                                    @RequestParam("omdCommentReportRemark") String omdCommentReportRemark) {
        transactionAdminService.updateCommentReportStatus(omdMusicCommentId, omdCommentReportStatus, omdCommentReportRemark, helperUtil.getCurrentUserId());
        return Result.success("审核评论举报成功");
    }

    /**
     * 获取用户反馈列表
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 用户反馈列表
     */
    @GetMapping("getFeedbackList")
    public Result<PageBean<OmdUserFeedback>> getFeedbackList(@RequestParam("pageNum") Integer pageNum,
                                                             @RequestParam("pageSize") Integer pageSize) {
        // 执行分页查询
        PageBean<OmdUserFeedback> omdUserFeedbackList = helperUtil.executePageQuery(
                pageNum,
                pageSize,
                () -> omdAdminService.getFeedbackListByStatus()
        );

        // 返回正确的分页对象
        return Result.success(omdUserFeedbackList);
    }

    /**
     * 更新用户反馈状态
     * @param omdUserFeedbackId 用户反馈ID
     * @param omdUserFeedbackResponse 用户反馈备注
     * @return 是否更新成功
     */
    @PostMapping("updateFeedbackStatus")
    public Result<String> updateFeedbackStatus(@RequestParam("omdUserFeedbackId") Long omdUserFeedbackId,
                                               @RequestParam("omdUserFeedbackResponse") String omdUserFeedbackResponse) {
        transactionAdminService.updateFeedbackStatus(omdUserFeedbackId, omdUserFeedbackResponse, helperUtil.getCurrentUserId());
        return Result.success("处理用户反馈成功");
    }

    /**
     * 获取操作日志列表
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param targetTypes 操作对象类型
     * @return 操作日志列表（Result包装类）
     */
    @GetMapping("getOperationLogList")
    public Result<PageBean<OmdOperationLogVO>> getOperationLogList(
            @RequestParam("pageNum") Integer pageNum,
            @RequestParam("pageSize") Integer pageSize,
            @RequestParam(required = false, value = "targetTypes") String targetTypes
    ) {
        // 1. 执行分页查询，获取PageBean
        PageBean<OmdOperationLogVO> pageBean = helperUtil.executePageQuery(
                pageNum,
                pageSize,
                () -> {
                    // 查询原始日志数据
                    List<OmdOperationLog> logList = targetTypes != null
                            ? omdAdminService.getOperationLogListByType(targetTypes)
                            : omdAdminService.getOperationLogList();

                    // 转换为VO对象
                    return logList.stream().map(logs -> {
                        OmdOperationLogVO vo = new OmdOperationLogVO();
                        BeanUtils.copyProperties(logs, vo);

                        // 设置默认值
                        vo.setTargetName("未知对象");
                        vo.setTargetDetail("无详情");
                        vo.setAdminName("未知管理员");

                        // 关联业务表数据
                        String targetType = logs.getOmdOperationLogTargetType();
                        Long targetId = logs.getOmdOperationLogTargetId();

                        if (targetType != null && targetId != null) {
                            try {
                                switch (targetType) {
                                    case OperationTargetTypeUtil.USER:
                                        processUserTarget(vo, targetId);
                                        break;
                                    case OperationTargetTypeUtil.MUSIC:
                                        processMusicTarget(vo, targetId);
                                        break;
                                    case OperationTargetTypeUtil.COMMENT:
                                        processCommentTarget(vo, targetId);
                                        break;
                                    case OperationTargetTypeUtil.APPLICATIONS:
                                        processApplicationTarget(vo, targetId);
                                        break;
                                    case OperationTargetTypeUtil.FEEDBACK:
                                        processFeedbackTarget(vo, targetId);
                                        break;
                                    case OperationTargetTypeUtil.ADMIN:
                                        processAdminTarget(vo, targetId);
                                        break;
                                    case OperationTargetTypeUtil.USER_REPORT:
                                        processUserReportTarget(vo, targetId);
                                        break;
                                    case OperationTargetTypeUtil.USER_APPEAL:
                                        processUserAppealTarget(vo, targetId);
                                        break;
                                    case OperationTargetTypeUtil.MUSIC_REPORT:
                                        processMusicReportTarget(vo, targetId);
                                        break;
                                    case OperationTargetTypeUtil.MUSIC_APPEAL:
                                        processMusicAppealTarget(vo, targetId);
                                        break;
                                    default:
                                        log.warn("未知操作对象类型: {}", targetType);
                                        break;
                                }
                            } catch (Exception e) {
                                log.error("处理操作日志关联数据失败, targetType={}, targetId={}",
                                        targetType, targetId, e);
                                vo.setTargetName("处理失败");
                                vo.setTargetDetail("系统错误，请联系管理员");
                            }
                        }

                        // 关联管理员信息
                        processAdminInfo(vo, logs.getOmdAdminId());

                        return vo;
                    }).collect(Collectors.toList());
                }
        );

        // 2. 将PageBean包装成Result返回（核心修复）
        return Result.success(pageBean);
    }

    // 以下是各类型的处理方法，封装空值检查逻辑
    private void processUserTarget(OmdOperationLogVO vo, Long targetId) {
        OmdUser user = omdUserService.findByUserId(targetId);
        if (user == null) {
            log.warn("用户不存在, userId={}", targetId);
            return;
        }
        vo.setTargetName(user.getOmdUserName() != null ? user.getOmdUserName() : "未知用户");
        vo.setTargetDetail("审核备注：" + (user.getOmdUserRemark() != null ? user.getOmdUserRemark() : "无"));
    }

    private void processMusicTarget(OmdOperationLogVO vo, Long targetId) {
        OmdMusicInfo music = omdMusicService.getMusicInfoByOmdMusicInfoId(targetId);
        if (music == null) {
            log.warn("音乐不存在, musicId={}", targetId);
            return;
        }
        vo.setTargetName(music.getOmdMusicInfoName() != null ? music.getOmdMusicInfoName() : "未知音乐");

        OmdSinger singer = music.getOmdSinger();
        vo.setTargetDetail("歌手：" + (singer != null && singer.getOmdSingerName() != null
                ? singer.getOmdSingerName() : "未知歌手"));
    }

    private void processCommentTarget(OmdOperationLogVO vo, Long targetId) {
        OmdCommentReport comment = omdAdminService.getCommentReportByCommentId(targetId);
        if (comment == null) {
            log.warn("评论举报不存在, commentId={}", targetId);
            return;
        }

        vo.setTargetName("举报描述：" + (comment.getOmdCommentReportDescription() != null
                ? comment.getOmdCommentReportDescription() : "无描述"));

        OmdMusicComment musicComment = comment.getOmdMusicComment();
        vo.setTargetDetail("被举报的评论内容：" + (musicComment != null && musicComment.getOmdMusicCommentContent() != null
                ? musicComment.getOmdMusicCommentContent() : "内容已删除"));
    }

    private void processApplicationTarget(OmdOperationLogVO vo, Long targetId) {
        OmdApplications application = omdUserService.getApplicationsByApplicationsId(targetId);
        if (application == null) {
            log.warn("申请不存在, applicationId={}", targetId);
            return;
        }
        vo.setTargetName("歌手姓名：" + (application.getOmdSingerName() != null
                ? application.getOmdSingerName() : "未知歌手"));
        vo.setTargetDetail("审核结果：" + (application.getOmdApplicationsReviewRemark() != null
                ? application.getOmdApplicationsReviewRemark() : "无结果"));
    }

    private void processFeedbackTarget(OmdOperationLogVO vo, Long targetId) {
        OmdUserFeedback feedback = omdUserService.getFeedbackByFeedbackId(targetId);
        if (feedback == null) {
            log.warn("反馈不存在, feedbackId={}", targetId);
            return;
        }
        vo.setTargetName("反馈类型：" + (feedback.getOmdUserFeedbackType() != null
                ? feedback.getOmdUserFeedbackType() : "未知类型"));
        vo.setTargetDetail("反馈内容：" + (feedback.getOmdUserFeedbackContent() != null
                ? feedback.getOmdUserFeedbackContent() : "无内容"));
    }

    private void processAdminTarget(OmdOperationLogVO vo, Long targetId) {
        OmdAdmin admin = omdAdminService.getAdminInfo(targetId);
        if (admin == null) {
            log.warn("管理员不存在, adminId={}", targetId);
            return;
        }
        vo.setTargetName("管理员姓名：" + (admin.getOmdAdminName() != null
                ? admin.getOmdAdminName() : "未知管理员"));
        vo.setTargetDetail("管理员身份：" + (admin.getOmdAdminRole() != null
                ? (admin.getOmdAdminRole() == 1 ? "超级管理员" : "普通管理员") : "未知身份"));
    }

    private void processUserReportTarget(OmdOperationLogVO vo, Long targetId) {
        OmdUserReport userReport = omdAdminService.findUserReportById(targetId);
        if (userReport == null) {
            log.warn("用户举报不存在, reportId={}", targetId);
            return;
        }

        // 处理被举报用户
        OmdUser reportedUser = userReport.getOmdReportedUser();
        String userName = reportedUser != null && reportedUser.getOmdUserName() != null
                ? reportedUser.getOmdUserName() : "未知用户";
        vo.setTargetName("用户姓名：" + userName);

        // 处理举报原因
        String reportReason = userReport.getOmdUserReportReason();
        vo.setTargetDetail("举报原因：" + (reportReason != null ? reportReason : "无"));
    }

    private void processUserAppealTarget(OmdOperationLogVO vo, Long targetId) {
        OmdUserAppeal userAppeal = omdAdminService.findUserAppealById(targetId);
        if (userAppeal == null) {
            log.warn("用户申诉不存在, appealId={}", targetId);
            return;
        }

        // 处理申诉用户
        OmdUser appealUser = userAppeal.getOmdAppealUser();
        String userName = appealUser != null && appealUser.getOmdUserName() != null
                ? appealUser.getOmdUserName() : "未知用户";
        vo.setTargetName("用户姓名：" + userName);

        // 处理申诉原因
        String appealReason = userAppeal.getOmdUserAppealReason();
        vo.setTargetDetail("申诉原因：" + (appealReason != null ? appealReason : "无"));
    }

    private void processMusicReportTarget(OmdOperationLogVO vo, Long targetId) {
        OmdMusicReport musicReport = omdAdminService.findMusicReportById(targetId);
        if (musicReport == null) {
            log.warn("音乐举报不存在, reportId={}", targetId);
            return;
        }

        // 处理被举报音乐
        OmdMusicInfo reportedMusic = musicReport.getOmdReportedMusic();
        String musicName = reportedMusic != null && reportedMusic.getOmdMusicInfoName() != null
                ? reportedMusic.getOmdMusicInfoName() : "未知音乐";
        vo.setTargetName("音乐名称：" + musicName);

        // 处理举报原因
        String reportReason = musicReport.getOmdMusicReportReason();
        vo.setTargetDetail("举报原因：" + (reportReason != null ? reportReason : "无"));
    }

    private void processMusicAppealTarget(OmdOperationLogVO vo, Long targetId) {
        OmdMusicAppeal musicAppeal = omdAdminService.findMusicAppealById(targetId);
        if (musicAppeal == null) {
            log.warn("音乐申诉不存在, appealId={}", targetId);
            return;
        }

        // 处理申诉音乐
        OmdMusicInfo appealMusic = musicAppeal.getOmdAppealMusicInfo();
        String musicName = appealMusic != null && appealMusic.getOmdMusicInfoName() != null
                ? appealMusic.getOmdMusicInfoName() : "未知音乐";
        vo.setTargetName("音乐名称：" + musicName);

        // 处理申诉原因
        String appealReason = musicAppeal.getOmdMusicAppealReason();
        vo.setTargetDetail("申诉原因：" + (appealReason != null ? appealReason : "无"));
    }

    private void processAdminInfo(OmdOperationLogVO vo, Long adminId) {
        if (adminId == null) {
            return;
        }
        OmdAdmin admin = omdAdminService.getAdminInfo(adminId);
        if (admin != null && admin.getOmdAdminName() != null) {
            vo.setAdminName(admin.getOmdAdminName());
        }
    }

    /**
     * 获取所有管理员信息
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 管理员信息列表
     */
    @GetMapping("getAllAdminList")
    public Result<PageBean<OmdAdmin>> getAllAdminList(@RequestParam("pageNum") Integer pageNum,
                                                      @RequestParam("pageSize") Integer pageSize) {
        // 执行分页查询
        PageBean<OmdAdmin> omdAdminList = helperUtil.executePageQuery(
                pageNum,
                pageSize,
                () -> omdAdminService.getAllAdminList()
        );
        return Result.success(omdAdminList);
    }

    /**
     * 新增管理员
     * @param omdAdmin 管理员信息
     * @return 是否新增成功
     */
    @PostMapping("insertAdmin")
    public Result<String> insertAdmin(@RequestBody OmdAdmin omdAdmin) {
        transactionAdminService.insertAdmin(omdAdmin, helperUtil.getCurrentUserId());
        return Result.success("新增管理员成功");
    }

    /**
     * 更新管理员信息
     * @param omdAdminId 管理员ID
     * @return 是否更新成功
     */
    @PostMapping("updateAdminStatus")
    public Result<String> updateAdminStatus(@RequestParam("omdAdminId") Long omdAdminId,
                                            @RequestParam("omdAdminStatus") Integer omdAdminStatus) {
        transactionAdminService.updateAdminStatus(omdAdminId,omdAdminStatus, helperUtil.getCurrentUserId());
        return Result.success("更新管理员成功");
    }

    /**
     * 获取所有用户举报列表
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 用户举报列表
     */
    @GetMapping("getUserReportList")
    public Result<PageBean<OmdUserReport>> getUserReportList(@RequestParam("pageNum") Integer pageNum,
                                                             @RequestParam("pageSize") Integer pageSize) {
        PageBean<OmdUserReport> userReportList = helperUtil.executePageQuery(
                pageNum,
                pageSize,
                () -> omdAdminService.getUserReportList()
        );
        return Result.success(userReportList);
    }

    /**
     * 更新用户举报状态
     * @param omdUserReportId 用户举报ID
     * @param omdUserReportStatus 用户举报状态
     * @param omdUserReportHandleRemark 用户举报处理备注
     * @return 是否更新成功
     */
    @PostMapping("updateUserReportStatus")
    public Result<String> updateUserReportStatus(@RequestParam("omdUserReportId") Long omdUserReportId,
                                                 @RequestParam("omdUserReportStatus") Integer omdUserReportStatus,
                                                 @RequestParam("omdUserReportHandleRemark") String omdUserReportHandleRemark) {
        transactionAdminService.updateUserReportStatus(omdUserReportId, omdUserReportStatus,omdUserReportHandleRemark, helperUtil.getCurrentUserId());
        return Result.success("更新用户举报成功");
    }

    /**
     * 获取所有音乐举报列表
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 音乐举报列表
     */
    @GetMapping("getMusicReportList")
    public Result<PageBean<OmdMusicReport>> getMusicReportList(@RequestParam("pageNum") Integer pageNum,
                                                             @RequestParam("pageSize") Integer pageSize) {
        PageBean<OmdMusicReport> musicReportList = helperUtil.executePageQuery(
                pageNum,
                pageSize,
                () -> omdAdminService.getMusicReportList()
        );
        return Result.success(musicReportList);
    }

    /**
     * 更新音乐举报状态
     * @param omdMusicReportId 音乐举报ID
     * @param omdMusicReportStatus 音乐举报状态
     * @param omdMusicReportHandleRemark 音乐举报处理备注
     * @return 是否更新成功
     */
    @PostMapping("updateMusicReportStatus")
    public Result<String> updateMusicReportStatus(@RequestParam("omdMusicReportId") Long omdMusicReportId,
                                                  @RequestParam("omdMusicReportStatus") Integer omdMusicReportStatus,
                                                  @RequestParam("omdMusicReportHandleRemark") String omdMusicReportHandleRemark) {
        transactionAdminService.updateMusicReportStatus(omdMusicReportId, omdMusicReportStatus,omdMusicReportHandleRemark, helperUtil.getCurrentUserId());
        return Result.success("更新音乐举报成功");
    }

    /**
     * 获取用户申诉列表
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 用户申诉列表
     */
    @GetMapping("getUserAppealList")
    public Result<PageBean<OmdUserAppeal>> getUserAppealList(@RequestParam("pageNum") Integer pageNum,
                                                             @RequestParam("pageSize") Integer pageSize) {
        PageBean<OmdUserAppeal> userAppealList = helperUtil.executePageQuery(
                pageNum,
                pageSize,
                () -> omdAdminService.getUserAppealList()
        );
        return Result.success(userAppealList);
    }

    /**
     * 更新用户申诉状态
     * @param omdUserAppealId 用户申诉ID
     * @param omdUserAppealStatus 用户申诉状态
     * @param omdUserAppealResult 用户申诉处理备注
     * @return 是否更新成功
     */
    @PostMapping("updateUserAppealStatus")
    public Result<String> updateUserAppealStatus(@RequestParam("omdUserAppealId") Long omdUserAppealId,
                                                  @RequestParam("omdUserAppealStatus") Integer omdUserAppealStatus,
                                                  @RequestParam("omdUserAppealResult") String omdUserAppealResult) {
        transactionAdminService.updateUserAppealStatus(omdUserAppealId, omdUserAppealStatus,omdUserAppealResult, helperUtil.getCurrentUserId());
        return Result.success("更新用户申诉成功");
    }

    /**
     * 获取音乐申诉列表
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 音乐申诉列表
     */
    @GetMapping("getMusicAppealList")
    public Result<PageBean<OmdMusicAppeal>> getMusicAppealList(@RequestParam("pageNum") Integer pageNum,
                                                               @RequestParam("pageSize") Integer pageSize) {
        PageBean<OmdMusicAppeal> musicAppealList = helperUtil.executePageQuery(
                pageNum,
                pageSize,
                () -> omdAdminService.getMusicAppealList()
        );
        return Result.success(musicAppealList);
    }

    /**
     * 更新音乐申诉状态
     * @param omdMusicAppealId 音乐申诉ID
     * @param omdMusicAppealStatus 音乐申诉状态
     * @param omdMusicAppealHandleRemark 音乐申诉处理备注
     * @return 是否更新成功
     */
    @PostMapping("updateMusicAppealStatus")
    public Result<String> updateMusicAppealStatus(@RequestParam("omdMusicAppealId") Long omdMusicAppealId,
                                                  @RequestParam("omdMusicAppealStatus") Integer omdMusicAppealStatus,
                                                  @RequestParam("omdMusicAppealHandleRemark") String omdMusicAppealHandleRemark) {
        transactionAdminService.updateMusicAppealStatus(omdMusicAppealId, omdMusicAppealStatus,omdMusicAppealHandleRemark, helperUtil.getCurrentUserId());
        return Result.success("更新音乐申诉成功");
    }
}
