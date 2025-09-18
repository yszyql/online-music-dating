package org.x.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.x.backend.pojo.*;
import org.x.backend.service.impl.TransactionMusicService;
import org.x.backend.service.OmdSingerService;
import org.x.backend.service.impl.CosService;
import org.x.backend.utils.*;

import java.util.Map;


/**
 * 歌手表 前端控制器
 */
@RestController
@RequestMapping("/singer")
@Validated
@Slf4j
public class OmdSingerController {

    // 歌手表的service层
    @Autowired
    private OmdSingerService omdSingerService;

    // 事务服务层
    @Autowired
    private TransactionMusicService transactionMusicService;

    // cos服务层
    @Autowired
    private CosService cosService;

    // HelperUtil工具类
    @Autowired
    private HelperUtil helperUtil;


    /**
     * 获取歌手信息
     * @return 歌手
     */
    @GetMapping("/getOmdSingerInfo")
    public Result<OmdSinger> getOmdSingerInfo() {
        OmdSinger omdSinger = omdSingerService.findSingerByUserId(helperUtil.getCurrentUserId());
        if (omdSinger == null) {
            return Result.error("未获取到用户信息");
        }
        return Result.success(omdSinger);
    }

    /**
     * 更新歌手信息
     * @param omdSinger 歌手
     * @return 结果
     */
    @PostMapping("/updateOmdSinger")
    public Result<String> updateOmdSinger(@RequestBody OmdSinger omdSinger) {
        boolean result = omdSingerService.updateSinger(omdSinger);
        if (!result) {
            return Result.error("更新失败");
        }
        return Result.success("更新成功");
    }

    /**
     * 歌曲文件文件上传
     * @param songFile 文件
     * @return 文件访问URL
     */
    @PostMapping("/songFileUpload")
    public Result songFileUpload(@RequestParam("songFile") MultipartFile songFile){
        String songUrl = cosService.fileUpload(songFile, CosTagsUtil.SONG);
        return Result.success(songUrl);
    }

    /**
     * 歌词文件文件上传
     * @param lrcFile 文件
     * @return 文件访问URL
     */
    @PostMapping("/lrcFileUpload")
    public Result lrcFileUpload(@RequestParam("lrcFile") MultipartFile lrcFile){
        String lrc = cosService.fileUpload(lrcFile, CosTagsUtil.LRC);
        return Result.success(lrc);
    }

    /**
     * 新增歌曲
     * 同时新增歌曲歌词信息和歌曲点赞信息
     * @param requestMap 歌曲和歌词信息
     * @return 结果
     */
    @PostMapping("/insertSongAndLyric")
    public Result<String> insertSongAndLyric(@RequestBody Map<String, Object> requestMap) {

        // 手动转换参数类型
        ObjectMapper mapper = new ObjectMapper();
        OmdMusicInfo musicInfo = mapper.convertValue(requestMap.get("musicInfo"), OmdMusicInfo.class);
        OmdMusicLyric musicLyric = mapper.convertValue(requestMap.get("musicLyric"), OmdMusicLyric.class);

        // 获取当前歌手信息
        OmdSinger omdSinger = omdSingerService.findSingerByUserId(helperUtil.getCurrentUserId());
        if (omdSinger == null) {
            return Result.error("未获取到用户信息");
        }

        // 设置歌手ID
        musicInfo.setOmdSingerId(omdSinger.getOmdSingerId());

        // 调用事务服务（异常由全局处理器处理）
        transactionMusicService.insertMusicInfoAndMusicLyricAndMusicLikeCache(musicInfo, musicLyric);
        return Result.success("新增成功");
    }

    /**
     * 获取歌手自己的音乐信息列表
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param omdMusicInfoStatus 歌曲状态
     * @return 音乐信息列表
     */
    @GetMapping("/getMusicInfoListBySingerId")
    public Result<PageBean<OmdMusicInfo>> getMusicInfoListBySingerId(@RequestParam("pageNum") Integer pageNum,
                                                                     @RequestParam("pageSize") Integer pageSize,
                                                                     @RequestParam(value = "omdMusicInfoStatus",required = false ) Integer omdMusicInfoStatus) {
        // 根据用户id查询歌手信息
        OmdSinger omdSinger = omdSingerService.findSingerByUserId(helperUtil.getCurrentUserId());

        // 执行分页查询
        PageBean<OmdMusicInfo> musicInfoListBySingerId = helperUtil.executePageQuery(
                pageNum,
                pageSize,
                () -> omdSingerService.getMusicInfoListBySingerId(omdSinger.getOmdSingerId(), omdMusicInfoStatus)
        );

        // 返回正确的分页对象
        return Result.success(musicInfoListBySingerId);
    }

    /**
     * 根据歌曲ID查询是否申诉过
     * @param omdMusicInfoId 歌曲ID
     * @return 歌曲信息
     */
    @GetMapping("/getMusicAppealStatusByMusic")
    public Result<Boolean> getMusicAppealStatusByMusic(@RequestParam("omdMusicInfoId") Long omdMusicInfoId) {
        // 根据用户id查询歌手信息
        OmdSinger omdSinger = omdSingerService.findSingerByUserId(helperUtil.getCurrentUserId());
        // 查询是否已经申诉过
        if (omdSingerService.findAppealByMusicInfoId(omdMusicInfoId,omdSinger.getOmdSingerId()) != null){
            return Result.success(false);
        }
        return Result.success(true);
    }

    /**
     * 插入歌曲申诉信息
     * @param omdMusicAppeal 歌曲申诉信息
     * @return 结果
     */
    @PostMapping("/insertMusicAppeal")
    public Result<String> insertMusicAppeal(@RequestBody OmdMusicAppeal omdMusicAppeal) {
        // 根据用户id查询歌手信息
        OmdSinger omdSinger = omdSingerService.findSingerByUserId(helperUtil.getCurrentUserId());

        // 查询是否已经申诉过
        if (omdSingerService.findAppealByMusicInfoId(omdMusicAppeal.getOmdMusicInfoId(),omdSinger.getOmdSingerId()) != null){
            return Result.error("您已经申诉过该歌曲");
        }

        // 插入歌曲申诉信息
        omdMusicAppeal.setOmdSingerId(omdSinger.getOmdSingerId());
        if (!omdSingerService.insertMusicAppeal(omdMusicAppeal)){
            return Result.error("插入失败");
        }
        return Result.success("申诉成功");
    }

    /**
     * 获取歌手自己的音乐申诉列表
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param omdMusicAppealStatus 申诉状态
     * @return 音乐信息列表
     */
    @GetMapping("/getMusicAppealListBySingerId")
    public Result<PageBean<OmdMusicAppeal>> getMusicAppealListBySingerId(@RequestParam("pageNum") Integer pageNum,
                                                                         @RequestParam("pageSize") Integer pageSize,
                                                                         @RequestParam(value = "omdMusicAppealStatus",required = false) Integer omdMusicAppealStatus){
        // 根据用户id查询歌手信息
        OmdSinger omdSinger = omdSingerService.findSingerByUserId(helperUtil.getCurrentUserId());

        // 执行分页查询
        PageBean<OmdMusicAppeal> omdMusicAppealList = helperUtil.executePageQuery(
                pageNum,
                pageSize,
                () -> omdSingerService.getMusicAppealListBySingerId(omdSinger.getOmdSingerId(),omdMusicAppealStatus)
        );

        // 返回正确的分页对象
        return Result.success(omdMusicAppealList);
    }

    /**
     * 更新歌曲申诉信息
     * @param omdMusicAppealId 歌曲申诉ID
     * @param omdMusicAppealEvidence 歌曲申诉证据
     * @param omdMusicAppealReason 歌曲申诉原因
     * @return 结果
     */
    @PostMapping("/updateMusicAppeal")
    public Result<String> updateMusicAppeal(@RequestParam("omdMusicAppealId") Long omdMusicAppealId,
                                            @RequestParam("omdMusicAppealEvidence") String omdMusicAppealEvidence,
                                            @RequestParam("omdMusicAppealReason") String omdMusicAppealReason ){
        // 查询歌曲申诉信息
        OmdMusicAppeal omdMusicAppeal = omdSingerService.findMusicAppealById(omdMusicAppealId);

        // 更新歌曲申诉信息
        omdMusicAppeal.setOmdMusicAppealEvidence(omdMusicAppealEvidence);
        omdMusicAppeal.setOmdMusicAppealReason(omdMusicAppealReason);
        omdMusicAppeal.setOmdMusicAppealStatus(0);
        if (!omdSingerService.updateMusicAppeal(omdMusicAppeal)){
            return Result.error("更新失败");
        }
        return Result.success("重新申诉成功");
    }

    /**
     * 根据歌曲ID重新上传歌曲
     * @param omdMusicInfoId 歌曲ID
     * @return 歌曲信息
     */
    @PostMapping("/updateMusicInfoStatus")
    public Result<String> updateMusicInfoStatus(@RequestParam("omdMusicInfoId") Long omdMusicInfoId){
        if (!omdSingerService.updateMusicInfoStatus(omdMusicInfoId)){
            return Result.error("更新失败");
        }
        return Result.success("更新成功");
    }

    /**
     * 根据歌曲ID删除歌曲
     * @param omdMusicInfoId 歌曲ID
     * @return 结果
     */
    @PostMapping("/deleteMusicInfo")
    public Result<String> deleteMusicInfo(@RequestParam("omdMusicInfoId") Long omdMusicInfoId){
        if (!omdSingerService.deleteMusicInfo(omdMusicInfoId)){
            return Result.error("删除失败");
        }
        return Result.success("删除成功");
    }

    /**
     * 根据歌曲ID查询歌曲是否因举报而下架
     * @param omdMusicInfoId 歌曲ID
     * @return 歌曲信息
     */
    @GetMapping("/findMusicReportById")
    public Result<Boolean> findMusicReportById(@RequestParam("omdMusicInfoId") Long omdMusicInfoId){
        if (omdSingerService.findMusicReportById(omdMusicInfoId) != null){
            return Result.success(true);
        }
        return Result.success(false);
    }

    /**
     * 获取当前登录歌手的歌曲被举报而下架的信息
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 结果
     */
    @GetMapping("/getMusicReportListBySingerId")
    public Result<PageBean<OmdMusicReport>> getMusicReportListBySingerId(@RequestParam("pageNum") Integer pageNum,
                                                                         @RequestParam("pageSize") Integer pageSize){
        // 根据用户id查询歌手信息
        OmdSinger omdSinger = omdSingerService.findSingerByUserId(helperUtil.getCurrentUserId());
        // 执行分页查询
        PageBean<OmdMusicReport> omdMusicReportList = helperUtil.executePageQuery(
                pageNum,
                pageSize,
                () -> omdSingerService.getMusicReportListBySingerId(omdSinger.getOmdSingerId())
        );
        // 返回正确的分页对象
        return Result.success(omdMusicReportList);
    }
}
