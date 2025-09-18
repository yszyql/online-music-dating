package org.x.backend.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.x.backend.pojo.*;
import org.x.backend.service.OmdMusicService;
import org.x.backend.service.OmdPublicService;
import org.x.backend.service.OmdUserService;
import org.x.backend.service.impl.CosVerifyCodeService;
import org.x.backend.service.impl.TransactionPublicService;
import org.x.backend.utils.HelperUtil;
import org.x.backend.utils.JwtUtil;
import org.x.backend.utils.RedisUtil;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 公共接口
 */
@RestController
@RequestMapping("/public")
@Validated
@Slf4j
public class OmdPublicController {

    // 用户服务
    @Autowired
    private OmdUserService omdUserService;

    @Autowired
    private OmdPublicService omdPublicService;

    // 验证码服务
    @Autowired
    private CosVerifyCodeService cosVerifyCodeService;

    // 音乐服务
    @Autowired
    private OmdMusicService omdMusicService;

    @Autowired
    private TransactionPublicService transactionPublicService;

    // 密码加密器
    @Autowired
    private PasswordEncoder passwordEncoder;

    // HelperUtil工具类
    @Autowired
    private HelperUtil helperUtil;

    // RedisUtil工具类
    @Autowired
    private RedisUtil redisUtil;

    // JwtUtil工具类
    @Autowired
    private JwtUtil jwtUtil;


    /**
     * 注册
     * @param omdUser 用户信息
     * @return 注册结果
     */
    @PostMapping("/register")
    public Result register(@RequestBody @Validated OmdUser omdUser){
        OmdUser existingOmdUser;

        // 查询用户名是否已被注册过
        existingOmdUser = omdUserService.findByUsername(omdUser.getOmdUserName());
        if (existingOmdUser != null){
            return Result.error("用户名已存在");
        }

        // 查询手机号是否已被注册过
        existingOmdUser = omdUserService.findByPhone(omdUser.getOmdUserPhone());
        if (existingOmdUser != null){
            return Result.error("手机号已被注册");
        }

        // 密码加密
        String encodedPassword = passwordEncoder.encode(omdUser.getOmdUserPassword());
        omdUser.setOmdUserPassword(encodedPassword);

        // 注册用户
        transactionPublicService.register(omdUser);
        return Result.success();
    }

    /**
     * 用户名或手机号登录
     * 传入的参数与前端一致
     * @param identifier 用户名或手机号
     * @param omdUserPassword 密码
     * @return 登录结果
     */
    @PostMapping("/loginByUsernameOrPhone")
    public Result<String> loginByUsername(@RequestParam("identifier") String identifier,
                                          @RequestParam("omdUserPassword") String omdUserPassword){

        // 验证输入格式
        if (!helperUtil.isValidIdentifier(identifier)) {
            return Result.error("用户名或手机号格式不正确");
        }

        // 查询用户
        OmdUser loginUser = omdUserService.findByUsernameOrPhone(identifier);
        if (loginUser == null) {
            return Result.error("用户不存在");
        }

        // 查询用户状态，判断是否被永久冻结
        if (loginUser.getOmdUserFreezeType() == 2) {
            return Result.error("您的账户已被永久冻结，冻结原因如下：" + loginUser.getOmdUserRemark());
        }

        // 判断密码是否正确
        if (!passwordEncoder.matches(omdUserPassword,loginUser.getOmdUserPassword())){
            return Result.error("密码错误");
        }

        // 查询用户状态，判断是否被临时冻结
        if (helperUtil.checkFreezeStatus(loginUser)) {
            if (!new Date().after(loginUser.getOmdUserFreezeEndTime())){
                return Result.error("您的账户已被临时冻结，冻结原因如下：" + loginUser.getOmdUserRemark());
            }
        }

        // 调用事务管理层
        String token = transactionPublicService.login(loginUser);

        return Result.success(token);
    }

    /**
     * 发送手机验证码
     */
    @PostMapping("/sendVerifyCode")
    public Result<String> sendVerifyCode(@RequestParam("omdUserPhone") String omdUserPhone) {
        String code = cosVerifyCodeService.sendSmsVerifyCode(omdUserPhone);
        if (code != null) {
            log.info("验证码发送成功，验证码为：{}", code);
            return Result.success("验证码发送成功："+code);
        } else {
            return Result.error("验证码发送失败");
        }
    }

    /**
     * 手机号短信验证登录/注册
     */
    @PostMapping("/loginOrRegisterByPhone")
    public Result<String> loginOrRegisterByPhone(
            @RequestParam("omdUserPhone") String omdUserPhone,
            @RequestParam("verifyCode") String verifyCode) {

        // 验证验证码
        if (!cosVerifyCodeService.verifyCode(omdUserPhone, verifyCode)) {
            return Result.error("验证码错误");
        }

        // 登录或注册
        OmdUser existingOmdUser = omdUserService.findByPhone(omdUserPhone);

        // 如果用户不存在，则注册新用户
        if (existingOmdUser == null) {
            OmdUser loginUser = new OmdUser();
            loginUser.setOmdUserPhone(omdUserPhone);
            loginUser.setOmdUserName(helperUtil.generateUsername(omdUserPhone));
            loginUser.setOmdUserPassword(passwordEncoder.encode(UUID.randomUUID().toString())); // 根据UUID格式生成随机密码
            omdUserService.register(loginUser);

            // 生成token并存入redis
            String token = helperUtil.generateTokenAndSave(loginUser);
            return Result.success(token);
        }

        // 查询用户状态，判断是否被临时冻结
        if (helperUtil.checkFreezeStatus(existingOmdUser)) {
            if (!new Date().after(existingOmdUser.getOmdUserFreezeEndTime())){
                return Result.error("您的账户已被临时冻结，冻结原因如下：" + existingOmdUser.getOmdUserRemark());
            }
        }

        // 调用事务管理层
        String token = transactionPublicService.login(existingOmdUser);

        return Result.success(token);
    }

    /**
     * 检查登录状态
     * @return 登录状态
     */
    @GetMapping("/checkLoginStatus")
    public Result<Boolean> checkLoginStatus(HttpServletRequest request) {

        // 获取token
        String token = helperUtil.getTokenFromRequest(request);
        // 验证token
        if (token == null || !jwtUtil.validateToken(token)) {
            return Result.success(false);
        }else {
            return Result.success(true);
        }
    }

    /**
     * 获取音乐名称列表
     * @param omdMusicInfoIdList 音乐ID列表
     * @return 音乐名称列表
     */
    @PostMapping("/getMusicInfoByIdList")
    public Result<List<String>> getMusicInfoByIdList(@RequestBody List<Long> omdMusicInfoIdList) {
        if (omdMusicInfoIdList == null || omdMusicInfoIdList.isEmpty()) {
            return Result.error("音乐ID列表为空");
        }

        List<OmdMusicInfo> musicList = omdPublicService.getMusicInfoByIdList(omdMusicInfoIdList);

        // 直接返回名称列表 [ "名称 " ]
        List<String> result = musicList.stream()
                .map(OmdMusicInfo::getOmdMusicInfoName)
                .collect(Collectors.toList());

        return Result.success(result);
    }

    /**
     * 音乐播放统计信息
     * @param omdMusicInfoId 音乐ID
     * @param isGuest 是否为游客
     * @param guestUuid 游客UUID
     * @return 播放统计信息
     */
    @PostMapping("/recordMusicPlayStat")
    public Result<String> recordMusicPlayStat(@RequestParam("omdMusicInfoId") Long omdMusicInfoId,
                                              @RequestParam("isGuest") Integer isGuest,
                                              @RequestParam("guestUuid") String guestUuid,
                                              HttpServletRequest request) {

        Long omdUserId = null;
        if (isGuest == 0) {
            // 非游客，从请求头中获取用户ID
            // 获取token
            String token = helperUtil.getTokenFromRequest(request);
            // 验证token
            if (token == null || !jwtUtil.validateToken(token)) {
                return Result.error("请求头未携带有效的token");
            }else {
                omdUserId = jwtUtil.getUserIdFromJWT(token);
            }
        }

        // 确定 omdUserId 后续不再修改，使用 final 修饰
        final Long finalOmdUserId = omdUserId;

        // 构建去重键
        String deDupKey;
        if (isGuest == 1) {
            // 游客使用UUID
            deDupKey = "music:deDupKey:guest:" + omdMusicInfoId + ":" + guestUuid;
        } else if (omdUserId != null) {
            // 登录用户使用用户ID
            deDupKey = "music:deDupKey:user:" + omdMusicInfoId + ":" + finalOmdUserId;
        } else {
            // 异常情况：非游客但无用户ID
            return Result.error("无效的用户状态");
        }

        // 检查是否在去重时间窗口内已经播放过
        if (redisUtil.hasKey(deDupKey)) {
            // 10分钟内已存在播放记录，跳过统计
            return Result.success("已记录播放，跳过重复");
        }

        // 设置去重标记（10分钟有效期）
        redisUtil.set(deDupKey, "1", 10, TimeUnit.MINUTES); // 10分钟

        // 1. 写入 MySQL 流水表（异步处理，避免阻塞）
        CompletableFuture.runAsync(() -> {
            OmdMusicPlayStat omdMusicPlayStat = new OmdMusicPlayStat();
            omdMusicPlayStat.setOmdMusicInfoId(omdMusicInfoId);
            omdMusicPlayStat.setOmdUserId(finalOmdUserId);
            omdMusicPlayStat.setOmdMusicPlayStatIsGuest(isGuest);
            omdMusicPlayStat.setOmdMusicPlayStatGuestUuid(guestUuid);
            omdPublicService.addMusicPlayStat(omdMusicPlayStat);
        });

        // 2. 更新 Redis 计数器（同步处理，保障实时性）
        // 同步更新Redis
        String totalKey = "music:play:total:" + omdMusicInfoId;
        String topTotalKey = "music:play:top:total";

        // 总播放量+1
        redisUtil.updateMusicPlayStatCount(totalKey, 1, 7, TimeUnit.DAYS);
        redisUtil.updateTopMusic(topTotalKey, omdMusicInfoId, 1, 7, TimeUnit.DAYS);

        if (isGuest == 1) {
            // 游客播放量
            String guestKey = "music:play:guest:" + omdMusicInfoId;
            String topGuestKey = "music:play:top:guest";
            redisUtil.updateMusicPlayStatCount(guestKey, 1, 7, TimeUnit.DAYS);
            redisUtil.updateTopMusic(topGuestKey, omdMusicInfoId, 1, 7, TimeUnit.DAYS);
        } else {
            // 用户播放量
            String userKey = "music:play:user:" + omdMusicInfoId;
            String topUserKey = "music:play:top:user";
            redisUtil.updateMusicPlayStatCount(userKey, 1, 7, TimeUnit.DAYS);
            redisUtil.updateTopMusic(topUserKey, omdMusicInfoId, 1, 7, TimeUnit.DAYS);
        }

        return Result.success("记录成功");
    }

    /**
     * 获取音乐播放量排行
     * @param omdMusicLimit 前N名
     * @return 播放量
     */
    @GetMapping("/getTopMusic")
    public Result<Map<String, Object>> getTopMusic(@RequestParam(value = "omdMusicLimit", defaultValue = "10") int omdMusicLimit) {
        Map<String, Object> result = new HashMap<>();

        // 获取总榜TOP10
        result.put("total", redisUtil.getTopMusic("music:play:top:total", omdMusicLimit));
        // 获取游客榜TOP10
        result.put("guest", redisUtil.getTopMusic("music:play:top:guest", omdMusicLimit));
        // 获取用户榜TOP10
        result.put("user", redisUtil.getTopMusic("music:play:top:user", omdMusicLimit));

        return Result.success(result);
    }

    /**
     * 获取音乐播放量排行榜的基本信息
     * 包括音乐名称、歌手、封面等
     * 用于前端展示
     * 只返回前N名
     * @param omdMusicLimit 前N名
     * @return 排行榜数据
     */
    @GetMapping("/getTopMusicInfoList")
    public Result<Map<String, Object>> getTopMusicInfoList(@RequestParam(value = "omdMusicLimit", defaultValue = "10") int omdMusicLimit) {
        Map<String, Object> result = new HashMap<>();

        // 获取总榜TOP N
        List<OmdMusicTopVO> totalTopMusic = redisUtil.getTopMusic("music:play:top:total", omdMusicLimit);
        if (!totalTopMusic.isEmpty()) {
            List<Long> totalMusicIds = totalTopMusic.stream().map(OmdMusicTopVO::getOmdMusicInfoId).collect(Collectors.toList());
            List<OmdMusicInfo> totalMusicInfoList = omdPublicService.getMusicInfoByIdList(totalMusicIds);

            // 构建完整的总榜数据
            List<Map<String, Object>> totalRankData = helperUtil.buildRankData(totalTopMusic, totalMusicInfoList);
            result.put("totalRankData", totalRankData);
        } else {
            result.put("totalRankData", Collections.emptyList());
        }

        // 获取用户榜TOP N
        List<OmdMusicTopVO> userTopMusic = redisUtil.getTopMusic("music:play:top:user", omdMusicLimit);
        if (!userTopMusic.isEmpty()) {
            List<Long> userMusicIds = userTopMusic.stream().map(OmdMusicTopVO::getOmdMusicInfoId).collect(Collectors.toList());
            List<OmdMusicInfo> userMusicInfoList = omdPublicService.getMusicInfoByIdList(userMusicIds);

            // 构建完整的用户榜数据
            List<Map<String, Object>> userRankData = helperUtil.buildRankData(userTopMusic, userMusicInfoList);
            result.put("userRankData", userRankData);
        } else {
            result.put("userRankData", Collections.emptyList());
        }

        // 获取游客榜TOP N
        List<OmdMusicTopVO> guestTopMusic = redisUtil.getTopMusic("music:play:top:guest", omdMusicLimit);
        if (!guestTopMusic.isEmpty()) {
            List<Long> guestMusicIds = guestTopMusic.stream().map(OmdMusicTopVO::getOmdMusicInfoId).collect(Collectors.toList());
            List<OmdMusicInfo> guestMusicInfoList = omdPublicService.getMusicInfoByIdList(guestMusicIds);

            // 构建完整的游客榜数据
            List<Map<String, Object>> guestRankData = helperUtil.buildRankData(guestTopMusic, guestMusicInfoList);
            result.put("guestRankData", guestRankData);
        } else {
            result.put("guestRankData", Collections.emptyList());
        }

        return Result.success(result);
    }

    /**
     * 获取随机歌手信息
     * @return 随机歌手信息
     */
    @GetMapping("/getRandomSingersInfo")
    public Result<Map<String,Object>> getRandomSingersInfo(){

        Map<String, Object> result = new HashMap<>();

        // 获取随机歌手信息
        List<OmdSinger> randomSingers = omdPublicService.getRandomSingersInfo();

        // 构建返回结果
        if (!randomSingers.isEmpty()) {
            result.put("singers", randomSingers);
        } else {
            result.put("singers", Collections.emptyList());
        }

        return Result.success(result);
    }


    /**
     * 根据查询参数获取音乐信息列表
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param omdMusicInfoName 音乐名称
     * @param omdSingerName 歌手名称
     * @param omdMusicInfoAlbum 音乐专辑
     * @param omdMusicInfoGenre 音乐类型
     * @return 音乐信息
     */
    @GetMapping("/getMusicInfoListByQueryParams")
    public Result<PageBean<OmdMusicInfo>> getMusicInfoListByQueryParams(@RequestParam(value = "pageNum",required = false) Integer pageNum,
                                                                        @RequestParam(value = "pageSize",required = false) Integer pageSize,
                                                                        @RequestParam(value = "omdMusicInfoName",required = false) String omdMusicInfoName,
                                                                        @RequestParam(value = "omdSingerName", required = false) String omdSingerName,
                                                                        @RequestParam(value = "omdMusicInfoAlbum",required = false) String omdMusicInfoAlbum,
                                                                        @RequestParam(value = "omdMusicInfoGenre",required = false) String omdMusicInfoGenre){
        // 校验并设置默认值
        pageNum = pageNum != null && pageNum > 0 ? pageNum : 1;
        pageSize = pageSize != null && pageSize > 0 ? pageSize : 10;

        // 创建分页对象
        PageBean<OmdMusicInfo> musicInfoListByQueryParams = new PageBean<>();

        // 开启分页查询（建议使用try-with-resources确保分页范围正确）
        try (Page<OmdMusicInfo> page = PageHelper.startPage(pageNum, pageSize)) {

            // 获取音乐信息列表
            List<OmdMusicInfo> musicInfoList = omdMusicService.getMusicInfoListByQueryParams(omdMusicInfoName, omdSingerName, omdMusicInfoAlbum, omdMusicInfoGenre);

            // 设置分页数据
            musicInfoListByQueryParams.setTotal(page.getTotal());
            musicInfoListByQueryParams.setItems(musicInfoList);
        }catch (Exception e) {
            log.error("查询音乐列表失败", e);
            return Result.error("查询音乐列表失败");
        }

        // 返回正确的分页对象
        return Result.success(musicInfoListByQueryParams);
    }

    /**
     * 根据歌手ID和音乐名称获取音乐信息
     * @param omdSingerId 歌手ID
     * @param omdMusicInfoName 音乐名称
     * @return 音乐信息
     */
    @GetMapping("/getMusicInfoBySingerId")
    public Result<OmdMusicInfo> getMusicInfoBySingerId (@RequestParam("omdSingerId") Long omdSingerId,
                                                        @RequestParam("omdMusicInfoName") String omdMusicInfoName){
        OmdMusicInfo omdMusicInfo = omdPublicService.getMusicInfoBySingerId(omdSingerId, omdMusicInfoName);
        if (omdMusicInfo == null) {
            return Result.error("查询音乐信息失败");
        }
        return Result.success(omdMusicInfo);
    }


    /**
     * 验证码修改密码
     * @param newVerifyPassword 新密码
     * @param confirmNewPassword 确认密码
     */
    @PostMapping("/updatePasswordByVerifyCode")
    public Result<String> updatePasswordByVerifyCode(@RequestParam("newVerifyPassword") String newVerifyPassword,
                                                     @RequestParam("confirmNewPassword") String confirmNewPassword,
                                                     @RequestParam("verifyCode") String verifyCode,
                                                     @RequestParam("omdUserPhone") String omdUserPhone ) {

        // 获取当前用户信息
        OmdUser existingOmdUser = omdUserService.findByPhone(omdUserPhone);
        if (existingOmdUser == null) {
            log.error("用户不存在");
            return Result.error("用户不存在");
        }

        // 校验新密码是否与确认密码一致
        if (!newVerifyPassword.equals(confirmNewPassword)) {
            log.error("新密码与确认密码不一致");
            log.error("newVerifyPassword:{},confirmNewPassword:{}", newVerifyPassword, confirmNewPassword);
            return Result.error("新密码与确认密码不一致");
        }

        // 验证验证码
        if (!cosVerifyCodeService.verifyCode(existingOmdUser.getOmdUserPhone(), verifyCode)) {
            return Result.error("验证码错误");
        }

        // 更新密码
        boolean success = omdUserService.updatePassword(existingOmdUser.getOmdUserId(), passwordEncoder.encode(newVerifyPassword));
        if (!success) {
            return Result.error("密码更新失败");
        }

        String token = helperUtil.updatePasswordAndGenerateToken(existingOmdUser);

        return Result.success(token);
    }

    /**
     * 根据音乐ID获取相应音乐的歌词
     * @param omdMusicInfoId 音乐ID
     * @return 歌词URL
     */
    @GetMapping("/getMusicInfoLyricByMusicId")
    public Result<String> getMusicInfoLyricByMusicId(@RequestParam("omdMusicInfoId") Long omdMusicInfoId) {
        OmdMusicLyric omdMusicLyric = omdPublicService.getMusicInfoLyricById(omdMusicInfoId);
        if (omdMusicLyric == null) {
            return Result.success("该音乐暂无歌词");
        }
        return Result.success(omdMusicLyric.getOmdMusicLyricContentUrl());
    }

    /**
     * 根据音乐ID获取相应音乐的评论
     * @param omdMusicInfoId 音乐ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 评论列表
     */
    @GetMapping("/getCommentsHybridByMusicId")
    public Result<PageBean<OmdMusicComment>> getCommentsHybridByMusicId(@RequestParam("omdMusicInfoId") Long omdMusicInfoId,
                                                                        @RequestParam("pageNum") Integer pageNum,
                                                                        @RequestParam("pageSize") Integer pageSize) {

        return Result.success(helperUtil.executePageQuery(pageNum, pageSize, () ->
            // 查询根评论（一级评论）
            omdPublicService.getMusicCommentListByMusicId(omdMusicInfoId)
        ));
    }

    /**
     * 根据父评论ID获取子评论列表
     * @param omdMusicCommentId 父评论ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 子评论列表
     */
    @GetMapping("/getChildCommentsByParentId")
    public Result<PageBean<OmdMusicComment>> getChildCommentsByParentId(@RequestParam("omdMusicCommentId") Long omdMusicCommentId,
                                                                        @RequestParam("pageNum") Integer pageNum,
                                                                        @RequestParam("pageSize") Integer pageSize) {

        return Result.success(helperUtil.executePageQuery(pageNum, pageSize, () ->
                omdPublicService.getChildCommentsByParentId(omdMusicCommentId)
        ));
    }

    /**
     * 批量获取音乐点赞信息
     * @param omdMusicInfoIdList 音乐信息ID列表
     * @return 音乐点赞信息列表
     */
    @GetMapping("/getMusicLikeInfoList")
    public Result<List<OmdMusicLikeInfo>> getMusicLikeInfoList(@RequestParam("omdMusicInfoIdList") List<Long> omdMusicInfoIdList,
                                                                HttpServletRequest request) {
        // 1. 获取点赞数
        List<OmdMusicLikeCache> likeCaches = omdMusicService.selectByOmdMusicInfoIdList(omdMusicInfoIdList);
        Map<Long, Long> likeCountMap = likeCaches.stream()
                .collect(Collectors.toMap(OmdMusicLikeCache::getOmdMusicInfoId, OmdMusicLikeCache::getOmdMusicLikeCacheCount));

        // 2. 获取用户点赞状态

        Long omdUserId;
        // 获取token
        String token = helperUtil.getTokenFromRequest(request);
        // 验证token
        if (token == null || !jwtUtil.validateToken(token)) {
            omdUserId = null;
        }else {
            omdUserId = jwtUtil.getUserIdFromJWT(token);
        }

        Map<Long, Boolean> userLikeMap;
        if (omdUserId != null) {
            List<OmdMusicLike> userLikes = omdMusicService.selectByUserIdAndMusicIds(omdUserId, omdMusicInfoIdList);
            userLikeMap = userLikes.stream()
                    .collect(Collectors.toMap(OmdMusicLike::getOmdMusicInfoId, like -> true));
        } else {
            userLikeMap = Collections.emptyMap();
        }

        // 3. 组装结果
        return Result.success(omdMusicInfoIdList.stream()
                .map(omdMusicInfoId -> {
                    OmdMusicLikeInfo dto = new OmdMusicLikeInfo();
                    dto.setOmdMusicInfoId(omdMusicInfoId);
                    dto.setOmdMusicLikeCacheCount(likeCountMap.getOrDefault(omdMusicInfoId, Long.valueOf(0)));
                    dto.setIsUserLiked(userLikeMap.getOrDefault(omdMusicInfoId, false));
                    return dto;
                })
                .collect(Collectors.toList()));
    };

    /**
     * 提交申诉
     * @param omdUserAppealReason 申诉原因
     * @param omdUserPhone 手机号
     * @return 结果
     */
    @PostMapping("/submitAppeal")
    public Result<String> submitAppeal(@RequestParam("omdUserAppealReason") String omdUserAppealReason,
                                       @RequestParam("omdUserPhone") String omdUserPhone) {
        // 查询用户是否存在
        OmdUser user = omdUserService.findByPhone(omdUserPhone);
        if (user == null) {
            return Result.error("用户不存在");
        }
        // 校验用户是否处于冻结状态
        if (user.getOmdUserStatus() != 0) {
            return Result.error("只有冻结状态的用户才可申诉");
        }
        // 新增申诉记录
        OmdUserAppeal appeal = new OmdUserAppeal();
        appeal.setOmdUserId(user.getOmdUserId());
        appeal.setOmdUserAppealReason(omdUserAppealReason);
        if (!omdPublicService.addOmdUserAppeal(appeal)) {
            return Result.error("提交申诉失败");
        }
        return Result.success("提交申诉成功");
    }

}
