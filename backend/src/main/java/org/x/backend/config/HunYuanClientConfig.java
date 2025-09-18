package org.x.backend.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.hunyuan.v20230901.HunyuanClient;

@Component
public class HunYuanClientConfig {

    @Value("${tencent.cloud.secret-id}")
    private String secretId;

    @Value("${tencent.cloud.secret-key}")
    private String secretKey;

    @Value("${tencent.hunyuan.endpoint}")
    private String endpoint;

    public HunyuanClient createClient() {
        Credential cred = new Credential(secretId, secretKey);
        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setEndpoint(endpoint);

        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setHttpProfile(httpProfile);

        return new HunyuanClient(cred, "", clientProfile);
    }

}
