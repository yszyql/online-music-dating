package org.x.backend.service;

import org.x.backend.pojo.*;

import java.util.List;

public interface OmdPublicService {

    /**
     * 增加音乐播放统计
     * @param omdMusicPlayStat 音乐播放统计信息
     */
    void addMusicPlayStat(OmdMusicPlayStat omdMusicPlayStat);

    /**
     * 根据音乐ID列表获取音乐名称
     * @param omdMusicInfoIdList 音乐ID列表
     * @return
     */
    List<OmdMusicInfo> getMusicInfoByIdList(List<Long> omdMusicInfoIdList);

    /**
     * 获取随机歌手信息
     * @return 随机歌手信息列表
     * */
    List<OmdSinger> getRandomSingersInfo();


    /**
     * 根据歌手ID和音乐名称获取音乐信息
     * @param omdSingerId 歌手ID
     * @param omdMusicInfoName 音乐名称
     * @return 音乐信息
     */
    OmdMusicInfo getMusicInfoBySingerId(Long omdSingerId,String omdMusicInfoName);

    /**
     * 根据音乐ID获取歌词信息
     * @param omdMusicInfoId 音乐ID
     * @return 歌词信息
     */
    OmdMusicLyric getMusicInfoLyricById(Long omdMusicInfoId);

    /**
     * 根据音乐ID获取音乐评论
     * @param omdMusicInfoId 音乐ID
     * @return 音乐评论
     */
    List<OmdMusicComment> getMusicCommentListByMusicId(Long omdMusicInfoId);

    /**
     * 根据音乐评论ID获取子评论数量
     * @param omdMusicCommentId 音乐评论ID
     * @return 子评论
     */
    int countChildComments(Long omdMusicCommentId);

    /**
     * 根据音乐评论ID获取子评论
     * @param omdMusicCommentId 音乐评论ID
     * @return 子评论
     */
    List<OmdMusicComment> getChildCommentsByParentId(Long omdMusicCommentId);

    /**
     * 新增用户申诉
     * @param omdUserAppeal 申诉信息
     * @return 是否新增成功
     */
    boolean addOmdUserAppeal(OmdUserAppeal omdUserAppeal);

    /**
     * 获取音乐评论
     * @param targetId 评论ID
     * @return 是否新增成功
     */
    OmdMusicComment getMusicCommentByMusicId(Long targetId);
}
