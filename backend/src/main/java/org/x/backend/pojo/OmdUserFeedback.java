package org.x.backend.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OmdUserFeedback {
    private Long omdUserFeedbackId; // 主键ID
    private Long omdUserId; // 用户ID

    @NotNull(message = "反馈类型不能为空")
    private Integer omdUserFeedbackType; // 反馈类型

    @NotBlank(message = "反馈内容不能为空")
    private String omdUserFeedbackContent; // 反馈内容

    private String omdUserFeedbackContact; // 联系方式
    private Integer omdUserFeedbackStatus; // 反馈状态

    private String omdUserFeedbackResponse; // 反馈回复

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date omdUserFeedbackCreateTime; // 反馈创建时间

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date omdUserFeedbackUpdateTime; // 反馈更新时间
}
