package org.x.backend.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OmdUserAppeal {
    private Long omdUserAppealId;

    private Long omdUserId;  // 被冻结用户ID

    private String omdUserAppealReason; // 申诉理由

    private Integer omdUserAppealStatus; // 申诉状态：0-待审核，1-通过，2-驳回

    private Long omdAdminId;

    private String omdUserAppealResult; // 处理结果

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date omdUserAppealCreateTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date omdUserAppealHandleTime;

    private OmdUser omdAppealUser; // 被冻结用户信息
}
