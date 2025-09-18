package org.x.backend.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OmdUserReport {
    private Long omdUserReportId;
    private Long omdUserId;           // 举报人ID
    private Long omdUserReportedUserId; // 被举报用户ID
    private String omdUserReportReason; // 举报原因
    private Integer omdUserReportType;  // 举报类型：1-垃圾信息；2-侮辱谩骂；3-欺诈；4-其他违规
    private Integer omdUserReportStatus; // 处理状态 0-未处理 1-已处理 2-已驳回
    private String omdUserReportEvidence; // 举报证据
    private Long omdAdminId;          // 处理管理员ID
    private String omdUserReportHandleRemark; // 处理备注

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date omdUserReportCreateTime; // 举报提交时间

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date omdUserReportHandleTime; // 处理时间

    private OmdUser omdUser;             // 举报人信息

    private OmdUser omdReportedUser;   // 被举报用户信息
}