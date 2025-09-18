<script setup>
import {ref, onMounted} from "vue";
import {ElMessage, ElMessageBox} from "element-plus";

// 导入状态管理
import {useAuthStore} from "@/stores/auth.js";
import {useMusicStore} from "@/stores/music.js";

// 导入服务
import {getCommentsHybridByMusicIdService, getChildCommentsByParentIdService} from "@/api/public.js";
import {
  getCommentListWithDynamicSortService,
  commentMusicService,
  likeMusicCommentOrNotService,
  reportMusicCommentService,
  deleteMusicCommentService,
  getMusicCommentListStatusByUserIdService
} from "@/api/user.js";

// 导入用户头像
import defaultAvatar from "@/assets/images/defaultAvatar.png";

// 状态管理
const musicStore = useMusicStore();
const authStore = useAuthStore();

// 响应式数据
const pageNum = ref(1);
const pageSize = ref(10);
const totalPages = ref(1);         // 总页数
const commentList = ref([]);         // 评论列表
const isLoading = ref(false);     // 加载状态

// 表单数据
// 新评论内容
const newCommentData = ref({
  omdMusicCommentContent: '',
  omdMusicInfoId: musicStore.currentMusic.omdMusicInfoId, // 假设这里有音乐ID
  omdMusicCommentParentId: 0, // 父评论ID，默认为0表示顶层评论
});

// 回复评论内容
const replyCommentData = ref({
  omdMusicCommentContent: '',
  omdMusicInfoId: musicStore.currentMusic.omdMusicInfoId, // 假设这里有音乐ID
  omdMusicCommentParentId: 0, // 父评论ID，默认为0表示顶层评论
});
const reportDialogVisible = ref(false); // 举报表单弹窗显示状态

// 举报表单数据
const omdCommentReportForm = ref({
  omdMusicCommentId: 0,
  omdCommentReportReason: '',
  omdCommentReportDescription: '',
});

// 举报原因选项
const reasonOptions = [
  {label: '垃圾广告', value: '1'},
  {label: '恶意攻击', value: '2'},
  {label: '色情内容', value: '3'},
  {label: '违法信息', value: '4'},
  {label: '其他原因', value: '5'}
];

// 表单验证规则
const omdCommentReportRules = {
  omdCommentReportReason: [{required: true, message: '请选择举报原因', trigger: 'change'}],
  omdCommentReportDescription: [{required: true, message: '请填写举报详情', trigger: 'blur'}]
};

// 监听页码变化
const handlePageChange = (pageNum) => {
  loadComments(pageNum, pageSize.value, musicStore.currentMusic.omdMusicInfoId);
};

// 监听每页数量变化
const handleSizeChange = (pageSize) => {
  loadComments(pageNum.value, pageSize, musicStore.currentMusic.omdMusicInfoId);
};

// 加载评论列表
// 加载评论列表并获取状态
const loadComments = async (currentPage, pageSize, omdMusicInfoId) => {
  if (isLoading.value) return;
  isLoading.value = true;

  try {
    // 1. 加载评论列表
    const {data} = await (authStore.isGuest
        ? getCommentsHybridByMusicIdService(currentPage, pageSize, omdMusicInfoId)
        : getCommentListWithDynamicSortService(currentPage, pageSize, omdMusicInfoId));

    // 更新总页数
    totalPages.value = data.total;
    // 2. 更新评论列表
    commentList.value = data.items.map(comment => ({
      ...comment,
      showReplies: false,
      isLoadingChildComments: false,
      loadChildCommentsError: false,
      showReplyInput: false,
      status: {  // 重要：设置默认状态
        isOwn: false,
        isLiked: false,
        isReported: false
      },
      omdMusicCommentReplies: comment.omdMusicCommentReplies || []
          .map(reply => ({
            ...reply,
            status: {  // 子评论独立状态
              isOwn: false,
              isLiked: false,
              isReported: false
            }
          }))
    }));

    // 3. 如果是登录用户，获取评论状态
    if (!authStore.isGuest && commentList.value.length > 0) {
      await fetchCommentStatuses();
    }
  } catch (error) {
    console.error("加载评论失败:", error);
    ElMessage.error("加载评论失败，请稍后再试");
  } finally {
    isLoading.value = false;
  }
};

// 获取评论状态
const fetchCommentStatuses = async () => {
  try {
    // 收集所有评论ID
    const allCommentIds = [
      ...commentList.value.map(c => c.omdMusicCommentId),
      ...commentList.value.flatMap(c => c.omdMusicCommentReplies.map(r => r.omdMusicCommentId))
    ];

    const response = await getMusicCommentListStatusByUserIdService(allCommentIds);

    const statusMap = response.data || {};

    // 安全地更新评论状态
    commentList.value = commentList.value.map(comment => {
      const status = statusMap[comment.omdMusicCommentId] || {};

      return {
        ...comment,
        status: {
          ...comment.status,
          // 从 OmdCommentStatusResult 对象中提取属性
          isLiked: status.isLiked,
          isOwn: status.isOwn,
          isReported: status.isReported
        },
        omdMusicCommentReplies: comment.omdMusicCommentReplies.map(reply => {
          const replyStatus = statusMap[reply.omdMusicCommentId] || {};

          return {
            ...reply,
            status: {
              ...reply.status,
              isLiked: replyStatus.isLiked,
              isOwn: replyStatus.isOwn,
              isReported: replyStatus.isReported
            }
          };
        })
      };
    });
  } catch (error) {
    console.error('获取评论状态失败:', error);
    ElMessage.error('获取评论状态失败，请稍后再试');
  }
};

// 发表评论
const postComment = async () => {

  if (!newCommentData.value.omdMusicCommentContent.trim()) {
    ElMessage.warning("评论内容不能为空");
    return;
  }

  try {
    // 这里需要调用发表评论的API
    const response = await commentMusicService(newCommentData.value);

    ElMessage.success(response.data || "评论发表成功");
    newCommentData.value.omdMusicCommentContent = "";
    await loadComments(1, 10, musicStore.currentMusic.omdMusicInfoId); // 重新加载第一页
  } catch (error) {
    console.error("发表评论失败:", error);
    ElMessage.error("评论发表失败，请稍后再试");
  }
};

// 处理点赞事件
const handleLike = async (omdMusicCommentId, isLiked) => {

  // 调用点赞API
  try {

    const response = await likeMusicCommentOrNotService(omdMusicCommentId);
    ElMessage.success("操作成功");
    // 安全地更新本地状态
    commentList.value = commentList.value.map(comment => {
      // 检查是否是目标父评论
      if (comment.omdMusicCommentId === omdMusicCommentId) {
        return {
          ...comment,
          status: {
            ...comment.status,
            isLiked: isLiked
          },
          omdMusicCommentLikeCount: isLiked
              ? comment.omdMusicCommentLikeCount + 1
              : comment.omdMusicCommentLikeCount - 1
        };
      }

      // 检查是否是目标子评论
      if (comment.omdMusicCommentReplies && comment.omdMusicCommentReplies.length > 0) {
        return {
          ...comment,
          omdMusicCommentReplies: comment.omdMusicCommentReplies.map(reply => {
            if (reply.omdMusicCommentId === omdMusicCommentId) {
              return {
                ...reply,
                status: {
                  ...reply.status,
                  isLiked: isLiked
                },
                omdMusicCommentLikeCount: isLiked
                    ? reply.omdMusicCommentLikeCount + 1
                    : reply.omdMusicCommentLikeCount - 1
              };
            }
            return reply;
          })
        };
      }
      return comment;
    })
    // 关键：强制刷新评论状态
    await fetchCommentStatuses();
  } catch (error) {
    console.error("操作失败:", error);
    ElMessage.error("操作失败，请稍后再试");
  }
};

// 处理举报事件
const handleReport = (omdMusicCommentId) => {

  // 初始化表单数据
  omdCommentReportForm.value.omdMusicCommentId = omdMusicCommentId;
  omdCommentReportForm.value.omdCommentReportReason = '';
  omdCommentReportForm.value.omdCommentReportDescription = '';

  // 显示举报表单弹窗
  reportDialogVisible.value = true;
};

// 提交举报表单
const submitReport = async (formData) => {

  if (!formData.omdCommentReportReason) {
    ElMessage.warning("请选择举报原因");
    return;
  }

  if (!formData.omdCommentReportDescription) {
    ElMessage.warning("请填写举报详情");
    return;
  }

  try {
    await reportMusicCommentService(formData);

    ElMessage.success('举报成功，我们将尽快审核');
    // 刷新评论状态
    await fetchCommentStatuses();

    // 关闭弹窗
    reportDialogVisible.value = false;

    // 清空表单
    omdCommentReportForm.omdMusicCommentId = 0;
    omdCommentReportForm.omdCommentReportReason = '';
    omdCommentReportForm.omdCommentReportDescription = '';

  } catch (error) {
    console.error("操作失败:", error);
    ElMessage.error("操作失败，请稍后再试");
  }
};

// 删除评论
const handleDelete = (omdMusicCommentId) => {
  ElMessageBox.confirm('确定要删除该评论吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    // 确认后调用删除接口
    try {
      await deleteMusicCommentService(omdMusicCommentId);
      ElMessage.success('评论删除成功');
      // 刷新评论列表
      await loadComments(pageNum.value, pageSize.value, musicStore.currentMusic.omdMusicInfoId);
    } catch (error) {
      console.error('删除评论接口调用失败', error);
      ElMessage.error('删除失败，请稍后再试');
    }
  }).catch(() => {
    // 取消删除操作
    ElMessage.info('已取消删除');
  });
}

// 点击回复按钮，切换回复输入框的展开状态
const handleReplyClick = (comment) => {
  comment.showReplyInput = !comment.showReplyInput;
  // 如果展开输入框，初始化回复内容为空
  if (comment.showReplyInput) {
    comment.replyContent = '';
  }
};

// 处理回复事件
const submitReply = async (comment) => {

  if (!replyCommentData.value.omdMusicCommentContent.trim()) {
    ElMessage.warning("评论内容不能为空");
    return;
  }

  replyCommentData.value.omdMusicCommentParentId = comment.omdMusicCommentId;

  try {
    // 这里需要调用发表评论的API
    const response = await commentMusicService(replyCommentData.value);

    ElMessage.success("评论发表成功");
    replyCommentData.value.omdMusicCommentContent = "";
    replyCommentData.value.omdMusicCommentParentId = 0;

    comment.showReplyInput = false;
    // 刷新评论列表
    await loadComments(pageNum.value, pageSize.value, musicStore.currentMusic.omdMusicInfoId); // 重新加载

  } catch (error) {
    console.error("发表评论失败:", error);
    ElMessage.error("评论发表失败，请稍后再试");
  }
};

// 加载子评论
const loadChildComments = async (omdMusicCommentId, comment) => {
  // 修正判断条件：只有当已经有子评论时才不重复加载
  if (comment.omdMusicCommentReplies && comment.omdMusicCommentReplies.length > 0) {
    console.log('子评论已加载，跳过');
    return;
  }

  comment.isLoadingChildComments = true;
  comment.loadChildCommentsError = false;

  try {
    const response = await getChildCommentsByParentIdService(omdMusicCommentId, 1, 10);
    comment.omdMusicCommentReplies = response.data.items || [];
    comment.childPage = {
      pageNum: 1,
      pageSize: 10,
      totalPages: Math.ceil(response.data.total / 10)
    };
  } catch (error) {
    ElMessage.error('加载回复失败，请稍后再试');
    comment.loadChildCommentsError = true;
    console.error('加载回复失败:', error);
  } finally {
    comment.isLoadingChildComments = false;
  }
};

// 加载更多子评论
const loadMoreChildComments = async (comment) => {
  if (!comment.childPage || comment.childPage.pageNum >= comment.childPage.totalPages) return;

  comment.isLoadingMore = true;

  try {
    const nextPage = comment.childPage.pageNum + 1;
    const response = await getChildCommentsByParentIdService(
        comment.omdMusicCommentId,
        nextPage,
        comment.childPage.pageSize
    );

    // 合并新旧数据
    comment.omdMusicCommentReplies = [
      ...comment.omdMusicCommentReplies,
      ...(response.data.items || [])
    ];
    comment.childPage.pageNum = nextPage;
  } catch (error) {
    ElMessage.error('加载更多回复失败，请稍后再试');
    console.error('加载更多回复失败:', error);
  } finally {
    comment.isLoadingMore = false;
  }
};

// 切换子评论显示/隐藏
const toggleReplies = (comment) => {
  comment.showReplies = !comment.showReplies;
  console.log('切换子评论显示状态:', comment.showReplies);

  // 只有在展开操作时才加载子评论
  if (comment.showReplies) {
    // 检查是否已经加载过子评论（避免重复加载）
    if (!comment.childPage || comment.childPage.totalPages === 0) {
      console.log('子评论未加载，调用接口加载');
      loadChildComments(comment.omdMusicCommentId, comment);
    } else {
      console.log('子评论已加载，直接显示');
    }
  }
};

// 组件挂载时加载评论
onMounted(() => {
  loadComments(pageNum.value, pageSize.value, musicStore.currentMusic.omdMusicInfoId);
});


</script>

<template>

  <div class="comments-container">
    <h2 class="comments-title">歌曲评论
      <el-tag v-if="authStore.isGuest" type="warning">游客模式不可评论</el-tag>
    </h2>

    <!-- 评论列表 -->
    <div class="comments-list" v-if="commentList !== null">
      <div
          v-for="comment in commentList"
          :key="comment.omdMusicCommentId"
          class="comment-item"
      >
        <div class="comment-header">
          <el-avatar :size="36" :src="comment.omdUser.omdUserAvatar || defaultAvatar"/>
          <div class="comment-info">
            <h3 class="comment-name">
              {{ comment.omdUser.omdUserNickname || comment.omdUser.omdUserName }}
              <el-tag v-if="comment.status?.isOwn" style="margin-left: 5px">本人</el-tag>
            </h3>
            <p class="comment-time">{{ comment.omdMusicCommentCreateTime }}</p>
          </div>
        </div>

        <div class="comment-content">{{ comment.omdMusicCommentContent }}</div>

        <div class="comment-actions">
          <!-- 点赞按钮 -->
          <el-button
              type="text"
              v-if="!authStore.isGuest"
              @click="handleLike(comment.omdMusicCommentId,(!comment.status?.isLiked) || false)"
              :class="{ 'liked': comment.status?.isLiked || false, 'reported': comment.status?.isReported || false }"
              class="action-btn"
          >
            <el-icon :size="16" v-if="!comment.status?.isLiked">
              <CircleCheck/>
            </el-icon>
            <el-icon :size="16" v-else>
              <CircleCheckFilled/>
            </el-icon>
            <span :class="{ 'liked': comment.status?.isLiked || false }">{{
                comment.status.isLiked ? '已点赞' : '点赞'
              }}</span>
            <span :class="{ 'liked': comment.status?.isLiked || false }"
                  class="like-count">{{ comment.omdMusicCommentLikeCount }}</span>
          </el-button>

          <!-- 举报按钮 -->
          <el-button type="text" v-if="!authStore.isGuest && !comment.status?.isOwn"
                     @click="handleReport(comment.omdMusicCommentId)"
                     :class="{ 'reported': comment.status?.isReported || false }"
                     class="action-btn"
                     :disabled="comment.status?.isReported"
          >
            <el-icon :size="16" v-if="!comment.status?.isReported" class="report-btn">
              <Warning/>
            </el-icon>
            <el-icon :class="{ 'reported': comment.status?.isReported || false }" :size="16" v-else>
              <WarningFilled/>
            </el-icon>
            <span :class="{ 'reported': comment.status?.isReported || false }">
              {{ comment.status?.isReported? '已举报' : '举报' }}
            </span>
          </el-button>

          <!-- 回复按钮 -->
          <el-button
              type="text"
              v-if="!authStore.isGuest"
              @click="handleReplyClick(comment)"
              class="action-btn"
          >
            <el-icon :size="16">
              <ChatLineRound/>
            </el-icon>
            <span>回复</span>
          </el-button>

          <!-- 删除按钮 -->
          <el-button type="text" v-if="!authStore.isGuest && comment.status?.isOwn || false"
                     @click="handleDelete(comment.omdMusicCommentId)" class="action-btn">
            <el-icon :size="16">
              <Delete/>
            </el-icon>
            <span>删除</span>
          </el-button>

          <!-- 子评论折叠/展开按钮 -->
          <el-button
              v-if="comment.omdMusicCommentReplyCount > 0"
              type="text"
              @click="toggleReplies(comment)"
          >
            {{ comment.showReplies ? '收起回复' : '查看回复' }}
            {{ comment.omdMusicCommentReplyCount }}条回复
          </el-button>
        </div>

        <!-- 条件渲染回复输入框和按钮 -->
        <template v-if="comment.showReplyInput">
          <div class="reply-row">
            <el-input
                v-model="replyCommentData.omdMusicCommentContent"
                placeholder="请输入回复内容"
                class="reply-input"
                type="textarea"
                rows="2"
            ></el-input>
            <el-button
                :disabled="!replyCommentData.omdMusicCommentContent.trim()"
                type="primary"
                class="reply-btn"
                @click="submitReply(comment)"
                @keyup.enter="submitReply(comment)"
            >
              回复
            </el-button>
          </div>
        </template>

        <!-- 子评论内容 -->
        <div class="replies-container" :class="{ 'active': comment.showReplies }" v-if="comment.showReplies">
          <div v-if="comment.isLoadingChildComments">
            <div class="loading">加载中...</div>
          </div>
          <div v-else-if="comment.loadChildCommentsError">
            <div class="load-error" @click="loadChildComments(comment.omdMusicCommentId, comment)">
              加载失败，点击重试
            </div>
          </div>
          <div v-else-if="comment.omdMusicCommentReplies === null">
            <div class="no-replies">暂无回复</div>
          </div>
          <div v-else>
            <div
                v-for="reply in comment.omdMusicCommentReplies"
                :key="reply.omdMusicCommentId"
                class="reply-item"
            >
              <div class="reply-header">
                <el-avatar :size="30" :src="reply.omdUser.omdUserAvatar || defaultAvatar"/>
                <div class="reply-info">
                  <h4 class="reply-name">
                    {{ reply.omdUser.omdUserNickname || reply.omdUser.omdUserName }}
                    <el-tag v-if="reply.status?.isOwn" style="margin-left: 5px">本人</el-tag>
                  </h4>
                  <p class="reply-time">{{ reply.omdMusicCommentCreateTime }}</p>
                </div>
              </div>

              <div class="reply-content">{{ reply.omdMusicCommentContent }}</div>

              <div class="reply-actions">
                <!-- 点赞按钮 -->
                <el-button
                    type="text"
                    v-if="!authStore.isGuest"
                    @click="handleLike(reply.omdMusicCommentId,!(reply.status?.isLiked || false))"
                    :class="{ 'liked': reply.status?.isLiked || false, 'reported': reply.status?.isReported || false }"
                    class="custom-like-btn action-btn"
                >
                  <el-icon :size="16" v-if="!reply.status?.isLiked">
                    <CircleCheck/>
                  </el-icon>
                  <el-icon :size="16" v-else>
                    <CircleCheckFilled/>
                  </el-icon>
                  <span :class="{ 'liked': reply.status?.isLiked || false }">{{
                      (reply.status?.isLiked || false) ? '已点赞' : '点赞'
                    }}</span>
                  <span :class="{ 'liked': reply.status?.isLiked || false }"
                        class="like-count">{{ reply.omdMusicCommentLikeCount }}</span>
                </el-button>

                <!-- 举报按钮 -->
                <el-button type="text" v-if="!authStore.isGuest && !reply.status?.isOwn"
                           @click="handleReport(reply.omdMusicCommentId)"
                           :class="{ 'reported': reply.status?.isReported || false }" class="action-btn"
                           :disabled="reply.status?.isReported">
                  <el-icon :class="{ 'reported': reply.status?.isReported || false }" :size="16" class="report-btn"
                           v-if="!reply.status?.isReported">
                    <Warning/>
                  </el-icon>
                  <el-icon :class="{ 'reported': reply.status?.isReported || false }" :size="16" v-else>
                    <WarningFilled/>
                  </el-icon>
                  <span>举报</span>
                </el-button>

                <!-- 删除按钮 -->
                <el-button type="text" v-if="!authStore.isGuest && reply.status?.isOwn || false"
                           @click="handleDelete(reply.omdMusicCommentId)" class="action-btn">
                  <el-icon :size="16">
                    <Delete/>
                  </el-icon>
                  <span>删除</span>
                </el-button>

              </div>

            </div>

            <!-- 加载更多回复 -->
            <div
                v-if="comment.childPage && comment.childPage.pageNum < comment.childPage.totalPages && !comment.isLoadingMore">
              <el-button
                  type="text"
                  @click="loadMoreChildComments(comment)"
                  class="load-more-btn"
              >
                加载更多回复
              </el-button>
            </div>
            <div v-if="comment.isLoadingMore">
              <div class="loading">加载中...</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 举报表单弹窗 -->
      <el-dialog
          v-model="reportDialogVisible"
          title="举报评论"
          width="350px"
          center
      >
        <el-form
            :model="omdCommentReportForm.value"
            :rules="omdCommentReportRules"
            label-width="100px"
        >
          <el-form-item label="举报原因" prop="omdCommentReportReason">
            <el-radio-group v-model="omdCommentReportForm.omdCommentReportReason">
              <el-radio v-for="item in reasonOptions" :key="item.value" :label="item.value">
                {{ item.label }}
              </el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="详细说明" prop="omdCommentReportDescription">
            <el-input
                v-model="omdCommentReportForm.omdCommentReportDescription"
                type="textarea"
                rows="3"
                placeholder="请详细说明举报原因"
            />
          </el-form-item>
        </el-form>

        <template #footer>
          <div class="dialog-footer">
            <el-button @click="reportDialogVisible = false">取消</el-button>
            <el-button type="danger" @click="submitReport(omdCommentReportForm)">提交举报</el-button>
          </div>
        </template>
      </el-dialog>

      <!-- 分页控制 -->
      <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :page-sizes="[10, 15, 20, 25]"
          :total="totalPages"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
          layout="prev, pager, next, jumper, ->, total, sizes"
      />

      <!-- 发表评论 -->
      <div class="comment-form">
        <el-input
            v-model="newCommentData.omdMusicCommentContent"
            type="textarea"
            rows="2"
            placeholder="写下你的评论..."
            :disabled="authStore.isGuest"
        ></el-input>
        <el-button
            type="primary"
            @click="postComment"
            :disabled="authStore.isGuest || !newCommentData.omdMusicCommentContent.trim()"
            @keyup.enter="postComment"
        >
          发表评论
        </el-button>
      </div>

    </div>

    <!-- 暂无评论提示 -->
    <div v-else class="no-comments">
      <el-empty description="暂无评论，快来发表你的看法吧"/>
    </div>

  </div>
</template>


<style scoped>

/* 评论列表容器 */
.comments-list {
  padding: 50px 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  margin-bottom: 50px;
}

.comments-title {
  margin-bottom: 30px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 单个评论项 */
.comment-item {
  padding: 16px;
  margin-bottom: 16px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.comment-item:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

/* 评论头部（头像 + 信息） */
.comment-header {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.comment-header .el-avatar {
  margin-right: 12px;
}

.comment-info {
  display: flex;
  flex-direction: column;
}

.comment-name {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin: 0;
}

.comment-time {
  font-size: 12px;
  color: #909399;
  margin: 2px 0 0 0;
}

/* 评论内容 */
.comment-content {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  margin-bottom: 12px;
}

/* 评论操作按钮区域 */
.comment-actions {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap; /* 防止按钮过多时溢出 */
}

.comment-actions .el-button {
  padding: 0;
  color: #909399;
  font-size: 14px;
  transition: color 0.3s ease;
}

.comment-actions .el-button:hover {
  color: #409eff;
}

/* 基础按钮样式：纯文字+图标，无背景/边框 */
/* 公共按钮样式（去除边框，统一间距） */
.action-btn {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  margin-right: 8px;
  color: #606266;
  font-size: 13px;
  border: none; /* 去除边框 */
  background: transparent; /* 去除背景 */
  transition: all 0.2s ease; /* 平滑过渡 */
}

.action-btn:hover {
  color: #409eff; /* 鼠标悬浮统一变主题色 */
}

.reply-row {
  display: flex;
  align-items: center; /* 垂直方向居中对齐 */
  gap: 8px; /* 输入框和按钮之间的间距，可根据需要调整 */
}

.reply-input {
  flex: 1; /* 让输入框占满剩余空间 */
}

.reply-btn {
  padding: 4px 12px; /* 按钮内边距 */
  font-size: 13px; /* 按钮文字大小 */
  color: white;
  width: 60px;
}

/* 点赞按钮状态样式 */
.liked {
  color: #409eff; /* 点赞后文字+图标变蓝色 */
}

.liked .el-icon {
  color: #409eff;
}

/* 举报按钮状态样式 */
.reported {
  color: #ff4d4f; /* 举报后文字+图标变黄色 */
}

.reported .report-btn {
  color: #ff4d4f;
}

/* 点赞数样式 */
.like-count {
  margin-left: 4px;
  font-size: 12px;
  opacity: 0.8;
}

/* 子回复容器 */
.replies-container {
  margin-top: 12px;
  margin-left: 40px; /* 缩进体现层级 */
  border-left: 1px dashed #dcdde0;
  padding-left: 12px;
}

.replies-container.active {
  animation: fadeIn 0.3s ease forwards;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-5px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 子回复项 */
.reply-item {
  padding: 12px;
  margin-bottom: 10px;
  background-color: #f8f9fa;
  border-radius: 4px;
  border: 1px solid #f0f0f0;
}

.reply-header {
  display: flex;
  align-items: center;
  margin-bottom: 6px;
}

.reply-header .el-avatar {
  margin-right: 8px;
}

.reply-info {
  display: flex;
  flex-direction: column;
}

.reply-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin: 0;
}

.reply-time {
  font-size: 12px;
  color: #909399;
  margin: 2px 0 0 0;
}

.reply-content {
  font-size: 13px;
  color: #606266;
  line-height: 1.5;
  margin-bottom: 8px;
}

/* 子回复操作按钮，和父评论操作按钮区分开，弱化一些 */
.reply-actions .el-button {
  padding: 0;
  color: #999;
  font-size: 13px;
  transition: color 0.3s ease;
}

.reply-actions .el-button:hover {
  color: #409eff;
}

/* 加载中、加载失败、暂无回复等状态 */
.loading,
.load-error,
.no-replies {
  font-size: 13px;
  color: #909399;
  margin: 8px 0;
}

.load-error {
  color: #f56c6c;
  cursor: pointer;
}

.pagination{
  margin-top: 30px;
  margin-left: 1200px;
}

.comment-form {
  display: flex;
  align-items: flex-start; /* 让输入框和按钮顶部对齐，可选 */
  margin-top: 30px;
  margin-left: 30px;
  margin-right: 15px;
}

/* 调整输入框和按钮的间距、占比等 */
.comment-form .el-input {
  margin-right: 60px; /* 与按钮保持间距 */
}

/* 如果需要调整按钮垂直对齐，可加如下样式（按需） */
.comment-form .el-button {
  align-self: flex-start;
  margin: 10px 30px; /* 与输入框顶部对齐的微调，按需 */
}

</style>