package org.x.backend.mapper;

import org.apache.ibatis.annotations.*;
import org.x.backend.pojo.*;

import java.util.Date;
import java.util.List;

@Mapper
public interface OmdAdminMapper {

    /**
     * 获取管理员信息
     * @param omdAdminId 当前用户ID
     * @return 管理员信息
     */
    @Select("select * from tb_omd_admin where omd_admin_id = #{omdAdminId}")
    OmdAdmin getAdminInfo(Long omdAdminId);

    /**
     * 更新管理员信息
     * @param omdAdmin 管理员信息
     * @return 是否更新成功
     */
    int updateAdminInfo(OmdAdmin omdAdmin);

    /**
     * 获取待审核的音乐信息
     * @return 音乐信息列表
     */
    @Select("select * from tb_omd_music_info where omd_music_info_status = 0")
    List<OmdMusicInfo> getMusicInfoListByStatus();

    /**
     * 获取待审核的申请信息
     * @return 申请信息列表
     */
    @Select("select * from tb_omd_applications where omd_applications_status = 0")
    List<OmdApplications> getApplicationsListByStatus();

    /**
     * 审核音乐信息状态
     * @param omdMusicInfoId 音乐信息ID
     * @param omdMusicInfoStatus 音乐信息状态
     * @param omdMusicInfoRemark 音乐信息备注
     * @return 是否更新成功
     */
    @Update("update tb_omd_music_info set omd_music_info_status = #{omdMusicInfoStatus},omd_music_info_remark = #{omdMusicInfoRemark}" +
            " where omd_music_info_id = #{omdMusicInfoId}")
    int updateMusicInfoStatus(Long omdMusicInfoId, Integer omdMusicInfoStatus,String omdMusicInfoRemark);

    /**
     * 新增管理员操作表
     * @param omdOperationLog 管理员操作表
     * @return 是否新增成功
     * */
    @Insert("insert into tb_omd_operation_log (omd_admin_id, omd_operation_log_type, omd_operation_log_desc" +
            ", omd_operation_log_target_id, omd_operation_log_target_type) " +
            "values (#{omdAdminId}, #{omdOperationLogType}, #{omdOperationLogDesc}, #{omdOperationLogTargetId}, #{omdOperationLogTargetType})")
    int addOmdOperationLog(OmdOperationLog omdOperationLog);

    /**
     * 获取申请信息详情
     * @param omdApplicationsId 歌手申请ID
     * @return 申请信息详情
     */
    @Select("select * from tb_omd_applications where omd_applications_id = #{omdApplicationsId}")
    OmdApplications getApplicationsById(Long omdApplicationsId);

    /**
     * 更新申请信息状态
     * @param omdApplications 申请信息
     * @return 是否更新成功
     */
    @Update("update tb_omd_applications set omd_applications_status = #{omdApplicationsStatus}, " +
            "omd_applications_review_remark = #{omdApplicationsReviewRemark}, omd_admin_name = #{omdAdminName}" +
            " , omd_applications_review_time = now() where omd_applications_id = #{omdApplicationsId}")
    int updateApplications(OmdApplications omdApplications);

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
    @Update("update tb_omd_comment_report set omd_comment_report_status = #{omdCommentReportStatus}, " +
            "omd_comment_report_remark = #{omdCommentReportRemark}, omd_admin_name = #{omdAdminName}," +
            " omd_comment_report_handle_time = now() where omd_music_comment_id = #{omdMusicCommentId}")
    int updateCommentReport(OmdCommentReport omdCommentReport);

    /**
     * 根据评论ID获取音乐评论信息
     * @return 音乐评论信息
     */
    @Select("select * from tb_omd_music_comment where omd_music_comment_id = #{omdMusicCommentId}")
    int findMusicCommentById(Long omdMusicCommentId);

    /**
     * 获取用户反馈信息列表
     * @return 用户反馈信息列表
     */
    @Select("select * from tb_omd_user_feedback where omd_user_feedback_status = 0")
    List<OmdUserFeedback> getFeedbackListByStatus();

    /**
     * 根据用户反馈ID获取用户反馈信息
     * @param omdUserFeedbackId 用户反馈ID
     * @return 用户反馈信息
     */
    @Select("select * from tb_omd_user_feedback where omd_user_feedback_id = #{omdUserFeedbackId}")
    OmdUserFeedback findUserFeedbackById(Long omdUserFeedbackId);

    /**
     * 更新用户反馈信息状态
     * @param omdUserFeedback 用户反馈信息
     * @return 是否更新成功
     */
    @Update("update tb_omd_user_feedback set omd_user_feedback_status = #{omdUserFeedbackStatus}, " +
            "omd_user_feedback_response = #{omdUserFeedbackResponse}, omd_user_feedback_update_time = now()" +
            " where omd_user_feedback_id = #{omdUserFeedbackId}")
    int updateUserFeedback(OmdUserFeedback omdUserFeedback);

    /**
     * 获取所有用户信息
     * @return 用户信息列表
     */
    @Select("select * from tb_omd_user")
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
    @Update("update tb_omd_user set omd_user_status = #{omdUserStatus}, omd_user_remark = #{omdUserRemark}," +
            " omd_user_freeze_type = #{omdUserFreezeType}, omd_user_freeze_end_time = #{omdUserFreezeEndTime} where omd_user_id = #{omdUserId}")
    int updateUserStatus(Long omdUserId, String omdUserRemark, Integer omdUserStatus, Integer omdUserFreezeType, Date omdUserFreezeEndTime);

    /**
     * 获取所有操作日志
     * @return 操作日志列表
     */
    List<OmdOperationLog> getOperationLogList();


    /**
     * 根据用户名获取用户信息
     * @param omdUserName 用户名
     * @return 用户信息列表
     */
    @Select("select * from tb_omd_user where omd_user_name like concat('%', #{omdUserName}, '%')")
    List<OmdUser> getUserByUsername(String omdUserName);

    /**
     * 获取所有管理员信息
     * @return 管理员信息列表
     */
    @Select("select * from tb_omd_admin")
    List<OmdAdmin> getAllAdminList();

    /**
     * 新增管理员信息
     * @param omdAdmin 管理员信息
     * @return 是否新增成功
     */
    @Insert("insert into tb_omd_admin (omd_admin_name, omd_admin_password, omd_admin_email, omd_admin_phone) " +
            "values (#{omdAdminName}, #{omdAdminPassword}, #{omdAdminEmail}, #{omdAdminPhone})")
    int insertAdmin(OmdAdmin omdAdmin);

    /**
     * 更新管理员信息
     * @param omdAdminId 管理员信息
     * @param omdAdminStatus 管理员状态
     * @return 是否更新成功
     */
    @Update("update tb_omd_admin set omd_admin_update_time = now() , omd_admin_status = #{omdAdminStatus} where omd_admin_id = #{omdAdminId}")
    int updateAdminStatus(Long omdAdminId,Integer omdAdminStatus);

    /**
     * 新增用户信息
     * @param omdUser 用户信息
     * @return 是否新增成功
     */
    @Insert("insert into tb_omd_user (omd_user_name, omd_user_password, omd_user_email, omd_user_phone) " +
            "values (#{omdUserName}, #{omdUserPassword}, #{omdUserEmail}, #{omdUserPhone})")
    int insertUser(OmdUser omdUser);

    /**
     * 删除评论
     * @param omdMusicCommentId 评论ID
     * @return 是否删除成功
     */
    @Delete("delete from tb_omd_music_comment where omd_music_comment_id = #{omdMusicCommentId}")
    int deleteMusicComment(Long omdMusicCommentId);

    /**
     * 更新用户角色
     * @param omdUserId 用户ID
     * @param roleSinger 角色
     * @return 是否更新成功
     */
    @Update("update tb_omd_user_role set  omd_role_code = #{roleSinger} where omd_user_id = #{omdUserId}")
    int updateUserRole(long omdUserId, String roleSinger);

    /**
     * 新增歌手信息
     * @param omdSinger 歌手信息
     * @return 是否新增成功
     */
    @Insert("insert into tb_omd_singer (omd_user_id, omd_singer_name, omd_singer_introduction, omd_singer_genre) " +
            "values (#{omdUserId}, #{omdSingerName}, #{omdSingerIntroduction}, #{omdSingerGenre})")
    int insertSinger(OmdSinger omdSinger);

    /**
     * 根据用户ID获取歌手信息
     * @param omdUserId 用户ID
     * @return 歌手信息
     */
    @Select("select * from tb_omd_singer where omd_user_id = #{omdUserId}")
    OmdSinger getSingerByUserId(Long omdUserId);

    /**
     * 更新歌手状态
     * @param omdUserId 用户ID
     * @param omdSingerStatus 歌手状态
     * @return 是否更新成功
     */
    @Update("update tb_omd_singer set omd_singer_update_time = now(), omd_singer_status = #{omdSingerStatus} where omd_user_id = #{omdUserId}")
    void updateSingerStatus(Long omdUserId, Integer omdSingerStatus);

    /**
     * 更新音乐状态
     * @param omdUserId 用户ID
     * @param omdMusicInfoStatus 音乐状态
     * @param omdMusicInfoRemark 音乐状态备注
     * @return 是否更新成功
     */
    @Update("update tb_omd_music_info set omd_music_info_update_time = now(), omd_music_info_status = #{omdMusicInfoStatus}" +
            ",omd_music_info_remark = #{omdMusicInfoRemark} where omd_user_id = #{omdUserId}")
    void updateMusicStatusBySingerId(Long omdUserId, Integer omdMusicInfoStatus,String omdMusicInfoRemark);

    /**
     * 新增用户角色
     * @param omdUserRole 用户角色
     * @return 是否新增成功
     */
    @Insert("insert into tb_omd_user_role (omd_user_id, omd_user_role) " +
            "values (#{omdUserId}, #{omdUserRole})")
    int insertUserRole(OmdUserRole omdUserRole);

    /**
     * 根据类型获取操作日志
     * @param targetTypes 类型
     * @return 操作日志列表
     */
    List<OmdOperationLog> getOperationLogListByType(String targetTypes);

    /**
     * 获取所有用户举报信息
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
    @Update("update tb_omd_user_report set omd_user_report_status = #{omdUserReportStatus}, " +
            "omd_user_report_handle_remark = #{omdUserReportHandleRemark}, omd_admin_id = #{omdAdminId}," +
            " omd_user_report_handle_time = now() where omd_user_report_id = #{omdUserReportId}")
    int updateUserReport(OmdUserReport omdUserReport);

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
    @Update("update tb_omd_music_report set omd_music_report_status = #{omdMusicReportStatus}, " +
            "omd_music_report_handle_remark = #{omdMusicReportHandleRemark}, omd_admin_id = #{omdAdminId}," +
            " omd_music_report_handle_time = now() where omd_music_report_id = #{omdMusicReportId}")
    int updateMusicReport(OmdMusicReport omdMusicReport);

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
    @Update("update tb_omd_user_appeal set omd_user_appeal_status = #{omdUserAppealStatus}, " +
            "omd_user_appeal_result = #{omdUserAppealResult}, omd_admin_id = #{omdAdminId}," +
            " omd_user_appeal_handle_time = now() where omd_user_appeal_id = #{omdUserAppealId}")
    int updateUserAppeal(OmdUserAppeal omdUserAppeal);

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
    @Update("update tb_omd_music_appeal set omd_music_appeal_status = #{omdMusicAppealStatus}, " +
            "omd_music_appeal_handle_remark = #{omdMusicAppealHandleRemark}, omd_admin_id = #{omdAdminId}," +
            " omd_music_appeal_handle_time = now() where omd_music_appeal_id = #{omdMusicAppealId}")
    int updateMusicAppeal(OmdMusicAppeal omdMusicAppeal);

    /**
     * 根据音乐ID获取音乐信息
     * @param omdMusicInfoId 音乐ID
     * @return 音乐信息
     */
    @Select("select * from tb_omd_music_info where omd_music_info_id = #{omdMusicInfoId}")
    OmdMusicInfo findMusicInfoByMusicId(Long omdMusicInfoId);

    /**
     * 根据评论ID获取评论举报信息
     * @param targetId 评论ID
     * @return 评论举报信息
     */
    OmdCommentReport getCommentReportByCommentId(Long targetId);
}
