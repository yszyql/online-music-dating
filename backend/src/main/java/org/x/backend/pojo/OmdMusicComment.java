package org.x.backend.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * 音乐评论
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties({"handler", "hibernateLazyInitializer"}) // 排除代理属性
public class OmdMusicComment {

    private Long omdMusicCommentId; // 评论ID
    private Long omdMusicInfoId; // 音乐ID
    private Long omdUserId; // 用户ID

    @NotBlank(message = "评论内容不能为空")
    private String omdMusicCommentContent; // 评论内容

    private Long omdMusicCommentParentId; // 父评论ID

    private Integer omdMusicCommentReplyCount;   // 子评论数量

    private Long omdMusicCommentLikeCount; // 点赞数

    private Integer omdMusicCommentStatus; // 评论状态（0：待审核，1：通过，2：拒绝）

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date omdMusicCommentCreateTime; // 创建时间

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date omdMusicCommentUpdateTime; // 更新时间

    // 评论用户信息
    private OmdUser omdUser;

    // 子评论列表
    private List<OmdMusicComment> omdMusicCommentReplies = new ArrayList<>();


}
