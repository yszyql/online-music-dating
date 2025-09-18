package org.x.backend.pojo;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * 权限实体类
 * 一个角色可以有多个权限，一个权限可以被多个角色拥有
 * 因此，权限实体类与角色实体类之间存在多对多的关系
 * 为了避免序列化循环引用，需要在权限实体类中添加 @JsonBackReference 注解
 * 同时，在角色实体类中添加 @JsonManagedReference 注解
 * 这样，在序列化时，权限实体类中的角色集合不会被序列化，角色实体类中的权限集合也不会被序列化
 * 这样就可以避免序列化循环引用的问题
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "omdPermissionId")
public class OmdPermission implements Serializable {
    private Long omdPermissionId; // 主键ID
    private String omdPermissionCode; // 权限编码（如 music:upload）
    private String omdPermissionName; // 权限名称
    private String omdPermissionDescription; // 描述

    // 添加角色集合（多对多反向关联）
    //@JsonBackReference // 避免序列化循环引用
    private Set<OmdRole> omdRoles = new HashSet<>();
}
