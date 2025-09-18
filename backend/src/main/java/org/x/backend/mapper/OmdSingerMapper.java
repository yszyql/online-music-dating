package org.x.backend.mapper;

import org.apache.ibatis.annotations.*;
import org.x.backend.pojo.*;

import java.util.List;

@Mapper
public interface OmdSingerMapper {


    /**
     * 根据用户ID查询歌手信息
     * @param omdUserId 用户ID
     * @return 歌手信息
     */
    @Select("select * from tb_omd_singer where omd_user_id = #{omdUserId}")
    OmdSinger findSingerByUserId(Long omdUserId);

    /**
     * 更新歌手信息
     * @param omdSinger 歌手信息
     * @return 是否成功
     */
    int updateSinger(OmdSinger omdSinger);

    /**
     * 插入歌曲信息
     * @param omdMusicInfo 歌曲信息
     * @return 歌曲信息
     */
    @Options(useGeneratedKeys = true, keyProperty = "omdMusicInfoId")
    @Insert("insert into tb_omd_music_info (omd_singer_id, omd_music_info_name, omd_music_info_album," +
            " omd_music_info_duration, omd_music_info_song_url, omd_music_info_cover_url, omd_music_info_genre) " +
            "values (#{omdSingerId}, #{omdMusicInfoName}, #{omdMusicInfoAlbum}, #{omdMusicInfoDuration}, " +
            "#{omdMusicInfoSongUrl}, #{omdMusicInfoCoverUrl}, #{omdMusicInfoGenre})")
    int insertMusicInfo(OmdMusicInfo omdMusicInfo);

    /**
     * 插入歌词信息
     * @param omdMusicLyric 歌词信息
     * @return 是否成功
     */
    @Insert("insert into tb_omd_music_lyric (omd_music_info_id, omd_music_lyric_language, omd_music_lyric_content_url) " +
            "values (#{omdMusicInfoId}, #{omdMusicLyricLanguage}, #{omdMusicLyricContentUrl})")
    int insertMusicLyric(OmdMusicLyric omdMusicLyric);

    /**
     * 插入歌曲点赞信息
     * @param omdMusicLikeCache 歌曲点赞信息
     * @return 是否成功
     */
    @Insert("insert into tb_omd_music_like_cache (omd_music_info_id) values (#{omdMusicInfoId})")
    int insertMusicLikeCache(OmdMusicLikeCache omdMusicLikeCache);

    /**
     * 根据歌曲名称查询歌曲信息
     * @param omdMusicInfoName 歌曲名称
     * @return 歌曲信息
     */
    @Select("select * from tb_omd_music_info where omd_music_info_name = #{omdMusicInfoName}")
    OmdMusicInfo findSongByMusicInfoName(String omdMusicInfoName);

    /**
     * 根据当前登录的歌手ID查询歌曲信息
     * @param omdSingerId 当前登录的歌手ID
     * @param omdMusicInfoStatus 歌曲状态
     * @return 歌曲信息
     */
    @Select("select * from tb_omd_music_info where omd_singer_id = #{omdSingerId} and omd_music_info_status = #{omdMusicInfoStatus}")
    List<OmdMusicInfo> getMusicInfoListBySingerId(Long omdSingerId,Integer omdMusicInfoStatus);

    /**
     * 根据歌曲名字和歌手ID查询歌词信息
     * @param omdMusicInfoName 歌曲名字
     * @param omdSingerId 歌手ID
     * @return 歌词信息
     */
    @Select("select * from tb_omd_music_info where omd_music_info_name = #{omdMusicInfoName} and omd_singer_id = #{omdSingerId}")
    OmdMusicInfo checkIfMusicExist(String omdMusicInfoName, Long omdSingerId);

    /**
     * 根据歌曲ID查询是否申诉过
     * @param omdMusicInfoId 歌曲ID
     * @param omdSingerId 歌手ID
     * @return 歌曲信息
     */
    @Select("select * from tb_omd_music_appeal where omd_music_info_id = #{omdMusicInfoId} and omd_singer_id = #{omdSingerId}")
    OmdMusicAppeal findAppealByMusicInfoId(Long omdMusicInfoId, long omdSingerId);

    /**
     * 插入歌曲申诉信息
     * @param omdMusicAppeal 歌曲申诉信息
     * @return 结果
     */
    @Insert("insert into tb_omd_music_appeal (omd_music_info_id, omd_singer_id, omd_music_report_id, omd_music_appeal_reason,omd_music_appeal_evidence) " +
            "values (#{omdMusicInfoId}, #{omdSingerId}, #{omdMusicReportId}, #{omdMusicAppealReason}, #{omdMusicAppealEvidence})")
    int insertMusicAppeal(OmdMusicAppeal omdMusicAppeal);

    /**
     * 根据歌手ID查询歌曲申诉信息
     * @param omdSingerId 歌手ID
     * @param omdMusicAppealStatus 歌曲申诉状态
     * @return 歌曲申诉信息
     */
    List<OmdMusicAppeal> getMusicAppealListBySingerId(long omdSingerId, Integer omdMusicAppealStatus);

    /**
     * 根据歌曲申诉ID查询歌曲申诉信息
     * @param omdMusicAppealId 申诉ID
     * @return 申诉信息
     */
    @Select("select * from tb_omd_music_appeal where omd_music_appeal_id = #{omdMusicAppealId}")
    OmdMusicAppeal findMusicAppealById(Long omdMusicAppealId);

    /**
     * 更新歌曲申诉信息
     * @param omdMusicAppeal 歌曲申诉信息
     * @return 是否成功
     */
    @Update("update tb_omd_music_appeal set omd_music_appeal_status = #{omdMusicAppealStatus}, omd_music_appeal_reason = #{omdMusicAppealReason}" +
            ", omd_music_appeal_evidence = #{omdMusicAppealEvidence} where omd_music_appeal_id = #{omdMusicAppealId}")
    int updateMusicAppeal(OmdMusicAppeal omdMusicAppeal);

    /**
     * 更新歌曲状态
     * @param omdMusicInfoId 歌曲ID
     * @return 是否成功
     */
    @Update("update tb_omd_music_info set omd_music_info_status = 0 where omd_music_info_id = #{omdMusicInfoId}")
    int updateMusicInfoStatus(Long omdMusicInfoId);

    /**
     * 删除歌曲信息
     * @param omdMusicInfoId 歌曲ID
     * @return 是否成功
     */
    @Delete("delete from tb_omd_music_info where omd_music_info_id = #{omdMusicInfoId}")
    int deleteMusicInfo(Long omdMusicInfoId);

    /**
     * 根据歌曲ID查询歌曲是否因举报而下架
     * @param omdMusicInfoId 歌曲ID
     * @return 歌曲信息
     */
    @Select("select * from tb_omd_music_report where omd_music_info_id = #{omdMusicInfoId}")
    OmdMusicReport findMusicReportById(Long omdMusicInfoId);

    /**
     * 获取当前登录歌手的歌曲被举报而下架的信息
     * @param omdSingerId 歌手ID
     * @return 结果
     */
    List<OmdMusicReport> getMusicReportListBySingerId(long omdSingerId);
}
