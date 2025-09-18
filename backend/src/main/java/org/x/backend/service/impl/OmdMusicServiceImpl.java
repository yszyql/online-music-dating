package org.x.backend.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.x.backend.mapper.OmdMusicMapper;
import org.x.backend.pojo.*;
import org.x.backend.service.OmdMusicService;

import java.util.List;
import java.util.Set;

@Service
public class OmdMusicServiceImpl implements OmdMusicService {

    @Autowired
    private OmdMusicMapper omdMusicMapper;

    /**
     * 获取音乐信息
     * @return 音乐信息
     */
    @Override
    public List<OmdMusicInfo> getAllMusicInfoList() {
        return omdMusicMapper.getAllMusicInfoList();
    }

    /**
     * 根据歌手名称获取歌手ID
     * @param omdSingerName 歌手名称
     * @return 歌手
     */
    @Override
    public OmdSinger getSingerIdBySingerName(String omdSingerName) {
        return omdMusicMapper.getSingerIdBySingerName(omdSingerName);
    }

    /**
     * 根据查询参数获取音乐信息列表
     * @param omdMusicInfoName 音乐名称
     * @param omdSingerName 歌手名称
     * @param omdMusicInfoAlbum 音乐专辑
     * @param omdMusicInfoGenre 音乐类型
     * @return 音乐信息
     * */
    @Override
    public List<OmdMusicInfo> getMusicInfoListByQueryParams(String omdMusicInfoName, String omdSingerName, String omdMusicInfoAlbum, String omdMusicInfoGenre) {
        return omdMusicMapper.getMusicInfoListByQueryParams(omdMusicInfoName, omdSingerName, omdMusicInfoAlbum, omdMusicInfoGenre);
    }

    /**
     * 根据音乐信息ID获取音乐信息
     * @param omdMusicInfoId 音乐信息ID
     * @return 音乐信息
     */
    @Override
    public OmdMusicLikeCache getMusicInfoLikeCache(Long omdMusicInfoId) {
        return omdMusicMapper.getMusicInfoLikeCache(omdMusicInfoId);
    }

    /**
     * 检查是否已经点赞过
     * @param omdMusicInfoId 音乐信息ID
     * @param omdUserId 当前用户ID
     * @return 音乐信息
     */
    @Override
    public OmdMusicLike checkIfLiked(Long omdMusicInfoId, Long omdUserId) {
        return omdMusicMapper.checkIfLiked(omdMusicInfoId, omdUserId);
    }

    /**
     * 更新音乐点赞缓存
     * @param omdMusicLikeCache 音乐信息点赞缓存
     * @return 结果
     */
    @Override
    public boolean updateMusicLikeCache(OmdMusicLikeCache omdMusicLikeCache) {
        return omdMusicMapper.updateMusicLikeCache(omdMusicLikeCache) > 0;
    }

    /**
     * 插入音乐信息点赞缓存
     * @param omdMusicLike 音乐信息点赞
     * @return 结果
     */
    @Override
    public boolean insertLikeMusicInfo(OmdMusicLike omdMusicLike) {
        return omdMusicMapper.insertLikeMusicInfo(omdMusicLike) > 0;
    }

    /**
     * 检查是否已经有点赞歌单
     * @param omdUserId 当前用户ID
     * @return 音乐信息
     */
    @Override
    public OmdPlaylist checkIfLikePlaylist(Long omdUserId) {
        return omdMusicMapper.checkIfLikePlaylist(omdUserId);
    }

    /**
     * 新增点赞歌单
     * @param omdMusicLikePlaylist 音乐信息点赞
     * @return 结果
     */
    @Override
    public boolean insertLikePlaylist(OmdPlaylist omdMusicLikePlaylist) {
        return omdMusicMapper.insertLikePlaylist(omdMusicLikePlaylist) > 0;
    }

    /**
     * 新增点赞歌单里的音乐
     * @param omdPlaylistMusic 音乐信息点赞
     * @return 结果
     */
    @Override
    public boolean insertLikePlaylistMusic(OmdPlaylistMusic omdPlaylistMusic) {
        return omdMusicMapper.insertLikePlaylistMusic(omdPlaylistMusic) > 0;
    }

    /**
     * 检查是否已经有点赞歌单音乐
     * @param omdPlaylistId 歌单ID
     * @return 音乐信息
     */
    @Override
    public OmdPlaylistMusic checkIfLikePlaylistMusic(Long omdPlaylistId) {
        return omdMusicMapper.checkIfLikePlaylistMusic(omdPlaylistId);
    }

    /**
     * 删除音乐信息点赞缓存
     * @param omdMusicInfoId 音乐信息ID
     * @param omdUserId 当前用户ID
     * @return 结果
     */
    @Override
    public boolean deleteMusicLike(Long omdMusicInfoId, Long omdUserId) {
        return omdMusicMapper.deleteMusicLike(omdMusicInfoId, omdUserId) > 0;
    }

    /**
     * 根据传入的播放列表ID查询播放列表
     * @param omdPlaylistId 播放列表ID
     * @return 音乐信息
     */
    @Override
    public List<OmdPlaylistMusic> findLikePlaylistMusicByPlaylistId(Long omdPlaylistId) {
        return omdMusicMapper.findLikePlaylistMusicByPlaylistId(omdPlaylistId);
    }

    /**
     * 根据传入的播放列表ID删除播放中间表的所有数据
     * @param omdPlaylistId 播放列表ID
     * @return 删除的记录数（通常为0或1）
     */
    @Override
    public boolean deleteAllLikePlaylistMusic(Long omdPlaylistId) {
        return omdMusicMapper.deleteAllLikePlaylistMusic(omdPlaylistId) > 0;
    }

    /**
     * 根据传入的播放列表ID删除播放列表
     * @param omdPlaylistId 播放列表ID
     * @param omdPlaylistName 播放列表名称
     * @return 删除的记录数（通常为0或1）
     */
    @Override
    public boolean deleteLikePlaylist(Long omdPlaylistId, String omdPlaylistName) {
        return omdMusicMapper.deleteLikePlaylist(omdPlaylistId, omdPlaylistName) > 0;
    }

    /**
     * 根据传入的播放列表ID和音乐ID删除播放列表音乐
     * @param omdPlaylistId 播放列表ID
     * @param omdMusicInfoId 音乐ID
     * @return 删除的记录数（通常为0或1）
     */
    @Override
    public boolean deleteLikePlaylistMusic(Long omdPlaylistId, Long omdMusicInfoId) {
        return omdMusicMapper.deleteLikePlaylistMusic(omdPlaylistId, omdMusicInfoId) > 0;
    }

    /**
     * 检查歌单名称是否存在
     * @param omdPlaylistName 歌单名称
     * @param omdUserId 当前用户ID
     * @return 结果
     */
    @Override
    public OmdPlaylist checkPlaylistNameIfExist(String omdPlaylistName, Long omdUserId) {
        return omdMusicMapper.checkPlaylistNameIfExist(omdPlaylistName, omdUserId);
    }

    /**
     * 新增歌单
     * @param omdPlaylist 歌单
     * @return 结果
     */
    @Override
    public boolean insertPlaylist(OmdPlaylist omdPlaylist) {
        return omdMusicMapper.insertPlaylist(omdPlaylist) > 0;
    }

    /**
     * 修改播放列表
     * @param omdPlaylist 播放列表
     * @return 结果
     */
    @Override
    public boolean updatePlaylist(OmdPlaylist omdPlaylist) {
        return omdMusicMapper.updatePlaylist(omdPlaylist) > 0;
    }

    /**
     * 新增播放列表音乐
     * @param omdPlaylistMusic 音乐信息点赞
     * @return 结果
     */
    @Override
    public boolean insertPlaylistMusic(OmdPlaylistMusic omdPlaylistMusic) {
        return omdMusicMapper.insertPlaylistMusic(omdPlaylistMusic) > 0;
    }

    /**
     * 根据传入的播放列表ID删除播放列表
     * @param omdPlaylistId 播放列表ID
     * @return 音乐信息
     */
    @Override
    public boolean deletePlaylist(Long omdPlaylistId) {
        return omdMusicMapper.deletePlaylist(omdPlaylistId) > 0;
    }

    /**
     * 根据传入的播放列表ID和音乐ID查询是否存在
     * @param omdPlaylistId 播放列表ID
     * @param omdMusicInfoId 音乐ID
     * @return 音乐信息
     */
    @Override
    public OmdPlaylistMusic checkIfMusicExistInPlaylist(Long omdPlaylistId, Long omdMusicInfoId) {
        return omdMusicMapper.checkIfMusicExistInPlaylist(omdPlaylistId, omdMusicInfoId);
    }


    /**
     * 根据传入的播放列表ID获取已存在的音乐信息ID集合
     * @param omdPlaylistId 播放列表ID
     * @return Set<Long> 音乐信息ID集合
     */
    @Override
    public Set<Long> getExistingMusicIdsByPlaylistId(Long omdPlaylistId) {
        return omdMusicMapper.getExistingMusicIdsByPlaylistId(omdPlaylistId);
    }

    /**
     * 根据传入的歌曲信息集合插入到播放列表中
     * @param omdPlaylistMusicList 插入的歌曲信息集合
     * @return Set<Long> 音乐信息ID集合
     */
    @Override
    public boolean insertMusicListToPlaylist(List<OmdPlaylistMusic> omdPlaylistMusicList) {
        return omdMusicMapper.insertMusicListToPlaylist(omdPlaylistMusicList) > 0;
    }

    /**
     * 根据传入的播放列表ID和音乐ID列表删除播放列表音乐
     * @param omdPlaylistId 播放列表ID
     * @param omdMusicInfoIdList 音乐ID列表
     * @return 删除的记录数
     * */
    @Override
    public int deleteMusicListFromPlaylist(Long omdPlaylistId, List<Long> omdMusicInfoIdList) {
        return omdMusicMapper.deleteMusicListFromPlaylist(omdPlaylistId, omdMusicInfoIdList);
    }

    /**
     * 根据传入的音乐ID列表查询音乐被点赞的次数
     * @param omdMusicInfoIdList 音乐ID列表
     * @return 音乐信息
     */
    @Override
    public List<OmdMusicLikeCache> selectByOmdMusicInfoIdList(List<Long> omdMusicInfoIdList) {
        return omdMusicMapper.selectByOmdMusicInfoIdList(omdMusicInfoIdList);
    }

    /**
     * 根据传入的用户ID和音乐ID列表查询用户是否已经点赞过
     * @param omdUserId 用户ID
     * @param omdMusicInfoIdList 音乐ID列表
     * @return 音乐信息
     */
    @Override
    public List<OmdMusicLike> selectByUserIdAndMusicIds(Long omdUserId, List<Long> omdMusicInfoIdList) {
        return omdMusicMapper.selectByUserIdAndMusicIds(omdUserId, omdMusicInfoIdList);
    }

    /**
     * 根据传入的播放列表ID查询播放列表
     * @param omdPlaylistId 播放列表ID
     * @return 音乐信息
     */
    @Override
    public List<OmdPlaylistMusic> getMusicListFromPlaylist(Long omdPlaylistId) {
        return omdMusicMapper.getMusicListFromPlaylist(omdPlaylistId);
    }

    /**
     * 根据传入的播放列表ID查询最大的音乐顺序
     * @param omdPlaylistId 播放列表ID
     * @return 最大的音乐顺序
     */
    @Override
    public int getMaxPlaylistMusicOrder(Long omdPlaylistId) {
        return omdMusicMapper.getMaxPlaylistMusicOrder(omdPlaylistId);
    }

    /**
     * 根据传入的音乐ID列表更新音乐信息点赞缓存
     * @param omdMusicInfoIdList 音乐ID列表
     * @param delta 增量（正数增加，负数减少）
     * @return 更新的记录数
     */
    @Override
    public int updateLikeCacheList(List<Long> omdMusicInfoIdList, int delta) {
        return omdMusicMapper.updateLikeCacheList(omdMusicInfoIdList, delta);
    }

    /**
     * 批量插入音乐信息点赞
     * @param omdMusicLikeList 音乐信息点赞
     * @return 结果
     */
    @Override
    public boolean insertLikeMusicInfoList(List<OmdMusicLike> omdMusicLikeList) {
        return omdMusicMapper.insertLikeMusicInfoList(omdMusicLikeList) > 0;
    }

    /**
     * 根据传入的音乐ID列表删除音乐信息点赞
     * @param omdMusicInfoIdList 音乐ID列表
     * @param omdUserId 当前用户ID
     * @return 结果
     */
    @Override
    public boolean deleteLikeMusicInfoList(List<Long> omdMusicInfoIdList, Long omdUserId) {
        return omdMusicMapper.deleteLikeMusicInfoList(omdMusicInfoIdList, omdUserId) > 0;
    }

    /**
     * 根据传入的音乐ID查询音乐信息
     * @param targetId 音乐ID
     * @return 音乐信息
     */
    @Override
    public OmdMusicInfo getMusicInfoByOmdMusicInfoId(Long targetId) {
        return omdMusicMapper.getMusicInfoByOmdMusicInfoId(targetId);
    }
}
