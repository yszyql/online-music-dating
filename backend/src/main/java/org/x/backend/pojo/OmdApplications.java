package org.x.backend.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import java.io.Serializable;
import java.util.Date;


/**
 * 歌手申请表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OmdApplications implements Serializable {
    private long omdApplicationsId;  // 主键ID

    private long omdUserId; // 用户ID

    private String omdSingerName;  // 歌手姓名

    private String omdApplicationsIntroduction;  // 歌手简介

    private String omdApplicationsGenre;  // 歌手类型

    private String omdApplicationsSingSample;  // 歌手作品示例url

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date omdApplicationsApplyTime;  // 申请时间

    private Integer omdApplicationsStatus;  // 申请状态 0-待审核 1-已通过 2-已驳回

    private String omdAdminName;  // 审核管理员用户名

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date omdApplicationsReviewTime;  // 审核时间

    private String omdApplicationsReviewRemark;  // 审核备注

}
