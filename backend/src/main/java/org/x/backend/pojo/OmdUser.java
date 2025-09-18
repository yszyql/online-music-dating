package org.x.backend.pojo;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import java.io.Serializable;
import java.util.*;

/**
 * 用户实体类
 * 一个用户可以有多个角色，一个角色可以有多个用户
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "omdUserId")
public class OmdUser implements Serializable {

    private Long omdUserId; // 主键

    @NotEmpty
    @Pattern(regexp = "^[\\u4e00-\\u9fa5a-zA-Z0-9_]{2,16}$",
            message = "用户名只能包含中文、英文、数字和下划线，长度2-16")
    private String omdUserName; // 用户名

    @NotBlank
    @Pattern(regexp = "^\\S{6,16}$")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // 只在写入（接收请求）时包含
    private String omdUserPassword; // 密码

    @NotEmpty
    @Pattern(regexp = "^1[3-9]\\d{9}$")
    private String omdUserPhone; // 电话

    @Email
    private String omdUserEmail; // 邮箱

    // 新增：将status转换为enabled
    public boolean isEnabled() {
        return omdUserStatus != null && omdUserStatus == 1;
    }

    private String omdUserNickname;

    private Integer omdUserGender; // 性别（1-男；2-女；0-未知）

    private String omdUserRegion; // 地区

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date omdUserBirthday; // 生日

    @URL(regexp = "^(https?|ftp)://.*$", message = "头像必须是有效的URL")
    private String omdUserAvatar; // 头像url

    private Integer omdUserStatus; // 状态（1-启用，0-禁用）

    private String omdUserRemark; // 冻结原因

    private Integer omdUserFreezeType; // 冻结类型（0-无冻结，1-临时冻结，2-永久冻结）

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date omdUserFreezeEndTime; // 冻结结束时间 (仅在临时冻结时使用， 永久冻结时为 null)

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date omdUserCreateTime; // 创建时间

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date omdUserUpdateTime; // 更新时间

    private List<OmdUserRole> omdUserRole = new ArrayList<>();

    private List<OmdRole> omdRole = new ArrayList<>();

    private List<OmdPermission> omdPermissions = new ArrayList<>();

    private Set<OmdFriend> omdFriends = new HashSet<>();

    private OmdSinger omdSinger;

}