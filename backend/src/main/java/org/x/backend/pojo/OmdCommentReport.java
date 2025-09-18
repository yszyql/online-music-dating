package org.x.backend.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OmdCommentReport {

    private Long omdCommentReportId; // 主键ID
    private Long omdUserId; // 用户ID

    @NotNull(message = "评论ID不能为空")
    private Long omdMusicCommentId; // 评论ID

    @NotNull(message = "举报类型不能为空")
    private Integer omdCommentReportReason; // 举报类型

    @NotBlank(message = "举报描述不能为空")
    private String omdCommentReportDescription; // 举报描述

    private Integer omdCommentReportStatus; // 举报状态

    private String omdAdminName; // 处理人姓名

    private String omdCommentReportRemark; // 处理备注

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date omdCommentReportHandleTime; // 处理时间

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date omdCommentReportCreateTime; // 创建时间

    private OmdMusicComment omdMusicComment; // 评论信息
}
