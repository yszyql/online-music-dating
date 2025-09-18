// 导入request.js
import request from '@/utils/request.js'

// 获取登录状态
export const checkLoginStatusService = ()=>{
    return request.get('/public/checkLoginStatus',{
        meta: {
            needLogin: false
        }
    });
}

// 普通登录接口
export const loginByUsernameOrPhoneService = (loginData)=>{
    // 借助于urlSearchParams对象，将对象转换为url参数
    const params = new URLSearchParams();
    for(let key in loginData) {
        params.append(key,loginData[key])
    }
    return request.post('/public/loginByUsernameOrPhone',params,{
        meta: {
            needLogin: false
        }
    });
}

// 注册接口
export const registerService = (registerData)=>{
    // 格式化日期（假设后端需要 yyyy-MM-dd 格式）
    if (registerData.omdUserBirthday) {
        registerData.omdUserBirthday = new Date(registerData.omdUserBirthday)
            .toISOString()
            .split('T')[0];
    }
    return request.post('/public/register',registerData,{
        meta: {
            needLogin: false
        }
    });
}

// 发送验证码
export const sendVerifyCodeService = (omdUserPhone)=>{

    return request.post('/public/sendVerifyCode', {}, {
        params: {
            omdUserPhone
        },
        meta: {
            needLogin: false
        }
    })
}

// 手机验证码登录或注册
export const loginOrRegisterByPhoneService = (loginData)=>{
    // 借助于urlSearchParams对象，将对象转换为url参数
    const params = new URLSearchParams();
    for(let key in loginData) {
        params.append(key,loginData[key])
    }
    return request.post('/public/loginOrRegisterByPhone',params,{
        meta: {
            needLogin: false
        }
    })
}

// 查询音乐被点赞的信息
export const getMusicLikeInfoListService = (omdMusicInfoIdList)=>{
    return request.get('/public/getMusicLikeInfoList', {
        params: {
            omdMusicInfoIdList: omdMusicInfoIdList.join(',')
        },
        meta: {
            needLogin: false
        }
    });
}

// 记录音乐播放
export const recordMusicPlayStatService = (musicPlayData)=>{
    // 借助于urlSearchParams对象，将对象转换为url参数
    const params = new URLSearchParams();
    for(let key in musicPlayData) {
        params.append(key,musicPlayData[key])
    }
    return request.post('/public/recordMusicPlayStat',params,{
        meta: {
            needLogin: false
        }
    })
}

// 获取音乐名称
export const getMusicInfoByIdListService = (omdMusicInfoIds)=>{
    return request.post('/public/getMusicInfoByIdList',omdMusicInfoIds,{
        meta: {
            needLogin: false
        }
    })
}

// 获取音乐播放量排行
export const getTopMusicService = (omdMusicLimit)=>{
    return request.get('/public/getTopMusic',omdMusicLimit,{
        meta: {
            needLogin: false
        }
    });
}

// 获取音乐播放量排行榜推荐列表
export const getTopMusicInfoListService = (omdMusicLimit)=>{
    return request.get('/public/getTopMusicInfoList',omdMusicLimit,{
        meta: {
            needLogin: false
        }
    })
}

// 获取随机三位歌手的基本信息
export const getRandomSingersInfoService = () =>{
    return request.get('/public/getRandomSingersInfo',{
        meta: {
            needLogin: false
        }
    })
}

// 分类浏览音乐信息
export const getMusicInfoListByQueryParamsService = (pageNum, pageSize, searchParams)=>{
    console.log("请求参数：", pageNum, pageSize, searchParams);

    // 构建请求参数对象
    const params = {
        pageNum,
        pageSize,
        ...searchParams
    };

    // 过滤掉空值参数
    const filteredParams = Object.fromEntries(
        Object.entries(params).filter(([key, value]) => value !== '' && value !== null)
    );

    return request.get('/public/getMusicInfoListByQueryParams', {
        params: filteredParams, // 使用params属性传递参数
        meta: {
            needLogin: false
        }
    })
}

// 根据歌手ID和音乐名称获取音乐信息
export const getMusicInfoBySingerIdService = (omdSingerId, omdMusicInfoName)=>{
    // 清理音乐名称中的特殊符号
    const cleanedName = omdMusicInfoName.replace(/[《》]/g, '');
    return request.get('/public/getMusicInfoBySingerId', {
        params: {
            omdSingerId,
            omdMusicInfoName: cleanedName
        },
        meta: {
            needLogin: false
        }
    })
}

// 根据验证码修改密码
export const updatePasswordByVerifyCodeService = (updateData)=>{
    // 借助于urlSearchParams对象，将对象转换为url参数
    const params = new URLSearchParams();
    for(let key in updateData) {
        params.append(key,updateData[key])
    }
    return request.post('/public/updatePasswordByVerifyCode',params,{
        meta: {
            needLogin: false
        }
    })
}

// 根据音乐ID获取歌词URL
export const getMusicInfoLyricByMusicIdService = (omdMusicInfoId)=>{
    return request.get('/public/getMusicInfoLyricByMusicId', {
        params: {
            omdMusicInfoId
        }
    },{
        meta: {
            needLogin: false
        }
    })
}

// 根据音乐ID获取评论区列表
export const getCommentsHybridByMusicIdService = (pageNum,pageSize,omdMusicInfoId)=>{

    // 构建请求参数对象
    const params = {
        pageNum : pageNum,
        pageSize : pageSize,
        omdMusicInfoId : omdMusicInfoId,
    };
    return request.get('/public/getCommentsHybridByMusicId', {
        params,
        meta: {
            needLogin: false
        }
    })
}

// 获取子评论
export const getChildCommentsByParentIdService = (pageNum,pageSize,omdMusicCommentId)=>{

    // 构建请求参数对象
    const params = {
        pageNum : pageNum,
        pageSize : pageSize,
        omdMusicCommentId : omdMusicCommentId,
    };

    return request.get('/public/getChildCommentsByParentId',{
        params,
        meta: {
            needLogin: false
        }
    })
}

// 申诉
export const submitAppealService = (appealData)=>{
    // 借助于urlSearchParams对象，将对象转换为url参数
    const params = new URLSearchParams();
    for(let key in appealData) {
        params.append(key,appealData[key])
    }
    return request.post('/public/submitAppeal',params,{
        meta: {
            needLogin: false
        }
    })
}