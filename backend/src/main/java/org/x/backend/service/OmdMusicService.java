package org.x.backend.service;

import jakarta.validation.constraints.NotBlank;
import org.x.backend.pojo.*;

import java.util.List;
import java.util.Set;

public interface OmdMusicService {

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
    OmdMusicLikeCache getMusicInfoLikeCache(Long omdMusicInfoId);

    /**
     * 检查是否已经点赞过
     * @param omdMusicInfoId 音乐信息ID
     * @param omdUserId 当前用户ID
     * @return 音乐信息
     */
    OmdMusicLike checkIfLiked(Long omdMusicInfoId, Long omdUserId);

    /**
     * 更新音乐点赞缓存
     * @param omdMusicLikeCache 音乐信息点赞缓存
     * @return 结果
     */
    boolean updateMusicLikeCache(OmdMusicLikeCache omdMusicLikeCache);


    /**
     * 插入音乐信息点赞缓存
     * @param omdMusicLike 音乐信息点赞
     * @return 结果
     */
    boolean insertLikeMusicInfo(OmdMusicLike omdMusicLike);

    /**
     * 检查是否已经有点赞歌单
     * @param omdUserId 当前用户ID
     * @return 音乐信息
     */
    OmdPlaylist checkIfLikePlaylist(Long omdUserId);

    /**
     * 新增点赞歌单
     * @param omdMusicLikePlaylist 音乐信息点赞
     * @return 结果
     */
    boolean insertLikePlaylist(OmdPlaylist omdMusicLikePlaylist);

    /**
     * 新增点赞歌单音乐
     * @param omdPlaylistMusic 音乐信息点赞
     * @return 结果
     * */
    boolean insertLikePlaylistMusic(OmdPlaylistMusic omdPlaylistMusic);

    /**
     * 检查是否已经有点赞歌单音乐
     * @param omdPlaylistId 当前用户ID
     * @return 音乐信息
     */
    OmdPlaylistMusic checkIfLikePlaylistMusic(Long omdPlaylistId);


    /**
     * 删除用户的点赞记录
     * @param omdMusicInfoId 音乐信息的ID
     * @param omdUserId 当前用户ID
     * @return 音乐信息
     */
    boolean deleteMusicLike(Long omdMusicInfoId, Long omdUserId);

    /**
     * 根据传入的播放列表ID查询播放列表
     * @param omdPlaylistId 播放列表ID
     * @return 音乐信息
     */
    List<OmdPlaylistMusic> findLikePlaylistMusicByPlaylistId(Long omdPlaylistId);

    /**
     * 根据传入的播放列表ID删除播放中间表的所有数据
     * @param omdPlaylistId 播放列表ID
     * @return 删除的记录数（通常为0或1）
     */
    boolean deleteAllLikePlaylistMusic(Long omdPlaylistId);

    /**
     * 根据传入的播放列表ID删除播放列表
     * @param omdPlaylistId 播放列表ID
     * @param omdPlaylistName 播放列表名称
     * @return 删除的记录数（通常为0或1）
     */
    boolean deleteLikePlaylist(Long omdPlaylistId,String omdPlaylistName);

    /**
     * 根据传入的播放列表ID和音乐ID删除播放列表音乐
     * @param omdPlaylistId 播放列表ID
     * @param omdMusicInfoId 音乐ID
     * @return 删除的记录数（通常为0或1）
     */
    boolean deleteLikePlaylistMusic(Long omdPlaylistId, Long omdMusicInfoId);

    /**
     * 检查歌单名称是否存在
     * @param omdPlaylistName 歌单名称
     * @param omdUserId 当前用户ID
     * @return 结果
     */
    OmdPlaylist checkPlaylistNameIfExist(@NotBlank(message = "歌单名称不能为空") String omdPlaylistName, Long omdUserId);

    /**
     * 新增播放列表
     * @param omdPlaylist 播放列表
     * @return 结果
     */
    boolean insertPlaylist(OmdPlaylist omdPlaylist);

    /**
     * 修改播放列表
     * @param omdPlaylist 播放列表
     * @return 结果
     */
    boolean updatePlaylist(OmdPlaylist omdPlaylist);

    /**
     * 新增播放列表音乐
     * @param omdPlaylistMusic 播放列表音乐
     * @return 结果
     * */
    boolean insertPlaylistMusic(OmdPlaylistMusic omdPlaylistMusic);

    /**
     * 根据传入的播放列表ID删除播放列表
     * @param omdPlaylistId 播放列表ID
     * @return 删除的记录数（通常为0或1）
     */
    boolean deletePlaylist(Long omdPlaylistId);

    /**
     * 检查是否已经存在于播放列表
     * @param omdPlaylistId 播放列表ID
     * @param omdMusicInfoId 音乐ID
     * @return 结果
     */
    OmdPlaylistMusic checkIfMusicExistInPlaylist(Long omdPlaylistId, Long omdMusicInfoId);

    /**
     * 根据传入的播放列表ID获取已存在的音乐信息ID集合
     * @param omdPlaylistId 播放列表ID
     * @return Set<Long> 音乐信息ID集合
     */
    Set<Long> getExistingMusicIdsByPlaylistId(Long omdPlaylistId);

    /**
     * 根据传入的歌曲信息集合插入到播放列表中
     * @param omdPlaylistMusicList 插入的歌曲信息集合
     * @return Set<Long> 音乐信息ID集合
     */
    boolean insertMusicListToPlaylist(List<OmdPlaylistMusic> omdPlaylistMusicList);

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
     * 根据传入的音乐ID列表查询音乐是否被用户点赞
     * @param omdUserId 当前用户ID
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
     * 根据传入的歌单ID查询播放列表的顺序最大值
     * @param omdPlaylistId 播放列表ID
     * @return 音乐信息
     */
    int getMaxPlaylistMusicOrder(Long omdPlaylistId);

    /**
     * 根据传入的音乐ID列表更新音乐的点赞次数
     * @param omdMusicInfoIdList 音乐ID列表
     * @param delta 增量（正数增加，负数减少）
     * @return 音乐信息
     */
    int updateLikeCacheList(List<Long> omdMusicInfoIdList, int delta);

    /**
     * 批量插入音乐信息点赞
     * @param omdMusicLikeList 音乐信息点赞
     * @return 结果
     */
    boolean insertLikeMusicInfoList(List<OmdMusicLike> omdMusicLikeList);

    /**
     * 批量删除音乐信息点赞
     * @param omdMusicInfoIdList 音乐信息ID列表
     * @param omdUserId 当前用户ID
     * @return 结果
     */
    boolean deleteLikeMusicInfoList(List<Long> omdMusicInfoIdList, Long omdUserId);

    /**
     * 根据传入的音乐ID列表查询音乐信息
     * @param targetId 音乐ID列表
     * @return 音乐信息
     */
    OmdMusicInfo getMusicInfoByOmdMusicInfoId(Long targetId);
}
