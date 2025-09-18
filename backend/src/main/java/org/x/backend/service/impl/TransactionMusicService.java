package org.x.backend.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.x.backend.pojo.*;
import org.x.backend.service.OmdMusicService;
import org.x.backend.service.OmdSingerService;
import org.x.backend.utils.HelperUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 音乐交易服务实现类
 */
@Service
@Slf4j
@Transactional(rollbackFor = Exception.class) // 统一管理事务
public class TransactionMusicService {

    @Autowired
    private OmdSingerService omdSingerService;

    @Autowired
    private OmdMusicService omdMusicService;

    /**
     * 原子性插入歌曲和歌词
     * @param musicInfo 歌曲信息
     * @param musicLyric 歌词信息
     */
    public void insertMusicInfoAndMusicLyricAndMusicLikeCache(OmdMusicInfo musicInfo, OmdMusicLyric musicLyric) {
        // 检查歌曲是否已存在
        OmdMusicInfo existingMusicInfo = omdSingerService.checkIfMusicExist(musicInfo.getOmdMusicInfoName(), musicInfo.getOmdSingerId());
        if (existingMusicInfo != null) {
            throw new RuntimeException("歌曲已存在");
        }
        // 插入歌曲并获取带ID的实体
        OmdMusicInfo newMusicInfo = omdSingerService.insertMusicInfo(musicInfo);
        if (newMusicInfo == null || newMusicInfo.getOmdMusicInfoId() == null) {
            log.error("歌曲插入后 ID 未正确生成: {}", newMusicInfo);
            throw new RuntimeException("新增歌曲信息失败");
        }
        // 直接从返回的实体中获取ID
        musicLyric.setOmdMusicInfoId(newMusicInfo.getOmdMusicInfoId());
        // 插入歌词
        if (!omdSingerService.insertMusicLyric(musicLyric)) {
            throw new RuntimeException("新增歌词信息失败");
        }
        // 插入歌曲点赞信息
        OmdMusicLikeCache omdMusicLikeCache = new OmdMusicLikeCache();
        omdMusicLikeCache.setOmdMusicInfoId(newMusicInfo.getOmdMusicInfoId());
        if (!omdSingerService.insertMusicLikeCache(omdMusicLikeCache)) {
            throw new RuntimeException("新增歌曲点赞信息失败");
        }
    }

    /**
     * 原子性更新点赞数和点赞用户以及用户的点赞播放列表
     * @param omdMusicInfoId 音乐信息ID
     * @param omdUserId 当前用户ID
     */
    public void insertLikeMusicInfo(Long omdMusicInfoId,Long omdUserId) {

        try {

            // 根据传入的id查询音乐点赞缓存表
            OmdMusicLikeCache omdMusicLikeCache = omdMusicService.getMusicInfoLikeCache(omdMusicInfoId);
            if (omdMusicLikeCache == null){
                throw new RuntimeException("点赞失败：查询缓存表失败");
            }
            // 更新缓存表点赞数
            omdMusicLikeCache.setOmdMusicLikeCacheCount(omdMusicLikeCache.getOmdMusicLikeCacheCount() + 1);
            if (!omdMusicService.updateMusicLikeCache(omdMusicLikeCache)){
                throw new RuntimeException("点赞失败：更新缓存表失败");
            }

            // 插入点赞信息
            OmdMusicLike omdMusicLike = new OmdMusicLike();
            omdMusicLike.setOmdMusicInfoId(omdMusicInfoId);
            omdMusicLike.setOmdUserId(omdUserId);
            if (!omdMusicService.insertLikeMusicInfo(omdMusicLike)){
                throw new RuntimeException("点赞失败：插入点赞信息失败");
            }



            // 点赞成功后，更新用户的点赞播放列表，若是没有点赞播放列表，则创建一个
            OmdPlaylist omdPlaylist = omdMusicService.checkIfLikePlaylist(omdUserId);
            if (omdPlaylist == null){
                OmdPlaylist omdMusicLikePlaylist = new OmdPlaylist();
                omdMusicLikePlaylist.setOmdUserId(omdUserId);

                if (!omdMusicService.insertLikePlaylist(omdMusicLikePlaylist)){
                    throw new RuntimeException("点赞失败：插入点赞播放列表失败");
                }

                // 新增播放列表成功后要新建一个歌单与音乐详情表
                OmdPlaylistMusic omdPlaylistMusic = new OmdPlaylistMusic();
                omdPlaylistMusic.setOmdMusicInfoId(omdMusicInfoId);
                omdPlaylistMusic.setOmdPlaylistId(omdMusicLikePlaylist.getOmdPlaylistId());
                omdPlaylistMusic.setOmdPlaylistMusicOrder(1);

                if (!omdMusicService.insertLikePlaylistMusic(omdPlaylistMusic)) {
                    throw new RuntimeException("点赞失败：新增播放列表音乐失败");
                }
            }else {
                // 若是有，则往点赞播放列表里面插入音乐信息
                OmdPlaylistMusic omdPlaylistMusic = omdMusicService.checkIfLikePlaylistMusic(omdPlaylist.getOmdPlaylistId());
                if (omdPlaylistMusic == null){
                    throw new RuntimeException("点赞失败：插入播放列表音乐失败");
                }
                omdPlaylistMusic.setOmdMusicInfoId(omdMusicInfoId);
                omdPlaylistMusic.setOmdPlaylistId(omdPlaylist.getOmdPlaylistId());
                omdPlaylistMusic.setOmdPlaylistMusicOrder(omdPlaylistMusic.getOmdPlaylistMusicOrder() + 1);

                if (!omdMusicService.insertLikePlaylistMusic(omdPlaylistMusic)){
                    throw new RuntimeException("点赞失败：插入播放列表音乐失败");
                }
            }

        } catch (Exception e) {
            // 记录详细错误日志
            log.error("点赞操作失败", e);
            // 手动回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            // 抛出包含具体错误原因的异常
            throw new RuntimeException("点赞失败：" + e.getMessage(), e);
        }
    }

    /**
     * 批量点赞音乐
     * @param omdMusicInfoIdList 音乐ID列表
     * @param omdUserId 当前用户ID
     */
    @Transactional
    public void insertLikeMusicInfoList(List<Long> omdMusicInfoIdList , Long omdUserId) {

        try {

            // 1. 批量更新点赞数缓存表
            int updatedRows = omdMusicService.updateLikeCacheList(omdMusicInfoIdList, 1);
            if (updatedRows != omdMusicInfoIdList.size()) {
                throw new RuntimeException("批量点赞失败：更新缓存表失败");
            }

            // 2. 批量插入点赞记录
            List<OmdMusicLike> omdMusicLikeList = omdMusicInfoIdList.stream()
                    .map(musicId -> {
                        OmdMusicLike like = new OmdMusicLike();
                        like.setOmdMusicInfoId(musicId);
                        like.setOmdUserId(omdUserId);
                        return like;
                    })
                    .collect(Collectors.toList());

            if (!omdMusicService.insertLikeMusicInfoList(omdMusicLikeList)){
                throw new RuntimeException("批量点赞失败：插入点赞信息失败");
            }

        } catch (Exception e) {
            log.error("批量点赞操作失败", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw new RuntimeException("批量点赞失败：" + e.getMessage(), e);
        }
    }

    /**
     * 原子性删除点赞数和点赞用户以及用户的点赞播放列表
     * @param omdMusicInfoId 音乐信息ID
     * @param omdUserId 当前用户ID
     */
    public void deleteLikeMusicInfo(Long omdMusicInfoId, Long omdUserId) {

        try {

            // 根据传入的id查询音乐点赞缓存表
            OmdMusicLikeCache omdMusicLikeCache = omdMusicService.getMusicInfoLikeCache(omdMusicInfoId);
            // 更新缓存表点赞数
            omdMusicLikeCache.setOmdMusicLikeCacheCount(omdMusicLikeCache.getOmdMusicLikeCacheCount() - 1);
            if (!omdMusicService.updateMusicLikeCache(omdMusicLikeCache)){
                throw new RuntimeException("点赞取消失败：更新缓存表失败");
            }

            // 删除用户点赞歌曲的记录
            if (!omdMusicService.deleteMusicLike(omdMusicInfoId, omdUserId)){
                throw new RuntimeException("点赞取消失败：删除点赞信息失败");
            }

            // 取消点赞成功后，删除用户播放列表里对应的歌曲，若是唯一一首，则同时删除播放列表
            String omdPlaylistName = "我喜欢的音乐";
            deleteMusicFromPlaylist(omdPlaylistName, omdMusicInfoId, omdUserId);

        } catch (Exception e) {
            // 记录详细错误日志
            log.error("取消点赞操作失败", e);
            // 手动回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            // 抛出包含具体错误原因的异常
            throw new RuntimeException("取消点赞失败：" + e.getMessage(), e);
        }
    }

    /**
     * 批量取消点赞音乐
     * @param omdMusicInfoIdList 音乐ID列表
     * @param omdUserId 当前用户ID
     */
    @Transactional
    public void deleteLikeMusicInfoList(List<Long> omdMusicInfoIdList , Long omdUserId) {
        try {

            // 1. 批量更新点赞数缓存表
            int updatedRows = omdMusicService.updateLikeCacheList(omdMusicInfoIdList, -1);
            if (updatedRows != omdMusicInfoIdList.size()) {
                throw new RuntimeException("批量取消点赞失败：更新缓存表失败");
            }

            // 2. 批量删除点赞记录
            if (!omdMusicService.deleteLikeMusicInfoList(omdMusicInfoIdList, omdUserId)){
                throw new RuntimeException("批量取消点赞失败：删除点赞信息失败");
            }


        } catch (Exception e) {
            log.error("批量取消点赞操作失败", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw new RuntimeException("批量取消点赞失败：" + e.getMessage(), e);
        }
    }


    /**
     * 新增歌单
     * @param omdPlaylist 歌单
     * @return 结果
     */
    public void insertPlaylist(OmdPlaylist omdPlaylist) {
        if(!omdMusicService.insertPlaylist(omdPlaylist)){
            throw new RuntimeException("新增歌单失败");
        }

        OmdPlaylistMusic omdPlaylistMusic = new OmdPlaylistMusic();
        omdPlaylistMusic.setOmdPlaylistId(omdPlaylist.getOmdPlaylistId());

        if(!omdMusicService.insertPlaylistMusic(omdPlaylistMusic)){
            throw new RuntimeException("新增歌单音乐失败");
        }
    }

    /**
     * 根据传入的播放列表ID和音乐ID删除播放列表音乐
     * @param omdPlaylistName 播放列表
     * @param omdMusicInfoIdList 音乐ID列表
     * @param omdUserId 当前用户ID
     * @return 音乐信息
     */
    public void deleteMusicListFromPlaylist(String omdPlaylistName, List<Long> omdMusicInfoIdList, Long omdUserId) {
        OmdPlaylist omdPlaylist = omdMusicService.checkPlaylistNameIfExist(omdPlaylistName, omdUserId);
        // 获取播放列表中当前最大的顺序号
        int maxOrder = omdMusicService.getMaxPlaylistMusicOrder(omdPlaylist.getOmdPlaylistId());
        // 若删除的音乐列表与目前播放列表的音乐列表数目相同，则直接删除播放列表
        if (maxOrder ==  omdMusicInfoIdList.size()){
            deletePlaylist(omdPlaylistName, omdUserId);
            return;
        }

        // 若删除的音乐列表与目前播放列表的音乐列表数目不同，则删除播放列表里对应的音乐
        int deleteCount = omdMusicService.deleteMusicListFromPlaylist(omdPlaylist.getOmdPlaylistId(), omdMusicInfoIdList);

        // 如果删除的记录数不等于传入的音乐ID列表大小，则抛出异常
        if (deleteCount != omdMusicInfoIdList.size()) {
            throw new RuntimeException("删除失败：删除播放列表音乐失败");
        }
    }

    /**
     * 根据传入的播放列表ID和音乐ID删除播放列表音乐
     * @param omdPlaylistName 播放列表
     * @param omdMusicInfoId 音乐ID
     * @param omdUserId 当前用户ID
     * @return 音乐信息
     */
    public void deleteMusicFromPlaylist(String omdPlaylistName, Long omdMusicInfoId, Long omdUserId) {
        OmdPlaylist omdPlaylist = omdMusicService.checkPlaylistNameIfExist(omdPlaylistName, omdUserId);
        // 获取播放列表中当前最大的顺序号
        int maxOrder = omdMusicService.getMaxPlaylistMusicOrder(omdPlaylist.getOmdPlaylistId());
        // 如果列表为空，则删除播放列表
        if (maxOrder <= 1 && !Objects.equals(omdPlaylistName, "我喜欢的音乐")){
            deletePlaylist(omdPlaylistName, omdUserId);
        }else {
            // 删除对应歌曲
            if (!omdMusicService.deleteLikePlaylistMusic(omdPlaylist.getOmdPlaylistId(), omdMusicInfoId)) {
                throw new RuntimeException("删除失败：删除播放列表音乐失败");
            }
        }
    }

    /**
     * 根据传入的播放列表ID删除播放列表
     * @param omdPlaylistName 播放列表
     * @param omdUserId 当前用户ID
     * @return 音乐信息
     * */
    public void deletePlaylist(String omdPlaylistName, Long omdUserId) {
        if (omdPlaylistName == null || omdPlaylistName.isEmpty()) {
            throw new RuntimeException("歌单名称不能为空");
        }
        OmdPlaylist omdPlaylist = omdMusicService.checkPlaylistNameIfExist(omdPlaylistName, omdUserId);
        if (!omdMusicService.deleteAllLikePlaylistMusic(omdPlaylist.getOmdPlaylistId())){
            throw new RuntimeException("删除播放列表中间表失败");
        }
        if (!omdMusicService.deletePlaylist(omdPlaylist.getOmdPlaylistId())){
            throw new RuntimeException("删除播放列表失败");
        }
    }


//    List<OmdPlaylistMusic> omdPlaylistMusicList = omdMusicService.findLikePlaylistMusicByPlaylistId(omdPlaylist.getOmdPlaylistId());
//    int omdPlaylistMusicOrder = omdPlaylistMusicList.stream()
//            // 将OmdPlaylistMusic对象转换为int类型的omdPlaylistMusicOrder
//            .mapToInt(OmdPlaylistMusic::getOmdPlaylistMusicOrder)
//            // 获取最大的omdPlaylistMusicOrder值
//            .max()
//            .orElse(0);  // 如果列表为空，返回0

}
