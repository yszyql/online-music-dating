package org.x.backend.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.x.backend.pojo.*;
import org.x.backend.service.impl.CosService;
import org.x.backend.service.impl.TransactionMusicService;
import org.x.backend.service.OmdMusicService;
import org.x.backend.utils.CosTagsUtil;
import org.x.backend.utils.HelperUtil;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/music")
@Validated
@Slf4j
public class OmdMusicController {

    // 音乐服务
    @Autowired
    private OmdMusicService omdMusicService;

    // cos服务层
    @Autowired
    private CosService cosService;

    // 事务服务层
    @Autowired
    private TransactionMusicService transactionMusicService;

    // HelperUtil工具类
    @Autowired
    private HelperUtil helperUtil;

    /**
     * 音乐信息点赞
     * @param omdMusicInfoId 音乐信息id
     * @return 音乐信息
     */
    @PostMapping("/insertLikeMusicInfo")
    public Result<String> insertLikeMusicInfo(@RequestParam("omdMusicInfoId") Long omdMusicInfoId) {

        if (omdMusicService.checkIfLiked(omdMusicInfoId, helperUtil.getCurrentUserId()) != null) {
            return Result.success("已经点赞过了");
        }
        // 调用事务服务层
        transactionMusicService.insertLikeMusicInfo(omdMusicInfoId, helperUtil.getCurrentUserId());
        return Result.success("点赞成功");
    }

    /**
     * 取消音乐信息点赞
     * @param omdMusicInfoId 音乐信息id
     * @return 音乐信息
     */
    @PostMapping("/deleteLikeMusicInfo")
    public Result<String> deleteLikeMusicInfo(@RequestParam("omdMusicInfoId") Long omdMusicInfoId) {
        if (omdMusicInfoId == null) {
            return Result.error("音乐信息为空");
        }
        // 调用事务服务层
        transactionMusicService.deleteLikeMusicInfo(omdMusicInfoId, helperUtil.getCurrentUserId());
        return Result.success("取消点赞成功");
    }

    /**
     * 获取歌单里面的音乐信息
     * @param omdPlaylistId 歌单ID
     * @return 音乐信息
     */
    @GetMapping("/getMusicListFromPlaylist")
    public Result<List<OmdPlaylistMusic>> getMusicListFromPlaylist(@RequestParam("omdPlaylistId") Long omdPlaylistId) {
        if (omdPlaylistId == null) {
            return Result.error("歌单ID为空");
        }
        List<OmdPlaylistMusic> omdPlaylistMusicList = omdMusicService.getMusicListFromPlaylist(omdPlaylistId);
        return Result.success(omdPlaylistMusicList);
    }

    /**
     * 封面文件文件上传
     * @param coverFile 文件
     * @return 文件访问URL
     */
    @PostMapping("/coverFileUpload")
    public Result coverFileUpload(@RequestParam("coverFile") MultipartFile coverFile){
        String coverUrl = cosService.fileUpload(coverFile, CosTagsUtil.COVER);
        return Result.success(coverUrl);
    }

    /**
     * 新增播放列表
     * @param omdPlaylist 播放列表
     * @return 结果
     */
    @PostMapping("/insertPlaylist")
    public Result<String> insertPlaylist(@RequestBody @Validated OmdPlaylist omdPlaylist) {
        if (omdMusicService.checkPlaylistNameIfExist(omdPlaylist.getOmdPlaylistName(), helperUtil.getCurrentUserId()) != null) {
            return Result.error("已有同名播放列表");
        }
        omdPlaylist.setOmdUserId(helperUtil.getCurrentUserId());

        transactionMusicService.insertPlaylist(omdPlaylist);

        return Result.success("新建播放列表成功");
    }

    /**
     * 修改播放列表
     * @param omdPlaylist 播放列表
     * @return 结果
     */
    @PostMapping("/updatePlaylist")
    public Result<String> updatePlaylist(@RequestBody @Validated OmdPlaylist omdPlaylist) {
        // 修改播放列表
        if (!omdMusicService.updatePlaylist(omdPlaylist)){
            return Result.error("修改播放列表失败");
        }
        return Result.success("修改播放列表成功");
    }

    /**
     * 根据传入的音乐ID加入到播放列表
     * @param omdMusicInfoId 播放列表ID
     * @return 音乐信息
     */
    @PostMapping("/insertMusicToPlaylist")
    public Result<String> insertMusicToPlaylist(@RequestParam("omdMusicInfoId") Long omdMusicInfoId ,
                                                @RequestParam("omdPlaylistName") String omdPlaylistName){
        // 校验参数
        if (omdMusicInfoId == null || omdPlaylistName == null) {
            return Result.error("加入失败：未选择歌曲或是未选择播放列表");
        }
        // 校验播放列表是否存在
        OmdPlaylist omdPlaylist = omdMusicService.checkPlaylistNameIfExist(omdPlaylistName, helperUtil.getCurrentUserId());
        if (omdPlaylist == null) {
            return Result.error("加入失败：播放列表不存在");
        }
        // 获取播放列表中当前最大的顺序号
        int maxOrder = omdMusicService.getMaxPlaylistMusicOrder(omdPlaylist.getOmdPlaylistId());
        // 校验歌曲是否已经存在于播放列表中
        if (omdMusicService.checkIfMusicExistInPlaylist(omdPlaylist.getOmdPlaylistId(), omdMusicInfoId) != null) {
            return Result.error("加入失败：歌曲已存在于播放列表中");
        }
        // 新增歌曲到播放列表
        OmdPlaylistMusic omdPlaylistMusic = new OmdPlaylistMusic();
        omdPlaylistMusic.setOmdMusicInfoId(omdMusicInfoId);
        omdPlaylistMusic.setOmdPlaylistId(omdPlaylist.getOmdPlaylistId());
        omdPlaylistMusic.setOmdPlaylistMusicOrder(maxOrder + 1);
        if (!omdMusicService.insertLikePlaylistMusic(omdPlaylistMusic)){
            return Result.error("加入失败：加入播放列表失败");
        }

        return Result.success("加入播放列表成功");
    }

    /**
     * 根据传入的音乐ID列表加入到播放列表
     * @param request 音乐ID列表
     * @return 音乐信息
     */
    @PostMapping("/insertMusicListToPlaylist")
    public Result<String> insertMusicListToPlaylist(@RequestBody Map<String, Object> request){

        String omdPlaylistName = (String) request.get("omdPlaylistName");
        // 将传入的音乐ID列表转换为Long类型的列表，为了避免出现运行时抛出ClassCastException，因为实际是List<String>的类型错误，故而使用了stream流来转换
        List<Long> omdMusicInfoIdList = ((List<?>) request.get("omdMusicInfoIdList")).stream()
                .map(omdMusicInfoId -> Long.valueOf(String.valueOf(omdMusicInfoId)))
                .collect(Collectors.toList());

        // 校验参数
        if (omdMusicInfoIdList.isEmpty() || omdPlaylistName == null) {
            return Result.error("加入失败：未选择歌曲或未选择播放列表");
        }

        // 校验播放列表是否存在
        OmdPlaylist omdPlaylist = omdMusicService.checkPlaylistNameIfExist(omdPlaylistName, helperUtil.getCurrentUserId());
        if (omdPlaylist == null) {
            return Result.error("加入失败：播放列表不存在");
        }

        // 如果批量加入的是“我喜欢的音乐”歌单，那么要先更新音乐点赞表和音乐点赞缓存表
        if (omdPlaylist.getOmdPlaylistName().equals("我喜欢的音乐")) {
            // 调用事务服务层
            transactionMusicService.insertLikeMusicInfoList(omdMusicInfoIdList, helperUtil.getCurrentUserId());
        }

        // 获取当前播放列表已存在的音乐ID
        Set<Long> existingMusicIds = omdMusicService.getExistingMusicIdsByPlaylistId(omdPlaylist.getOmdPlaylistId());
        // 获取播放列表中当前最大的顺序号
        int maxOrder = omdMusicService.getMaxPlaylistMusicOrder(omdPlaylist.getOmdPlaylistId());
        List<OmdPlaylistMusic> insertOmdPlaylistMusicLists = new ArrayList<>();
        // 将传入的音乐ID列表与已存在的音乐ID列表进行比较，找出新增的音乐ID
        int i = 1;
        for (Long omdMusicInfoId : omdMusicInfoIdList) {
            if (!existingMusicIds.contains(omdMusicInfoId)) {
                OmdPlaylistMusic omdPlaylistMusic = new OmdPlaylistMusic();
                omdPlaylistMusic.setOmdMusicInfoId(omdMusicInfoId);
                omdPlaylistMusic.setOmdPlaylistId(omdPlaylist.getOmdPlaylistId());
                omdPlaylistMusic.setOmdPlaylistMusicOrder(maxOrder + i);
                insertOmdPlaylistMusicLists.add(omdPlaylistMusic);
            }
        }

        // 批量插入音乐到播放列表
        if (!omdMusicService.insertMusicListToPlaylist(insertOmdPlaylistMusicLists)) {
            return Result.error("加入失败：加入播放列表失败");
        }

        return Result.success("加入播放列表成功");
    }

    /**
     * 根据传入的播放列表ID和音乐ID删除播放列表音乐
     * @param omdPlaylistName 播放列表
     * @param omdMusicInfoId 音乐ID
     * @return 音乐信息
     */
    @PostMapping("/deleteMusicFromPlaylist")
    public Result<String> deleteMusicFromPlaylist(@RequestParam("omdPlaylistName") String omdPlaylistName,
                                                  @RequestParam("omdMusicInfoId") Long omdMusicInfoId) {
        // 调用事务服务层
        transactionMusicService.deleteMusicFromPlaylist(omdPlaylistName, omdMusicInfoId, helperUtil.getCurrentUserId());
        return Result.success("删除播放列表音乐成功");
    }

    /**
     * 根据传入的音乐ID列表删除播放列表音乐
     * @param request 音乐ID列表
     * @return 音乐信息
     */
    @PostMapping("/deleteMusicListFromPlaylist")
    public Result<String> deleteMusicListFromPlaylist(@RequestBody Map<String, Object> request) {
        log.info("删除播放列表音乐：{}", request);

        String omdPlaylistName = (String) request.get("omdPlaylistName");
        // 将传入的音乐ID列表转换为Long类型的列表，为了避免出现运行时抛出ClassCastException，因为实际是List<String>的类型错误，故而使用了stream流来转换
        List<Long> omdMusicInfoIdList = ((List<?>) request.get("omdMusicInfoIdList")).stream()
                .map(omdMusicInfoId -> Long.valueOf(String.valueOf(omdMusicInfoId)))
                .collect(Collectors.toList());

        // 校验参数
        if (omdMusicInfoIdList.isEmpty() || omdPlaylistName == null) {
            return Result.error("删除失败：未选择歌曲或未选择播放列表");
        }

        // 如果是批量删除“我喜欢的音乐”歌单的音乐，那么要先更新音乐点赞表和音乐点赞缓存表
        if (omdPlaylistName.equals("我喜欢的音乐")) {
            // 调用事务服务层
            transactionMusicService.deleteLikeMusicInfoList(omdMusicInfoIdList, helperUtil.getCurrentUserId());
        }

        // 调用事务服务层
        transactionMusicService.deleteMusicListFromPlaylist(omdPlaylistName, omdMusicInfoIdList, helperUtil.getCurrentUserId());
        return Result.success("批量删除播放列表音乐成功");
    }

    /**
     * 根据传入的播放列表ID删除播放列表
     * @param omdPlaylistName 播放列表
     * @return 音乐信息
     * */
    @PostMapping("/deletePlaylist")
    public Result<String> deletePlaylist(@RequestParam("omdPlaylistName") String omdPlaylistName) {
        // 调用事务服务层
        transactionMusicService.deletePlaylist(omdPlaylistName, helperUtil.getCurrentUserId());
        return Result.success("删除播放列表成功");
    }

}
