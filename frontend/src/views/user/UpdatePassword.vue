<script setup>
import {ref, watch} from 'vue'
import router from "@/router/index.js";
import {ElLoading, ElMessage} from "element-plus";

// 导入服务器请求函数
import {sendVerifyCodeService, updatePasswordByVerifyCodeService} from "@/api/public.js";
import {updatePasswordService} from "@/api/user.js";

// 响应数据
const countdown = ref(0); // 倒计时
const timer = ref(null); // 计时器引用
const changeFrom = ref(true) // 切换表单

// 表单绑定数据
const formData = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
  omdUserPhone: '',
  newVerifyPassword: '',
  verifyCode: '',
  confirmNewPassword: ''
})

// 表单ref
const normalFromRef = ref(null)
const sendVerifyCodeFormRef = ref(null)

// 表单验证规则
const formRules = ref({
  oldPassword: [
    {required: true, message: '请输入密码', trigger: 'blur'},
    {min: 6, max: 16, message: '密码长度必须在6到16个字符之间', trigger: 'blur'}
  ],
  newPassword: [
    {required: true, message: '请输入密码', trigger: 'blur'},
    {min: 6, max: 16, message: '密码长度必须在6到16个字符之间', trigger: 'blur'}
  ],
  newVerifyPassword: [
    {required: true, message: '请输入密码', trigger: 'blur'},
    {min: 6, max: 16, message: '密码长度必须在6到16个字符之间', trigger: 'blur'}
  ],
  confirmPassword: [
    {required: true, message: '请再次输入密码', trigger: 'blur'},
    {
      validator: (rule, value, callback) => {
        // 对比表单数据里的密码字段
        if (value !== formData.value.newPassword) {
          callback(new Error('两次输入的密码不一致'));
        } else {
          callback();
        }},trigger: 'blur'}
  ],
  confirmNewPassword: [
    {required: true, message: '请再次输入密码', trigger: 'blur'},
    {
      validator: (rule, value, callback) => {
        // 对比表单数据里的密码字段
        if (value !== formData.value.newVerifyPassword) {
          callback(new Error('两次输入的密码不一致'));
        } else {
          callback();
        }},trigger: 'blur'}
  ],
  omdUserPhone: [
    {required: true, message: '请输入手机号', trigger: 'blur'},
    {pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号', trigger: 'blur'}
  ],
  verifyCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '验证码应为6位数字', trigger: 'blur' }
  ],
})


// 倒计时逻辑
const startCountdown = () => {
  countdown.value = 60;
  timer.value = setInterval(() => {
    countdown.value--;
    if (countdown.value <= 0) {
      clearInterval(timer.value);
      countdown.value = 0;
    }
  }, 1000);
};


// 获取验证码
const getVerifyCode = async () => {
  // 发送请求获取验证码
  try {
    // 显示加载状态
    const loadingInstance = ElLoading.service({
      lock: true,
      text: '发送中...',
      spinner: 'el-icon-loading',
      background: 'rgba(0, 0, 0, 0.1)'
    });
    // 发送请求
    const result = await sendVerifyCodeService(formData.value.omdUserPhone);
    // 关闭加载状态
    loadingInstance.close();
    // 提示信息
    ElMessage.success(result.message || '获取验证码成功');

    // 开始倒计时
    startCountdown();
  } catch (error) {
    console.error('获取验证码失败', error);
    ElMessage.error('获取验证码失败，请重试');
  }
}

// 普通重置密码
const resetPasswordByNormal = async () => {
  // 验证通过，提交数据，调用登录接口
  try {
    await updatePasswordService(formData.value);
    // 提示信息
    ElMessage.success('修改成功，请重新登录');
    // 重置成功后要重新登录
    router.push('/login');
  }catch (error) {
    console.error('修改密码:', error);
  }
}

// 验证码重置密码
const resetPasswordByVerify = async () => {
  // 验证通过，提交数据，调用登录接口
  try {
    await updatePasswordByVerifyCodeService(formData.value);
    // 提示信息
    ElMessage.success('修改成功，请重新登录');
    // 重置成功后要重新登录
    router.push('/login');
  }catch (error) {
    console.error('修改密码:', error);
  }
}

watch(changeFrom, (newValue) => {
  // 重置倒计时
  if (timer.value) {
    clearInterval(timer.value);
    timer.value = null;
    countdown.value = 0;
  }
  // 安全重置表单（添加空值检查）
  const resetForm = (formRef) => {
    if (formRef.value) {
      formRef.value.resetFields();
    }
  };

  if (newValue) {
    // 切换到普通修改密码表单，重置验证码表单
    resetForm(sendVerifyCodeFormRef);
  } else {
    // 切换到验证码表单，重置普通修改密码表单
    resetForm(normalFromRef);
  }
})

</script>

<template>
  <el-card>

    <!-- 卡片头部 -->
    <template #header>

      <!-- 卡片标题 -->
      <div class="card-header">
        <h1>更改密码</h1>
      </div>


    </template>

    <!-- 卡片内容 -->
    <el-container>

      <el-form ref="normalFromRef" :rules="formRules" :model="formData" label-width="100px" v-if="changeFrom">
        <el-form-item label="旧密码：" prop="oldPassword">
          <el-input prefix-icon="Lock" v-model="formData.oldPassword" name="oldPassword" type="password" placeholder="请设置新密码" show-password clearable />
        </el-form-item>
        <el-form-item label="新密码：" prop="newPassword">
          <el-input prefix-icon="Lock" v-model="formData.newPassword" name="newPassword" type="password" placeholder="请设置新密码" show-password clearable />
        </el-form-item>
        <el-form-item label="确认密码：" prop="confirmPassword">
          <el-input prefix-icon="Lock" v-model="formData.confirmPassword" name="confirmPassword" type="password" placeholder="请确认新密码" show-password clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="resetPasswordByNormal">确认修改</el-button>
        </el-form-item>
        <el-form-item class="flex">
          <el-link type="info" :underline="false" @click="changeFrom = false">
            验证码修改密码
            <el-icon>
              <ArrowRightBold/>
            </el-icon>
          </el-link>
        </el-form-item>
      </el-form>

      <el-form ref="sendVerifyCodeFormRef" :rules="formRules" :model="formData" label-width="100px" v-else>
        <el-form-item label="手机号：" prop="omdUserPhone">
          <el-input prefix-icon="phone" v-model="formData.omdUserPhone" name="omdUserPhone" placeholder="请输入注册手机号" clearable />
        </el-form-item>
        <el-form-item label="验证码：" prop="verifyCode">
          <el-input
              name="verifyCode"
              prefix-icon="Message"
              placeholder="请输入短信验证码"
              clearable
              v-model="formData.verifyCode"
              style="width: 60%; display: inline-block;"
          >
          </el-input>
          <!-- 验证码获取按钮 -->
          <el-button
              :disabled="countdown > 0"
              type="primary"
              @click="getVerifyCode"
              style="margin-left: 10px;
                       width: 35%;
                       display: inline-block;"
              auto-insert-space
          >
            {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
          </el-button>
        </el-form-item>
        <el-form-item label="新密码：" prop="newVerifyPassword">
          <el-input prefix-icon="Lock" v-model="formData.newVerifyPassword" name="newVerifyPassword" type="password" placeholder="请设置新密码" show-password clearable />
        </el-form-item>
        <el-form-item label="确认密码：" prop="confirmNewPassword">
          <el-input prefix-icon="Lock" v-model="formData.confirmNewPassword" name="confirmNewPassword" type="password" placeholder="请确认新密码" show-password clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="resetPasswordByVerify">确认修改</el-button>
        </el-form-item>
        <el-form-item class="flex">
          <el-link type="info" :underline="false" @click="changeFrom = true">
            <el-icon>
              <ArrowLeftBold/>
            </el-icon>
            普通修改密码
          </el-link>
        </el-form-item>
      </el-form>

    </el-container>

    <!-- 卡片底部 -->
    <template #footer>


    </template>
  </el-card>
</template>

<style scoped lang="scss">
.el-card {

  .card-header {
    text-align: center;
  }

  /* 卡片主体 */
  .el-container {
    height: 350px;
    padding: 70px 400px;
  }

  /* 卡片底部 */
  .card-footer {
    display: flex;
    /* 子元素靠右对齐 */
    justify-content: flex-end;
  }

}
</style>