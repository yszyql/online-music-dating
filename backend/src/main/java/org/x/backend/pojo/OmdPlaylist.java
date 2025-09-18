package org.x.backend.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "omdPlaylistId")
public class OmdPlaylist implements Serializable {

    private Long omdPlaylistId;          // 歌单ID

    @NotBlank(message = "歌单名称不能为空")
    private String omdPlaylistName;      // 歌单名称

    private Long omdUserId;              // 创建用户ID
    private String omdPlaylistDescription; // 歌单描述
    private String omdPlaylistCover;     // 歌单封面
    private Integer omdPlaylistPublic;   // 公开状态

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date omdPlaylistCreateTime;  // 创建时间

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date omdPlaylistUpdateTime;  // 更新时间

    private OmdUser omdUser;             // 歌单创建用户

}