<script setup>
// 导入ref
import {onMounted, onUnmounted, ref, watch} from 'vue'
import {ElMessage} from "element-plus";
import { ElLoading } from 'element-plus';

// 导入轮播图
import login1 from '@/assets/images/loginSwiper/login1.jpg'
import login2 from '@/assets/images/loginSwiper/login2.jpg'
import login3 from '@/assets/images/loginSwiper/login3.jpg'
import login4 from '@/assets/images/loginSwiper/login4.jpeg'

// 导入路由
import {useRouter} from 'vue-router'

// 导入状态管理
import { useAuthStore } from '@/stores/auth.js'

// 导入公共接口
import {
  loginByUsernameOrPhoneService,
  registerService,
  sendVerifyCodeService,
  loginOrRegisterByPhoneService,
  updatePasswordByVerifyCodeService, submitAppealService
} from '@/api/public.js'

// 调用获得路由
const router = useRouter()

// 调用状态管理
const authStore = useAuthStore()

// 轮播图
const banners = [
  { url: login1 },
  { url: login2 },
  { url: login3 },
  { url: login4 }
]

// 表单切换 0为登录表单，1为验证码表单，2为注册表单
const changeForm = ref(0)
const countdown = ref(0); // 倒计时
const timer = ref(null); // 计时器引用
// 申诉相关状态
const showAppealDialog = ref(false);
const appealFormRef = ref(null);
const submitAppealLoading = ref(false);
const evidenceFileList = ref([]);
// 弹窗控制
const showForgetPasswordDialog = ref(false);

// 表单绑定数据
const formData = ref({
  omdUserName: '',
  identifier:'',
  omdUserPassword: '',
  confirmPassword: '',
  omdUserNickname: '',
  omdUserBirthday: '',
  omdUserEmail: '',
  omdUserGender: '',
  omdUserPhone: '',
  omdUserRegion: '',
  verifyCode: '',
  newVerifyPassword: '',
  confirmNewPassword: ''
})

// 地区选项
const regions = [
  {
    value: '中国',
    label: '中国',
  },
  {
    value: '国外',
    label: '国外',
  }
]

// 表单ref
const loginFormRef = ref(null)
const registerFormRef = ref(null)
const sendVerifyCodeFormRef = ref(null)
const forgetPasswordFormRef = ref(null);

// 表单验证规则
const formRules = ref({
  omdUserName: [
    {required: true, message: '请输入用户名', trigger: 'blur'},
    {pattern: /^[\u4e00-\u9fa5a-zA-Z0-9_]{2,16}$/, message: '用户名只能包含英文、数字和下划线', trigger: 'blur'},
    {min: 2, max: 16, message: '用户名长度必须在2到16个字符之间', trigger: 'blur'},
  ],
  identifier: [
    {required: true, message: '请输入用户名或者手机号', trigger: 'blur'},
    {
      validator: (rule, value, callback) => {
        if (!value) {
          callback(new Error('请输入用户名或手机号'));
          return;
        }

        // 判断是否为手机号格式
        if (/^1[3-9]\d{9}$/.test(value)) {
          // 手机号格式正确，通过校验
          callback();
        } else {
          // 按用户名格式校验（示例：2-16位字母数字组合）
          if (/^[\u4e00-\u9fa5a-zA-Z0-9_]{2,16}$/.test(value)) {
            callback();
          } else {
            callback(new Error('请输入正确的用户名或手机号'));
          }
        }
      },
      trigger: 'blur'
    }
  ],
  omdUserPassword: [
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
        if (value !== formData.value.omdUserPassword) {
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
  omdUserNickname: [
    { message: '请输入昵称', trigger: 'blur'},
    {min: 1, max: 16, message: '昵称长度必须在1到16个字符之间', trigger: 'blur'}
  ],
  omdUserEmail: [
    { pattern: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/,message: '请输入有效的邮箱地址',trigger: 'blur'}
  ]
})

// 申诉表单数据
const appealForm = ref({
  omdUserPhone: '',
  omdUserAppealReason: '',
});

// 申诉表单验证规则
const appealRules = ref({
  omdUserPhone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号', trigger: 'blur' }
  ],
  omdUserAppealReason: [
    { required: true, message: '请填写申诉理由', trigger: 'blur' },
    { min: 20, message: '申诉理由至少20个字符', trigger: 'blur' }
  ]
});

// 提交登录表单
const login = async () => {
  // 验证通过，提交数据，调用登录接口
  try {
    const result = await loginByUsernameOrPhoneService(formData.value);
    // 存储token
    authStore.setToken(result.data);
    // 关键：登录成功后，先恢复滚动
    document.body.style.overflow = 'auto';
    document.documentElement.style.overflow = 'auto';
    // 再跳转到首页
    // 提示信息
    ElMessage.success('登录成功');
    // 登录成功后，跳转到首页
    router.push('/');
  } catch (error) {
    console.error('登录失败:', error);
  }
};

// 提交注册表单
const register = async () => {

  // 验证通过，提交数据，调用注册接口
  try {
    const result = await registerService(formData.value);
    // 提示信息
    ElMessage.success('注册成功');
    // 注册成功后，跳转到登录表单
    changeForm.value = 0;
  } catch (error) {
    console.error('注册失败:', error);
  }
};

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
    ElMessage.success('获取验证码成功');

    // 开始倒计时
    startCountdown();
  } catch (error) {
    console.error('获取验证码失败', error);
    ElMessage.error('获取验证码失败，请重试');
  }
};

// 手机号验证码登录或注册
const loginOrRegisterByPhone = async () => {
  // 验证通过，提交数据，调用登录接口
  try {
    const result = await loginOrRegisterByPhoneService(formData.value);
    // 存储token
    authStore.setToken(result.data);
    // 关键：登录成功后，先恢复滚动
    document.body.style.overflow = 'auto';
    document.documentElement.style.overflow = 'auto';
    // 再跳转到首页
    // 提示信息
    ElMessage.success('登录成功');
    // 登录成功后，跳转到首页
    router.push('/');
  }catch (error) {
    console.error('手机号注册或登录:', error);
  }
};

// 重置密码
const resetPassword = async () => {
  // 验证通过，提交数据，调用登录接口
  try {
    const result = await updatePasswordByVerifyCodeService(formData.value);
    // 提示信息
    ElMessage.success('修改成功');
    // 登录成功后，跳转到首页
    router.push('/login');
  }catch (error) {
    console.error('修改密码:', error);
  }
}

// 新增：提交申诉
const submitAppeal = async () => {
  try {
    // 表单验证
    await appealFormRef.value.validate();

    // 显示加载
    const loading = ElLoading.service({
      lock: true,
      text: '提交中...',
      background: 'rgba(0, 0, 0, 0.1)'
    });

    // 调用后端申诉接口
    const result = await submitAppealService(appealForm.value);

    // 处理结果
    ElMessage.success('申诉提交成功');
    showAppealDialog.value = false;
    // 重置表单
    evidenceFileList.value = [];
  } catch (error) {
    if (error.name !== 'ValidationError') {
      console.error('申诉提交失败:', error);
      ElMessage.error(error.response?.data?.message || '申诉提交失败，请重试');
    }
  } finally {
    submitAppealLoading.value = false;
    ElLoading.service().close();
  }
};


// 禁止滚动
const disableScroll = () => {
  document.body.style.overflow = 'hidden'
  document.documentElement.style.overflow = 'hidden' // 兼容某些浏览器
}

watch(changeForm, (newValue) => {
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

  if (newValue === 0) {
    // 切换到登录表单，重置另外两个表单
    resetForm(registerFormRef);
    resetForm(sendVerifyCodeFormRef);
  } else if (newValue === 1) {
    // 切换到验证码表单，重置另外两个表单
    resetForm(loginFormRef);
    resetForm(registerFormRef);
  } else {
    // 切换到注册表单，重置另外两个表单
    resetForm(loginFormRef);
    resetForm(sendVerifyCodeFormRef);
  }
})

// 在页面加载时就禁止滚动
onMounted(() => {
  disableScroll() // 应用加载时禁止滚动
})

// 组件卸载时清除定时器，避免内存泄漏
onUnmounted(() => {
  if (timer.value) {
    clearInterval(timer.value);
  }
  // 卸载时恢复滚动
  document.body.style.overflow = 'auto'
});

</script>

<template>
  <div class="common-layout">
    <el-container>
      <!-- 头部区域 -->
      <el-header>
        <HeaderMenu/>
      </el-header>

      <!-- 中间区域 -->
      <el-main>

        <!-- 内容主体的左边 -->
        <div class="nav-left">

          <el-carousel height="600px" direction="vertical" type="card" :autoplay="false">
            <el-carousel-item v-for="(item, index) in banners" :key="index">
              <img :src="item.url" alt="轮播图" class="carousel-img" />
            </el-carousel-item>
          </el-carousel>

        </div>

        <!-- 内容主体的右边 -->
        <div class="nav-right">

          <div class="form-container">

            <!-- 忘记密码弹窗 -->
            <el-dialog v-model="showForgetPasswordDialog" title="重置密码" width="30%" center>
              <el-form ref="forgetPasswordFormRef" :rules="formRules" :model="formData" label-width="100px">
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
              </el-form>
              <template #footer>
                <span class="dialog-footer">
                  <el-button @click="showForgetPasswordDialog = false">取消</el-button>
                  <el-button type="primary" @click="resetPassword">确认修改</el-button>
                </span>
              </template>
            </el-dialog>


            <!-- 登录表单 -->
            <el-form ref="loginFormRef" size="large" label-width="80px" autocomplete="off" v-if="changeForm === 0" :rules="formRules" :model="formData">
              <!--表单名-->
              <h1>登录</h1>
              <el-form-item label="用户：" prop="identifier">
                <el-input prefix-icon="User" name="identifier" placeholder="请输入用户名或手机号" clearable v-model="formData.identifier"></el-input>
              </el-form-item>
              <el-form-item label="密码：" prop="omdUserPassword">
                <el-input name="omdUserPassword" prefix-icon="Lock" type="password"
                          placeholder="请输入密码" clearable show-password v-model="formData.omdUserPassword"></el-input>
              </el-form-item>
              <el-form-item class="flex">
                <div class="flex">
                  <el-link type="primary" :underline="false" @click="showForgetPasswordDialog = true">忘记密码？</el-link>
                </div>
              </el-form-item>
              <el-form-item class="flex">
                <el-link type="info" :underline="false" @click="showAppealDialog = true">
                  账号被冻结？点击申诉
                </el-link>
              </el-form-item>
              <!-- 登录按钮 -->
              <el-form-item>
                <el-button class="button" type="primary" auto-insert-space @click="login">登录</el-button>
              </el-form-item>
              <el-form-item class="flex">
                <el-link type="info" :underline="false" @click="changeForm = 2">
                  注册
                  <el-icon>
                    <ArrowRightBold/>
                  </el-icon>
                </el-link>
              </el-form-item>
              <el-form-item class="flex">
                <el-link type="info" :underline="false" @click="changeForm = 1">
                  验证码登录或注册
                  <el-icon>
                    <ArrowRightBold/>
                  </el-icon>
                </el-link>
              </el-form-item>
            </el-form>

            <el-form v-else-if="changeForm === 1" :model="formData" ref="sendVerifyCodeFormRef" size="large" label-width="80px" autocomplete="off" :rules="formRules">
              <!--表单名-->
              <h1>手机号验证码注册或登录</h1>

              <el-form-item label="手机号：" prop="omdUserPhone">
                <el-input prefix-icon="User" name="omdUserPhone" placeholder="请输入手机号" clearable v-model="formData.omdUserPhone"></el-input>
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
              <el-form-item>
                <el-button class="button" type="primary" auto-insert-space @click="loginOrRegisterByPhone">
                  注册/登录
                </el-button>
              </el-form-item>
              <el-form-item class="flex">
                <el-link type="info" :underline="false" @click="changeForm = 0">
                  登录
                  <el-icon>
                    <ArrowRightBold/>
                  </el-icon>
                </el-link>
              </el-form-item>
              <el-form-item class="flex">
                <el-link type="info" :underline="false" @click="changeForm = 2">
                  注册
                  <el-icon>
                    <ArrowRightBold/>
                  </el-icon>
                </el-link>
              </el-form-item>
            </el-form>

            <!-- 注册表单 -->
            <el-form ref="registerFormRef" size="large" label-width="100px" autocomplete="off" v-else :rules="formRules" :model="formData">
              <!--表单名-->
              <h1>注册</h1>
              <el-form-item label="用户名：" prop="omdUserName">
                <el-input prefix-icon="User" placeholder="请输入用户名" clearable v-model="formData.omdUserName"></el-input>
              </el-form-item>
              <el-form-item label="密码：" prop="omdUserPassword">
                <el-input prefix-icon="Lock" type="password" placeholder="请输入密码" clearable
                          show-password v-model="formData.omdUserPassword"></el-input>
              </el-form-item>
              <el-form-item label="确认密码：" prop="confirmPassword">
                <el-input prefix-icon="Lock" type="password" placeholder="请再次输入密码" clearable
                          show-password v-model="formData.confirmPassword"></el-input>
              </el-form-item>
              <el-form-item label="手机号：" prop="omdUserPhone">
                <el-input prefix-icon="Cellphone" placeholder="请输入手机号" clearable v-model="formData.omdUserPhone"></el-input>
              </el-form-item>
              <el-form-item label="昵称：" prop="omdUserNickname">
                <el-input prefix-icon="User" placeholder="请输入昵称" clearable v-model="formData.omdUserNickname"></el-input>
              </el-form-item>
              <el-form-item label="邮箱：" prop="omdUserEmail">
                <el-input prefix-icon="Message" placeholder="请输入邮箱" clearable v-model="formData.omdUserEmail"></el-input>
              </el-form-item>
              <el-form-item label="出生日期：">
                <el-date-picker
                    type="date"
                    placeholder="请选择您的生日"
                    v-model="formData.omdUserBirthday"
                />
              </el-form-item>
              <el-form-item label="性别：">
                <el-radio-group v-model="formData.omdUserGender">
                  <el-radio :value="0">
                    <el-icon>
                      <Male/>
                    </el-icon>
                    男
                  </el-radio>
                  <el-radio :value="1">
                    <el-icon>
                      <Female/>
                    </el-icon>
                    女
                  </el-radio>
                  <el-radio :value="2">
                    <el-icon>
                      <UserFilled/>
                    </el-icon>
                    保密
                  </el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="地区：">
                <el-select
                    v-model="formData.omdUserRegion"
                    clearable
                    placeholder="请选择您的地区"
                    style="width: 240px"
                >
                  <el-option
                      v-for="item in regions"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                  />
                </el-select>
              </el-form-item>
              <!-- 注册按钮 -->
              <el-form-item>
                <el-button class="button" type="primary" auto-insert-space @click="register">
                  注册
                </el-button>
              </el-form-item>
              <el-form-item class="flex">
                <el-link type="info" :underline="false" @click="changeForm = 0">
                  <el-icon>
                    <ArrowLeftBold/>
                  </el-icon>
                  登录
                </el-link>
              </el-form-item>
              <el-form-item class="flex">
                <el-link type="info" :underline="false" @click="changeForm = 1">
                  <el-icon>
                    <ArrowLeftBold/>
                  </el-icon>
                  验证码登录或注册
                </el-link>
              </el-form-item>
            </el-form>

          </div>

        </div>

      </el-main>

      <!-- 新增：用户申诉弹窗 -->
      <el-dialog
          v-model="showAppealDialog"
          title="账号申诉"
          width="500px"
          center
      >
        <el-form
            ref="appealFormRef"
            :model="appealForm"
            :rules="appealRules"
            label-width="100px"
            class="appeal-form"
        >
          <el-form-item label="手机号" prop="omdUserPhone">
            <el-input
                v-model="appealForm.omdUserPhone"
                placeholder="请输入被冻结账号的手机号"
                clearable
                prefix-icon="Phone"
            />
          </el-form-item>
          <el-form-item label="申诉理由" prop="omdUserAppealReason">
            <el-input
                v-model="appealForm.omdUserAppealReason"
                type="textarea"
                :rows="4"
                placeholder="请详细描述您的申诉理由..."
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="showAppealDialog = false">取消</el-button>
          <el-button
              type="primary"
              @click="submitAppeal"
              :loading="submitAppealLoading"
          >
            提交申诉
          </el-button>
        </template>
      </el-dialog>

    </el-container>
  </div>
</template>


<style scoped>
.common-layout {
  height: 100%;
  /* 导入图片 */
  background-image: url('@/assets/images/bg.jpeg');
  /* 图片等比缩放，覆盖整个容器 */
  background-size: cover;
  /* 图片居中显示 */
  background-position: center;

  /* 头部 */

  .el-header {
    /* 居中显示 */
    text-align: center;
    /* 背景色 */
    background-color: #fff;
    margin-bottom: 50px;
    padding-right: 50px;

    .custom-title {
      background: linear-gradient(90deg, #ff6b6b, #4ecdc4);
      -webkit-background-clip: text;
      background-clip: text;
      color: transparent; /* 关键：让文字透明，显示背景渐变 */
      text-shadow: 0 2px 3px rgba(0,0,0,0.1);
    }

  }

  /* 中间区域 */

  .el-main {
    display: flex;
    padding: 70px 50px;
    /* 减去头部和底部高度 */
    height: calc(100vh - 60px);

    /* 新增：申诉弹窗样式 */
    .appeal-form {
      margin-top: 10px;
    }

    .evidence-list {
      display: flex;
      flex-wrap: wrap;
      gap: 10px;
      margin-top: 5px;
    }

    .evidence-item {
      display: flex;
      align-items: center;
      padding: 5px 10px;
      background: #f5f7fa;
      border-radius: 4px;
      max-width: 350px;
    }

    .file-icon {
      margin-right: 8px;
      color: #409eff;
    }

    .file-name {
      flex: 1;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      font-size: 13px;
    }

    .remove-btn {
      padding: 0 5px;
      color: #f56c6c;
    }

    ::v-deep .appeal-form .el-upload {
      margin-top: 5px;
    }

    ::v-deep .el-dialog__body {
      max-height: 60vh;
      overflow-y: auto;
      padding: 20px;
    }


    /* 左边内容 */

    .nav-left {
      margin-top: 50px;
      width: 60%;

      .el-carousel__item {
        height: 80%;
        display: flex;
        justify-content: center;
        align-items: center;
      }

      .carousel-img{
        width: 100%;
        height: 100%;
      }
    }

    /* 右边内容 */

    .nav-right {
      display: flex;
      width: 30%;
      margin-left: 105px;
      margin-bottom: 120px;
      /* 左对齐 */
      justify-content: flex-end;
      /* 居中显示 */
      align-items: center;

      .form-container {
        width: 400px;
        padding: 0 40px;
        background-color: #fff;
        border: gainsboro 1px solid;
        border-radius: 8px;
        transition: all 0.3s ease;

        /* 按钮悬停效果 */
        .el-button:hover {
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(50, 50, 93, 0.1);
        }

        /* 底下的选项 */

        .flex {
          width: 100%;
          display: flex;
          justify-content: space-between;
        }
      }

    }

  }

}
</style>