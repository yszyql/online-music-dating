package org.x.backend.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.x.backend.pojo.OmdOperationLog;
import org.x.backend.pojo.OmdUser;
import org.x.backend.service.OmdAdminService;
import org.x.backend.service.OmdUserService;

import java.util.Date;
import java.util.List;

// 标记为Spring组件，使其被扫描并注入
@Component
@Transactional // 事务注解：确保解冻和日志记录同时成功/失败
@EnableScheduling // 启用定时任务功能
public class UserTask {

    @Autowired
    private OmdUserService omdUserService;

    @Autowired
    private OmdAdminService omdAdminService;


    // 定时任务：每天凌晨2点执行
    @Scheduled(cron = "0 0 2 * * ?")
    public void autoUnfreezeExpiredUsers() {
        // 1. 查询所有“临时冻结且已过截止时间”的用户
        // SQL条件：omd_user_status = 0（冻结） AND omd_user_freeze_type = 1（临时） AND omd_user_freeze_end_time < NOW()
        List<OmdUser> expiredUsers = omdUserService.selectExpiredFreezeUsers();

        // 2. 遍历用户，逐个解冻
        for (OmdUser user : expiredUsers) {
            // 更新用户状态为“正常”
            user.setOmdUserStatus(1); // 1表示正常
            // 更新备注，记录自动解冻原因
            String newRemark = (user.getOmdUserRemark() == null ? "" : user.getOmdUserRemark())
                    + "（" + new Date() + " 临时冻结到期，系统自动解冻）";
            user.setOmdUserRemark(newRemark);
            // 清空冻结截止时间（可选，标记为已解冻）
            user.setOmdUserFreezeEndTime(null);

            // 3. 执行更新
            omdUserService.updateUserStatus(user);

            // 4. 记录操作日志（操作人为系统，用null或固定值表示）
            OmdOperationLog omdOperationLog = new OmdOperationLog();
            omdOperationLog.setOmdAdminId(0L);
            omdOperationLog.setOmdOperationLogType("user:auto_unfreeze");
            omdOperationLog.setOmdOperationLogDesc("用户" + user.getOmdUserName() + "的临时冻结已到期，系统自动解冻");
            omdOperationLog.setOmdOperationLogTargetId(user.getOmdUserId());
            omdOperationLog.setOmdOperationLogTargetType("user");
            omdAdminService.addOmdOperationLog(omdOperationLog);

        }
    }
}