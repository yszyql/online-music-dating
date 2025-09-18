package org.x.backend.service.impl;

import org.x.backend.utils.RedisUtil;
import org.x.backend.utils.TencentSmsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class CosVerifyCodeService {

    @Autowired
    private TencentSmsUtil tencentSmsUtil;

    @Autowired
    private RedisUtil redisUtil;

    private static final String VERIFY_CODE_KEY = "verify_code:%s";
    private static final long EXPIRE_TIME = 5; // 5分钟过期

    /**
     * 发送手机验证码
     */
    public String sendSmsVerifyCode(String phone) {
        String verifyCode = tencentSmsUtil.generateVerifyCode();
        boolean sendSuccess = tencentSmsUtil.sendVerifyCode(phone, verifyCode);

        if (sendSuccess) {
            // 存储验证码到Redis
            redisUtil.set(
                    String.format(VERIFY_CODE_KEY, phone),
                    verifyCode,
                    EXPIRE_TIME,
                    TimeUnit.MINUTES
            );
        }

        return sendSuccess ? verifyCode : null;
    }

    /**
     * 验证手机验证码
     */
    public boolean verifyCode(String phone, String code) {
        String storedCode = redisUtil.get(String.format(VERIFY_CODE_KEY, phone));
        if (storedCode == null) {
            return false;
        }
        // 验证后删除验证码
        redisUtil.delete(String.format(VERIFY_CODE_KEY, phone));
        return storedCode.equals(code);
    }
}