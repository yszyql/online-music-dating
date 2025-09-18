package org.x.backend.mapper;

import org.apache.ibatis.annotations.*;
import org.x.backend.pojo.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface OmdUserMapper {

    /**
     * 根据ID查询用户
     * @param omdUserId 用户ID
     * @return User
     */
    @Select("select * from tb_omd_user where omd_user_id = #{omdUserId}")
    OmdUser findById(Long omdUserId);

    /**
     * 根据用户名查询用户
     * @param omdUserName 用户名
     * @return User
     */
    @Select("select * from tb_omd_user where omd_user_name = #{omdUserName}")
    OmdUser findByUsername(String omdUserName);

    /**
     * 根据手机号查询用户
     * @param omdUserPhone 手机号
     * @return
     */
    @Select("select * from tb_omd_user where omd_user_phone = #{omdUserPhone}")
    OmdUser findByPhone(String omdUserPhone);

    /**
     * 注册用户
     * @param omdUser 用户信息
     */
    void register(OmdUser omdUser);

    /**
     * 根据用户名或手机号查询用户
     * @param identifier 用户名或手机号
     * @return User
     */
    @Select("select * from tb_omd_user where omd_user_name = #{identifier} or omd_user_phone = #{identifier}")
    OmdUser findByUsernameOrPhone(@Param("identifier") String identifier);

    /**
     * 根据用户ID查询用户信息及其角色
     * @param omdUserId 当前用户ID
     * @return 用户信息及其角色
     */
    OmdUser findOmdUserByIdWithRoles(Long omdUserId);

    /**
     * 更新用户信息
     * @param omdUser 用户信息
     */
    void updateUserInfo(OmdUser omdUser);

    /**
     * 更新密码
     * @param omdUserId 用户ID
     * @param newPassword 新密码
     * @return
     */
    @Update("update tb_omd_user set omd_user_password = #{newPassword}, omd_user_update_time = now() where omd_user_id = #{omdUserId}")
    boolean updatePassword(Long omdUserId, String newPassword);

    /**
     * 申请成为歌手
     * @param omdApplications 申请信息
     */
    @Insert("insert into tb_omd_applications(omd_user_id, omd_singer_name, omd_applications_introduction," +
            " omd_applications_genre, omd_applications_sing_sample) values (#{omdUserId}, #{omdSingerName}, #{omdApplicationsIntroduction}," +
            "#{omdApplicationsGenre}, #{omdApplicationsSingSample})")
    void applicationSinger(OmdApplications omdApplications);

    /**
     * 根据用户ID查询申请信息
     * @param omdUserId 用户ID
     * @return 申请信息
     */
    @Select("select * from tb_omd_applications where omd_user_id = #{omdUserId}")
    OmdApplications findApplicationByOmdUserId(Long omdUserId);

    /**
     * 根据音乐评论的ID查询音乐评论
     * @param omdMusicCommentId 音乐评论ID
     * @return 音乐评论
     */
    @Select("select * from tb_omd_music_comment where omd_music_comment_id = #{omdMusicCommentId}")
    OmdMusicComment findCommentByOmdMusicCommentId(Long omdMusicCommentId);

    /**
     * 插入音乐评论
     * @param omdMusicComment 音乐评论
     * @return
     */
    @Insert("insert into tb_omd_music_comment( omd_music_info_id, omd_user_id, omd_music_comment_content, omd_music_comment_parent_id) " +
            "values ( #{omdMusicInfoId}, #{omdUserId}, #{omdMusicCommentContent}, #{omdMusicCommentParentId})")
    boolean insertMusicComment(OmdMusicComment omdMusicComment);

    /**
     * 根据用户ID和评论ID删除音乐评论
     * @param omdMusicCommentId 音乐评论ID
     * @param omdUserId 当前用户ID
     * @return
     */
    @Delete("delete from tb_omd_music_comment where omd_music_comment_id = #{omdMusicCommentId} and omd_user_id = #{omdUserId}")
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
    @Update("update tb_omd_music_comment set omd_music_comment_like_count = omd_music_comment_like_count + 1" +
            " where omd_music_comment_id = #{omdMusicCommentId}")
    boolean likeMusicComment(Long omdMusicCommentId);

    /**
     * 插入音乐评论举报
     * @param omdCommentReport 音乐评论举报
     * @return 是否插入成功
     */
    @Insert("insert into tb_omd_comment_report(omd_music_comment_id, omd_user_id, omd_comment_report_reason, omd_comment_report_description) " +
            "values (#{omdMusicCommentId}, #{omdUserId}, #{omdCommentReportReason}, #{omdCommentReportDescription})")
    boolean insertOmdCommentReport(OmdCommentReport omdCommentReport);

    /**
     * 检查用户是否已经点赞过该评论
     * @param omdMusicCommentId 音乐评论ID
     * @param omdUserId 当前用户ID
     * @return 是否点赞过
     */
    @Select("select exists(select 1 from tb_omd_comment_like where omd_music_comment_id = #{omdMusicCommentId} and omd_user_id = #{omdUserId})")
    boolean hasLikedMusicComment(Long omdMusicCommentId, Long omdUserId);

    /**
     * 取消点赞
     * @param omdMusicCommentId 音乐评论ID
     * @return 是否取消点赞成功
     */
    @Update("update tb_omd_music_comment set omd_music_comment_like_count = omd_music_comment_like_count - 1 " +
            "where omd_music_comment_id = #{omdMusicCommentId} and omd_music_comment_like_count > 0")
    boolean unlikeMusicComment(Long omdMusicCommentId);

    /**
     * 删除评论点赞记录
     * @param omdMusicCommentId 音乐评论ID
     * @param omdUserId 当前用户ID
     * @return 是否删除成功
     */
    @Delete("delete from tb_omd_comment_like where omd_music_comment_id = #{omdMusicCommentId} and omd_user_id = #{omdUserId}")
    boolean deleteCommentLike(Long omdMusicCommentId, Long omdUserId);

    /**
     * 插入评论点赞记录
     * @param omdMusicCommentId 音乐评论ID
     * @param omdUserId 当前用户ID
     * @return 是否插入成功
     */
    @Insert("insert into tb_omd_comment_like(omd_music_comment_id, omd_user_id) values (#{omdMusicCommentId}, #{omdUserId})")
    boolean insertCommentLike(Long omdMusicCommentId, Long omdUserId);

    /**
     * 插入用户反馈
     * @param omdUserFeedback 用户反馈
     * @return 是否插入成功
     */
    @Insert("insert into tb_omd_user_feedback(omd_user_id,omd_user_feedback_type, omd_user_feedback_content,omd_user_feedback_contact)" +
            " values (#{omdUserId},#{omdUserFeedbackType}, #{omdUserFeedbackContent}, #{omdUserFeedbackContact})")
    boolean insertOmdFeedback(OmdUserFeedback omdUserFeedback);

    /**
     * 检查用户是否已经举报过该评论
     * @param omdMusicCommentIdList 音乐评论ID
     * @param omdUserId 当前用户ID
     * @return 是否举报过
     */
    @Select("select exists(select 1 from tb_omd_comment_report where omd_music_comment_id = #{omdMusicCommentId} and omd_user_id = #{omdUserId})")
    boolean hasReportedMusicComment(Long omdMusicCommentIdList, Long omdUserId);

    /**
     * 查看该评论是否为登录用户评论的
     * @param omdMusicCommentIdList 评论ID
     * @param omdUserId 用户ID
     * @return 结果
     */
    @Select("select exists(select 1 from tb_omd_music_comment where omd_user_id = #{omdUserId} and omd_music_comment_id = #{omdMusicCommentId})")
    boolean isOwnComment(Long omdUserId, Long omdMusicCommentIdList);

    /**
     * 更新父评论的回复数
     * @param omdMusicCommentParentId 父评论ID
     * @param delta 数量变化值   1 表示增加回复数，-1 表示减少回复数
     * @return 是否更新成功
     */
    @Update("update tb_omd_music_comment set omd_music_comment_reply_count = omd_music_comment_reply_count + #{delta} " +
            "where omd_music_comment_id = #{omdMusicCommentParentId}")
    boolean updateReplyCountByParentId(Long omdMusicCommentParentId, int delta);

    /**
     * 批量删除音乐评论
     * @param commentIds 评论ID列表
     * @return 影响的行数
     */
    int deleteMusicCommentByIdList(List<Long> commentIds);

    /**
     * 根据父评论ID查询所有子评论ID
     * @param omdMusicCommentParentId 父评论ID
     * @return 子评论ID列表
     */
    @Select("select omd_music_comment_id from tb_omd_music_comment where omd_music_comment_parent_id = #{omdMusicCommentParentId}")
    List<Long> selectChildCommentIds(Long omdMusicCommentParentId);

    /**
     * 根据OmdUserId获取OmdPlaylist
     * @param omdUserId OmdUserId
     * @return OmdPlaylist
     */
    @Select("select * from tb_omd_playlist where omd_user_id = #{omdUserId}")
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
    @Select("select * from tb_omd_user where omd_user_id = #{omdUserId}")
    OmdUser getUserInfoByOmdUserId(Long omdUserId);

    /**
     * 根据用户ID获取用户的公开歌单
     * @param omdUserId 用户ID
     * @return 歌单列表
     */
    @Select("select * from tb_omd_playlist where omd_user_id = #{omdUserId} and omd_playlist_public = 1")
    List<OmdPlaylist> getOmdUserPlaylistPublic(Long omdUserId);

    /**
     * 获取其他用户的身份
     * @param omdUserId 用户ID
     * @return 歌单列表
     */
    @Select("select * from tb_omd_user_role where omd_user_id = #{omdUserId}")
    List<OmdUserRole> getOmdUserRoleList(Long omdUserId);

    /**
     * 根据用户ID获取用户的举报记录
     * @param omdUserId 用户ID
     * @param omdCommentReportStatus 举报状态
     * @return 举报记录列表
     * */
    List<OmdCommentReport> getOmdCommentReportList(Long omdUserId, Integer omdCommentReportStatus);

    /**
     * 根据用户ID获取用户的反馈记录
     * @param omdUserId 用户ID
     * @param omdUserFeedbackStatus 反馈状态
     * @return 反馈记录列表
     */
    @Select("select * from tb_omd_user_feedback where omd_user_id = #{omdUserId} and omd_user_feedback_status = #{omdUserFeedbackStatus}")
    List<OmdUserFeedback> getOmdUserFeedbackList(Long omdUserId, Integer omdUserFeedbackStatus);

    /**
     * 更新用户状态
     * @param updateUser 用户信息
     * @return 是否更新成功
     * */
    @Update("update tb_omd_user set omd_user_status = #{omdUserStatus}, omd_user_update_time = now(), omd_user_freeze_end_time = #{omdUserFreezeEndTime}" +
            " , omd_user_remark = #{omdUserRemark} where omd_user_id = #{omdUserId}")
    void updateUserStatus(OmdUser updateUser);

    /**
     * 获取被临时冻结且到时间的用户列表
     * @return 用户列表
     */
    @Select("select * from tb_omd_user where omd_user_status = 0 and omd_user_freeze_type = 1 and omd_user_freeze_end_time is not null and omd_user_freeze_end_time < now()")
    List<OmdUser> selectExpiredFreezeUsers();

    /**
     * 根据申请ID获取申请信息
     * @param targetId 申请ID
     * @return 申请信息
     * */
    @Select("select * from tb_omd_applications where omd_applications_id = #{targetId}")
    OmdApplications getApplicationsByApplicationsId(Long targetId);

    /**
     * 根据反馈ID获取反馈信息
     * @param targetId 反馈ID
     * @return 反馈信息
     * */
    @Select("select * from tb_omd_user_feedback where omd_user_feedback_id = #{targetId}")
    OmdUserFeedback getFeedbackByFeedbackId(Long targetId);

    /**
     * 插入用户举报信息
     * @param omdUserReport 用户举报信息
     * @return 是否插入成功
     */
    @Insert("insert into tb_omd_user_report(omd_user_id,omd_user_reported_user_id,omd_user_report_type, omd_user_report_reason, omd_user_report_evidence)" +
            " values (#{omdUserId}, #{omdUserReportedUserId}, #{omdUserReportType}, #{omdUserReportReason},#{omdUserReportEvidence})")
    boolean insertUserReport(OmdUserReport omdUserReport);

    /**
     * 检查用户是否已经举报过该用户
     * @param omdUserReportedUserId 被举报用户ID
     * @param omdUserId 当前用户ID
     * @return 是否举报过
     */
    @Select("select exists(select 1 from tb_omd_user_report where omd_user_reported_user_id = #{omdUserReportedUserId} and omd_user_id = #{omdUserId})")
    boolean checkUserReport(Long omdUserReportedUserId, Long omdUserId);

    /**
     * 检查用户是否已经举报过该音乐
     * @param omdMusicInfoId 音乐ID
     * @param omdUserId 当前用户ID
     * @return 是否举报过
     */
    @Select("select exists(select 1 from tb_omd_music_report where omd_music_info_id = #{omdMusicInfoId} and omd_user_id = #{omdUserId})")
    boolean checkMusicReport(Long omdMusicInfoId, Long omdUserId);

    /**
     * 插入音乐举报信息
     * @param omdMusicReport 音乐举报信息
     * @return 是否插入成功
     */
    @Insert("insert into tb_omd_music_report(omd_music_info_id, omd_user_id, omd_music_report_reason, omd_music_report_type,omd_music_report_evidence)" +
            " values (#{omdMusicInfoId}, #{omdUserId}, #{omdMusicReportReason}, #{omdMusicReportType},#{omdMusicReportEvidence})")
    boolean insertMusicReport(OmdMusicReport omdMusicReport);

    /**
     * 根据用户ID获取用户的举报列表
     * @param omdUserId 用户ID
     * @param omdUserReportStatus 举报状态
     * @return 举报列表
     */
    List<OmdUserReport> getOmdUserReportList(Long omdUserId, Integer omdUserReportStatus);

    /**
     * 根据用户ID获取用户的举报列表
     * @param omdUserId 用户ID
     * @param omdMusicReportStatus 举报状态
     * @return 举报列表
     */
    List<OmdMusicReport> getOmdMusicReportList(Long omdUserId, Integer omdMusicReportStatus);
}
