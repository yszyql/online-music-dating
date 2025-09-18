package org.x.backend.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OmdMusicLyric implements Serializable {

    private Long omdMusicLyricId; // 主键

    private Long omdMusicInfoId; // 歌曲ID

    @NotEmpty
    private String omdMusicLyricLanguage; // 语言


    @NotBlank(message = "歌词URL不能为空")
    @URL(regexp = "^(https?|ftp)://.*$", message = "歌词内容必须是有效的URL")
    private String omdMusicLyricContentUrl; // 歌词内容

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date omdMusicLyricCreateTime; // 创建时间

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date omdMusicLyricUpdateTime; // 更新时间
}