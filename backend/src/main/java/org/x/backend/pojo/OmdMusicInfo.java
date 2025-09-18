package org.x.backend.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "omdMusicInfoId")
public class OmdMusicInfo implements Serializable {

    private Long omdMusicInfoId;  // 主键ID

    @NotBlank(message = "音乐名称不能为空")
    @Pattern(regexp = "^\\S(1,20)$")
    private String omdMusicInfoName;  // 新增音乐名称字段

    @NotBlank(message = "音乐名称不能为空")
    @Pattern(regexp = "^\\S(1,20)$")
    private String omdMusicInfoAlbum;  // 新增专辑字段

    @NotNull(message = "歌曲时长不能为空")
    @Positive(message = "歌曲时长必须为正数")
    private Integer omdMusicInfoDuration;  // 新增时长字段

    @NotBlank(message = "音乐URL不能为空")
    @URL(regexp = "^(https?|ftp)://.*$", message = "音乐必须是有效的URL")
    private String omdMusicInfoSongUrl;  // 新增COS音乐URL字段

    @NotBlank(message = "封面URL不能为空")
    @URL(regexp = "^(https?|ftp)://.*$", message = "歌词内容必须是有效的URL")
    private String omdMusicInfoCoverUrl;  // 新增COS封面URL字段

    @NotEmpty
    private String omdMusicInfoGenre;  // 音乐风格

    private Long omdSingerId;  // 新增上传用户ID字段

    private Integer omdMusicInfoStatus;  // 新增审核状态：0-待审核，1-通过，2-拒绝

    private String omdMusicInfoRemark;  // 新增审核备注字段

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date omdMusicInfoCreateTime;  // 新增创建时间字段

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date omdMusicInfoUpdateTime;  // 新增更新时间字段

    private OmdSinger omdSinger;  // 关联歌手信息

}