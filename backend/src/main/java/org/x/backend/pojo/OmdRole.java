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
 * 角色实体类
 * 一个用户可以有多个角色，一个角色可以被多个用户拥有
 * 因此，在 OmdRole 类中，我们使用 Set<Users> 来表示一个角色被哪些用户拥有。
 * 在 Users 类中，我们使用 Set<OmdRole> 来表示一个用户拥有哪些角色。
 * 这样，我们就可以通过 OmdRole 对象来查询哪些用户拥有这个角色，
 * 或者通过 Users 对象来查询这个用户拥有哪些角色。
 * 同时，我们还需要在 OmdRole 和 Users 类中添加 @JsonBackReference 和 @JsonManagedReference 注解，
 * 来避免序列化循环引用的问题。
 * 这样，当我们查询一个角色时，它的 users 集合中的每个 Users 对象都不会被序列化，
 * 从而避免了序列化循环引用的问题。
 * 同理，一个角色可以有多个权限，因此在 OmdRole 类中，我们使用 Set<OmdPermission> 来表示一个角色拥有哪些权限。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "omdRoleId")
public class OmdRole implements Serializable {

    private Long omdRoleId; // 主键字段
    private String omdRoleCode; // 角色编码（如 ROLE_USER、ROLE_ADMIN）
    private String omdRoleName;  // 角色名称字段
    private String omdRoleDescription;  // 描述字段


    // 添加权限集合（多对多关系）
    //@JsonManagedReference
    private Set<OmdPermission> omdPermissions = new HashSet<>();

}
