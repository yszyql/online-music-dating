package org.x.backend.pojo;

import lombok.Data;

import java.io.Serializable;

/**
 * 角色权限实体类
 * 中间表，用于存储角色和权限的关联关系
 * 注意：这个类通常不直接映射到数据库表，而是作为中间表来存储角色和权限的关联关系
 * 可以通过 MyBatis 的 @ManyToMany 注解来实现多对多关系的映射
 * 也可以通过 JPA 的 @ManyToMany 注解来实现多对多关系的映射
 * 也可以通过 Spring Data JPA 的 @ManyToMany 注解来实现多对多关系的映射
 */
@Data
public class OmdRolePermission implements Serializable {
    private String omdRoleCode; // 新增角色代码字段
    private String omdPermissionCode; // 新增权限代码字段
}
