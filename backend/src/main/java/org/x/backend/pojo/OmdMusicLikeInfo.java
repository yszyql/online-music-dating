package org.x.backend.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OmdMusicLikeInfo {
    private Long omdMusicInfoId;
    private Long omdMusicLikeCacheCount;
    private Boolean isUserLiked;
}
