<script lang="ts" setup>
import {computed, onMounted, ref, watch} from 'vue'
import {ElMessage} from 'element-plus'

// 导入状态管理
import { useAuthStore } from '@/stores/auth';
import {applicationSingerService, getIsUserInfoCompleteService,findApplicationByOmdUserIdService} from "@/api/user";

// 状态管理
const authStore = useAuthStore();

// 响应式数据
const audioUrl = ref('');// 存储音频URL
const isCompleted = ref(false); // 存储用户信息是否完整
const isReady = ref(false); // 存储用户信息是否准备好
const isApplication = ref(false); // 存储用户是否已经申请
const applicationData = ref(null); // 存储用户申请之后的数据
const dialogVisible = ref(false);
const countdown = ref(5);
let countdownTimer = null;

// 表单绑定数据
const formData = ref({
  omdSingerName: '',
  omdApplicationsIntroduction: '',
  omdApplicationsGenre: '',
  omdApplicationsSingSample: '',
})

// 表单ref
const formRef = ref(null)

// 表单验证规则
const formRules = ref({
  omdSingerName: [
    {required: true, message: '请输入艺名', trigger: 'blur'},
    {min: 2, max: 16, message: '密码长度必须在2到16个字符之间', trigger: 'blur'}
  ],
  omdApplicationsIntroduction: [{required: true, message: '请输入个人简历', trigger: 'blur'}],
  omdApplicationsGenre: [
    {required: true, message: '请输入个人风格', trigger: 'blur'},
    {min: 2, max: 16, message: '个人风格长度必须在2到16个字符之间', trigger: 'blur'}
  ],
  omdApplicationsSingSample: [{required: true, message: '请上传歌曲样例', trigger: 'blur'}]
})

// 上传前的验证
const beforeAudioUpload = (file) => {
  const isAudio = file.type.startsWith('audio/');
  const isLt10M = file.size / 1024 / 1024 < 10;

  if (!isAudio) {
    ElMessage.error('只能上传音频文件!');
    return false;
  }
  if (!isLt10M) {
    ElMessage.error('音频大小不能超过10MB!');
    return false;
  }
  return true;
};

// 封面图片上传成功处理
const handleSampleSuccess = (response) => {
  if (response.data) {
    formData.value.omdApplicationsSingSample = response.data;
    audioUrl.value = response.data; // 本地预览
    ElMessage.success('上传成功');
  }
};

const showDialog = () => {
  dialogVisible.value = true;
  countdown.value = 5;

  // 清除现有计时器（如果有）
  if (countdownTimer) {
    clearInterval(countdownTimer);
  }

  // 启动倒计时
  countdownTimer = setInterval(() => {
    countdown.value -= 1;
    if (countdown.value <= 0) {
      clearInterval(countdownTimer);
    }
  }, 1000);
};

// 申请成为歌手
const handleApplication = async () => {
  if (!formRef.value) {
    ElMessage.error('表单实例未获取');
    return;
  }
  await formRef.value.validate(async (valid) => {
    if (valid) {
      await applicationSingerService(formData.value);
      ElMessage.success('申请成功，请耐心等待审核');
      isApplication.value = true;
    }
  })
}

// 监听弹窗关闭
watch(dialogVisible, (newVal) => {
  if (!newVal && countdownTimer) {
    clearInterval(countdownTimer);
  }
});

// 监听是否申请
watch(isApplication, async (newVal) => {
  if (newVal) {
    await findApplicationByOmdUserIdService();
  }
})

onMounted( async () => {
  // 页面加载时，检查是否完善了个人信息
  const result = await getIsUserInfoCompleteService();
  if (result.data) {
    isCompleted.value = result.data;
    if (isCompleted.value) {
      // 检查是否已经申请过
      const applicationResult = await findApplicationByOmdUserIdService();
      if (applicationResult.data !== null) {
        isApplication.value = true;
        applicationData.value = applicationResult.data;
        ElMessage.success('您已申请，请耐心等待审核');
      }else {
        ElMessage.success('您已经完善了个人信息，可以申请成为歌手');
      }
    }
  }else {
    ElMessage.warning('您还未完善个人信息，请先完善个人信息');
  }
})

</script>

<template>
  <el-card>

      <!-- 卡片标题 -->
      <div class="card-header" v-if="!isApplication">
        <h1>申请成为歌手</h1>
      </div>


    <!-- 卡片内容 -->
    <el-container>


      <el-form ref="formRef" :rules="formRules" :model="formData" label-width="100px" v-if="!isApplication">
        <el-form-item label="歌手艺名：" prop="omdSingerName">
          <el-input prefix-icon="Mic" v-model="formData.omdSingerName" name="omdSingerName" placeholder="请输入您的艺名" clearable />
        </el-form-item>
        <el-form-item label="个人风格：" prop="omdApplicationsGenre">
          <el-input prefix-icon="PriceTag" v-model="formData.omdApplicationsGenre" name="omdApplicationsGenre" placeholder="请输入您的风格" clearable />
        </el-form-item>
        <el-form-item label="样例歌曲：" prop="omdApplicationsSingSample">
          <el-upload
              class="cover-uploader"
              action="/api/user/sampleFileUpload"
              name="sampleFile"
              :show-file-list="false"
              :on-success="handleSampleSuccess"
              :headers="{ Authorization: `Bearer ${authStore.token}` }"
              :before-upload="beforeAudioUpload"
          >
            <!-- 回显区域：有音频时显示播放器，无音频时显示上传按钮 -->
            <div v-if="audioUrl">
              <audio controls :src="audioUrl" class="audio-player"></audio>
              <div class="el-upload__tip">点击更换音频</div>
            </div>
            <div v-else style="">
              <el-icon class="cover-uploader-icon" style="margin: 0 100px;"><Plus /></el-icon>
            </div>
          </el-upload>
          <el-button type="primary" style="margin-left: 10px;">点击上传</el-button>
        </el-form-item>
        <el-form-item label="个人简介：" prop="omdApplicationsIntroduction">
          <el-input prefix-icon="PostCard" type="textarea" v-model="formData.omdApplicationsIntroduction" name="omdApplicationsIntroduction" placeholder="请输入您的个人简介" clearable />
        </el-form-item>
        <el-form-item label="申请须知：">
          <el-button type="primary" @click="showDialog">阅读申请须知</el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :disabled="!isCompleted || !isReady" @click="handleApplication">申请</el-button>
        </el-form-item>
      </el-form>

      <div v-else style="text-align: center;margin-left: 100px">
        <h2>您已经提交过申请，本站管理员会尽快审核，请耐心等候</h2>

        <el-form ref="formRef" :rules="formRules" :model="formData" label-width="100px">
          <el-form-item label="歌手艺名：" prop="omdSingerName">
            <el-input prefix-icon="Mic" v-model="applicationData.omdSingerName" name="omdSingerName" disabled />
          </el-form-item>
          <el-form-item label="个人风格：" prop="omdApplicationsGenre">
            <el-input prefix-icon="PriceTag" v-model="applicationData.omdApplicationsGenre" name="omdApplicationsGenre" disabled/>
          </el-form-item>
          <el-form-item label="样例歌曲：" prop="omdApplicationsSingSample">
            <audio controls :src="applicationData.omdApplicationsSingSample" class="audio-player"></audio>
          </el-form-item>
          <el-form-item label="个人简介：" prop="omdApplicationsIntroduction">
            <el-input prefix-icon="PostCard" type="textarea" v-model="applicationData.omdApplicationsIntroduction" name="omdApplicationsIntroduction" disabled/>
          </el-form-item>
        </el-form>
      </div>

    </el-container>

    <el-dialog
        v-model="dialogVisible"
        title="用户服务协议"
        class="agreement-dialog"
        :show-close="false"
    >
      <div class="dialog-header">
        <h2><i class="fas fa-file-contract"></i> 用户服务协议</h2>
      </div>

      <div class="dialog-body">
        <h3>1. 导言</h3>
        <p>欢迎使用我们的服务！在您开始使用我们的产品之前，请仔细阅读本《用户服务协议》（以下简称"协议"）。本协议是您与我们之间就使用服务所订立的有效合约。</p>

        <h3>2. 隐私保护</h3>
        <p>2.1 我们非常重视用户隐私。我们的《隐私政策》说明了我们如何收集、使用和披露您的个人信息。</p>
        <p>2.2 您同意我们可以根据《隐私政策》处理您的个人信息。</p>

        <h3>3. 使用限制</h3>
        <p>您承诺不会进行以下行为：</p>
        <p>• 发送垃圾邮件、病毒或其他有害内容</p>
        <p>• 试图未经授权访问我们的系统</p>
        <p>• 干扰或破坏服务的正常运行</p>

        <h3>4. 免责声明</h3>
        <p>服务按"现状"提供，我们不对服务的<span class="highlight">适用性、可靠性、可用性、及时性、准确性</span>做任何明示或暗示的保证。</p>

        <h3>5. 责任限制</h3>
        <p>在法律允许的最大范围内，我们不对任何间接性、后果性、惩罚性损害承担责任。</p>

        <h3>6. 协议修改</h3>
        <p>我们保留随时修改本协议条款的权利。修改后的协议将在网站上公布，并自公布之日起生效。</p>

        <h3>7. 法律适用</h3>
        <p>本协议的解释、效力及纠纷的解决，适用中华人民共和国法律。</p>

        <div class="timer-container">
          <p class="timer-text">请仔细阅读以上协议，<span class="countdown">{{ countdown }}</span>秒后可以确认</p>
        </div>
      </div>

      <div class="dialog-footer">
        <el-button
            class="confirm-btn"
            type="primary"
            :disabled="countdown > 0"
            @click="dialogVisible = false;isReady = true;"
            style="margin: 0 50%;"
        >
          <span v-if="countdown > 0">请阅读协议 ({{ countdown }}s)</span>
          <span v-else>我同意并接受协议</span>
        </el-button>
      </div>
    </el-dialog>

  </el-card>
</template>

<style scoped lang="scss">
.el-card {

  .card-header {
    font-size: 20px;
    text-align: center;
  }

  /* 卡片主体 */

  .el-container {
    height: 470px;
    padding: 50px 200px;

    .el-form{
      width: 100%;
    }

    .audio-player {
      width: 100%;
      margin-top: 10px;
      min-width: 400px;
      max-height: 150px;
      outline: none;
    }

    .cover-uploader {
      display: block;
      border: 1px dashed #d9d9d9;
      border-radius: 6px;
      cursor: pointer;
      position: relative;
      overflow: hidden;
      padding: 20px;
      text-align: center;
      transition: border-color 0.3s;
    }

    .cover-uploader:hover {
      border-color: #409eff;
    }

    .cover-uploader-icon {
      font-size: 28px;
      color: #8c939d;
      width: auto;
      height: auto;
      line-height: normal;
      margin-bottom: 10px;
    }

    .el-upload__tip {
      margin-top: 10px;
      color: #999;
      font-size: 12px;
    }

  }

  /* 卡片底部 */

  .card-footer {
    display: flex;
    /* 子元素靠右对齐 */
    justify-content: flex-end;
  }

}
</style>