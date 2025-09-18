package org.x.backend.service;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import org.x.backend.pojo.*;

import java.util.List;

public interface OmdSingerService {

    /**
     * 根据用户ID查询歌手信息
     * @param omdUserId 用户ID
     * @return 歌手信息
     */
    OmdSinger findSingerByUserId(Long omdUserId);


    /**
     * 更新歌手信息
     * @param omdSinger 歌手信息
     * @return 是否成功
     */
    boolean updateSinger(OmdSinger omdSinger);

    /**
     * 插入歌曲信息
     * @param omdMusicInfo 歌曲信息
     * @return 歌曲信息
     */
    OmdMusicInfo insertMusicInfo(OmdMusicInfo omdMusicInfo);

    /**
     * 插入歌词信息
     * @param omdMusicLyric 歌词信息
     * @return 是否成功
     */
    boolean insertMusicLyric(OmdMusicLyric omdMusicLyric);

    /**
     * 插入歌曲点赞信息
     * @param omdMusicLikeCache 歌曲点赞信息
     * @return 是否成功
     */
    boolean insertMusicLikeCache(OmdMusicLikeCache omdMusicLikeCache);

    /**
     * 根据歌曲名称查询歌曲信息
     * @param omdMusicInfoName 歌曲名称
     * @return 歌曲信息
     */
    OmdMusicInfo findSongByMusicInfoName(String omdMusicInfoName);


    /**
     * 根据当前登录的歌手ID查询歌词信息
     * @param omdSingerId 当前登录的歌手ID
     * @param omdMusicInfoStatus 歌曲状态
     * @return 歌词信息
     */
    List<OmdMusicInfo> getMusicInfoListBySingerId(Long omdSingerId,Integer omdMusicInfoStatus);

    /**
     * 根据歌曲名字和歌手ID查询歌词信息
     * @param omdMusicInfoName 歌曲名字
     * @param omdSingerId 歌手ID
     * @return 歌词信息
     */
    OmdMusicInfo checkIfMusicExist(@NotBlank(message = "音乐名称不能为空") @Pattern(regexp = "^\\S(1,20)$") String omdMusicInfoName, Long omdSingerId);

    /**
     * 根据歌曲ID和歌手ID查询歌曲申诉信息
     * @param omdMusicInfoId 歌曲ID
     * @param omdSingerId 歌手ID
     * @return 歌曲信息
     */
    OmdMusicAppeal findAppealByMusicInfoId(Long omdMusicInfoId, long omdSingerId);

    /**
     * 插入歌曲申诉信息
     * @param omdMusicAppeal 歌曲申诉信息
     * @return 是否成功
     */
    boolean insertMusicAppeal(OmdMusicAppeal omdMusicAppeal);

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
    OmdMusicAppeal findMusicAppealById(Long omdMusicAppealId);

    /**
     * 更新歌曲申诉信息
     * @param omdMusicAppeal 歌曲申诉信息
     * @return 是否成功
     * */
    boolean updateMusicAppeal(OmdMusicAppeal omdMusicAppeal);

    /**
     * 更新歌曲状态
     * @param omdMusicInfoId 歌曲ID
     * @return 是否成功
     */
    boolean updateMusicInfoStatus(Long omdMusicInfoId);

    /**
     * 删除歌曲信息
     * @param omdMusicInfoId 歌曲ID
     * @return 是否成功
     * */
    boolean deleteMusicInfo(Long omdMusicInfoId);

    /**
     * 根据歌曲ID查询歌曲是否因举报而下架
     * @param omdMusicInfoId 歌曲ID
     * @return 歌曲信息
     */
    OmdMusicReport findMusicReportById(Long omdMusicInfoId);

    /**
     * 获取当前登录歌手的歌曲被举报而下架的信息
     * @param omdSingerId 歌手ID
     * @return 结果
     */
    List<OmdMusicReport> getMusicReportListBySingerId(long omdSingerId);
}
