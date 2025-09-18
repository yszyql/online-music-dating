package org.x.backend.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.x.backend.pojo.OmdRole;
import org.x.backend.pojo.OmdUserRole;
import org.x.backend.pojo.OmdUser;

import java.util.List;

@Mapper
public interface OmdUserRoleMapper {

    /**
     * 根据用户ID查询用户的所有角色
     * @param omdUserId 用户ID
     * @return 角色列表
     */
    List<OmdRole> findRolesByUserId(@Param("omdUserId") Long omdUserId);

    /**
     * 根据角色代码查询用户列表
     * @param omdRoleCode 角色代码
     * @return 用户列表
     */
    List<OmdUser> findUsersByRoleCode(@Param("omdRoleCode") String omdRoleCode);


    /**
     * 插入用户角色关联
     * @param omdUserRole 用户角色关联对象
     */
    @Insert("insert into tb_omd_user_role (omd_user_id, omd_role_code) values (#{omdUserId}, #{omdRoleCode})")
    Integer insert(OmdUserRole omdUserRole);
}
