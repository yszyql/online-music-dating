package org.x.backend.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OmdMusicReport {
    private Long omdMusicReportId;
    private Long omdUserId;           // 举报人ID
    private Long omdMusicInfoId;      // 被举报音乐ID
    private String omdMusicReportReason; // 举报原因
    private Integer omdMusicReportType;  // 举报类型：1-盗版侵权；2-色情低俗；3-暴力血腥；4-政治敏感；5-其他违规
    private Integer omdMusicReportStatus; // 处理状态：0-待审核；1-已受理（违规成立）；2-已驳回（违规不成立）
    private String omdMusicReportEvidence; // 举报证据
    private Long omdAdminId;          // 处理管理员ID
    private String omdMusicReportHandleRemark; // 处理备注

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date omdMusicReportCreateTime; // 举报提交时间

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date omdMusicReportHandleTime; // 处理时间

    private OmdMusicInfo omdReportedMusic; // 被举报的音乐信息
}