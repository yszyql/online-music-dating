package org.x.backend.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OmdMusicLike implements Serializable {

    private Long omdMusicLikeId; // 主键ID
    private Long omdMusicInfoId; // 音乐ID
    private Long omdUserId; // 用户ID

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date omdMusicLikesTime; // 点赞时间
}