package org.x.backend.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "omdSingerId")
public class OmdSinger implements Serializable {

    private long omdSingerId; // 主键

    private Long omdUserId; // 用户ID

    private String omdSingerName; // 歌手名称

    @NotEmpty
    private String  omdSingerIntroduction; // 简介

    @NotEmpty
    private String  omdSingerRepresentativeSing; // 代表作

    @NotEmpty
    private String  omdSingerGenre; // 流派

    @NotEmpty
    private String  omdSingerLabel; // 唱片公司

    private Integer  omdSingerStatus; // 状态（0-未认证；1-已认证；2-认证失败）

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date  omdSingerVerifyTime; // 认证时间

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date  omdSingerUpdateTime; // 更新时间

    private OmdUser omdUser; // 关联用户信息

    private OmdMusicInfo omdMusicInfo; // 音乐信息列表
}
