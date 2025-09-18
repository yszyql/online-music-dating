package org.x.backend.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 管理员表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OmdAdmin implements Serializable {

    private Long omdAdminId; // 主键ID
    private String omdAdminName; // 管理员用户名

    @Pattern(regexp = "^\\S{6,16}$")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // 只在写入（接收请求）时包含
    private String omdAdminPassword; // 管理员密码

    private String omdAdminPhone; // 手机号

    @Email(message = "邮箱格式不正确")
    private String omdAdminEmail; // 邮箱
    private String omdAdminAvatar; // 头像
    private Integer omdAdminRole; // 角色（0-普通管理员，1-超级管理员）
    private Integer omdAdminStatus; // 状态，1：启用，0：禁用

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date omdAdminCreateTime; // 创建时间

}
