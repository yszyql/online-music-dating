package org.x.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.x.backend.pojo.*;
import org.x.backend.service.OmdSingerService;
import org.x.backend.service.OmdUserService;
import org.x.backend.service.impl.CosService;
import org.x.backend.service.impl.TransactionUserService;
import org.x.backend.utils.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 用户管理
 */
@RestController
@RequestMapping("/user")
@Validated
@Slf4j
public class OmdUserController {

    // 用户服务
    @Autowired
    private OmdUserService omdUserService;

    // 歌手服务
    @Autowired
    private OmdSingerService omdSingerService;

    // 事务管理服务
    @Autowired
    private TransactionUserService transactionUserService;

    // cos服务层
    @Autowired
    private CosService cosService;

    // 密码加密器
    @Autowired
    private PasswordEncoder passwordEncoder;

    // jwt工具类
    @Autowired
    private JwtUtil jwtUtil;

    // redis工具类
    @Autowired
    private RedisUtil redisUtil;

    // HelperUtil工具类
    @Autowired
    private HelperUtil helperUtil;

    // token的请求头
    @Value("${jwt.token-header}")
    private String tokenHeader;

    // 前缀Bearer
    @Value("${jwt.token-prefix}")
    private String tokenPrefix;

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public Result<String> logout() {
        try {
            // 获取请求头和Token
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attributes.getRequest();
            String authHeader = request.getHeader(tokenHeader);
            if (authHeader == null) {
                return Result.error("请求头中缺少指定的 token 信息");
            }
            String token = authHeader.substring(tokenPrefix.length()).trim();
            log.info("接收到登出请求，token参数: {}", token);

            // 解析用户ID
            Long userId = jwtUtil.getUserIdFromJWT(token);
            if (userId == null) {
                return Result.error("无效Token");
            }

            // 构建Redis Key并获取存储的Token
            String redisKey = "user:token:" + userId;
            String storedToken = redisUtil.get(redisKey);

            // 校验Token是否匹配
            if (!token.equals(storedToken)) {
                log.warn("Token不匹配：请求Token[{}] vs Redis存储Token[{}]", token, storedToken);
                return Result.error("Token已失效，请重新登录");
            }

            // 将用户ID添加到离线队列
            redisUtil.userOffline(userId);

            // 删除Token
            Boolean deleted = redisUtil.delete(redisKey);
            if (deleted) {
                log.info("用户[{}]退出登录成功", userId);
                return Result.success();
            } else {
                log.warn("用户[{}]退出登录失败，Token不存在", userId);
                return Result.error("登出失败，Token不存在");
            }
        } catch (Exception e) {
            log.error("登出失败", e);
            return Result.error("登出失败");
        }
    }

    /**
     * 获取用户信息
     * @return 用户信息
     */
    @GetMapping("/getUserInfo")
    public Result<OmdUser> getUserInfo(){
        // 从ThreadLocal中获取用户信息
        OmdUser omdUser = omdUserService.findOmdUserByIdWithRoles(helperUtil.getCurrentUserId());
        // 直接返回Claims中的用户信息
        return Result.success(omdUser);
    }

    /**
     * 头像上传
     * @param avatarFile 文件
     * @return 文件访问URL
     */
    @PostMapping("/avatarFileUpload")
    public Result<String> avatarFileUpload(@RequestParam("avatarFile") MultipartFile avatarFile){
        String avatarUrl = cosService.fileUpload(avatarFile, CosTagsUtil.AVATAR);
        return Result.success(avatarUrl);
    }

    /**
     * 修改用户信息
     * 这里不允许修改密码，与此同时用户名和手机号是不可修改的
     * @param omdUser 用户信息
     */
    @PostMapping("/updateUserInfo")
    public Result<String> updateUserInfo(@RequestBody OmdUser omdUser) {
        omdUser.setOmdUserId(helperUtil.getCurrentUserId());
        // 调用服务层方法更新用户信息
        omdUserService.updateUserInfo(omdUser);
        return Result.success("用户信息更新成功");
    }

    /**
     * 普通修改密码
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @param confirmPassword 确认密码
     */
    @PostMapping("/updatePassword")
    public Result<String> updatePassword(@RequestParam("oldPassword") String oldPassword,
                                         @RequestParam("newPassword") String newPassword,
                                         @RequestParam("confirmPassword") String confirmPassword) {

        // 获取当前用户信息
        OmdUser existingOmdUser = omdUserService.findByUserId(helperUtil.getCurrentUserId());
        // 校验新旧密码
        if (!passwordEncoder.matches(oldPassword, existingOmdUser.getOmdUserPassword())) {
            log.error("原密码错误");
            return Result.error("原密码错误");
        }

        // 检查新密码是否与旧密码相同（比较原文）
        if (newPassword.equals(oldPassword)) {
            log.error("新密码与旧密码相同");
            return Result.error("新密码不能与旧密码相同");
        }

        // 校验新密码是否与确认密码一致
        if (!newPassword.equals(confirmPassword)) {
            log.error("新密码与确认密码不一致");
            return Result.error("新密码与确认密码不一致");
        }

        // 调用服务层更新密码
        boolean success = omdUserService.updatePassword(existingOmdUser.getOmdUserId(), passwordEncoder.encode(newPassword));
        if (!success) {
            return Result.error("密码更新失败");
        }

        // 更新密码生成新 Token（使用 jwtUtil）
        String token = helperUtil.updatePasswordAndGenerateToken(existingOmdUser);

        // 返回结果中包含新 Token
        return Result.success(token);
    }

    /**
     * 歌曲样例文件上传
     * @param sampleFile 文件
     * @return 文件访问URL
     */
    @PostMapping("/sampleFileUpload")
    public Result<String> sampleFileUpload(@RequestParam("sampleFile") MultipartFile sampleFile){
        String sampleUrl = cosService.fileUpload(sampleFile, CosTagsUtil.SAMPLE);
        return Result.success(sampleUrl);
    }

    /**
     * 判断用户的信息是否完善
     * @return 是否完善
     */
    @GetMapping("/getIsUserInfoComplete")
    public Result<Boolean> getIsUserInfoComplete(){
        OmdUser omdUser = omdUserService.findByUserId(helperUtil.getCurrentUserId());
        if (helperUtil.isUserInfoComplete(omdUser)) {
            return Result.success(true);
        }
        return Result.success(false);
    }

    /**
     * 获取用户歌手申请的信息
     * @return 结果
     */
    @GetMapping("/findApplicationByOmdUserId")
    public Result<OmdApplications> findApplicationByOmdUserId(){
        OmdApplications omdApplications = omdUserService.findApplicationByOmdUserId(helperUtil.getCurrentUserId());
        return Result.success(omdApplications);
    }

    /**
     * 申请成为歌手
     * @param omdApplications 申请信息
     */
    @PostMapping("/applicationSinger")
    public Result<String> applicationSinger(@RequestBody @Validated OmdApplications omdApplications) {
        // 从claims中获取用户id并查询
        OmdUser omdUser = omdUserService.findByUserId(helperUtil.getCurrentUserId());
        if (omdUser == null) {
            return Result.error("用户不存在");
        }

        // 校验用户信息是否完善
        if (!helperUtil.isUserInfoComplete(omdUser)) {
            return Result.error("用户信息不完整，请先完善个人信息");
        }
        // 校验用户是否已经是歌手
        if (omdUserService.findApplicationByOmdUserId(helperUtil.getCurrentUserId()) != null) {
            return Result.error("您已经申请了，无需再次申请，请耐心等待审核");
        }
        // 进行申请
        omdApplications.setOmdUserId(helperUtil.getCurrentUserId());
        // 调用service层的方法
        omdUserService.applicationSinger(omdApplications);
        return Result.success("申请成功，请耐心等待审核");
    }

    /**
     * 评论音乐
     * @param omdMusicComment 评论内容
     * @return 结果
     */
    @PostMapping("/commentMusic")
    public Result<String> commentMusic(@RequestBody @Validated OmdMusicComment omdMusicComment){
        // 创建评论信息
        omdMusicComment.setOmdUserId(helperUtil.getCurrentUserId());
        // 调用事务管理层
        transactionUserService.insertMusicComment(omdMusicComment);

        return Result.success("评论成功");
    }

    /**
     * 删除评论
     * @param omdMusicCommentId 评论ID
     * @return 结果
     */
    @PostMapping("/deleteMusicComment")
    public Result<String> deleteMusicComment(@RequestParam("omdMusicCommentId") Long omdMusicCommentId){

        // 调用事务管理层
        transactionUserService.deleteMusicComment(omdMusicCommentId, helperUtil.getCurrentUserId());

        return Result.success("删除成功");
    }

    /**
     * 查看评论
     * @param omdMusicInfoId 音乐信息ID
     * @return 评论列表
     */
    @GetMapping("/getCommentListWithDynamicSort")
    public Result<PageBean<OmdMusicComment>> getCommentListWithDynamicSort(@RequestParam("pageNum") Integer pageNum,
                                                                           @RequestParam("pageSize") Integer pageSize,
                                                                           @RequestParam("omdMusicInfoId") Long omdMusicInfoId){
        // 执行分页查询
        PageBean<OmdMusicComment> musicCommentListByOmdMusicInfoId = helperUtil.executePageQuery(
                pageNum,
                pageSize,
                () -> omdUserService.findCommentListByOmdMusicInfoIdWithDynamicSort(omdMusicInfoId,helperUtil.getCurrentUserId())
        );

        return Result.success(musicCommentListByOmdMusicInfoId);
    }

    /**
     * 点赞或不点赞评论
     * @param omdMusicCommentId 评论ID
     * @return 结果
     */
    @PostMapping("/likeMusicCommentOrNot")
    public Result<String> likeMusicCommentOrNot(@RequestParam("omdMusicCommentId") Long omdMusicCommentId){
        transactionUserService.likeMusicCommentOrNot(omdMusicCommentId,helperUtil.getCurrentUserId());
        return Result.success("操作成功");
    }

    /**
     * 举报评论
     * @param omdCommentReport 举报信息
     * @return 结果
     */
    @PostMapping("/reportMusicComment")
    public Result<String> reportMusicComment(@RequestBody @Validated OmdCommentReport omdCommentReport){
        omdCommentReport.setOmdUserId(helperUtil.getCurrentUserId());
        if (!omdUserService.insertOmdCommentReport(omdCommentReport)){
            return Result.error("举报评论失败");
        }
        return Result.success("举报评论成功");
    }

    /**
     * 查看用户对该评论的各个状态
     * @param omdMusicCommentIdList 评论ID
     * @return 结果
     */
    @GetMapping("/getMusicCommentListStatusByUserId")
    public Result<Map<Long, OmdCommentStatusResult>> getMusicCommentListStatusByUserId(@RequestParam("omdMusicCommentIdList") List<Long> omdMusicCommentIdList) {

        if (CollectionUtils.isEmpty(omdMusicCommentIdList) || helperUtil.getCurrentUserId() == null) {
            return Result.success(Collections.emptyMap());
        }

        // 1. 查询是否本人评论
        List<Map<String, Object>> isOwnMap = omdUserService.isOwnCommentList(omdMusicCommentIdList, helperUtil.getCurrentUserId());

        // 2. 查询点赞状态
        List<Map<String, Object>> likeStatusMap = omdUserService.hasLikedMusicCommentList(omdMusicCommentIdList, helperUtil.getCurrentUserId());

        // 3. 查询举报状态
        List<Map<String, Object>> reportStatusMap = omdUserService.hasReportedMusicCommentList(omdMusicCommentIdList, helperUtil.getCurrentUserId());

        // 4. 拼装结果

        // 合并三种状态到一个 Map
        Map<Long, OmdCommentStatusResult> statusMap = new HashMap<>();

        // 初始化所有评论的状态
        // 初始化所有评论的状态
        omdMusicCommentIdList.forEach(id -> {
            OmdCommentStatusResult status = new OmdCommentStatusResult();
            status.setOmdMusicCommentId(id);
            statusMap.put(id, status);
        });

        // 填充点赞状态
        likeStatusMap.forEach(m -> {
            Long omdMusicCommentId = ((Number) m.get("omdMusicCommentId")).longValue();
            Object isLikedObj = m.get("isLiked");
            Boolean isLiked = helperUtil.convertToBoolean(isLikedObj);
            statusMap.get(omdMusicCommentId).setIsLiked(isLiked);
        });

        // 填充是否本人状态
        isOwnMap.forEach(m -> {
            Long omdMusicCommentId = ((Number) m.get("omdMusicCommentId")).longValue();
            Object isOwnObj = m.get("isOwn");
            Boolean isOwn = helperUtil.convertToBoolean(isOwnObj);
            statusMap.get(omdMusicCommentId).setIsOwn(isOwn);
        });

        // 填充举报状态
        reportStatusMap.forEach(m -> {
            Long omdMusicCommentId = ((Number) m.get("omdMusicCommentId")).longValue();
            Object isReportedObj = m.get("isReported");
            Boolean isReported = helperUtil.convertToBoolean(isReportedObj);
            statusMap.get(omdMusicCommentId).setIsReported(isReported);
        });

        return Result.success(statusMap);

    }

    /**
     * 反馈给管理员
     * @param omdUserFeedback 反馈信息
     * @return 结果
     */
    @PostMapping("/feedbackToAdmin")
    public Result<String> feedbackToAdmin(@RequestBody @Validated OmdUserFeedback omdUserFeedback){
        omdUserFeedback.setOmdUserId(helperUtil.getCurrentUserId());
        if (!omdUserService.insertOmdFeedback(omdUserFeedback)){
            return Result.error("反馈失败");
        }
        return Result.success("反馈成功");
    }

    /**
     * 获取用户创建的所有歌单
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 歌单列表
     */
    @GetMapping("/getPlaylistListByOmdUserId")
    public Result<PageBean<OmdPlaylist>> getPlaylistListByOmdUserId(@RequestParam("pageNum") Integer pageNum,
                                                                    @RequestParam("pageSize") Integer pageSize ){

        // 执行分页查询
        PageBean<OmdPlaylist> PlaylistList = helperUtil.executePageQuery(
                pageNum,
                pageSize,
                () -> omdUserService.getPlaylistListByOmdUserId(helperUtil.getCurrentUserId())
        );

        return Result.success(PlaylistList);
    }

    /**
     * 获取所有用户创建的公开歌单，除了自己的公共歌单
     * 这里只展示公开的歌单，不展示私密的歌单
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 歌单列表
     */
    @GetMapping("/getPlaylistPublicList")
    public Result<PageBean<OmdPlaylist>> getPlaylistPublicList(@RequestParam("pageNum") Integer pageNum,
                                                               @RequestParam("pageSize") Integer pageSize){
        // 执行分页查询
        PageBean<OmdPlaylist> PlaylistList = helperUtil.executePageQuery(
                pageNum,
                pageSize,
                () -> omdUserService.getPlaylistPublicList(helperUtil.getCurrentUserId())
        );

        return Result.success(PlaylistList);
    }

    /**
     * 获取其他用户的基本信息
     * @param omdUserId 用户ID
     * @return 用户
     */
    @GetMapping("/getUserInfoByOmdUserId")
    public Result<OmdUser> getUserInfoByOmdUserId(@RequestParam("omdUserId") Long omdUserId){
        OmdUser omdUser = omdUserService.getUserInfoByOmdUserId(omdUserId);
        return Result.success(omdUser);
    }

    /**
     * 获取其他用户的公开歌单
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param omdUserId 用户ID
     * @return 歌单列表
     */
    @GetMapping("/getOmdUserPlaylistPublic")
    public Result<PageBean<OmdPlaylist>> getOmdUserPlaylistPublic(@RequestParam("pageNum") Integer pageNum,
                                                                  @RequestParam("pageSize") Integer pageSize,
                                                                  @RequestParam("omdUserId") Long omdUserId){
        // 执行分页查询
        PageBean<OmdPlaylist> PlaylistList = helperUtil.executePageQuery(
                pageNum,
                pageSize,
                () -> omdUserService.getOmdUserPlaylistPublic(omdUserId)
        );

        return Result.success(PlaylistList);
    }

    /**
     * 获取其他用户的身份
     * @param omdUserId 用户ID
     * @return 歌单列表
     */
    @GetMapping("/getOmdUserRoleList")
    public Result<List<OmdUserRole>> getOmdUserRoleList(@RequestParam("omdUserId") Long omdUserId){
        List<OmdUserRole> omdUserRoleList = omdUserService.getOmdUserRoleList(omdUserId);
        return Result.success(omdUserRoleList);
    }


    /**
     * 根据用户ID查询歌手信息
     * @param omdUserId 用户ID
     * @return 歌手信息
     */
    @GetMapping("/getOmdSingerInfoByOmdUserId")
    public Result<OmdSinger> getOmdSingerInfoByOmdUserId(@RequestParam("omdUserId") Long omdUserId){
        OmdSinger omdSinger = omdSingerService.findSingerByUserId(omdUserId);
        return Result.success(omdSinger);
    }

    /**
     * 根据歌手ID查询歌曲信息
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param omdSingerId 歌手ID
     * @return 歌曲信息
     */
    @GetMapping("/getMusicInfoBySingerId")
    public Result<PageBean<OmdMusicInfo>> getMusicInfoBySingerId(@RequestParam("pageNum") Integer pageNum,
                                                                 @RequestParam("pageSize") Integer pageSize,
                                                                 @RequestParam("omdSingerId") Long omdSingerId) {
        // 执行分页查询
        PageBean<OmdMusicInfo> musicInfoListBySingerId = helperUtil.executePageQuery(
                pageNum,
                pageSize,
                () -> omdSingerService.getMusicInfoListBySingerId(omdSingerId, 1)
        );
        return Result.success(musicInfoListBySingerId);
    }

    /**
     * 获取用户的举报列表
     * @param omdCommentReportStatus 举报状态
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 举报列表
     */
    @GetMapping("/getOmdCommentReportList")
    public Result<PageBean<OmdCommentReport>> getOmdCommentReportList(@RequestParam("omdCommentReportStatus") Integer omdCommentReportStatus,
                                                                      @RequestParam("pageNum") Integer pageNum,
                                                                      @RequestParam("pageSize") Integer pageSize){
        // 执行分页查询
        PageBean<OmdCommentReport> omdCommentReportList = helperUtil.executePageQuery(
                pageNum,
                pageSize,
                () -> omdUserService.getOmdCommentReportList(helperUtil.getCurrentUserId(),omdCommentReportStatus)
        );

        return Result.success(omdCommentReportList);
    }

    /**
     * 获取用户的反馈列表
     * @param omdUserFeedbackStatus 反馈状态
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 反馈列表
     */
    @GetMapping("/getOmdUserFeedbackList")
    public Result<PageBean<OmdUserFeedback>> getOmdUserFeedbackList(@RequestParam("omdUserFeedbackStatus") Integer omdUserFeedbackStatus,
                                                                    @RequestParam("pageNum") Integer pageNum,
                                                                    @RequestParam("pageSize") Integer pageSize){
        // 执行分页查询
        PageBean<OmdUserFeedback> omdUserFeedbackList = helperUtil.executePageQuery(
                pageNum,
                pageSize,
                () -> omdUserService.getOmdUserFeedbackList(helperUtil.getCurrentUserId(),omdUserFeedbackStatus)
        );
        return Result.success(omdUserFeedbackList);
    }

    /**
     * 举报证据上传
     * @param evidenceFile 证据文件
     * @return 文件访问URL
     */
    @PostMapping("/evidenceFileUpload")
    public Result<String> evidenceFileUpload(@RequestParam("evidenceFile") MultipartFile evidenceFile){
        String sampleUrl = cosService.fileUpload(evidenceFile, CosTagsUtil.EVIDENCE);
        return Result.success(sampleUrl);
    }

    /**
     * 检查用户是否已经举报过该用户
     * @param omdUserId 用户ID
     * @return 结果
     */
    @GetMapping("/getUserReportStatus")
    public Result<Boolean> getUserReportStatus(@RequestParam("omdUserId") Long omdUserId){
        if (omdUserService.checkUserReport(omdUserId,helperUtil.getCurrentUserId())){
            return Result.success(true);
        }
        return Result.success(false);
    }

    /**
     * 举报用户
     * @param omdUserReport 举报信息
     * @return 结果
     */
    @PostMapping("/insertUserReport")
    public Result<String> insertUserReport(@RequestBody OmdUserReport omdUserReport){
        // 检查是否已经举报过该用户
        if (omdUserService.checkUserReport(omdUserReport.getOmdUserReportedUserId(),helperUtil.getCurrentUserId())){
            return Result.error("您已经举报过该用户，请勿重复举报");
        }
        omdUserReport.setOmdUserId(helperUtil.getCurrentUserId());
        // 举报用户的同时拉黑用户
        transactionUserService.insertUserReport(omdUserReport,helperUtil.getCurrentUserId());
        return Result.success("举报成功");
    }

    /**
     * 检查用户是否已经举报过该歌曲
     * @param omdMusicInfoId 歌曲ID
     * @return 结果
     */
    @GetMapping("/getMusicReportStatus")
    public Result<Boolean> getMusicReportStatus(@RequestParam("omdMusicInfoId") Long omdMusicInfoId){
        if (omdUserService.checkMusicReport(omdMusicInfoId,helperUtil.getCurrentUserId())){
            return Result.success(true);
        }
        return Result.success(false);
    }

    /**
     * 举报歌曲
     * @param omdMusicReport 举报信息
     * @return 结果
     */
    @PostMapping("/insertMusicReport")
    public Result<String> insertMusicReport(@RequestBody OmdMusicReport omdMusicReport){
        // 检查是否已经举报过该歌曲
        if (omdUserService.checkMusicReport(omdMusicReport.getOmdMusicInfoId(),helperUtil.getCurrentUserId())){
            return Result.error("您已经举报过该歌曲，请勿重复举报");
        }
        omdMusicReport.setOmdUserId(helperUtil.getCurrentUserId());
        // 举报歌曲
        if (!omdUserService.insertMusicReport(omdMusicReport)){
            return Result.error("举报失败");
        }
        return Result.success("举报成功");
    }

    /**
     * 获取用户举报用户的举报列表
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param omdUserReportStatus 举报状态
     * @return 举报列表
     */
    @GetMapping("/getOmdUserReportList")
    public Result<PageBean<OmdUserReport>> getOmdUserReportList(@RequestParam("pageNum") Integer pageNum,
                                                                @RequestParam("pageSize") Integer pageSize,
                                                                @RequestParam("omdUserReportStatus") Integer omdUserReportStatus){
        // 执行分页查询
        PageBean<OmdUserReport> omdUserReportList = helperUtil.executePageQuery(
                pageNum,
                pageSize,
                () -> omdUserService.getOmdUserReportList(helperUtil.getCurrentUserId(),omdUserReportStatus)
        );
        return Result.success(omdUserReportList);
    }

    /**
     * 获取用户举报音乐的举报列表
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param omdMusicReportStatus 举报状态
     * @return 举报列表
     */
    @GetMapping("/getOmdMusicReportList")
    public Result<PageBean<OmdMusicReport>> getOmdMusicReportList(@RequestParam("pageNum") Integer pageNum,
                                                                  @RequestParam("pageSize") Integer pageSize,
                                                                  @RequestParam("omdMusicReportStatus")Integer omdMusicReportStatus){
        // 执行分页查询
        PageBean<OmdMusicReport> omdMusicReportList = helperUtil.executePageQuery(
                pageNum,
                pageSize,
                () -> omdUserService.getOmdMusicReportList(helperUtil.getCurrentUserId(),omdMusicReportStatus)
        );
        return Result.success(omdMusicReportList);
    }

}
