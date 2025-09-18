package org.x.backend.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OmdPlaylistMusic implements Serializable {

    private Long omdPlaylistMusicId;     // 关联ID
    private Long omdPlaylistId;          // 歌单ID
    private Long omdMusicInfoId;         // 音乐ID
    private Integer omdPlaylistMusicOrder; // 歌曲顺序

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date omdPlaylistMusicCreateTime; // 添加时间

    // 关联对象
    private OmdMusicInfo omdMusicInfo;         // 关联音乐
}