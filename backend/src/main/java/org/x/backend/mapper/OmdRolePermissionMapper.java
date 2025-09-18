package org.x.backend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.x.backend.pojo.OmdPermission;

import java.util.List;

@Mapper
public interface OmdRolePermissionMapper {
    /**
     * 根据角色代码查询角色的所有权限
     * @param omdRoleCode 角色代码
     * @return 权限列表
     */
    List<OmdPermission> findPermissionsByRoleCode(String omdRoleCode);
}
