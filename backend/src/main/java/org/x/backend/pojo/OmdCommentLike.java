package org.x.backend.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OmdCommentLike {
    private Long omdCommentLikeId;

    @NotNull(message = "用户ID不能为空")
    private Long omdUserId;

    @NotNull(message = "音乐评论ID不能为空")
    private Long omdMusicCommentId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date omdCommentLikeCreateTime;
}
