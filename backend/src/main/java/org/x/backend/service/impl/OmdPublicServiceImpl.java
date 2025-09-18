package org.x.backend.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.x.backend.mapper.OmdPublicMapper;
import org.x.backend.pojo.*;
import org.x.backend.service.OmdPublicService;

import java.util.List;

@Service
public class OmdPublicServiceImpl implements OmdPublicService {

    @Autowired
    private OmdPublicMapper omdPublicMapper;

    /**
     * 增加音乐播放统计
     * @param omdMusicPlayStat 音乐播放统计信息
     */
    @Override
    public void addMusicPlayStat(OmdMusicPlayStat omdMusicPlayStat) {
        omdPublicMapper.addMusicPlayStat(omdMusicPlayStat);
    }

    /**
     * 根据音乐ID列表获取音乐名称
     * @param omdMusicInfoIdList 音乐ID列表
     * @return
     */
    @Override
    public List<OmdMusicInfo> getMusicInfoByIdList(List<Long> omdMusicInfoIdList) {
        return omdPublicMapper.getMusicInfoByIdList(omdMusicInfoIdList);
    }

    /**
     * 获取随机歌手信息
     * @return 随机歌手信息列表
     * */
    @Override
    public List<OmdSinger> getRandomSingersInfo() {
        return omdPublicMapper.getRandomSingersInfo();
    }

    /**
     * 根据歌手ID和音乐名称获取音乐信息
     * @param omdSingerId 歌手ID
     * @param omdMusicInfoName 音乐名称
     * @return 音乐信息
     */
    @Override
    public OmdMusicInfo getMusicInfoBySingerId(Long omdSingerId, String omdMusicInfoName) {
        return omdPublicMapper.getMusicInfoBySingerId(omdSingerId , omdMusicInfoName);
    }

    /**
     * 根据音乐ID获取歌词信息
     * @param omdMusicInfoId 音乐ID
     * @return 歌词信息
     */
    @Override
    public OmdMusicLyric getMusicInfoLyricById(Long omdMusicInfoId) {
        return omdPublicMapper.getMusicInfoLyricById(omdMusicInfoId);
    }

    /**
     * 根据音乐ID获取音乐评论
     * @param omdMusicInfoId 音乐ID
     * @return 音乐评论
     */
    @Override
    public List<OmdMusicComment> getMusicCommentListByMusicId(Long omdMusicInfoId) {
        return omdPublicMapper.getMusicCommentListByMusicId(omdMusicInfoId);
    }

    /**
     * 根据音乐评论ID获取子评论数量
     * @param omdMusicCommentId 音乐评论ID
     * @return 子评论数量
     */
    @Override
    public int countChildComments(Long omdMusicCommentId) {
        return omdPublicMapper.countChildComments(omdMusicCommentId);
    }

    /**
     * 根据音乐评论ID获取子评论
     * @param omdMusicCommentId 音乐评论ID
     * @return 子评论
     */
    @Override
    public List<OmdMusicComment> getChildCommentsByParentId(Long omdMusicCommentId) {
        return omdPublicMapper.getChildCommentsByParentId(omdMusicCommentId);
    }

    /**
     * 新增用户申诉
     * @param omdUserAppeal 用户申诉信息
     * @return 是否新增成功
     */
    @Override
    public boolean addOmdUserAppeal(OmdUserAppeal omdUserAppeal) {
        return omdPublicMapper.addOmdUserAppeal(omdUserAppeal) > 0;
    }

    /**
     * 根据评论ID获取音乐评论
     * @param targetId 评论ID
     * @return 音乐评论
     */
    @Override
    public OmdMusicComment getMusicCommentByMusicId(Long targetId) {
        return omdPublicMapper.getMusicCommentByMusicId(targetId);
    }
}
