package org.x.backend.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OmdMessage implements Serializable {
    private Long omdMessageId; // 消息ID

    @NotNull(message = "发送者ID不能为空")
    private Long omdUserId; // 发送者ID

    @NotNull(message = "接收者ID不能为空")
    private Long omdFriendUserId; // 接收者ID

    @NotBlank(message = "消息内容不能为空")
    private String omdMessageContent; // 消息内容

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date omdMessageSendTime; // 消息发送时间


    private Integer omdMessageReadStatus; // 0: 未读, 1: 已读
}
