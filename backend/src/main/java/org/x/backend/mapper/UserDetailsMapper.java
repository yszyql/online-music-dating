package org.x.backend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.x.backend.pojo.OmdUser;

import java.util.Optional;

/**
 * 用户信息Mapper
 */
@Mapper
public interface UserDetailsMapper {
    /**
     * 根据ID查询用户
     * @param omdUserId 用户ID
     * @return User
     */
    @Select("select * from tb_omd_user where omd_user_id = #{omdUserId}")
    Optional<OmdUser> findById(Long omdUserId);

    /**
     * 根据用户名查询用户
     * @param omdUserName 用户名
     * @return User
     */
    @Select("select * from tb_omd_user where omd_user_name = #{omdUserName}")
    Optional<OmdUser> findByUsername(String omdUserName);
}
