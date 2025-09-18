package org.x.backend.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OmdMusicLikeCache {
    private Long omdMusicLikeCacheId;

    private Long omdMusicInfoId;

    private Long omdMusicLikeCacheCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date omdMusicLikeCacheUpdateTime;
}
