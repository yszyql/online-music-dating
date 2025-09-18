package org.x.backend.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.x.backend.mapper.OmdSingerMapper;
import org.x.backend.pojo.*;
import org.x.backend.service.OmdSingerService;

import java.util.List;

/**
 * 歌手表 服务实现类
 */
@Slf4j
@Service
public class OmdSingerServiceImpl implements OmdSingerService {

    @Autowired
    private OmdSingerMapper omdSingerMapper;

    /**
     * 根据用户ID查询歌手信息
     * @param omdUserId 用户ID
     * @return 歌手信息
     */
    @Override
    public OmdSinger findSingerByUserId(Long omdUserId) {
        return omdSingerMapper.findSingerByUserId(omdUserId);
    }


    /**
     * 更新歌手信息
     * @param omdSinger 歌手信息
     * @return 是否成功
     */
    @Override
    public boolean updateSinger(OmdSinger omdSinger) {
        return omdSingerMapper.updateSinger(omdSinger) > 0;
    }

    /**
     * 插入歌曲信息
     * @param musicInfo 歌曲信息
     * @return 歌曲信息
     */
    @Override
    public OmdMusicInfo insertMusicInfo(OmdMusicInfo musicInfo) {
        int rows = omdSingerMapper.insertMusicInfo(musicInfo);
        if (rows > 0) {
            return musicInfo; // 此时 musicInfo 已包含自增ID
        }
        return null;
    }


    /**
     * 插入歌词信息
     * @param omdMusicLyric 歌词信息
     * @return 是否成功
     */
    @Override
    public boolean insertMusicLyric(OmdMusicLyric omdMusicLyric) {
        return omdSingerMapper.insertMusicLyric(omdMusicLyric) > 0;
    }

    /**
     * 插入歌曲点赞信息
     * @param omdMusicLikeCache 歌曲点赞信息
     * @return 是否成功
     */
    @Override
    public boolean insertMusicLikeCache(OmdMusicLikeCache omdMusicLikeCache) {
        return omdSingerMapper.insertMusicLikeCache(omdMusicLikeCache) > 0;
    }

    /**
     * 根据歌曲名称查询歌曲信息
     * @param omdMusicInfoName 歌曲名称
     * @return 歌曲信息
     */
    @Override
    public OmdMusicInfo findSongByMusicInfoName(String omdMusicInfoName) {
        return omdSingerMapper.findSongByMusicInfoName(omdMusicInfoName);
    }

    /**
     * 根据当前登录的歌手ID查询歌词信息
     * @param omdSingerId 当前登录的歌手ID
     * @param omdMusicInfoStatus 歌曲状态
     * @return 歌词信息
     */
    @Override
    public List<OmdMusicInfo> getMusicInfoListBySingerId(Long omdSingerId,Integer omdMusicInfoStatus) {
        return omdSingerMapper.getMusicInfoListBySingerId(omdSingerId,omdMusicInfoStatus);
    }

    /**
     * 根据歌曲名字和歌手ID查询歌词信息
     * @param omdMusicInfoName 歌曲名字
     * @param omdSingerId 歌手ID
     * @return 歌词信息
     */
    @Override
    public OmdMusicInfo checkIfMusicExist(String omdMusicInfoName, Long omdSingerId) {
        return omdSingerMapper.checkIfMusicExist(omdMusicInfoName, omdSingerId);
    }

    /**
     * 根据歌曲ID和歌手ID查询是否申诉过
     * @param omdMusicInfoId 歌曲ID
     * @return 歌曲信息
     */
    @Override
    public OmdMusicAppeal findAppealByMusicInfoId(Long omdMusicInfoId, long omdSingerId) {
        return omdSingerMapper.findAppealByMusicInfoId(omdMusicInfoId,omdSingerId);
    }

    /**
     * 插入歌曲申诉信息
     * @param omdMusicAppeal 歌曲申诉信息
     * @return 结果
     */
    @Override
    public boolean insertMusicAppeal(OmdMusicAppeal omdMusicAppeal) {
        return omdSingerMapper.insertMusicAppeal(omdMusicAppeal) > 0;
    }

    /**
     * 根据歌手ID查询歌曲申诉信息
     * @param omdSingerId 歌手ID
     * @param omdMusicAppealStatus 歌曲申诉状态
     * @return 歌曲申诉信息
     */
    @Override
    public List<OmdMusicAppeal> getMusicAppealListBySingerId(long omdSingerId, Integer omdMusicAppealStatus) {
        return omdSingerMapper.getMusicAppealListBySingerId(omdSingerId,omdMusicAppealStatus);
    }

    /**
     * 根据歌曲申诉ID查询歌曲申诉信息
     * @param omdMusicAppealId 申诉ID
     * @return 申诉信息
     */
    @Override
    public OmdMusicAppeal findMusicAppealById(Long omdMusicAppealId) {
        return omdSingerMapper.findMusicAppealById(omdMusicAppealId);
    }

    /**
     * 更新歌曲申诉信息
     * @param omdMusicAppeal 歌曲申诉信息
     * @return 是否成功
     */
    @Override
    public boolean updateMusicAppeal(OmdMusicAppeal omdMusicAppeal) {
        return omdSingerMapper.updateMusicAppeal(omdMusicAppeal) > 0;
    }

    /**
     * 更新歌曲状态
     * @param omdMusicInfoId 歌曲ID
     * @return 是否成功
     */
    @Override
    public boolean updateMusicInfoStatus(Long omdMusicInfoId) {
        return omdSingerMapper.updateMusicInfoStatus(omdMusicInfoId) > 0;
    }

    /**
     * 删除歌曲信息
     * @param omdMusicInfoId 歌曲ID
     * @return 是否成功
     */
    @Override
    public boolean deleteMusicInfo(Long omdMusicInfoId) {
        return omdSingerMapper.deleteMusicInfo(omdMusicInfoId) > 0;
    }

    /**
     * 根据歌曲ID查询歌曲是否因举报而下架
     * @param omdMusicInfoId 歌曲ID
     * @return 歌曲信息
     */
    @Override
    public OmdMusicReport findMusicReportById(Long omdMusicInfoId) {
        return omdSingerMapper.findMusicReportById(omdMusicInfoId);
    }

    /**
     * 获取当前登录歌手的歌曲被举报而下架的信息
     * @param omdSingerId 歌手ID
     * @return 结果
     */
    @Override
    public List<OmdMusicReport> getMusicReportListBySingerId(long omdSingerId) {
        return omdSingerMapper.getMusicReportListBySingerId(omdSingerId);
    }
}
