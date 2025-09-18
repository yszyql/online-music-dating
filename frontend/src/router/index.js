//导入vue-router
import { createRouter, createWebHistory } from 'vue-router'
import {ElMessage} from "element-plus";

// 导入状态管理
import { useAuthStore } from '@/stores/auth.js'

//导入主要页面
import Home from '@/views/Home.vue';
import Login from '@/views/Login.vue';
// 导入管理页面组件
import AdminHome from '@/views/admin/AdminHome.vue';
import AdminInfo from "@/views/admin/AdminInfo.vue";
import UserList from '@/views/admin/UserList.vue'
import MusicList from "@/views/admin/MusicList.vue";
import ReviewMusic from "@/views/admin/ReviewMusic.vue";
import ReviewSingerApplication from "@/views/admin/ReviewSingerApplication.vue";
import AdminList from "@/views/admin/AdminList.vue";
import ReviewCommentReport from "@/views/admin/ReviewCommentReport.vue";
import ReviewFeedback from "@/views/admin/ReviewFeedback.vue";
import ReviewUserReport from "@/views/admin/ReviewUserReport.vue";
import ReviewMusicReport from "@/views/admin/ReviewMusicReport.vue";
import ReviewMusicAppeal from "@/views/admin/ReviewMusicAppeal.vue";
import ReviewUserAppeal from "@/views/admin/ReviewUserAppeal.vue";
import OperationLog from "@/views/admin/OperationLog.vue";
// 导入用户页面组件
import UserHome from '@/views/user/UserHome.vue';
import UserInfo from '@/views/user/UserInfo.vue'
import ApplicationSinger from "@/views/user/ApplicationSinger.vue";
import UpdatePassword from "@/views/user/UpdatePassword.vue";
import ReportComment from "@/views/user/ReportComment.vue";
import ReportUser from "@/views/user/ReportUser.vue";
import ReportMusic from "@/views/user/ReportMusic.vue";
import Feedback from "@/views/user/Feedback.vue";
// 导入音乐页面组件
import MusicHome from "@/views/music/MusicHome.vue";
import MusicBox from "@/views/music/MusicBox.vue";
import CategoryBrowsing from "@/views/music/CategoryBrowsing.vue";
import ChatMessage from "@/views/music/ChatMessage.vue";
import ChatNotice from "@/views/music/ChatNotice.vue";
// 导入歌手页面组件
import SingerHome from "@/views/singer/SingerHome.vue";
import SingerInfo from "@/views/singer/SingerInfo.vue";
import UpdateSingerPassword from "@/views/singer/UpdateSingerPassword.vue";
import UploadMusic from "@/views/singer/UploadMusic.vue";
import OwnMusic from "@/views/singer/OwnMusic.vue";
import MusicAppeal from "@/views/singer/MusicAppeal.vue";
import MusicReportToMyself from "@/views/singer/MusicReportToMyself.vue";
// 导入详情组件
import IntroductionHome from "@/views/introduction/IntroductionHome.vue";
import MusicDetail from "@/views/introduction/MusicDetail.vue";
import UserDetail from "@/views/introduction/UserDetail.vue";
import SingerDetail from "@/views/introduction/SingerDetail.vue";
import AdminDetail from "@/views/introduction/AdminDetail.vue";

//定义路由关系
const routes = [
    { path: '/login', component: Login ,meta: { requiresAuth: false } }, //登录页面
    { path: '/', component: Home , meta: { requiresAuth: false } }, // 展示页面
    // 管理员
    { path: '/admin', component: AdminHome, redirect:'/admin/info', meta: { requiresAuth: true }, //  管理页面
        children: [
            { path: 'info' , component: AdminInfo , meta: { requiresAuth: true } },
            { path: 'userList', component: UserList , meta: { requiresAuth: true } }, // 用户列表页面
            { path: 'musicList', component: MusicList , meta: { requiresAuth: true } }, // 音乐列表页面
            { path: 'reviewMusic', component: ReviewMusic , meta: { requiresAuth: true } }, // 上传音乐页面
            { path: 'reviewSingerApplication', component: ReviewSingerApplication , meta: { requiresAuth: true } }, // 歌手申请页面
            { path: 'adminList', component: AdminList , meta: { requiresAuth: true } }, // 管理员列表页面
            { path: 'reviewCommentReport', component: ReviewCommentReport, meta: { requiresAuth: true } }, // 举报评论审核页面
            { path: 'reviewFeedback', component: ReviewFeedback, meta: { requiresAuth: true } }, // 反馈评论审核页面
            { path: 'reviewUserReport', component: ReviewUserReport, meta: { requiresAuth: true } }, // 用户举报审核页面
            { path: 'reviewMusicReport', component: ReviewMusicReport, meta: { requiresAuth: true } }, // 音乐举报审核页面
            { path: 'reviewMusicAppeal', component: ReviewMusicAppeal, meta: { requiresAuth: true } }, // 音乐申诉审核页面
            { path: 'reviewUserAppeal', component: ReviewUserAppeal, meta: { requiresAuth: true } }, // 用户申诉审核页面
            { path: 'operationLog', component: OperationLog, meta: { requiresAuth: true } }, // 操作日志页面
        ]
    },
    // 普通用户
    { path: '/user', component: UserHome,redirect:'/user/info', meta: { requiresAuth: true }, // 用户页面
        children: [
            { path: 'info' , component: UserInfo , meta: { requiresAuth: true } }, // 用户基本信息
            { path: 'applicationSinger', component: ApplicationSinger , meta: { requiresAuth: true } }, // 申请成为歌手
            { path: 'updatePassword', component: UpdatePassword , meta: { requiresAuth: true } }, // 更新密码
            { path: 'reportComment', component: ReportComment, meta: { requiresAuth: true } }, // 举报评论
            { path: 'reportUser', component: ReportUser, meta: { requiresAuth: true } }, // 举报用户
            { path: 'reportMusic', component: ReportMusic, meta: { requiresAuth: true } }, // 举报音乐
            { path: 'feedback', component: Feedback, meta: { requiresAuth: true } }, // 反馈
        ]
    },
    // 音乐页面
    { path: '/music', component: MusicHome , redirect: '/music/musicBox', meta: { requiresAuth: true }, // 音乐页面
        children: [
            { path: 'musicBox', component: MusicBox , meta: { requiresAuth: true } }, // 音乐盒
            { path: 'categoryBrowsing', component: CategoryBrowsing , meta: { requiresAuth: false } }, // 分类浏览
            { path: 'chatMessage', component: ChatMessage , meta: { requiresAuth: true } }, // 聊天消息
            { path: 'chatNotice', component: ChatNotice , meta: { requiresAuth: true } }, // 消息通知
        ]
    },
    // 歌手页面
    { path: '/singer', component: SingerHome, redirect: '/singer/info', meta: { requiresAuth: true }, // 歌手页面
        children: [
            { path: 'info', component: SingerInfo , meta: { requiresAuth: true } }, // 歌手信息
            { path: 'uploadMusic', component: UploadMusic , meta: { requiresAuth: true } }, // 上传音乐
            { path: 'updateSingerPassword', component: UpdateSingerPassword, meta: { requiresAuth: true } }, // 更新密码
            { path: 'ownMusic', component: OwnMusic, meta: { requiresAuth: true } }, // 自己的音乐
            { path: 'musicAppeal', component: MusicAppeal, meta: { requiresAuth: true } }, // 音乐申诉
            { path: 'musicReportToMyself', component: MusicReportToMyself, meta: { requiresAuth: true } }, // 自己的音乐举报
        ]
    },
    // 介绍页面
    { path: '/introduction', component: IntroductionHome, meta: { requiresAuth: false }, // 介绍页面
        children: [
            { path: 'musicDetail/:omdMusicInfoId', name: 'music-detail', component: MusicDetail, meta: { requiresAuth: false }, props: true }, // 音乐详情页面
            { path: 'userDetail/:omdUserId', name:'user-detail', component: UserDetail, meta: { requiresAuth: false }, props: true }, // 用户详情页面
            { path: 'singerDetail/:omdSingerId', name:'singer-detail', component: SingerDetail, meta: { requiresAuth: false }, props: true }, // 歌手详情页面
            { path: 'adminDetail/:omdAdminId', name:'admin-detail', component: AdminDetail, meta: { requiresAuth: false }, props: true }, // 管理员详情页面
        ]
    }

]

// 创建路由实例并传递 `routes` 配置
const router = createRouter({
    history: createWebHistory(), //路由模式为history模式
    routes: routes //路由关系
});

// 全局前置守卫
router.beforeEach((to, from, next) => {
    try {
        const authStore = useAuthStore()
        const requiresAuth = to.meta.requiresAuth || false;

        if (!requiresAuth) {
            console.log('允许访问，无需登录');
            next();
        } else {
            if (authStore.isLoggedIn) {
                console.log('已登录，允许访问');
                next();
            } else {
                // 未登录，显示带关闭回调的提示框
                const messageBox = ElMessage({
                    message: '您需要登录才能访问此页面',
                    type: 'warning',
                    center: true, // 居中显示
                });
                // 阻止当前导航
                next(false);
            }
        }
    } catch (error) {
        console.error('路由守卫异常:', error);
        next(); // 发生异常时允许访问，避免卡死
    }
});

// 导出路由实例
export default router