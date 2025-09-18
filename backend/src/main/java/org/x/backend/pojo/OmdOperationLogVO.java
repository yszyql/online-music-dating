package org.x.backend.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OmdOperationLogVO {
    // 日志表原有字段
    private Long omdOperationLogId;
    private Long omdAdminId;
    private Long omdOperationLogTargetId;
    private String omdOperationLogTargetType;
    private String omdOperationLogType;
    private String omdOperationLogDesc;
    private Date omdOperationLogTime;

    // 新增：被操作对象的详情（从业务表查询）
    private String targetName; // 对象名称（如用户名、音乐名）
    private String targetDetail; // 对象其他信息（如用户昵称、歌手名）
    private String adminName; // 操作管理员名称（可关联管理员表查询）
}
