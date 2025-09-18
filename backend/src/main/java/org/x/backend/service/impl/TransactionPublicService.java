package org.x.backend.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.x.backend.pojo.*;
import org.x.backend.service.OmdAdminService;
import org.x.backend.service.OmdMusicService;
import org.x.backend.service.OmdPublicService;
import org.x.backend.service.OmdUserService;
import org.x.backend.utils.HelperUtil;
import org.x.backend.utils.RedisUtil;

@Service
@Slf4j
@Transactional(rollbackFor = Exception.class) // 统一管理事务
public class TransactionPublicService {

    @Autowired
    private OmdAdminService adminService;

    @Autowired
    private OmdPublicService omdPublicService;

    @Autowired
    private OmdUserService omdUserService;

    @Autowired
    private OmdMusicService omdMusicService;

    @Autowired
    private HelperUtil helperUtil;

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private OmdAdminService omdAdminService;

    /**
     * 注册
     * @param omdUser 用户信息
     */
    public void register(OmdUser omdUser) {
        try {
            // 注册用户
            omdUserService.register(omdUser);

            // 注册成功后，创建默认歌单：“我喜欢的音乐”
            OmdPlaylist omdPlaylist = new OmdPlaylist();
            omdPlaylist.setOmdUserId(omdUser.getOmdUserId());
            omdPlaylist.setOmdPlaylistName("我喜欢的音乐");
            omdPlaylist.setOmdPlaylistDescription("这是您的默认歌单");
            omdMusicService.insertPlaylist(omdPlaylist);

            // 默认歌单创建成功后，创建歌单详情表
            OmdPlaylistMusic omdPlaylistMusic = new OmdPlaylistMusic();
            omdPlaylistMusic.setOmdPlaylistId(omdPlaylist.getOmdPlaylistId());
            omdMusicService.insertPlaylistMusic(omdPlaylistMusic);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 登录
     * @param loginUser 登录用户信息
     * @return token
     */
    public String login(OmdUser loginUser) {

        // 查询用户状态，判断是否被临时冻结，自动解冻
        if (!helperUtil.checkFreezeStatus(loginUser)) {
            // 不是冻结状态
            // 生成token并存入redis
            String token = helperUtil.generateTokenAndSave(loginUser);

            // 标记用户为在线状态
            redisUtil.userOnline(loginUser.getOmdUserId());

            return token;
        }else {
            // 冻结状态，前面已经做了是否到达日期的判断，这里就只需要修改用户的冻结状态，新增日志表，登录就可以了
            // 自动解冻：更新用户状态为正常
            OmdUser updateUser = new OmdUser();
            updateUser.setOmdUserId(loginUser.getOmdUserId());
            updateUser.setOmdUserStatus(1);
            updateUser.setOmdUserFreezeType(0); // 解冻后状态为无冻结
            updateUser.setOmdUserRemark(loginUser.getOmdUserRemark() + "（临时冻结到期，系统自动解冻）");
            updateUser.setOmdUserFreezeEndTime(null); // 清空冻结时间
            omdUserService.updateUserStatus(updateUser); // 确保updateUserStatus方法仅更新非null字段

            // 新增日志表
            OmdOperationLog omdOperationLog = new OmdOperationLog();
            omdOperationLog.setOmdAdminId(0L);
            omdOperationLog.setOmdOperationLogType("user:unfreeze_auto");
            omdOperationLog.setOmdOperationLogDesc("用户" + loginUser.getOmdUserName() + "的临时冻结已到期，系统自动解冻");
            omdOperationLog.setOmdOperationLogTargetId(loginUser.getOmdUserId());
            omdOperationLog.setOmdOperationLogTargetType("user");
            omdAdminService.addOmdOperationLog(omdOperationLog);

            // 生成token并存入redis
            String token = helperUtil.generateTokenAndSave(loginUser);

            // 标记用户为在线状态
            redisUtil.userOnline(loginUser.getOmdUserId());

            return token;
        }
    }
}
