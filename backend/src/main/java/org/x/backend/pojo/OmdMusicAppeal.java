package org.x.backend.pojo;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OmdMusicAppeal {
    private Long omdMusicAppealId;
    private Long omdSingerId;         // 申诉歌手ID
    private Long omdMusicInfoId;      // 被申诉音乐ID
    private Long omdMusicReportId;    // 原举报ID
    private String omdMusicAppealReason; // 申诉理由
    private String omdMusicAppealEvidence; // 申诉证据
    private Integer omdMusicAppealStatus; // '申诉状态：0-待审核；1-申诉通过；2-申诉驳回
    private Long omdAdminId;          // 处理管理员ID
    private String omdMusicAppealHandleRemark; // 处理备注

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date omdMusicAppealCreateTime; // 申诉提交时间

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date omdMusicAppealHandleTime; // 处理时间

    private OmdMusicInfo omdAppealMusicInfo; // 被申诉的音乐信息
}