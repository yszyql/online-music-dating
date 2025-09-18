package org.x.backend.pojo;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


/**
 * 用户角色实体类
 * 中间表
 * 用于表示用户与角色之间的关联关系
 * 包含用户ID和角色编码
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "omdUserId")
public class OmdUserRole implements Serializable {
    private Long omdUserRoleId;  // 主键ID
    private Long omdUserId;  // 用户ID
    private String omdRoleCode;  // 角色编码
}
