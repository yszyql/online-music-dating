package org.x.backend.mapper;

import org.apache.ibatis.annotations.*;
import org.springframework.web.bind.annotation.RequestParam;
import org.x.backend.pojo.*;

import java.util.List;
import java.util.Set;

@Mapper
public interface OmdMusicMapper {

    /**
     * 获取音乐信息
     * @return
     */
    List<OmdMusicInfo> getAllMusicInfoList();

    /**
     * 根据歌手名称获取歌手ID
     * @param omdSingerName 歌手名称
     * @return  歌手
     */
    @Select("select * from tb_omd_singer where omd_singer_name = #{omdSingerName}")
    OmdSinger getSingerIdBySingerName(String omdSingerName);

    /**
     * 根据查询参数获取音乐信息列表
     * @param omdMusicInfoName 音乐名称
     * @param omdSingerName 歌手名称
     * @param omdMusicInfoAlbum 音乐专辑
     * @param omdMusicInfoGenre 音乐类型
     * @return 音乐信息
     */
    List<OmdMusicInfo> getMusicInfoListByQueryParams(String omdMusicInfoName, String omdSingerName, String omdMusicInfoAlbum, String omdMusicInfoGenre);

    /**
     * 根据音乐信息ID获取音乐信息
     * @param omdMusicInfoId 音乐信息ID
     * @return 音乐信息
     */
    @Select("select * from tb_omd_music_like_cache where omd_music_info_id = #{omdMusicInfoId}")
    OmdMusicLikeCache getMusicInfoLikeCache(Long omdMusicInfoId);

    /**
     * 检查是否已经点赞过
     * @param omdMusicInfoId 音乐信息ID
     * @param omdUserId 当前用户ID
     * @return 音乐信息
     */
    @Select("select * from tb_omd_music_like where omd_music_info_id = #{omdMusicInfoId} and omd_user_id = #{omdUserId}")
    OmdMusicLike checkIfLiked(Long omdMusicInfoId, Long omdUserId);

    /**
     * 更新音乐点赞缓存
     * @param omdMusicLikeCache 音乐信息点赞缓存
     * @return 结果
     */
    @Update("update tb_omd_music_like_cache set omd_music_like_cache_count = #{omdMusicLikeCacheCount}" +
            " where omd_music_like_cache_id = #{omdMusicLikeCacheId}")
    int updateMusicLikeCache(OmdMusicLikeCache omdMusicLikeCache);

    /**
     * 插入音乐信息点赞缓存
     * @param omdMusicLike 音乐信息点赞
     * @return 结果
     * */
    @Insert("insert into tb_omd_music_like (omd_music_info_id, omd_user_id) values (#{omdMusicInfoId}, #{omdUserId})")
    int insertLikeMusicInfo(OmdMusicLike omdMusicLike);

    /**
     * 检查是否已经有点赞歌单
     * @param omdUserId 当前用户ID
     * @return 音乐信息
     */
    @Select("select * from tb_omd_playlist where omd_user_id = #{omdUserId} and omd_playlist_name = '我喜欢的音乐'")
    OmdPlaylist checkIfLikePlaylist(Long omdUserId);

    /**
     * 新增点赞歌单
     * @param omdMusicLikePlaylist 音乐信息点赞
     * @return 结果
     */
    @Options(useGeneratedKeys = true, keyProperty = "omdPlaylistId", keyColumn = "omd_playlist_id")
    @Insert("insert into tb_omd_playlist (omd_user_id) values (#{omdUserId})")
    int insertLikePlaylist(OmdPlaylist omdMusicLikePlaylist);

    /**
     * 新增点赞歌单里的音乐
     * @param omdPlaylistMusic 音乐信息点赞
     * @return 结果
     */
    @Insert("insert into tb_omd_playlist_music (omd_playlist_id, omd_music_info_id, omd_playlist_music_order)" +
            " values (#{omdPlaylistId}, #{omdMusicInfoId}, #{omdPlaylistMusicOrder})")
    int insertLikePlaylistMusic(OmdPlaylistMusic omdPlaylistMusic);

    /**
     * 检查是否已经有点赞歌单音乐的中间表
     * @param omdPlaylistId 当前用户ID
     * @return 音乐信息
     */
    @Select("select * from tb_omd_playlist_music where omd_playlist_id = #{omdPlaylistId} order by omd_playlist_music_order desc limit 1")
    OmdPlaylistMusic checkIfLikePlaylistMusic(Long omdPlaylistId);

    /**
     * 删除用户点赞歌曲的记录
     * @param omdMusicInfoId 音乐信息ID
     * @param omdUserId 当前用户ID
     * @return 结果
     */
    @Delete("delete from tb_omd_music_like where omd_music_info_id = #{omdMusicInfoId} and omd_user_id = #{omdUserId}")
    int deleteMusicLike(Long omdMusicInfoId, Long omdUserId);

    /**
     * 根据传入的播放列表ID查询播放列表
     * @param omdPlaylistId 播放列表ID
     * @return 音乐信息
     */
    @Select("select * from tb_omd_playlist_music where omd_playlist_id = #{omdPlaylistId}")
    List<OmdPlaylistMusic> findLikePlaylistMusicByPlaylistId(Long omdPlaylistId);

    /**
     * 根据传入的播放列表ID删除播放中间表的所有数据
     * @param omdPlaylistId 播放列表ID
     * @return 删除的记录数（通常为0或1）
     */
    @Delete("delete from tb_omd_playlist_music where omd_playlist_id = #{omdPlaylistId}")
    int deleteAllLikePlaylistMusic(Long omdPlaylistId);

    /**
     * 根据传入的播放列表ID删除播放列表
     * @param omdPlaylistId 播放列表ID
     * @param omdPlaylistName 播放列表名称
     * @return 删除的记录数（通常为0或1）
     */
    @Delete("delete from tb_omd_playlist where omd_playlist_id = #{omdPlaylistId} and omd_playlist_name = #{omdPlaylistName}")
    int deleteLikePlaylist(Long omdPlaylistId, String omdPlaylistName);

    /**
     * 根据传入的播放列表ID和音乐ID删除播放列表音乐
     * @param omdPlaylistId 播放列表ID
     * @param omdMusicInfoId 音乐ID
     * @return 删除的记录数（通常为0或1）
     */
    @Delete("delete from tb_omd_playlist_music where omd_playlist_id = #{omdPlaylistId} and omd_music_info_id = #{omdMusicInfoId}")
    int deleteLikePlaylistMusic(Long omdPlaylistId, Long omdMusicInfoId);

    /**
     * 检查歌单名称是否存在
     * @param omdPlaylistName 歌单名称
     * @param omdUserId 当前用户ID
     * @return 结果
     */
    @Select("select * from tb_omd_playlist where omd_playlist_name = #{omdPlaylistName} and omd_user_id = #{omdUserId}")
    OmdPlaylist checkPlaylistNameIfExist(String omdPlaylistName, Long omdUserId);

    /**
     * 新增播放列表
     * @param omdPlaylist 播放列表
     * @return 结果
     */
    int insertPlaylist(OmdPlaylist omdPlaylist);

    /**
     * 修改播放列表
     * @param omdPlaylist 播放列表
     * @return 结果
     */
    int updatePlaylist(OmdPlaylist omdPlaylist);

    /**
     * 新增播放列表里的音乐
     * @param omdPlaylistMusic 播放列表里的音乐信息
     * @return 结果
     */
    @Insert("insert into tb_omd_playlist_music (omd_playlist_id) values (#{omdPlaylistId})")
    int insertPlaylistMusic(OmdPlaylistMusic omdPlaylistMusic);

    /**
     * 根据传入的播放列表ID删除播放列表
     * @param omdPlaylistId 播放列表ID
     * @return 删除的记录数（通常为0或1）
     */
    @Delete("delete from tb_omd_playlist where omd_playlist_id = #{omdPlaylistId}")
    int deletePlaylist(Long omdPlaylistId);

    /**
     * 根据传入的播放列表ID和音乐ID查询播放列表音乐
     * @param omdPlaylistId 播放列表ID
     * @param omdMusicInfoId 音乐ID
     * @return 音乐信息
     */
    @Select("select * from tb_omd_playlist_music where omd_playlist_id = #{omdPlaylistId} and omd_music_info_id = #{omdMusicInfoId}")
    OmdPlaylistMusic checkIfMusicExistInPlaylist(Long omdPlaylistId, Long omdMusicInfoId);

    /**
     * 根据传入的播放列表ID查询已存在的音乐ID
     * @param omdPlaylistId 播放列表ID
     * @return 音乐ID集合
     */
    @Select("select omd_music_info_id from tb_omd_playlist_music where omd_playlist_id = #{playlistId}")
    Set<Long> getExistingMusicIdsByPlaylistId(Long omdPlaylistId);

    /**
     * 根据传入的歌曲信息集合插入到播放列表中
     * @param omdPlaylistMusicList 插入的歌曲信息集合
     * @return Set<Long> 音乐信息ID集合
     */
    int insertMusicListToPlaylist(@RequestParam("omdPlaylistMusicList") List<OmdPlaylistMusic> omdPlaylistMusicList);

    /**
     * 根据传入的播放列表ID和音乐ID列表删除播放列表音乐
     * @param omdPlaylistId 播放列表ID
     * @param omdMusicInfoIdList 音乐ID列表
     * @return 删除的记录数
     * */
    int deleteMusicListFromPlaylist(Long omdPlaylistId, List<Long> omdMusicInfoIdList);

    /**
     * 根据传入的音乐ID列表查询音乐被点赞的次数
     * @param omdMusicInfoIdList 音乐ID列表
     * @return 音乐信息
     */
    List<OmdMusicLikeCache> selectByOmdMusicInfoIdList(List<Long> omdMusicInfoIdList);

    /**
     * 根据传入的用户ID和音乐ID列表查询用户是否已经点赞过
     * @param omdUserId 用户ID
     * @param omdMusicInfoIdList 音乐ID列表
     * @return 音乐信息
     */
    List<OmdMusicLike> selectByUserIdAndMusicIds(Long omdUserId, List<Long> omdMusicInfoIdList);

    /**
     * 根据传入的播放列表ID查询播放列表
     * @param omdPlaylistId 播放列表ID
     * @return 音乐信息
     */
    List<OmdPlaylistMusic> getMusicListFromPlaylist(Long omdPlaylistId);

    /**
     * 根据传入的播放列表ID获取最大的音乐排序
     * @param omdPlaylistId 播放列表ID
     * @return 最大的音乐排序
     */
    @Select("SELECT COALESCE(MAX(omd_playlist_music_order), 0) FROM tb_omd_playlist_music WHERE omd_playlist_id = #{omdPlaylistId}")
    int getMaxPlaylistMusicOrder(Long omdPlaylistId);

    /**
     * 根据传入的音乐ID列表更新音乐的点赞次数
     * @param omdMusicInfoIdList 音乐ID列表
     * @param delta 增量（正数增加，负数减少）
     * @return 更新的记录数
     */
    int updateLikeCacheList(List<Long> omdMusicInfoIdList, int delta);

    /**
     * 批量插入音乐信息点赞
     * @param omdMusicLikeList 音乐信息点赞
     * @return 结果
     */
    int insertLikeMusicInfoList(List<OmdMusicLike> omdMusicLikeList);

    /**
     * 批量删除音乐信息点赞
     * @param omdMusicInfoIdList 音乐信息ID列表
     * @param omdUserId 当前用户ID
     * @return 结果
     */
    int deleteLikeMusicInfoList(List<Long> omdMusicInfoIdList, Long omdUserId);

    /**
     * 根据传入的音乐ID查询音乐信息
     * @param targetId 音乐ID
     * @return 音乐信息
     */
    OmdMusicInfo getMusicInfoByOmdMusicInfoId(Long targetId);
}
