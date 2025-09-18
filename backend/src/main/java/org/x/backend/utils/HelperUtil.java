package org.x.backend.utils;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.x.backend.pojo.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Component
@Slf4j
public class HelperUtil {

    @Value("${jwt.token-header}")
    private String tokenHeader;
    @Value("${jwt.token-prefix}")
    private String tokenPrefix;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private JwtUtil jwtUtil;

    // 辅助方法：验证输入是否为有效标识
    public boolean isValidIdentifier(String identifier) {
        return isOmdUserName(identifier) || isOmdUserPhone(identifier);
    }

    // 辅助方法：判断是否为用户名
    public boolean isOmdUserName(String omdUserName) {
        return omdUserName.matches("^[\\u4e00-\\u9fa5a-zA-Z0-9_]{2,16}$");
    }

    // 辅助方法：判断是否为手机号
    public boolean isOmdUserPhone(String omdUserPhone) {
        return omdUserPhone.matches("^1[3-9]\\d{9}$");
    }

    // 辅助方法：生成token并存入redis
    public String generateTokenAndSave(OmdUser loginUser) {
        // 构建Redis Key（格式：user:token:{userId}）
        String redisKey = "user:token:" + loginUser.getOmdUserId();

        // 检查Redis中是否存在该用户的有效Token
        String existingToken = redisUtil.get(redisKey);
        if (existingToken != null) {
            // 若存在有效Token，直接返回（不重复存入）
            log.info("用户[{}]已有有效Token，直接返回", loginUser.getOmdUserId());
            return existingToken;
        }

        // 登录成功准备JWT
        Map<String, Object> claims = new HashMap<>();
        // 将用户信息存入claims
        claims.put("omdUserId", loginUser.getOmdUserId());
        claims.put("omdUserName", loginUser.getOmdUserName());
        claims.put("omdUserPhone", loginUser.getOmdUserPhone());
        log.info("用户[{}]登录成功", loginUser.getOmdUserId());

        // 生成token
        String token = jwtUtil.generateToken(claims);

        // 将token存入redis,过期时间与工具类总JwtUtil中token的存活时间一致
        redisUtil.set(redisKey,token,24, TimeUnit.HOURS);
        log.info("用户[{}]生成新Token并存储到Redis", loginUser.getOmdUserId());

        return token;
    }

    // 辅助方法：生成用户名
    public String generateUsername(String phone) {
        return "user_" + phone.substring(phone.length() - 4); // 截取后四位作为用户名后缀
    }

    // 辅助方法：从 ThreadLocal 获取用户
    public Long getCurrentUserId() {
        Map<String, Object> claims = ThreadLocalUtil.get();
        if (claims == null) {
            log.warn("未获取到用户信息");
            return null;
        }
        Long omdUserId = (Long) claims.get("omdUserId");

        return omdUserId;
    }

    // 辅助方法：更新密码后生成新的token并存入redis中
    public String updatePasswordAndGenerateToken(OmdUser existingOmdUser) {

        // 删除旧Token
        String redisKey = "user:token:" + existingOmdUser.getOmdUserId();
        String storedToken = redisUtil.get(redisKey);
        if (storedToken == null) {
            log.warn("用户[{}]的Token不存在", existingOmdUser.getOmdUserId());
        } else {
            Boolean deleted = redisUtil.delete(redisKey);
            if (deleted) {
                log.info("用户[{}]的Token已删除", existingOmdUser.getOmdUserId());
            } else {
                log.warn("用户[{}]的Token删除失败", existingOmdUser.getOmdUserId());
            }
        }

        // 生成新 Token（使用 jwtUtil）
        // 生成token并存入redis
        String token = generateTokenAndSave(existingOmdUser);
        return token;
    }

    // 辅助方法：校验用户信息是否完善
    public boolean isUserInfoComplete(OmdUser existingOmdUser) {

        // 校验关键字段
        return StringUtils.isNotBlank(existingOmdUser.getOmdUserNickname()) // 昵称
                && existingOmdUser.getOmdUserGender() != 2 // 性别
                && StringUtils.isNotBlank(existingOmdUser.getOmdUserRegion()) // 地区
                && existingOmdUser.getOmdUserBirthday() != null // 生日
                && StringUtils.isNotBlank(existingOmdUser.getOmdUserAvatar()) // 头像
                && StringUtils.isNotBlank(existingOmdUser.getOmdUserEmail()); // 邮箱
    }

    // 辅助方法：封装分页查询
    public <T> PageBean<T> executePageQuery(Integer pageNum, Integer pageSize, Supplier<List<T>> querySupplier) {
        // 校验并设置默认值
        pageNum = pageNum != null && pageNum > 0 ? pageNum : 1;
        pageSize = pageSize != null && pageSize > 0 ? pageSize : 10;

        PageBean<T> pageResult = new PageBean<>();

        try (Page<T> page = PageHelper.startPage(pageNum, pageSize)) {
            // 执行查询逻辑
            List<T> list = querySupplier.get();

            // 设置分页数据
            pageResult.setTotal(page.getTotal());
            pageResult.setItems(list);
        } catch (Exception e) {
            log.error("分页查询失败", e);
            throw new RuntimeException("分页查询失败", e);
        }

        return pageResult;
    }

    // 辅助方法：从请求头中获取 Token
    public String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(tokenHeader);
        if (org.springframework.util.StringUtils.isEmpty(bearerToken)) {
            return null;
        }

        // 1. 清理首尾空格，避免前端传递多余空格
        String cleanedToken = org.springframework.util.StringUtils.trimWhitespace(bearerToken);

        // 2. 校验前缀（tokenPrefix 建议配置为 "Bearer "，这里兼容 trim 后匹配）
        String prefix = org.springframework.util.StringUtils.trimWhitespace(tokenPrefix);
        if (cleanedToken.startsWith(prefix)) {
            // 3. 截取 Token（再次清理，确保无残留空格）
            return cleanedToken.substring(prefix.length()).trim();
        }

        log.warn("public接口 Token 前缀不匹配，原始 Token: {}", bearerToken);
        return null;
    }

    // 辅助方法：返回歌曲榜单列表
    public List<Map<String, Object>> buildRankData(List<OmdMusicTopVO> topList, List<OmdMusicInfo> infoList) {
        // 先构建ID到音乐信息的映射
        Map<Long, OmdMusicInfo> infoMap = infoList.stream()
                .collect(Collectors.toMap(OmdMusicInfo::getOmdMusicInfoId, info -> info));

        // 构建最终返回的排行榜数据
        return topList.stream()
                .map(top -> {
                    OmdMusicInfo info = infoMap.get(top.getOmdMusicInfoId());
                    Map<String, Object> item = new HashMap<>();
                    item.put("omdMusicInfoId", top.getOmdMusicInfoId());
                    item.put("omdMusicInfoName", info != null ? info.getOmdMusicInfoName() : "未知歌曲");
                    item.put("omdMusicInfoCoverUrl", info != null ? info.getOmdMusicInfoCoverUrl() : "");
                    item.put("omdMusicInfoStatus", info!= null? info.getOmdMusicInfoStatus() : "");
                    item.put("playCount", top.getPlayCount());
                    item.put("omdMusicInfoSongUrl", info != null ? info.getOmdMusicInfoSongUrl() : "");
                    item.put("omdMusicInfoDuration", info != null ? info.getOmdMusicInfoDuration() : "00:00");
                    item.put("omdMusicInfoAlbum", info!= null? info.getOmdMusicInfoAlbum() : "未知专辑");
                    item.put("omdSingerId", info!= null? info.getOmdSingerId() : "未知歌手ID");
                    item.put("omdSingerName", info != null ? info.getOmdSinger().getOmdSingerName() : "未知歌手");
                    return item;
                })
                .collect(Collectors.toList());
    }

    // 辅助方法：安全类型转换方法
    public Boolean convertToBoolean(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof Number) return ((Number) value).intValue() != 0;
        if (value instanceof String) return Boolean.parseBoolean((String) value);
        return false;
    }

    // 辅助方法：判断用户是否被冻结
    public boolean checkFreezeStatus(OmdUser user) {
        // 检查用户状态是否为冻结
        if (user.getOmdUserStatus() == 0 // 冻结状态
                && user.getOmdUserFreezeType() == 1 // 临时冻结
                && user.getOmdUserFreezeEndTime() != null) {
            return true;
        }
        return false;
    }

    // 辅助方法：新增操作日志
    public OmdOperationLog setOmdOperationLog(Long omdAdminId,String type,String desc,Long targetId,String targetType) {
        OmdOperationLog omdOperationLog = new OmdOperationLog();
        omdOperationLog.setOmdAdminId(omdAdminId);
        omdOperationLog.setOmdOperationLogType(type);
        omdOperationLog.setOmdOperationLogDesc(desc);
        omdOperationLog.setOmdOperationLogTargetId(targetId);
        omdOperationLog.setOmdOperationLogTargetType(targetType);
        return omdOperationLog;
    }
}
