package org.x.backend.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OmdCommentStatusResult {
    private Long omdMusicCommentId;
    private Boolean isLiked;      // 是否已点赞
    private Boolean isReported;   // 是否已举报
    private Boolean isOwn;        // 是否为本人评论
}