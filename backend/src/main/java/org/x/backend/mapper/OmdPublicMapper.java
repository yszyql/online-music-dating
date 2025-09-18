package org.x.backend.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.x.backend.pojo.*;

import java.util.List;

@Mapper
public interface OmdPublicMapper {

    /**
     * 增加音乐播放统计
     * @param omdMusicPlayStat 音乐播放统计信息
     */
    @Insert("insert into tb_omd_music_play_stat (omd_music_info_id, omd_user_id, omd_music_play_stat_is_guest, omd_music_play_stat_guest_uuid) " +
            "values (#{omdMusicInfoId}, #{omdUserId}, #{omdMusicPlayStatIsGuest}, #{omdMusicPlayStatGuestUuid})")
    void addMusicPlayStat(OmdMusicPlayStat omdMusicPlayStat);

    /**
     * 根据音乐ID列表获取音乐名称
     * @param omdMusicInfoIdList 音乐ID列表
     * @return 音乐名称列表
     */
    List<OmdMusicInfo> getMusicInfoByIdList(List<Long> omdMusicInfoIdList);

    /**
     * 获取随机歌手信息
     * @return 随机歌手信息列表
     */
    List<OmdSinger> getRandomSingersInfo();

    /**
     * 根据歌手ID和音乐名称获取音乐信息
     * @param omdSingerId 歌手ID
     * @param omdMusicInfoName 音乐名称
     * @return 音乐信息
     */
    @Select("select * from tb_omd_music_info where omd_singer_id = #{omdSingerId} and omd_music_info_name = #{omdMusicInfoName}")
    OmdMusicInfo getMusicInfoBySingerId(Long omdSingerId, String omdMusicInfoName);

    /**
     * 根据音乐ID获取歌词信息
     * @param omdMusicInfoId 音乐ID
     * @return 歌词信息
     */
    @Select("select * from tb_omd_music_lyric where omd_music_info_id = #{omdMusicInfoId}")
    OmdMusicLyric getMusicInfoLyricById(Long omdMusicInfoId);

    /**
     * 根据音乐ID获取评论信息
     * @param omdMusicInfoId 音乐ID
     * @return 评论信息
     */
    List<OmdMusicComment> getMusicCommentListByMusicId(Long omdMusicInfoId);

    /**
     * 根据音乐评论ID获取子评论数量
     * @param omdMusicCommentId 音乐评论ID
     * @return 子评论数量
     */
    @Select("select count(*) from tb_omd_music_comment where omd_music_comment_parent_id = #{omdMusicCommentId}")
    int countChildComments(Long omdMusicCommentId);

    /**
     * 根据音乐评论ID获取子评论
     * @param omdMusicCommentId 音乐评论ID
     * @return 子评论
     */
    List<OmdMusicComment> getChildCommentsByParentId(Long omdMusicCommentId);

    /**
     * 新增用户申诉
     * @param omdUserAppeal 用户申诉信息
     * @return 是否新增成功
     */
    @Insert("insert into tb_omd_user_appeal (omd_user_id, omd_user_appeal_reason) " +
            "values (#{omdUserId}, #{omdUserAppealReason})")
    int addOmdUserAppeal(OmdUserAppeal omdUserAppeal);

    /**
     * 根据评论ID获取音乐评论
     * @param targetId 评论ID
     * @return 音乐评论
     */
    OmdMusicComment getMusicCommentByMusicId(Long targetId);
}
