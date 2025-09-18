<script setup>
import {computed, ref} from "vue";
import {ElMessage} from "element-plus";

// 导入状态管理
import { useAuthStore } from '@/stores/auth.js'

// 导入服务器端的接口
import {feedbackToAdminService} from "@/api/user.js";

// 状态管理
const authStore = useAuthStore()

// 控制弹窗显示
const aboutUsVisible = ref(false);
const useTermsVisible = ref(false);
const privacyPolicyVisible = ref(false);
const contactUsVisible = ref(false);

// 联系表单的ref
const contactRef = ref(null);

// 计算属性
const isGuest = computed(() => authStore.isGuest);

// 联系表单数据
const contactForm = ref({
  omdUserFeedbackType: "",
  omdUserFeedbackContent: "",
  omdUserFeedbackContact: ""
});

// 联系校验规则
const contactRules = {
  omdUserFeedbackType: [
    {required: true, message: "请选择反馈类型", trigger: "change"}
  ],
  omdUserFeedbackContent: [
    {required: true, message: "请输入反馈内容", trigger: "blur"}
  ],
  omdUserFeedbackContact: [
    {message: "请输入联系方式", trigger: "blur"}
  ]
};

// 提交联系表单
const submitContactForm = async () => {
  contactRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await feedbackToAdminService(contactForm.value);
        ElMessage.success("反馈已提交，我们将尽快处理");
        contactUsVisible.value = false;
        contactForm.value.omdUserFeedbackType = "";
        contactForm.value.omdUserFeedbackContent = "";
        contactForm.value.omdUserFeedbackContact = "";
      }catch (error) {
        ElMessage.error("提交反馈失败，请稍后重试");
      }
    } else {
      console.log("表单验证失败");
      return false;
    }
  });
};
</script>

<template>
  <el-divider>
    <!-- 分割线 -->
    <el-icon>
      <StarFilled/>
    </el-icon>
  </el-divider>
  <!-- 底部信息 -->
  <footer class="footer">
    <div class="footer-content">
      <p>© 2025 音友尽有 - 高品质在线音乐交流平台</p>
      <div class="footer-links">
        <!-- 关于我们 -->
        <el-link type="primary" :underline="false" @click="aboutUsVisible = true">关于我们</el-link>
        <!-- 关于我们的弹窗 -->
        <el-dialog v-model="aboutUsVisible" title="关于我们" width="35%" center class="custom-dialog">
          <div class="dialog-content">
            <h3 style="font-size: 18px; color: #333; margin-bottom: 15px; font-weight: 600;">音友尽有 -
              音乐爱好者的交流平台</h3>
            <p style="text-align: left; margin-bottom: 10px;">
              欢迎来到音友尽有，我们致力于为音乐爱好者提供一个便捷的在线音乐交流平台。</p>
            <p style="text-align: left; margin-bottom: 10px;">
              我们的目标是为用户提供一个安全、可靠的音乐分享平台，让每个人都能轻松地分享和发现优质的音乐作品。</p>
            <p style="text-align: left; margin-bottom: 10px;">
              我们的团队由经验丰富的音乐专业人员和技术开发人员组成，致力于为用户提供最佳的音乐交流体验。</p>
            <p style="text-align: left; margin-bottom: 10px;">
              如果您有任何问题或建议，请随时通过"联系我们"功能与我们取得联系。</p>
          </div>
          <template #footer>
                  <span class="dialog-footer">
                    <el-button @click="aboutUsVisible = false">关闭</el-button>
                  </span>
          </template>
        </el-dialog>

        <!-- 使用条款 -->
        <el-link type="primary" :underline="false" @click="useTermsVisible = true">使用条款</el-link>
        <!-- 使用条款的弹窗 -->
        <el-dialog v-model="useTermsVisible" title="使用条款" width="40%" center class="custom-dialog">
          <div class="dialog-content">
            <h3 style="font-size: 18px; color: #333; margin-bottom: 15px; font-weight: 600;">服务使用条款</h3>
            <h3>1. 导言</h3>
            <p>欢迎使用我们的服务！在您开始使用我们的产品之前，请仔细阅读本《用户服务协议》（以下简称"协议"）。本协议是您与我们之间就使用服务所订立的有效合约。</p>

            <h3>2. 服务内容</h3>
            <p>我们向您提供包括但不限于：软件使用许可、在线服务、技术支持、内容更新等服务（以下简称"服务"）。服务的具体内容由我们根据实际情况提供。</p>

            <h3>3. 用户账户</h3>
            <p>3.1 您需要注册账户才能使用部分服务。注册时，您必须提供<span class="highlight">真实、准确、完整</span>的注册信息。</p>
            <p>3.2 您应对账户安全负全部责任，任何情况下不应将账户信息透露给他人。</p>

            <h3>4.使用限制</h3>
            <p>您承诺不会进行以下行为：</p>
            <p>• 发送垃圾邮件、病毒或其他有害内容</p>
            <p>• 试图未经授权访问我们的系统</p>
            <p>• 干扰或破坏服务的正常运行</p>

            <h3>5. 免责声明</h3>
            <p>服务按"现状"提供，我们不对服务的<span class="highlight">适用性、可靠性、可用性、及时性、准确性</span>做任何明示或暗示的保证。</p>

            <h3>6. 责任限制</h3>
            <p>在法律允许的最大范围内，我们不对任何间接性、后果性、惩罚性损害承担责任。</p>

            <h3>7. 协议修改</h3>
            <p>我们保留随时修改本协议条款的权利。修改后的协议将在网站上公布，并自公布之日起生效。</p>

            <h3>8. 法律适用</h3>
            <p>本协议的解释、效力及纠纷的解决，适用中华人民共和国法律。</p>
          </div>
          <template #footer>
                  <span class="dialog-footer">
                    <el-button @click="useTermsVisible = false">关闭</el-button>
                  </span>
          </template>
        </el-dialog>

        <!-- 隐私政策 -->
        <el-link type="primary" :underline="false" @click="privacyPolicyVisible = true">隐私政策</el-link>
        <!-- 隐私政策的弹窗 -->
        <el-dialog v-model="privacyPolicyVisible" title="隐私政策" width="40%" center class="custom-dialog">
          <div class="dialog-content">
            <h3 style="font-size: 18px; color: #333; margin-bottom: 15px; font-weight: 600;">隐私保护政策</h3>
            <p style="text-align: left; margin-bottom: 10px;">1.
              我们收集的信息包括但不限于用户注册信息、音乐偏好和使用行为数据。</p>
            <p style="text-align: left; margin-bottom: 10px;">2.
              我们不会向第三方泄露用户个人信息，除非有法律规定或用户明确同意。</p>
            <p style="text-align: left; margin-bottom: 10px;">3.
              我们使用cookies来改善用户体验，用户可以根据浏览器设置拒绝cookies。</p>
            <p style="text-align: left; margin-bottom: 10px;">4.
              平台会采取合理措施保护用户数据安全，但不保证绝对安全。</p>
            <p style="text-align: left; margin-bottom: 10px;">5.
              用户有权访问、修改和删除自己的个人信息，可通过账户设置进行操作。</p>
          </div>
          <template #footer>
                  <span class="dialog-footer">
                    <el-button @click="privacyPolicyVisible = false">关闭</el-button>
                  </span>
          </template>
        </el-dialog>

        <!-- 联系我们 -->
        <el-link type="primary" :underline="false" v-if="!isGuest" @click="contactUsVisible = true">联系我们</el-link>
        <!-- 联系我们的弹窗 -->
        <el-dialog v-model="contactUsVisible" title="联系我们" width="40%" center class="custom-dialog">
          <div class="dialog-content">
            <el-form ref="contactRef" :model="contactForm" :rules="contactRules" label-width="100px">
              <el-form-item label="反馈类型" prop="omdUserFeedbackType">
                <el-select v-model="contactForm.omdUserFeedbackType" placeholder="请选择反馈类型">
                  <el-option label="功能建议" value="1"></el-option>
                  <el-option label="问题反馈" value="2"></el-option>
                  <el-option label="投诉举报" value="3"></el-option>
                  <el-option label="其他" value="4"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="反馈内容" prop="omdUserFeedbackContent">
                <el-input v-model="contactForm.omdUserFeedbackContent" type="textarea" rows="4"
                          placeholder="请详细描述您的反馈内容"></el-input>
              </el-form-item>
              <el-form-item label="联系方式" prop="omdUserFeedbackContact">
                <el-input v-model="contactForm.omdUserFeedbackContact"
                          placeholder="请留下您的联系方式，以便我们回复"></el-input>
              </el-form-item>
            </el-form>
          </div>
          <template #footer>
                  <span class="dialog-footer">
                    <el-button @click="contactUsVisible = false">取消</el-button>
                    <el-button type="primary" @click="submitContactForm">提交</el-button>
                  </span>
          </template>
        </el-dialog>
      </div>
    </div>
  </footer>

</template>

<style scoped>
/* 底部样式 */
.footer {
  text-align: center;
  font-size: 14px;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
}

.footer-links {
  display: flex;
  gap: 30px;
}

/* 对话框样式 */
.custom-dialog .el-dialog__body {
  padding: 20px 30px;
}

.dialog-content {
  text-align: left;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .footer-links {
    flex-direction: column;
    align-items: center;
    gap: 10px;
  }

  .custom-dialog {
    width: 90% !important;
  }
}
</style>