package org.x.backend.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OmdOperationLog {

    private Long omdOperationLogId;

    @NotBlank(message = "管理员ID不能为空")
    private Long omdAdminId;

    @NotBlank(message = "操作对象ID不能为空")
    private Long omdOperationLogTargetId;

    @NotBlank(message = "操作对象类型不能为空")
    private String omdOperationLogTargetType;

    @NotBlank(message = "操作类型不能为空")
    private String omdOperationLogType;

    @NotBlank(message = "操作描述不能为空")
    private String omdOperationLogDesc;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private String omdOperationLogTime;

    private OmdAdmin omdAdmin;
}
