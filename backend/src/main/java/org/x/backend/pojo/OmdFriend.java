package org.x.backend.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class,property = "omdFriendId")
/**
 * 好友关系实体类
 */
public class OmdFriend implements Serializable {

    private Long omdFriendId;          // 好友关系ID
    private Long omdUserId;            // 用户ID
    private Long omdFriendUserId;      // 好友用户ID
    private Integer omdFriendStatus;   // 关系状态

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date omdFriendCreateTime;  // 关系创建时间

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date omdFriendVerifyTime;  // 通过验证时间

    private Long unreadMessageCount;   // 未读消息数量

    private OmdMessage lastMessage;

    // 双向关联属性
    private OmdUser omdFriendUser;        // 好友用户信息
}