package org.x.backend.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.x.backend.mapper.OmdUserMapper;
import org.x.backend.mapper.OmdUserRoleMapper;
import org.x.backend.pojo.*;
import org.x.backend.service.OmdUserService;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class OmdUserServiceImpl implements OmdUserService {

    @Autowired
    private OmdUserMapper omdUserMapper;

    @Autowired
    private OmdUserRoleMapper omdUserRoleMapper;

    /**
     * 根据用户ID查询用户信息
     * @param omdUserId 用户ID
     * @return 用户信息
     */
    @Override
    public OmdUser findByUserId(Long omdUserId) {
        return omdUserMapper.findById(omdUserId);
    }

    /**
     * 根据用户名查询用户信息
     * @param omdUserName 用户名
     * @return 用户信息
     */
    @Override
    public OmdUser findByUsername(String omdUserName) {
        return omdUserMapper.findByUsername(omdUserName);
    }

    /**
     * 根据手机号查询用户信息
     * @param omdUserPhone 手机号
     * @return 用户信息
     */
    @Override
    public OmdUser findByPhone(String omdUserPhone) {
        return omdUserMapper.findByPhone(omdUserPhone);
    }

    /**
     * 注册用户
     * @param omdUser 用户信息
     */
    @Override
    public void register(OmdUser omdUser) {

        // 注册要放在前，不然后面插入不到用户角色表里
        omdUserMapper.register(omdUser);

        // 默认为普通用户
        OmdUserRole omdUserRole = new OmdUserRole();
        omdUserRole.setOmdUserId(omdUser.getOmdUserId());
        omdUserRole.setOmdRoleCode("ROLE_USER");
        omdUserRoleMapper.insert(omdUserRole);
    }

    /**
     * 根据用户名或手机号查询用户信息
     * @param identifier 用户名或手机号
     * @return 用户信息
     */
    @Override
    public OmdUser findByUsernameOrPhone(String identifier) {
        return omdUserMapper.findByUsernameOrPhone(identifier);
    }

    /**
     * 根据用户ID查询用户信息及其角色
     * @param omdUserId 当前用户ID
     * @return 用户信息及其角色
     */
    @Override
    public OmdUser findOmdUserByIdWithRoles(Long omdUserId) {
        return omdUserMapper.findOmdUserByIdWithRoles(omdUserId);
    }

    /**
     * 修改用户信息
     * 这里不允许修改密码，与此同时用户名和手机号是不可修改的
     * @param omdUser 用户信息
     */
    @Override
    public void updateUserInfo(OmdUser omdUser) {
        omdUserMapper.updateUserInfo(omdUser);
    }

    /**
     * 更新用户密码
     * @param omdUserId 用户ID
     * @param newPassword 新密码
     */
    @Override
    public boolean updatePassword(Long omdUserId, String newPassword) {
        return omdUserMapper.updatePassword(omdUserId, newPassword);
    }

    /**
     * 根据用户ID查询申请信息
     * @param omdUserId 用户ID
     * @return 申请信息
     */
    @Override
    public OmdApplications findApplicationByOmdUserId(Long omdUserId) {
        return omdUserMapper.findApplicationByOmdUserId(omdUserId);
    }

    /**
     * 申请成为歌手
     * @param omdApplications 申请信息
     */
    @Override
    public void applicationSinger(OmdApplications omdApplications) {
        omdUserMapper.applicationSinger(omdApplications);
    }

    /**
     * 根据音乐评论的ID查询音乐评论
     * @param omdMusicCommentId 音乐评论ID
     * @return 音乐评论
     */
    @Override
    public OmdMusicComment findCommentByOmdMusicCommentId(Long omdMusicCommentId) {
        return omdUserMapper.findCommentByOmdMusicCommentId(omdMusicCommentId);
    }

    /**
     * 插入音乐评论
     * @param omdMusicComment 音乐评论
     */
    @Override
    public boolean insertMusicComment(OmdMusicComment omdMusicComment) {
        return omdUserMapper.insertMusicComment(omdMusicComment);
    }

    /**
     * 根据用户ID和评论ID删除音乐评论
     * @param omdMusicCommentId 音乐评论ID
     * @param omdUserId 当前用户ID
     * @return 是否删除成功
     */
    @Override
    public boolean deleteMusicComment(Long omdMusicCommentId, Long omdUserId) {
        return omdUserMapper.deleteMusicComment(omdMusicCommentId, omdUserId);
    }

    /**
     * 根据音乐ID查询音乐评论列表
     * @param omdMusicInfoId 音乐ID
     * @param omdUserId 当前用户ID
     * @return 音乐评论列表
     */
    @Override
    public List<OmdMusicComment> findCommentListByOmdMusicInfoIdWithDynamicSort(Long omdMusicInfoId, Long omdUserId) {
        return omdUserMapper.findCommentListByOmdMusicInfoIdWithDynamicSort(omdMusicInfoId,omdUserId);
    }

    /**
     * 点赞音乐评论
     * @param omdMusicCommentId 音乐评论ID
     * @return 是否点赞成功
     */
    @Override
    public boolean likeMusicComment(Long omdMusicCommentId) {
        return omdUserMapper.likeMusicComment(omdMusicCommentId);
    }

    /**
     * 插入音乐评论举报
     * @param omdCommentReport 音乐评论举报
     * @return 是否插入成功
     */
    @Override
    public boolean insertOmdCommentReport(OmdCommentReport omdCommentReport) {
        return omdUserMapper.insertOmdCommentReport(omdCommentReport);
    }

    /**
     * 检查用户是否已经点赞过该评论
     * @param omdMusicCommentIdList 音乐评论ID
     * @param omdUserId 当前用户ID
     * @return 是否点赞过
     */
    @Override
    public boolean hasLikedMusicComment(Long omdMusicCommentIdList, Long omdUserId) {
        return omdUserMapper.hasLikedMusicComment(omdMusicCommentIdList, omdUserId);
    }

    /**
     * 取消点赞
     * @param omdMusicCommentId 音乐评论ID
     * @return 是否取消点赞成功
     */
    @Override
    public boolean unlikeMusicComment(Long omdMusicCommentId) {
        return omdUserMapper.unlikeMusicComment(omdMusicCommentId);
    }

    /**
     * 删除评论点赞记录
     * @param omdMusicCommentId 音乐评论ID
     * @param omdUserId 当前用户ID
     * @return 是否删除成功
     */
    @Override
    public boolean deleteCommentLike(Long omdMusicCommentId, Long omdUserId) {
        return omdUserMapper.deleteCommentLike(omdMusicCommentId, omdUserId);
    }

    /**
     * 插入评论点赞记录
     * @param omdMusicCommentId 音乐评论ID
     * @param omdUserId 当前用户ID
     * @return 是否插入成功
     */
    @Override
    public boolean insertCommentLike(Long omdMusicCommentId, Long omdUserId) {
        return omdUserMapper.insertCommentLike(omdMusicCommentId, omdUserId);
    }

    /**
     * 插入用户反馈
     * @param omdUserFeedback 用户反馈
     * @return 是否插入成功
     */
    @Override
    public boolean insertOmdFeedback(OmdUserFeedback omdUserFeedback) {
        return omdUserMapper.insertOmdFeedback(omdUserFeedback);
    }

    /**
     * 根据父评论ID更新子评论数量
     * @param omdMusicCommentParentId 父评论ID
     * @param delta 数量变化值（+1 或 -1）
     */
    @Override
    public boolean updateReplyCountByParentId(Long omdMusicCommentParentId , int delta) {
        return omdUserMapper.updateReplyCountByParentId(omdMusicCommentParentId, delta);
    }

    /**
     * 根据评论ID列表删除评论
     * @param commentIds 评论ID列表
     * @return 删除的评论数量
     */
    @Override
    public int deleteMusicCommentByIdList(List<Long> commentIds) {
        return omdUserMapper.deleteMusicCommentByIdList(commentIds);
    }

    /**
     * 递归查询子评论ID
     * @param omdMusicCommentParentId 父评论ID
     * @return 子评论ID列表
     */
    @Override
    public List<Long> selectChildCommentIds(Long omdMusicCommentParentId) {
        return omdUserMapper.selectChildCommentIds(omdMusicCommentParentId);
    }

    /**
     * 根据OmdUserId获取OmdPlaylist
     * @param omdUserId OmdUserId
     * @return OmdPlaylist
     */
    @Override
    public List<OmdPlaylist> getPlaylistListByOmdUserId(Long omdUserId) {
        return omdUserMapper.getPlaylistListByOmdUserId(omdUserId);
    }

    /**
     * 根据OmdPlaylistId获取OmdPlaylistMusic
     * @param omdPlaylistId OmdPlaylistId
     * @return OmdPlaylistMusic
     */
    @Override
    public List<OmdPlaylistMusic> getPlaylistMusicByOmdPlaylistId(Long omdPlaylistId) {
        return omdUserMapper.getPlaylistMusicByOmdPlaylistId(omdPlaylistId);
    }

    /**
     * 根据omdMusicCommentIdList获取是否为用户本人评论
     * @param omdMusicCommentIdList 评论列表
     * @param omdUserId 用户ID
     * @return 状态列表
     */
    @Override
    public List<Map<String, Object>> isOwnCommentList(List<Long> omdMusicCommentIdList, Long omdUserId) {
        return omdUserMapper.isOwnCommentList(omdMusicCommentIdList, omdUserId);
    }

    /**
     * 根据omdMusicCommentIdList获取是否被用户点赞
     * @param omdMusicCommentIdList 评论列表
     * @param omdUserId 用户ID
     * @return 状态列表
     */
    @Override
    public List<Map<String, Object>> hasLikedMusicCommentList(List<Long> omdMusicCommentIdList, Long omdUserId) {
        return omdUserMapper.hasLikedMusicCommentList(omdMusicCommentIdList, omdUserId);

    }

    /**
     * 根据omdMusicCommentIdList获取是否被用户举报
     * @param omdMusicCommentIdList 评论列表
     * @param omdUserId 用户ID
     * @return 状态列表
     */
    @Override
    public List<Map<String, Object>> hasReportedMusicCommentList(List<Long> omdMusicCommentIdList, Long omdUserId) {
        return omdUserMapper.hasReportedMusicCommentList(omdMusicCommentIdList, omdUserId);

    }

    /**
     * 获取所有公开的歌单
     * @param omdUserId 用户ID
     * @return 歌单列表
     */
    @Override
    public List<OmdPlaylist> getPlaylistPublicList(Long omdUserId) {
        return omdUserMapper.getPlaylistPublicList(omdUserId);
    }

    /**
     * 根据用户ID获取其他用户信息
     * @param omdUserId 用户ID
     * @return 用户信息
     */
    @Override
    public OmdUser getUserInfoByOmdUserId(Long omdUserId) {
        return omdUserMapper.getUserInfoByOmdUserId(omdUserId);
    }

    /**
     * 根据用户ID获取用户的公开歌单
     * @param omdUserId 用户ID
     * @return 歌单列表
     */
    @Override
    public List<OmdPlaylist> getOmdUserPlaylistPublic(Long omdUserId) {
        return omdUserMapper.getOmdUserPlaylistPublic(omdUserId);
    }

    /**
     * 获取其他用户的身份
     * @param omdUserId 用户ID
     * @return 歌单列表
     */
    @Override
    public List<OmdUserRole> getOmdUserRoleList(Long omdUserId) {
        return omdUserMapper.getOmdUserRoleList(omdUserId);
    }

    /**
     * 获取用户的举报列表
     * @param omdCommentReportStatus 举报状态
     * @param omdUserId 用户ID
     * @return 举报列表
     */
    @Override
    public List<OmdCommentReport> getOmdCommentReportList(Long omdUserId, Integer omdCommentReportStatus) {
        return omdUserMapper.getOmdCommentReportList(omdUserId, omdCommentReportStatus);
    }

    /**
     * 获取用户的反馈列表
     * @param omdUserId 用户ID
     * @param omdUserFeedbackStatus 反馈状态
     * @return 反馈列表
     */
    @Override
    public List<OmdUserFeedback> getOmdUserFeedbackList(Long omdUserId, Integer omdUserFeedbackStatus) {
        return omdUserMapper.getOmdUserFeedbackList(omdUserId, omdUserFeedbackStatus);
    }

    /**
     * 更新用户状态
     * @param updateUser 用户信息
     * */
    @Override
    public void updateUserStatus(OmdUser updateUser) {
        omdUserMapper.updateUserStatus(updateUser);
    }

    /**
     * 获取被临时冻结且到时间的用户列表
     * @return 用户列表
     */
    @Override
    public List<OmdUser> selectExpiredFreezeUsers() {
        return omdUserMapper.selectExpiredFreezeUsers();
    }

    /**
     * 根据申请ID获取申请信息
     * @param targetId 申请ID
     * @return 申请信息
     * */
    @Override
    public OmdApplications getApplicationsByApplicationsId(Long targetId) {
        return omdUserMapper.getApplicationsByApplicationsId(targetId);
    }

    /**
     * 根据反馈ID获取反馈信息
     * @param targetId 反馈ID
     * @return 反馈信息
     */
    @Override
    public OmdUserFeedback getFeedbackByFeedbackId(Long targetId) {
        return omdUserMapper.getFeedbackByFeedbackId(targetId);
    }

    /**
     * 插入举报信息
     * @param omdUserReport 举报信息
     * @return 是否插入成功
     */
    @Override
    public boolean insertUserReport(OmdUserReport omdUserReport) {
        return omdUserMapper.insertUserReport(omdUserReport);
    }

    /**
     * 检查用户是否已经举报过该用户
     * @param omdUserReportedUserId 被举报用户ID
     * @param omdUserId 当前用户ID
     * @return 是否举报过
     */
    @Override
    public boolean checkUserReport(Long omdUserReportedUserId, Long omdUserId) {
        return omdUserMapper.checkUserReport(omdUserReportedUserId, omdUserId);
    }

    /**
     * 检查用户是否已经举报过该音乐
     * @param omdMusicInfoId 音乐ID
     * @param omdUserId 当前用户ID
     * @return 是否举报过
     */
    @Override
    public boolean checkMusicReport(Long omdMusicInfoId, Long omdUserId) {
        return omdUserMapper.checkMusicReport(omdMusicInfoId, omdUserId);
    }

    /**
     * 插入音乐举报信息
     * @param omdMusicReport 音乐举报信息
     * @return 是否插入成功
     * */
    @Override
    public boolean insertMusicReport(OmdMusicReport omdMusicReport) {
        return omdUserMapper.insertMusicReport(omdMusicReport);
    }

    /**
     * 获取用户举报用户的举报列表
     * @param omdUserId 用户ID
     * @param omdUserReportStatus 举报状态
     * @return 举报列表
     */
    @Override
    public List<OmdUserReport> getOmdUserReportList(Long omdUserId, Integer omdUserReportStatus) {
        return omdUserMapper.getOmdUserReportList(omdUserId, omdUserReportStatus);
    }

    /**
     * 获取用户举报音乐的举报列表
     * @param omdUserId 用户ID
     * @param omdMusicReportStatus 举报状态
     * @return 举报列表
     */
    @Override
    public List<OmdMusicReport> getOmdMusicReportList(Long omdUserId, Integer omdMusicReportStatus) {
        return omdUserMapper.getOmdMusicReportList(omdUserId, omdMusicReportStatus);
    }
}
