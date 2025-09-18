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
public class OmdMusicPlayStat implements Serializable {

    private Long omdMusicPlayStatId; // 主键ID
    private Long omdMusicInfoId; // 音乐ID
    private Long omdUserId; // 用户ID
    private Integer omdMusicPlayStatIsGuest; // 是否是游客
    private String omdMusicPlayStatGuestUuid; // 游客UUID

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date omdMusicPlayStatPlayTime; // 播放时间

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date omdMusicPlayStatPlayUpdateTime; // 最后更新时间

}