package org.x.backend.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.x.backend.service.impl.HunYuanService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/ai")
@Slf4j
public class HunYuanController {

    @Autowired
    private HunYuanService hunYuanService;

    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateText(@RequestBody String prompt) {
        try {
            log.info("Received prompt: {}", prompt);
            String result = hunYuanService.generateText(prompt);

            // 返回JSON格式
            Map<String, Object> response = new HashMap<>();
            response.put("code", 0);
            response.put("message", "请求成功");
            response.put("data", result);
            log.info("Generated text: {}", response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "请求失败：" + e.getMessage());

            return ResponseEntity.status(500).body(response);
        }
    }

}
