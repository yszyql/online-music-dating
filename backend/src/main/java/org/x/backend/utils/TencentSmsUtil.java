package org.x.backend.utils;

import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.sms.v20210111.models.SendSmsResponse;
import com.tencentcloudapi.sms.v20210111.models.SendSmsRequest;
import com.tencentcloudapi.sms.v20210111.SmsClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Random;

@Slf4j
@Component
public class TencentSmsUtil {

    @Value("${tencent.cos.secret-id}")
    private String secretId;

    @Value("${tencent.cos.secret-key}")
    private String secretKey;

    @Value("${tencent.cos.region}")
    private String region;

    @Value("${tencent.sms.app-id}")
    private String sdkAppId;

    @Value("${tencent.sms.sign-name}")
    private String signName;

    @Value("${tencent.sms.template-id}")
    private String templateId;

    private static final Random random = new Random();

    /**
     * 生成6位随机验证码
     */
    public String generateVerifyCode() {
        return String.format("%06d", random.nextInt(999999));
    }

    /**
     * 发送短信验证码
     */
    public boolean sendVerifyCode(String phoneNumber, String verifyCode) {
        try {
            // 腾讯云短信发送逻辑
            Credential cred = new Credential(secretId, secretKey);
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint("sms.tencentcloudapi.com");

            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            SmsClient client = new SmsClient(cred, region, clientProfile);

            String[] phoneNumbers = {phoneNumber};
            String[] templateParams = {verifyCode, "5"}; // 验证码和过期时间(分钟)

            SendSmsRequest req = new SendSmsRequest();
            req.setSmsSdkAppId(sdkAppId);
            req.setSignName(signName);
            req.setTemplateId(templateId);
            req.setPhoneNumberSet(phoneNumbers);
            req.setTemplateParamSet(templateParams);

            SendSmsResponse resp = client.SendSms(req);
            log.info("短信发送结果: " + resp.toString());
            return true;
        } catch (TencentCloudSDKException e) {
            log.error("短信发送失败", e);
            return false;
        }
    }
}