package org.x.backend.service.impl;

import com.tencentcloudapi.common.AbstractModel;
import com.tencentcloudapi.hunyuan.v20230901.HunyuanClient;
import com.tencentcloudapi.hunyuan.v20230901.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.x.backend.config.HunYuanClientConfig;

@Service
public class HunYuanService {

    private final HunyuanClient client;
    private final String model;

    @Autowired
    public HunYuanService(HunYuanClientConfig factory,
                          @Value("${tencent.hunyuan.model}") String model) {
        this.client = factory.createClient();
        this.model = model;
    }

    public String generateText(String prompt) throws Exception {
        ChatCompletionsRequest req = new ChatCompletionsRequest();
        req.setModel(model);
        // 关键：显式关闭流式响应（必须设置，确保结果结构完整）
        req.setStream(false);
        // 打印请求参数（确认模型和prompt正确）
        System.out.println("请求参数：" + AbstractModel.toJsonString(req));

        Message[] messages1 = new Message[1];
        Message message1 = new Message();
        message1.setRole("user");
        message1.setContent(prompt); // 确保prompt正确传入
        messages1[0] = message1;
        req.setMessages(messages1);
        req.setTemperature(0.8f);
        req.setTopP(0.9f);

        // 调用API并打印完整响应
        ChatCompletionsResponse resp = client.ChatCompletions(req);

        // 提取结果（严格检查非空）
        if (resp.getChoices() == null || resp.getChoices().length == 0) {
            throw new RuntimeException("响应结果为空，Choices长度为0");
        }
        if (resp.getChoices()[0].getMessage() == null) {
            throw new RuntimeException("响应结果中Message为空");
        }
        if (resp.getChoices()[0].getMessage().getContent() == null) {
            throw new RuntimeException("响应结果中Content为空");
        }

        return resp.getChoices()[0].getMessage().getContent();
    }

}
