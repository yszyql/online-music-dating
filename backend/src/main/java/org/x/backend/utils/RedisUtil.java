package org.x.backend.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;
import org.x.backend.pojo.OmdMessage;
import org.x.backend.pojo.OmdMusicTopVO;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Redis工具类
 */
@Component // 标记为Spring组件，使其可以被注入
@RequiredArgsConstructor // 生成构造函数，用于注入依赖
@Slf4j // 生成日志记录器
public class RedisUtil {

    // RedisTemplate，用于操作Redis数据库
    private final RedisTemplate<String, Object> redisTemplate;

    // 键名常量
    private static final String ONLINE_USERS_KEY = "online_users";
    // 离线消息键名前缀
    private static final String OFFLINE_MESSAGES_KEY = "offline_messages:";


    /**
     * 检查键是否存在
     * @param key 键名
     * @return 如果存在返回 true，否则 false
     */
    public boolean hasKey(String key) {
        try {
            Boolean exists = redisTemplate.hasKey(key);
            return exists != null && exists;
        } catch (Exception e) {
            // 记录异常但不中断流程
            log.error("Redis hasKey error: {}", e.getMessage(), e);
            return false;
        }
    }

    // 普通缓存获取
    /**
     * @param key 键
     * @return 值
     */
    public <T> T get(String key) {
        return key == null ? null : (T) redisTemplate.opsForValue().get(key);
    }

    // 普通缓存放入
    /**
     * @param key   键
     * @param value 值
     * @return true成功 false失败
     */
    public <T> boolean set(String key, T value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // 普通缓存放入并设置时间
    /**
     * @param key   键
     * @param value 值
     * @param time  时间(秒) time要大于0 如果time小于等于0 将设置无限期
     * @return true成功 false 失败
     */
    public <T> boolean set(String key, T value, long time, TimeUnit unit) {
        try {
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, unit);
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // 删除缓存
    /**
     * @param key 可以传一个值 或多个
     */
    public boolean delete(String... key) {
        if (key != null && key.length > 0) {
            if (key.length == 1) {
                redisTemplate.delete(key[0]);
            } else {
                redisTemplate.delete(java.util.Arrays.asList(key));
            }
            return true;
        }
        return false;
    }

    // 设置过期时间
    /**
     * @param key  键
     * @param time 时间(秒)
     * @return true成功 false失败
     */
    public boolean expire(String key, long time, TimeUnit unit) {
        try {
            if (time > 0) {
                redisTemplate.expire(key, time, unit);
            }
            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // 用户上线
    public void userOnline(Long omdUserId) {
        log.info("用户{}已标记为在线", omdUserId);
        redisTemplate.opsForSet().add(ONLINE_USERS_KEY, omdUserId);
    }

    // 用户下线
    public void userOffline(Long omdUserId) {
        log.info("用户{}已标记为离线", omdUserId);
        redisTemplate.opsForSet().remove(ONLINE_USERS_KEY, omdUserId);
    }

    // 检查用户是否在线
    public boolean isUserOnline(Long omdUserId) {
        log.info("检查用户{}是否在线", omdUserId);
        log.info("当前在线用户: {}", redisTemplate.opsForSet().members(ONLINE_USERS_KEY));
        return redisTemplate.opsForSet().isMember(ONLINE_USERS_KEY, omdUserId);
    }

    // 保存离线消息
    public void saveOfflineMessage(Long omdUserId, OmdMessage omdMessage) {
        String key = OFFLINE_MESSAGES_KEY + omdUserId;
        redisTemplate.opsForList().rightPush(key, omdMessage);
    }

    // 获取并清除离线消息
    @SuppressWarnings("unchecked")
    public List<OmdMessage> getAndClearOfflineMessages(Long omdUserId) {
        String key = OFFLINE_MESSAGES_KEY + omdUserId;
        List<Object> messages = redisTemplate.opsForList().range(key, 0, -1);
        redisTemplate.delete(key);

        return messages.stream()
                .map(obj -> (OmdMessage) obj)
                .collect(Collectors.toList());
    }

    // 更新音乐播放次数
    /**
     * @param key 键
     * @param delta 要增加几(大于0)
     * @return
     */
    public void updateMusicPlayStatCount(String key,long delta, long time, TimeUnit timeUnit) {
        // 执行自增操作
        redisTemplate.opsForValue().increment(key, delta);

        // 设置过期时间
        expire(key, time, timeUnit);
    }

    // 更新热门音乐排行榜
    /**
     * @param topKey 排行榜键
     * @param omdMusicInfoId 音乐ID
     * @param delta 要增加几(大于0)
     * @return
     */
    public void updateTopMusic(String topKey ,Long omdMusicInfoId, long delta , long time, TimeUnit timeUnit) {
        // 更新有序集合中的分数
        redisTemplate.opsForZSet().incrementScore(topKey, omdMusicInfoId.toString(), delta);

        // 设置排行榜的过期时间
        expire(topKey, time, timeUnit);
    }

    // 获取排行榜
    /**
     * @param key 键
     * @param limit 前几名
     * @return
     */
    public List<OmdMusicTopVO> getTopMusic(String key, int limit) {
        // 获取结果并进行类型转换
        Set<ZSetOperations.TypedTuple<Object>> set = redisTemplate.opsForZSet()
                .reverseRangeWithScores(key, 0, limit - 1);

        return set.stream().map(tuple ->
                new OmdMusicTopVO(
                        Long.parseLong(tuple.getValue().toString()),
                        tuple.getScore()
                )).collect(Collectors.toList());
    }

}